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
package com.sun.tools.ws.impl;

import com.sun.tools.ws.resources.WsdlMessages;
import com.sun.tools.ws.wscompile.ErrorReceiverFilter;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.tools.ws.wsdl.document.jaxws.JAXWSBindingsConstants;
import com.sun.tools.ws.wsdl.parser.DOMForest;
import com.sun.tools.ws.wsdl.parser.DOMForestParser;
import com.sun.tools.ws.wsdl.parser.DOMForestScanner;
import com.sun.tools.ws.wsdl.parser.MetadataFinder;
import com.sun.tools.ws.wsdl.parser.WSDLInternalizationLogic;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import org.jvnet.wom.api.parser.WOMParser;
import org.jvnet.wom.api.WSDLSet;
import org.jvnet.wom.impl.parser.XMLParserImpl;
import org.jvnet.wom.impl.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public final class WSDLModeler {
    private WsimportOptions options;
    private ErrorReceiverFilter errReceiver;
    private XMLSchemaExtensionHandler schemaParser;

    public WSDLModeler(WsimportOptions options, ErrorReceiverFilter receiver) {
        this.options = options;
        this.errReceiver = receiver;
    }

    private MetadataFinder buildDOMForest() throws IOException, SAXException {
        MetadataFinder forest = new MetadataFinder(new WSDLInternalizationLogic(), options, errReceiver);
        forest.parseWSDL();
        if(forest.isMexMetadata)
            errReceiver.reset();

        // parse external binding files
        for (InputSource value : options.getWSDLBindings()) {
            errReceiver.pollAbort();
            Document root = forest.parse(value, false);
            if(root==null)       continue;   // error must have been reported
            Element binding = root.getDocumentElement();
            if (!XmlUtil.fixNull(binding.getNamespaceURI()).equals(JAXWSBindingsConstants.NS_JAXWS_BINDINGS)
                    || !binding.getLocalName().equals("bindings")){
                    errReceiver.error(forest.locatorTable.getStartLocation(binding), WsdlMessages.PARSER_NOT_A_BINDING_FILE(
                        binding.getNamespaceURI(),
                        binding.getLocalName()));
                continue;
            }

            NodeList nl = binding.getElementsByTagNameNS(
                "http://java.sun.com/xml/ns/javaee", "handler-chains");
            for(int i = 0; i < nl.getLength(); i++){
                options.addHandlerChainConfiguration((Element) nl.item(i));
            }

        }
        //internalizes jaxws and jaxb binding customizations
        forest.transform();
        return forest;
    }

    public void buildModel() throws IOException, SAXException {
        WOMParser parser = new WOMParser();
        parser.setErrorHandler(errReceiver);
        parser.setEntityResolver(options.entityResolver);
        SchemaCompiler schemaCompiler = options.getSchemaCompiler();
        schemaCompiler.resetSchema();
        schemaParser = new XMLSchemaExtensionHandler(schemaCompiler);

        //build DOM forest
        MetadataFinder forest = buildDOMForest();
        DOMForestScanner scanner = new DOMForestScanner(forest);
        WOMParser womParser = createWOMParser(forest);
        // find <xsd:schema>s and parse them individually
        for( InputSource wsdl : options.getWSDLs() ) {
            Document wsdlDom = forest.get( wsdl.getSystemId() );
            NodeList wsdls = wsdlDom.getElementsByTagNameNS(WSDLConstants.NS_WSDL, "definitions");
            for( int i=0; i<wsdls.getLength(); i++ )
                scanner.scan( (Element)wsdls.item(i), womParser.getParserHandler() );
        }
        WSDLSet wsdlSet =  womParser.getResult();
    }

    

    public WOMParser createWOMParser(final DOMForest forest) {
        WOMParser p = new WOMParser(forest.createParser());
        p.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                // DOMForest only parses documents that are rearchable through systemIds,
                // and it won't pick up references like <xs:import namespace="..." /> without
                // @schemaLocation. So we still need to use an entity resolver here to resolve
                // these references, yet we don't want to just run them blindly, since if we do that
                // DOMForestParser always get the translated system ID when catalog is used
                // (where DOMForest records trees with their original system IDs.)
                if(systemId!=null && forest.get(systemId)!=null)
                    return new InputSource(systemId);
                if(options.entityResolver!=null)
                    return options.entityResolver.resolveEntity(publicId,systemId);

                return null;
            }
        });
        return p;
    }



}
