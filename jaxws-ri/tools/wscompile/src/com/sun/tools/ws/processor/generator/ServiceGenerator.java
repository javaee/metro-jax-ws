/*
 * $Id: ServiceGenerator.java,v 1.4 2005-09-10 19:49:36 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.tools.ws.processor.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.CodeWriter; 
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.*;
import com.sun.codemodel.writer.ProgressCodeWriter;
import java.util.Properties;

import com.sun.tools.ws.processor.ProcessorAction;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.model.java.JavaInterface;
import com.sun.tools.ws.wscompile.WSCodeWriter;
import com.sun.xml.ws.encoding.soap.SOAPVersion;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.tools.ws.util.JAXWSUtils;
import com.sun.xml.ws.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.namespace.QName;


/**
 *
 * @author WS Development Team
 */
public class ServiceGenerator extends GeneratorBase implements ProcessorAction {
    private String serviceNS;

    public ServiceGenerator() {
        super();
    }

    private ServiceGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        super(model, config, properties);
    }

    public GeneratorBase getGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        return new ServiceGenerator(model, config, properties);
    }

    public GeneratorBase getGenerator(
        Model model,
        Configuration config,
        Properties properties,
        SOAPVersion ver) {
        return new ServiceGenerator(model, config, properties);
    }

    private JType getType(String typeName) throws IOException {
        JType type = null;
        try {
            type = cm.parseType(typeName);
            return type;
        } catch (ClassNotFoundException e) {
            type = cm.ref(typeName);
        }
        return type;
    }    
    
    /**
     * Generates an expression that evaluates to "new QName(...)"
     */
    private JInvocation createQName(QName name) {
        return JExpr._new(cm.ref(QName.class)).arg(name.getNamespaceURI()).arg(name.getLocalPart());
    }
    
    private JInvocation createURL(URL url) {
        return JExpr._new(cm.ref(URL.class)).arg(url.toExternalForm());
    }
    
    protected void visitService(Service service) {
        try {
            JavaInterface intf = (JavaInterface) service.getJavaInterface();
            String className = env.getNames().customJavaTypeClassName(intf);
            if (donotOverride && GeneratorUtil.classExists(env, className)) {
                log("Class " + className + " exists. Not overriding.");
                return;
            }

            JDefinedClass cls = getClass(className, ClassType.CLASS);

            cls._extends(javax.xml.ws.Service.class);
            
            JFieldVar urlField = cls.field(JMod.PRIVATE|JMod.STATIC, URL.class, "url");
            JClass qNameCls = cm.ref(QName.class);
//              JExpression jExp = new JExpression(qNameCls.staticInvoke("QName"));
            JInvocation inv;// = cm.ref(QName.class).staticInvoke("QName");
            inv = JExpr._new(qNameCls);
            inv.arg("namespace");
            inv.arg("localpart");
            JFieldVar serviceField = cls.field(JMod.PRIVATE|JMod.STATIC, QName.class, "serviceName", createQName(service.getName()));

            JFieldVar portField;
            String fieldName;
            for (Port port: service.getPorts()) {
                if (port.isProvider()) {
                    continue;  // No getXYZPort() for porvider based endpoint
                }                
                inv = JExpr._new(qNameCls);
                inv.arg("namespace");
                inv.arg("localpart");
                fieldName = StringUtils.decapitalize(port.getName().getLocalPart())+"Name";
                portField = cls.field(JMod.PRIVATE|JMod.STATIC, QName.class, fieldName, createQName(port.getName()));
            }
            
            
//            field.assign(inv);
            JBlock staticBlock = cls.init();
            JTryBlock tryBlock = staticBlock._try();
//            System.out.println("wsdlLocation: "+wsdlLocation);
            URL url = new URL(JAXWSUtils.absolutize(JAXWSUtils.getFileOrURLName(wsdlLocation)));
            tryBlock.body().assign(urlField, createURL(url));
//            tryBlock.directStatement("new URL("+"location"+");");
            JCatchBlock catchBlock = tryBlock._catch(cm.ref(MalformedURLException.class));
            catchBlock.param("e");
            catchBlock.body().directStatement("e.printStackTrace();");
          
//            cls._ref(staticBody.JStaticBlock().body(body));
            //write class comment - JAXWS warning
            JDocComment comment = cls.javadoc();
            for (String doc : getJAXWSClassComment()) {
                comment.add(doc);
            }
            
            JMethod constructor = cls.constructor(JMod.PUBLIC);
            constructor.param(URL.class, "wsdlLocation");
            constructor.param(QName.class, "serviceName");
            constructor.body().directStatement("super(wsdlLocation, serviceName);");
            
            constructor = cls.constructor(JMod.PUBLIC);
            constructor.body().directStatement("super(url, serviceName);");
            
            //@WebService
            JAnnotationUse webServiceClientAnn = cls.annotate(cm.ref(WebServiceClient.class));
            writeWebServiceClientAnnotation(service, webServiceClientAnn);
            for (Port port: service.getPorts()) {
                if (port.isProvider()) {
                    continue;  // No getXYZPort() for porvider based endpoint
                }                
                //@WebEndpoint
                JMethod m = null;
                JDocComment methodDoc = null;
                JType retType = getClass(port.getJavaInterface().getName(), ClassType.INTERFACE);
                m = cls.method(JMod.PUBLIC, retType, port.getPortGetter());
                methodDoc = m.javadoc();
                JCommentPart ret = methodDoc.addReturn();
                ret.add("returns "+retType.name());
                JBlock body = m.body();
                StringBuffer statement = new StringBuffer("return (");
                statement.append(retType.name());
                fieldName = StringUtils.decapitalize(port.getName().getLocalPart())+"Name";                
                statement.append(")super.getPort("+fieldName+", ");
                statement.append(retType.name());
                statement.append(".class);");
                body.directStatement(statement.toString());
                writeWebEndpoint(port, m);
            }
            CodeWriter cw = new WSCodeWriter(sourceDir,env);

            if(env.verbose())
                cw = new ProgressCodeWriter(cw, System.out);
            cm.build(cw);            
            
        } catch (IOException e) {
            throw new GeneratorException(
                "generator.nestedGeneratorError",
                new LocalizableExceptionAdapter(e));
        }
    }

    protected JDefinedClass getClass(String className, ClassType type) {
        JDefinedClass cls = null;
        try {
            cls = cm._class(className, type);
        } catch (JClassAlreadyExistsException e){
            cls = cm._getClass(className);
        }        
        return cls;
    }      
    
    private void writeWebServiceClientAnnotation(Service service, JAnnotationUse wsa) {
        String serviceName = service.getName().getLocalPart();
        serviceNS = service.getName().getNamespaceURI();
        wsa.param("name", serviceName);
        wsa.param("targetNamespace", serviceNS);
        wsa.param("wsdlLocation", wsdlLocation);
    }    
    
    private void writeWebEndpoint(Port port, JMethod m) {
        JAnnotationUse webEndpointAnn = m.annotate(cm.ref(WebEndpoint.class));
        webEndpointAnn.param("name", port.getName().getLocalPart());
    }    
}
