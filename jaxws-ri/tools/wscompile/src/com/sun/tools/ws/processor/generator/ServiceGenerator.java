/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.ws.processor.generator;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.ModelProperties;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.model.java.JavaInterface;
import com.sun.tools.ws.resources.GeneratorMessages;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.Options;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.tools.ws.wsdl.document.PortType;
import com.sun.xml.bind.api.JAXBRIContext;
import org.xml.sax.Locator;

import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;


/**
 * @author WS Development Team
 */
public class ServiceGenerator extends GeneratorBase {

    public static void generate(Model model, WsimportOptions options, ErrorReceiver receiver) {
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

        JDefinedClass cls;
        try {
            cls = getClass(className, ClassType.CLASS);
        } catch (JClassAlreadyExistsException e) {
            receiver.error(service.getLocator(), GeneratorMessages.GENERATOR_SERVICE_CLASS_ALREADY_EXIST(className, service.getName()));
            return;
        }

        cls._extends(javax.xml.ws.Service.class);
        String serviceFieldName = JAXBRIContext.mangleNameToClassName(service.getName().getLocalPart()).toUpperCase();
        String wsdlLocationName = serviceFieldName + "_WSDL_LOCATION";
        JFieldVar urlField = cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, URL.class, wsdlLocationName);

        String serviceName = serviceFieldName + "_QNAME";
        cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, QName.class, serviceName,
            JExpr._new(cm.ref(QName.class)).arg(service.getName().getNamespaceURI()).arg(service.getName().getLocalPart()));

        cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, Logger.class, "logger", cm.ref(Logger.class).staticInvoke("getLogger").arg(JExpr.dotclass(cm.ref(className)).invoke("getName")));

        JClass qNameCls = cm.ref(QName.class);
        JInvocation inv;
        inv = JExpr._new(qNameCls);
        inv.arg("namespace");
        inv.arg("localpart");

        JBlock staticBlock = cls.init();
        JVar urlVar = staticBlock.decl(cm.ref(URL.class), "url", JExpr._null());
        JTryBlock tryBlock = staticBlock._try();
        JVar baseUrl = tryBlock.body().decl(cm.ref(URL.class), "baseUrl");
        tryBlock.body().assign(baseUrl, JExpr.dotclass(cm.ref(className)).invoke("getResource").arg("."));
        tryBlock.body().assign(urlVar, JExpr._new(cm.ref(URL.class)).arg(baseUrl).arg(wsdlLocation));
        JCatchBlock catchBlock = tryBlock._catch(cm.ref(MalformedURLException.class));
        catchBlock.param("e");

        catchBlock.body().directStatement("logger.warning(\"Failed to create URL for the wsdl Location: " + JExpr.quotify('\'', wsdlLocation) + ", retrying as a local file\");");
        catchBlock.body().directStatement("logger.warning(e.getMessage());");

        staticBlock.assign(urlField, urlVar);

        //write class comment - JAXWS warning
        JDocComment comment = cls.javadoc();

        if (service.getJavaDoc() != null) {
            comment.add(service.getJavaDoc());
            comment.add("\n\n");
        }

        for (String doc : getJAXWSClassComment()) {
            comment.add(doc);
        }

        JMethod constructor1 = cls.constructor(JMod.PUBLIC);
        String constructor1Str = String.format("super(%s, %s);", wsdlLocationName, serviceName);
        constructor1.body().directStatement(constructor1Str);

        JMethod constructor2 = cls.constructor(JMod.PUBLIC);
        constructor2.varParam(WebServiceFeature.class, "features");
        String constructor2Str = String.format("super(%s, %s, features);", wsdlLocationName, serviceName);
        constructor2.body().directStatement(constructor2Str);

        JMethod constructor3 = cls.constructor(JMod.PUBLIC);
        constructor3.param(URL.class, "wsdlLocation");
        String constructor3Str = String.format("super(wsdlLocation, %s);", serviceName);
        constructor3.body().directStatement(constructor3Str);

        JMethod constructor4 = cls.constructor(JMod.PUBLIC);
        constructor4.param(URL.class, "wsdlLocation");
        constructor4.varParam(WebServiceFeature.class, "features");
        String constructor4Str = String.format("super(wsdlLocation, %s, features);", serviceName);
        constructor4.body().directStatement(constructor4Str);       

        JMethod constructor5 = cls.constructor(JMod.PUBLIC);
        constructor5.param(URL.class, "wsdlLocation");
        constructor5.param(QName.class, "serviceName");
        constructor5.body().directStatement("super(wsdlLocation, serviceName);");

        JMethod constructor6 = cls.constructor(JMod.PUBLIC);
        constructor6.param(URL.class, "wsdlLocation");
        constructor6.param(QName.class, "serviceName");
        constructor6.varParam(WebServiceFeature.class, "features");
        constructor6.body().directStatement("super(wsdlLocation, serviceName, features);");


        //@WebService
        JAnnotationUse webServiceClientAnn = cls.annotate(cm.ref(WebServiceClient.class));
        writeWebServiceClientAnnotation(service, webServiceClientAnn);

        //@HandlerChain
        writeHandlerConfig(Names.customJavaTypeClassName(service.getJavaInterface()), cls, options);

        for (Port port : service.getPorts()) {
            if (port.isProvider()) {
                continue;  // No getXYZPort() for porvider based endpoint
            }

            //Get the SEI class
            JType retType;
            try {
                retType = getClass(port.getJavaInterface().getName(), ClassType.INTERFACE);
            } catch (JClassAlreadyExistsException e) {
                QName portTypeName =
                        (QName) port.getProperty(
                                ModelProperties.PROPERTY_WSDL_PORT_TYPE_NAME);
                Locator loc = null;
                if (portTypeName != null) {
                    PortType pt = port.portTypes.get(portTypeName);
                    if (pt != null)
                        loc = pt.getLocator();
                }
                receiver.error(loc, GeneratorMessages.GENERATOR_SEI_CLASS_ALREADY_EXIST(port.getJavaInterface().getName(), portTypeName));
                return;
            }

            //write getXyzPort()
            writeDefaultGetPort(port, retType, cls);

            //write getXyzPort(WebServicesFeature...)
            if (options.target.isLaterThan(Options.Target.V2_1))
                writeGetPort(port, retType, cls);
        }
    }

    private void writeGetPort(Port port, JType retType, JDefinedClass cls) {
        JMethod m = cls.method(JMod.PUBLIC, retType, port.getPortGetter());
        JDocComment methodDoc = m.javadoc();
        if (port.getJavaDoc() != null)
            methodDoc.add(port.getJavaDoc());
        JCommentPart ret = methodDoc.addReturn();
        JCommentPart paramDoc = methodDoc.addParam("features");
        paramDoc.append("A list of ");
        paramDoc.append("{@link " + WebServiceFeature.class.getName() + "}");
        paramDoc.append("to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.");
        ret.add("returns " + retType.name());
        m.varParam(WebServiceFeature.class, "features");
        JBlock body = m.body();
        StringBuffer statement = new StringBuffer("return ");
        statement.append("super.getPort(new QName(\"").append(port.getName().getNamespaceURI()).append("\", \"").append(port.getName().getLocalPart()).append("\"), ");
        statement.append(retType.name());
        statement.append(".class, features);");
        body.directStatement(statement.toString());
        writeWebEndpoint(port, m);
    }

    private void writeDefaultGetPort(Port port, JType retType, JDefinedClass cls) {
        String portGetter = port.getPortGetter();
        JMethod m = cls.method(JMod.PUBLIC, retType, portGetter);
        JDocComment methodDoc = m.javadoc();
        if (port.getJavaDoc() != null)
            methodDoc.add(port.getJavaDoc());
        JCommentPart ret = methodDoc.addReturn();
        ret.add("returns " + retType.name());
        JBlock body = m.body();
        StringBuffer statement = new StringBuffer("return ");
        statement.append("super.getPort(new QName(\"").append(port.getName().getNamespaceURI()).append("\", \"").append(port.getName().getLocalPart()).append("\"), ");
        statement.append(retType.name());
        statement.append(".class);");
        body.directStatement(statement.toString());
        writeWebEndpoint(port, m);
    }

    private void writeWebServiceClientAnnotation(Service service, JAnnotationUse wsa) {
        String serviceName = service.getName().getLocalPart();
        String serviceNS = service.getName().getNamespaceURI();
        wsa.param("name", serviceName);
        wsa.param("targetNamespace", serviceNS);
        wsa.param("wsdlLocation", wsdlLocation);
    }

    private void writeWebEndpoint(Port port, JMethod m) {
        JAnnotationUse webEndpointAnn = m.annotate(cm.ref(WebEndpoint.class));
        webEndpointAnn.param("name", port.getName().getLocalPart());
    }
}
