/**
 * $Id: JaxRpcAP.java,v 1.2 2005-06-06 23:03:23 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.istack.ws;

import com.sun.tools.ws.processor.Processor;
import com.sun.tools.ws.processor.ProcessorConstants;
import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.generator.WSDLGenerator;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext;
import com.sun.tools.ws.processor.modeler.annotation.WebServiceAP;
import com.sun.tools.ws.processor.modeler.annotation.WebServiceModeler;
import com.sun.tools.ws.processor.modeler.annotation.WebServiceWrapperGenerator;
import com.sun.tools.ws.processor.modeler.annotation.WebServiceVisitor;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessage;
import com.sun.xml.ws.util.VersionUtil;

import java.util.*;

import javax.jws.*;



public class JaxRpcAP extends WebServiceAP {
             
    public JaxRpcAP(AnnotationProcessorContext context) {         
        super(null, (ProcessorEnvironment)null, null, context);    
    }

    protected WebServiceVisitor createWrapperGenerator() {
        return new WebServiceWrapperGenerator(this, context);    
    }

    protected WebServiceVisitor createModeler() {
        return new WebServiceModeler(this, context);
    }    
    
    protected boolean shouldProcessWebService(WebService webService) {
        return webService != null;
    }    
    
    protected void runProcessorActions(Model model) throws Exception {
        Map<String, String> options = apEnv.getOptions();
        String classDir = options.get("-d");
        if (classDir == null)
            classDir = ".";
        String srcDir = options.get("-s");    
        Properties properties = new Properties();
        if (srcDir == null)
            srcDir = classDir;
        properties.setProperty(ProcessorOptions.SOURCE_DIRECTORY_PROPERTY, srcDir);
        properties.setProperty(ProcessorConstants.JAXRPC_VERSION,
                                getVersionString());
        properties.setProperty(ProcessorOptions.JAXRPC_SOURCE_VERSION,
            VersionUtil.JAXRPC_VERSION_DEFAULT);
        properties.setProperty(ProcessorOptions.DESTINATION_DIRECTORY_PROPERTY, classDir);
        String ndDir = classDir;
        for (String key : options.keySet()) {
            if (key.startsWith("-And")) {
                String value = key.substring(key.indexOf('=')+1);
//                System.out.println("nd: "+value);
                ndDir = value;
                break;
            }
        }
//        System.out.println("ndDir: "+ndDir);
        
        properties.setProperty(
            ProcessorOptions.NONCLASS_DESTINATION_DIRECTORY_PROPERTY,
            ndDir);               
//        properties.setProperty(ProcessorOptions.PRINT_STACK_TRACE_PROPERTY, "true");
        Configuration config = new Configuration(env);
        
        Processor processor = new Processor(config, properties, model);
        registerGenerators(processor);
        
        processor.runActions(); 
    }
    
    private void registerGenerators(Processor processor) {
//        processor.add(new WSDLGenerator());
    }    
    
    public void onError(Localizable msg) {
        String message;
        if (messager != null) {
            message = localizer.localize(msg);
            messager.printError(localizer.localize(msg));     
            throw new RuntimeException(message);
        } else {
            message = localizer.localize(getMessage("webserviceap.error", localizer.localize(msg)));            
//            report(message);
//            throw new RuntimeException("modeler.error", message);
        }
//        throw new RuntimeException(message);        
    }         
}
