/*
 * $Id: CustomizationParser.java,v 1.8 2005-09-07 19:48:44 bbissett Exp $
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
import com.sun.xml.ws.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.tools.ws.util.JAXWSUtils;
import com.sun.tools.ws.wsdl.document.jaxws.JAXWSBindingsConstants;

import javax.xml.stream.XMLStreamReader;

/**
 * @author Vivek Pandey
 *
 */
public class CustomizationParser extends InputParser {

    /**
     * @param env
     * @param options
     */
    public CustomizationParser(ProcessorEnvironment env, Properties options) {
        super(env, options);
//        getModelInfoParsers().put(JAXWSBindingsConstants.JAXWS_BINDINGS, new JAXWSBindingInfoParser(env));
    }


    /* (non-Javadoc)
     * @see com.sun.xml.ws.processor.config.parser.InputParser#parse(java.io.File[], java.lang.String)
     */
    protected Configuration parse(List<String> inputFiles) throws Exception{
        //File wsdlFile = inputFiles[0];
        Configuration configuration = new Configuration(getEnv());
        wsdlModelInfo = new WSDLModelInfo();
        wsdlModelInfo.setLocation(inputFiles.get(0));
        if(_options.get(ProcessorOptions.WSDL_LOCATION) == null)
            _options.setProperty(ProcessorOptions.WSDL_LOCATION, inputFiles.get(0));

        //modelInfoParser = (JAXWSBindingInfoParser)getModelInfoParsers().get(JAXWSBindingsConstants.JAXWS_BINDINGS);
        modelInfoParser = new JAXWSBindingInfoParser(getEnv());

        //get the jaxws bindingd file and add it to the modelInfo
        Set<String> bindingFiles = (Set<String>)_options.get(ProcessorOptions.BINDING_FILES);
        for(String bindingFile : bindingFiles){
            addBinding(JAXWSUtils.absolutize(JAXWSUtils.getFileOrURLName(bindingFile)));
        }


        for(String jaxwsBinding : jaxwsBindings){
            Element root = modelInfoParser.parse(new InputSource(jaxwsBinding));
            if(root != null){
                wsdlModelInfo.addJAXWSBindings(root);
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
        JAXWSUtils.checkAbsoluteness(bindingLocation);
        URL url = new URL(bindingLocation);
        XMLStreamReader reader =
                XMLStreamReaderFactory.createXMLStreamReader(url.openStream(), true);
        XMLStreamReaderUtil.nextElementContent(reader);
        if(reader.getName().equals(JAXWSBindingsConstants.JAXWS_BINDINGS)){
            jaxwsBindings.add(bindingLocation);
        }else if(reader.getName().equals(JAXWSBindingsConstants.JAXB_BINDINGS)){
            jaxbBindings.add(bindingLocation);
        }else{
            warn("configuration.notBindingFile");
        }
    }

    private void addHandlerChainInfo() throws Exception{
        //setup handler chain info
        for(Element e:wsdlModelInfo.getJAXWSBindings()){
            NodeList nl = e.getElementsByTagNameNS(
                "http://java.sun.com/xml/ns/javaee", "handler-chains");            
            if(nl.getLength()== 0)
                continue;
            //take the first one, anyway its 1 handler-config per customization
            Element hc = (Element)nl.item(0);
            wsdlModelInfo.setHandlerConfig(hc);
            return;
        }
    }

    private WSDLModelInfo wsdlModelInfo;
    private JAXWSBindingInfoParser modelInfoParser;
    private Set<String> jaxwsBindings = new HashSet<String>();
    private Set<String> jaxbBindings = new HashSet<String>();

}
