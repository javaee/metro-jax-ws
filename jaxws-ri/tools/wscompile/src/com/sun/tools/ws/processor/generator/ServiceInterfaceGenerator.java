/*
 * $Id: ServiceInterfaceGenerator.java,v 1.9 2005-08-10 23:48:25 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.CodeWriter; 
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.writer.ProgressCodeWriter;
import java.util.Properties;

import com.sun.tools.ws.processor.ProcessorAction;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.model.java.JavaInterface;
//import com.sun.tools.ws.processor.util.GeneratedFileInfo;
import com.sun.tools.ws.wscompile.WSCodeWriter;
import com.sun.xml.ws.encoding.soap.SOAPVersion;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import java.io.File;
import java.io.IOException;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;


/**
 *
 * @author WS Development Team
 */
public class ServiceInterfaceGenerator extends GeneratorBase implements ProcessorAction {
    private String wsdlLocation;
    private String serviceNS;

    public ServiceInterfaceGenerator() {
        super();
    }

    private ServiceInterfaceGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        super(model, config, properties);
        this.wsdlLocation = ((WSDLModelInfo)config.getModelInfo()).getLocation();            
    }

    public GeneratorBase getGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        return new ServiceInterfaceGenerator(model, config, properties);
    }

    public GeneratorBase getGenerator(
        Model model,
        Configuration config,
        Properties properties,
        SOAPVersion ver) {
        return new ServiceInterfaceGenerator(model, config, properties);
    }

    protected void visitService(Service service) {
        try {
            JavaInterface intf = (JavaInterface) service.getJavaInterface();
            String className = env.getNames().customJavaTypeClassName(intf);
            if (donotOverride && GeneratorUtil.classExists(env, className)) {
                log("Class " + className + " exists. Not overriding.");
                return;
            }

            JDefinedClass cls = getClass(className, ClassType.INTERFACE);

            cls._implements(javax.xml.ws.Service.class);
            
            //write class comment - JAXWS warning
            JDocComment comment = cls.javadoc();
            for (String doc : getJAXWSClassComment()) {
                comment.add(doc);
            }

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
//                JType retType = cm.ref(port.getJavaInterface().getName());
                m = cls.method(JMod.PUBLIC, retType, port.getPortGetter());
                methodDoc = m.javadoc();
                JCommentPart ret = methodDoc.addReturn();
                ret.add("returns "+retType.name());
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
