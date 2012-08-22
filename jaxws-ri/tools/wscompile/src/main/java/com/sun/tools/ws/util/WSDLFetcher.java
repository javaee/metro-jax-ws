/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.tools.ws.util;

import com.sun.istack.NotNull;
import com.sun.tools.ws.resources.WscompileMessages;
import com.sun.tools.ws.wscompile.WsimportListener;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.tools.ws.wsdl.parser.DOMForest;
import com.sun.tools.ws.wsdl.parser.MetadataFinder;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import com.sun.xml.ws.api.server.PortAddressResolver;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import com.sun.xml.ws.wsdl.writer.DocumentLocationResolver;
import com.sun.xml.ws.wsdl.writer.WSDLPatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Rama Pulavarthi
 */
public class WSDLFetcher {
    private WsimportOptions options;
    private WsimportListener listener;
    public WSDLFetcher(WsimportOptions options, WsimportListener listener) {
        this.options = options;
        this.listener = listener;
    }


    /**
     *  Fetches the wsdls in the DOMForest to the options.destDir
     * @param forest
     * @return location of fetched root WSDL document
     * @throws IOException
     * @throws XMLStreamException
     * @throws FileNotFoundException
     */
    public String fetchWsdls(MetadataFinder forest) throws IOException, XMLStreamException {
        String rootWsdl = null;
        for(String root: forest.getRootDocuments()) {
            rootWsdl = root;
        }

        Set<String> externalRefs = forest.getExternalReferences();
        Map<String,String> documentMap = createDocumentMap(forest, getWSDLDownloadDir(), rootWsdl, externalRefs);
        String rootWsdlName = fetchFile(rootWsdl,forest, documentMap,getWSDLDownloadDir());
        for(String reference: forest.getExternalReferences()) {
            fetchFile(reference,forest,documentMap,getWSDLDownloadDir());
        }
        return WSDL_PATH +"/" + rootWsdlName;
    }

