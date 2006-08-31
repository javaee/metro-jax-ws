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
package com.sun.tools.ws.processor.config.parser;

import java.net.URL;
import java.util.*;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;

import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.JAXWSUtils;
import com.sun.tools.ws.wsdl.document.jaxws.JAXWSBindingsConstants;

import javax.xml.stream.XMLStreamReader;

/**
 * @author Vivek Pandey
 *
 */
public class CustomizationParser extends InputParser {

    /**
     * @param entityResolver
     * @param env
     * @param options
     */
    public CustomizationParser(EntityResolver entityResolver, ProcessorEnvironment env, Properties options) {
        super(env, options);
        this.entityResolver = entityResolver;
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
            addBinding(bindingFile);
        }


        for(InputSource jaxwsBinding : jaxwsBindings){
            Document doc = modelInfoParser.parse(jaxwsBinding);
            if(doc != null){
                wsdlModelInfo.putJAXWSBindings(jaxwsBinding.getSystemId(), doc);
            }
        }

        //copy jaxb binding sources in modelInfo
        for(InputSource jaxbBinding : jaxbBindings){
            wsdlModelInfo.addJAXBBIndings(jaxbBinding);
        }

        addHandlerChainInfo();
        configuration.setModelInfo(wsdlModelInfo);
        return configuration;
    }

    private void addBinding(String bindingLocation) throws Exception{
        JAXWSUtils.checkAbsoluteness(bindingLocation);
        InputSource is = null;
        if(entityResolver != null){
            is = entityResolver.resolveEntity(null, bindingLocation);
        }
        if(is == null)
            is = new InputSource(bindingLocation);

        XMLStreamReader reader =
                XMLStreamReaderFactory.createFreshXMLStreamReader(is, true);
        XMLStreamReaderUtil.nextElementContent(reader);
        if(reader.getName().equals(JAXWSBindingsConstants.JAXWS_BINDINGS)){
            jaxwsBindings.add(is);
        }else if(reader.getName().equals(JAXWSBindingsConstants.JAXB_BINDINGS)){
            jaxbBindings.add(is);
        }else{
            warn("configuration.notBindingFile");
        }
        reader.close();
    }

    private void addHandlerChainInfo() throws Exception{
        //setup handler chain info
        for(Map.Entry<String, Document> entry:wsdlModelInfo.getJAXWSBindings().entrySet()){
            Element e = entry.getValue().getDocumentElement();
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
    private Set<InputSource> jaxwsBindings = new HashSet<InputSource>();
    private Set<InputSource> jaxbBindings = new HashSet<InputSource>();
    private EntityResolver entityResolver;

}
