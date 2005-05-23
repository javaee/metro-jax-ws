/*
 * $Id: CustomizationParser.java,v 1.1 2005-05-23 23:13:21 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.config.parser;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.streaming.XMLReader;
import com.sun.xml.ws.streaming.XMLReaderFactory;
import com.sun.tools.ws.util.JAXRPCUtils;
import com.sun.tools.ws.wsdl.document.jaxrpc.JAXRPCBindingsConstants;

/**
 * @author Vivek Pandey
 *
 */
public class CustomizationParser extends InputParser {

    /**
     * @param env
     * @param jaxbBindings
     * @param jaxrpcCustomization
     * @param _options
     */
    public CustomizationParser(ProcessorEnvironment env, Properties options) {
        super(env, options);
//        getModelInfoParsers().put(JAXRPCBindingsConstants.JAXRPC_BINDINGS, new JAXRPCBindingInfoParser(env));
    }


    /* (non-Javadoc)
     * @see com.sun.xml.rpc.processor.config.parser.InputParser#parse(java.io.File[], java.lang.String)
     */
    protected Configuration parse(List<String> inputFiles) throws Exception{
        //File wsdlFile = inputFiles[0];
        Configuration configuration = new Configuration(getEnv());
        wsdlModelInfo = new WSDLModelInfo();
        wsdlModelInfo.setLocation(inputFiles.get(0));

        //modelInfoParser = (JAXRPCBindingInfoParser)getModelInfoParsers().get(JAXRPCBindingsConstants.JAXRPC_BINDINGS);
        modelInfoParser = new JAXRPCBindingInfoParser(getEnv());

        //get the jaxrpc bindingd file and add it to the modelInfo
        Set<String> bindingFiles = (Set<String>)_options.get(ProcessorOptions.BINDING_FILES);
        for(String bindingFile : bindingFiles){
            addBinding(bindingFile);
        }

        for(String jaxrpcBinding : jaxrpcBindings){
            Element root = modelInfoParser.parse(new InputSource(jaxrpcBinding));
            if(root != null){
                wsdlModelInfo.addJAXRPCBindings(root);
            }
        }

        //get the jaxb bindingd file and add it to the modelInfo
        for(String jaxbBinding : jaxbBindings){
            wsdlModelInfo.addJAXBBIndings(new InputSource(jaxbBinding));
        }

        addHandlerChainInfo();
        configuration.setModelInfo(wsdlModelInfo);
        return configuration;
    }

    private void addBinding(String bindingLocation) throws Exception{
        JAXRPCUtils.checkAbsoluteness(bindingLocation);
        URL url = new URL(bindingLocation);

        XMLReader reader = XMLReaderFactory.newInstance().createXMLReader(url.openStream());
        reader.next();
        if(reader.getName().equals(JAXRPCBindingsConstants.JAXRPC_BINDINGS)){
            jaxrpcBindings.add(bindingLocation);
        }else if(reader.getName().equals(JAXRPCBindingsConstants.JAXB_BINDINGS)){
            jaxbBindings.add(bindingLocation);
        }else{
            warn("configuration.notBindingFile");
        }
    }

    private void addHandlerChainInfo() throws Exception{
        //setup handler chain info
        for(Element e:wsdlModelInfo.getJAXRPCBindings()){
            NodeList nl = e.getElementsByTagNameNS("http://www.bea.com/xml/ns/jws", "handler-chain");            
            if(nl.getLength()== 0)
                continue;
            //take the first one, anyway its 1 handler-config per customization
            Element hc = (Element)nl.item(0);
            wsdlModelInfo.setHandlerConfig(hc);
            return;
        }
    }

    private WSDLModelInfo wsdlModelInfo;
    private JAXRPCBindingInfoParser modelInfoParser;
    private Set<String> jaxrpcBindings = new HashSet<String>();
    private Set<String> jaxbBindings = new HashSet<String>();

}