    private String fetchFile(final String doc, DOMForest forest, final Map<String, String> documentMap, File destDir) throws IOException, XMLStreamException {

        DocumentLocationResolver docLocator = createDocResolver(doc, forest, documentMap);
        WSDLPatcher wsdlPatcher = new WSDLPatcher(new PortAddressResolver() {
            @Override
            public String getAddressFor(@NotNull QName serviceName, @NotNull String portName) {
                return null;
            }
        }, docLocator);

        //XMLInputFactory readerFactory = XMLInputFactory.newInstance();
        //XMLStreamReader xsr = readerFactory.createXMLStreamReader(new DOMSource(forest.get(rootWsdl)));

        XMLStreamReader xsr = SourceReaderFactory.createSourceReader(new DOMSource(forest.get(doc)), false);
        XMLOutputFactory writerfactory = XMLOutputFactory.newInstance();
        String resolvedRootWsdl = docLocator.getLocationFor(null, doc);
        File outFile = new File(destDir, resolvedRootWsdl);
        OutputStream os = new FileOutputStream(outFile);
        if(options.verbose) {
            listener.message(WscompileMessages.WSIMPORT_DOCUMENT_DOWNLOAD(doc,outFile));
        }
        XMLStreamWriter xsw = writerfactory.createXMLStreamWriter(os);
        //DOMForest eats away the whitespace loosing all the indentation, so write it through
        // indenting writer for better readability of fetched documents 
        IndentingXMLStreamWriter indentingWriter = new IndentingXMLStreamWriter(xsw);
        wsdlPatcher.bridge(xsr, indentingWriter);
        xsr.close();
        xsw.close();
        os.close();
        options.addGeneratedFile(outFile);
        return resolvedRootWsdl;


    }
    private Map<String,String> createDocumentMap(MetadataFinder forest, File baseDir, final String rootWsdl, Set<String> externalReferences) {
        Map<String,String> map = new HashMap<String,String>();
        String rootWsdlFileName = rootWsdl;
        String rootWsdlName;

        int slashIndex = rootWsdl.lastIndexOf("/");
        if( slashIndex >= 0) {
            rootWsdlFileName = rootWsdl.substring(slashIndex+1);
        }
        if(!rootWsdlFileName.endsWith(WSDL_FILE_EXTENSION)) {
            Document rootWsdlDoc =  forest.get(rootWsdl);
            NodeList serviceNodes = rootWsdlDoc.getElementsByTagNameNS(WSDLConstants.QNAME_SERVICE.getNamespaceURI(),WSDLConstants.QNAME_SERVICE.getLocalPart());
            if(serviceNodes.getLength() == 0)
                rootWsdlName = "Service";
            else {
                Node serviceNode = serviceNodes.item(0);
                String serviceName = ((Element)serviceNode).getAttribute( WSDLConstants.ATTR_NAME);
                rootWsdlName = serviceName;
            }
            rootWsdlFileName = rootWsdlName+ WSDL_FILE_EXTENSION;
        } else {
            rootWsdlName = rootWsdlFileName.substring(0,rootWsdlFileName.length()-5);
        }

        map.put(rootWsdl,sanitize(rootWsdlFileName));

        int i =1;
        for(String ref: externalReferences) {
            Document refDoc =  forest.get(ref);
            Element rootEl = refDoc.getDocumentElement();
            String fileExtn;
            String fileName = null;
            int index = ref.lastIndexOf("/");
            if (index >= 0) {
                fileName = ref.substring(index + 1);
            }
            if(rootEl.getLocalName().equals(WSDLConstants.QNAME_DEFINITIONS.getLocalPart()) && rootEl.getNamespaceURI().equals(WSDLConstants.NS_WSDL)) {
              fileExtn = WSDL_FILE_EXTENSION;
            } else if(rootEl.getLocalName().equals(WSDLConstants.QNAME_SCHEMA.getLocalPart()) && rootEl.getNamespaceURI().equals(WSDLConstants.NS_XMLNS)) {
              fileExtn = SCHEMA_FILE_EXTENSION;
            } else {
                fileExtn = ".xml";
            }
            if(fileName != null && (fileName.endsWith(WSDL_FILE_EXTENSION) || fileName.endsWith(SCHEMA_FILE_EXTENSION))) {
                map.put(ref, rootWsdlName+"_"+fileName);                
            } else {
                map.put(ref, rootWsdlName+"_metadata"+ (i++) + fileExtn);
            }
        }
        return map;
    }

    private DocumentLocationResolver createDocResolver(final String baseWsdl, final DOMForest forest, final Map<String,String> documentMap) {

        return new DocumentLocationResolver() {
            public String getLocationFor(String namespaceURI, String systemId) {
                try {
                    URL reference = new URL(new URL(baseWsdl),systemId);
                    systemId = reference.toExternalForm();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                if(documentMap.get(systemId) != null) {
                    return documentMap.get(systemId);
                } else {
                    String parsedEntity = forest.getReferencedEntityMap().get(systemId);
                    return documentMap.get(parsedEntity);
                }
            }
        };
    }

    private String sanitize(String fileName) {
        fileName = fileName.replace('?', '.');
        StringBuffer sb = new StringBuffer(fileName);
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (Character.isLetterOrDigit(c) ||
                    (c == '/') ||
                    (c == '.') ||
                    (c == '_') ||
                    (c == ' ') ||
                    (c == '-')) {
                continue;
            } else {
                sb.setCharAt(i, '_');
            }
        }
        return sb.toString();
    }

    private File getWSDLDownloadDir() {
        File wsdlDir = new File(options.destDir,WSDL_PATH);
        wsdlDir.mkdirs();
        return wsdlDir;
    }

    private static String WSDL_PATH="META-INF/wsdl";
    private static String WSDL_FILE_EXTENSION=".wsdl";
    private static String SCHEMA_FILE_EXTENSION=".xsd";
}
