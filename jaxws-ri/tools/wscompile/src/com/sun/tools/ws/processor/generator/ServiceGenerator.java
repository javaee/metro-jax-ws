/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.tools.ws.processor.generator;

import com.sun.codemodel.*;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.model.java.JavaInterface;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.Options;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.tools.ws.resources.GeneratorMessages;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.util.JAXWSUtils;

import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;


/**
 *
 * @author WS Development Team
 */
public class ServiceGenerator extends GeneratorBase{

    public static void generate(Model model, WsimportOptions options, ErrorReceiver receiver){
        ServiceGenerator serviceGenerator = new ServiceGenerator(model, options, receiver);
        serviceGenerator.doGeneration();
    }
    private ServiceGenerator(Model model, WsimportOptions options, ErrorReceiver receiver) {
        super(model, options, receiver);
    }

    @Override
    public void visit(Service service) {
        JavaInterface intf = service.getJavaInterface();
        String className = Names.customJavaTypeClassName(intf);
        if (donotOverride && GeneratorUtil.classExists(options, className)) {
            log("Class " + className + " exists. Not overriding.");
            return;
        }

        JDefinedClass cls = getClass(className, ClassType.CLASS);

        cls._extends(javax.xml.ws.Service.class);
        String serviceFieldName = JAXBRIContext.mangleNameToClassName(service.getName().getLocalPart()).toUpperCase();
        String wsdlLocationName = serviceFieldName+"_WSDL_LOCATION";
        JFieldVar urlField = cls.field(JMod.PRIVATE|JMod.STATIC|JMod.FINAL, URL.class, wsdlLocationName);


        cls.field(JMod.PRIVATE|JMod.STATIC|JMod.FINAL, Logger.class, "logger", cm.ref(Logger.class).staticInvoke("getLogger").arg(JExpr.dotclass(cm.ref(className)).invoke("getName")));

        JClass qNameCls = cm.ref(QName.class);
        JInvocation inv;
        inv = JExpr._new(qNameCls);
        inv.arg("namespace");
        inv.arg("localpart");


        JBlock staticBlock = cls.init();
        JVar urlVar = staticBlock.decl(cm.ref(URL.class),"url", JExpr._null());
        JTryBlock tryBlock = staticBlock._try();
        JVar baseUrl = tryBlock.body().decl(cm.ref(URL.class),"baseUrl");
        tryBlock.body().assign(baseUrl, JExpr.dotclass(cm.ref(className)).invoke("getResource").arg("."));
        tryBlock.body().assign(urlVar, JExpr._new(cm.ref(URL.class)).arg(baseUrl).arg(wsdlLocation));
        JCatchBlock catchBlock = tryBlock._catch(cm.ref(MalformedURLException.class));
        catchBlock.param("e");
        catchBlock.body().directStatement("logger.warning(\"Failed to create URL for the wsdl Location: "+wsdlLocation+"\");");
        catchBlock.body().directStatement("logger.warning(e.getMessage());");

        staticBlock.assign(urlField, urlVar);

        //write class comment - JAXWS warning
        JDocComment comment = cls.javadoc();

        if(service.getJavaDoc() != null){
            comment.add(service.getJavaDoc());
            comment.add("\n\n");
        }

        for (String doc : getJAXWSClassComment()) {
            comment.add(doc);
        }

        JMethod constructor = cls.constructor(JMod.PUBLIC);
        constructor.param(URL.class, "wsdlLocation");
        constructor.param(QName.class, "serviceName");
        constructor.body().directStatement("super(wsdlLocation, serviceName);");

        constructor = cls.constructor(JMod.PUBLIC);
        constructor.body().directStatement("super("+wsdlLocationName+", new QName(\""+service.getName().getNamespaceURI()+"\", \""+service.getName().getLocalPart()+"\"));");

        //@WebService
        JAnnotationUse webServiceClientAnn = cls.annotate(cm.ref(WebServiceClient.class));
        writeWebServiceClientAnnotation(service, webServiceClientAnn);

        //@HandlerChain
        writeHandlerConfig(Names.customJavaTypeClassName(service.getJavaInterface()), cls, options);

        for (Port port: service.getPorts()) {
            if (port.isProvider()) {
                continue;  // No getXYZPort() for porvider based endpoint
            }

            //write getXyzPort()
            writeDefaultGetPort(port, cls);

            //write getXyzPort(WebServicesFeature...)
            if(options.target.isLaterThan(Options.Target.V2_1))
                writeGetPort(port, cls);
        }
    }

    private void writeGetPort(Port port, JDefinedClass cls) {
        JType retType = getClass(port.getJavaInterface().getName(), ClassType.INTERFACE);
        JMethod m = cls.method(JMod.PUBLIC, retType, port.getPortGetter());
        JDocComment methodDoc = m.javadoc();
        if(port.getJavaDoc() != null)
            methodDoc.add(port.getJavaDoc());
        JCommentPart ret = methodDoc.addReturn();
        JCommentPart paramDoc = methodDoc.addParam("features");
        paramDoc.append("A list of ");
        paramDoc.append("{@link "+WebServiceFeature.class.getName()+"}");
        paramDoc.append("to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.");
        ret.add("returns "+retType.name());
        m.varParam(WebServiceFeature.class, "features");
        JBlock body = m.body();
        StringBuffer statement = new StringBuffer("return ");
        statement.append("super.getPort(new QName(\"").append(port.getName().getNamespaceURI()).append("\", \"").append(port.getName().getLocalPart()).append("\"), ");
        statement.append(retType.name());
        statement.append(".class, features);");
        body.directStatement(statement.toString());
        writeWebEndpoint(port, m);
    }

    private void writeDefaultGetPort(Port port, JDefinedClass cls) {
        JType retType = getClass(port.getJavaInterface().getName(), ClassType.INTERFACE);
        String portGetter = port.getPortGetter();
        JMethod m = cls.method(JMod.PUBLIC, retType, portGetter);
        JDocComment methodDoc = m.javadoc();
        if(port.getJavaDoc() != null)
            methodDoc.add(port.getJavaDoc());
        JCommentPart ret = methodDoc.addReturn();
        ret.add("returns "+retType.name());
        JBlock body = m.body();
        StringBuffer statement = new StringBuffer("return ");
        statement.append("super.getPort(new QName(\"").append(port.getName().getNamespaceURI()).append("\", \"").append(port.getName().getLocalPart()).append("\"), ");
        statement.append(retType.name());
        statement.append(".class);");
        body.directStatement(statement.toString());
        writeWebEndpoint(port, m);
    }


    protected JDefinedClass getClass(String className, ClassType type) {
        JDefinedClass cls;
        try {
            cls = cm._class(className, type);
        } catch (JClassAlreadyExistsException e){
            cls = cm._getClass(className);
        }
        return cls;
    }      
    
    private void writeWebServiceClientAnnotation(Service service, JAnnotationUse wsa) {
        String serviceName = service.getName().getLocalPart();
        String serviceNS= service.getName().getNamespaceURI();
        wsa.param("name", serviceName);
        wsa.param("targetNamespace", serviceNS);
        wsa.param("wsdlLocation", wsdlLocation);
    }    
    
    private void writeWebEndpoint(Port port, JMethod m) {
        JAnnotationUse webEndpointAnn = m.annotate(cm.ref(WebEndpoint.class));
        webEndpointAnn.param("name", port.getName().getLocalPart());
    }    
}
