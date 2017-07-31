/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.server;

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.server.*;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.wsdl.SDDocumentResolver;
import com.sun.xml.ws.util.RuntimeVersion;
import org.jvnet.staxex.util.XMLStreamReaderToXMLStreamWriter;
import com.sun.xml.ws.wsdl.parser.ParserUtil;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import com.sun.xml.ws.wsdl.writer.DocumentLocationResolver;
import com.sun.xml.ws.wsdl.writer.WSDLPatcher;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link SDDocument} implmentation.
 *
 * <p>
 * This extends from {@link SDDocumentSource} so that
 * JAX-WS server runtime code can use {@link SDDocument}
 * as {@link SDDocumentSource}.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
public class SDDocumentImpl extends SDDocumentSource implements SDDocument {

    private static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";
    private static final QName SCHEMA_INCLUDE_QNAME = new QName(NS_XSD, "include");
    private static final QName SCHEMA_IMPORT_QNAME = new QName(NS_XSD, "import");
    private static final QName SCHEMA_REDEFINE_QNAME = new QName(NS_XSD, "redefine");
    private static final String VERSION_COMMENT =
        " Published by JAX-WS RI (http://jax-ws.java.net). RI's version is "+RuntimeVersion.VERSION+". ";

    private final QName rootName;
    private final SDDocumentSource source;

    /**
     * Set when {@link ServiceDefinitionImpl} is constructed.
     */
    @Nullable List<SDDocumentFilter> filters;
    @Nullable SDDocumentResolver sddocResolver;


    /**
     * The original system ID of this document.
     *
     * When this document contains relative references to other resources,
     * this field is used to find which {@link com.sun.xml.ws.server.SDDocumentImpl} it refers to.
     *
     * Must not be null.
     */
    private final URL url;
    private final Set<String> imports;

    /**
     * Creates {@link SDDocument} from {@link SDDocumentSource}.
     * @param src WSDL document infoset
     * @param serviceName wsdl:service name
     * @param portTypeName
     *      The information about the port of {@link WSEndpoint} to which this document is built for.
     *      These values are used to determine which document is the concrete and abstract WSDLs
     *      for this endpoint.
     *
     * @return null
     *      Always non-null.
     */
    public static SDDocumentImpl create(SDDocumentSource src, QName serviceName, QName portTypeName) {
        URL systemId = src.getSystemId();

        try {
            // RuntimeWSDLParser parser = new RuntimeWSDLParser(null);
            XMLStreamReader reader = src.read();
            try {
                XMLStreamReaderUtil.nextElementContent(reader);

                QName rootName = reader.getName();
                if(rootName.equals(WSDLConstants.QNAME_SCHEMA)) {
                    String tns = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_TNS);
                    Set<String> importedDocs = new HashSet<String>();
                    while (XMLStreamReaderUtil.nextContent(reader) != XMLStreamConstants.END_DOCUMENT) {
                         if (reader.getEventType() != XMLStreamConstants.START_ELEMENT)
                            continue;
                        QName name = reader.getName();
                        if (SCHEMA_INCLUDE_QNAME.equals(name) || SCHEMA_IMPORT_QNAME.equals(name) ||
                                SCHEMA_REDEFINE_QNAME.equals(name)) {
                            String importedDoc = reader.getAttributeValue(null, "schemaLocation");
                            if (importedDoc != null) {
                                importedDocs.add(new URL(src.getSystemId(), importedDoc).toString());
                            }
                        }
                    }
                    return new SchemaImpl(rootName,systemId,src,tns,importedDocs);
                } else if (rootName.equals(WSDLConstants.QNAME_DEFINITIONS)) {
                    String tns = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_TNS);

                    boolean hasPortType = false;
                    boolean hasService = false;
                    Set<String> importedDocs = new HashSet<String>();
                    Set<QName> allServices = new HashSet<QName>();

                    // if WSDL, parse more
                    while (XMLStreamReaderUtil.nextContent(reader) != XMLStreamConstants.END_DOCUMENT) {
                         if(reader.getEventType() != XMLStreamConstants.START_ELEMENT)
                            continue;

                        QName name = reader.getName();
                        if (WSDLConstants.QNAME_PORT_TYPE.equals(name)) {
                            String pn = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
                            if (portTypeName != null) {
                                if(portTypeName.getLocalPart().equals(pn)&&portTypeName.getNamespaceURI().equals(tns)) {
                                    hasPortType = true;
                                }
                            }
                        } else if (WSDLConstants.QNAME_SERVICE.equals(name)) {
                            String sn = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
                            QName sqn = new QName(tns,sn);
                            allServices.add(sqn);
                            if(serviceName.equals(sqn)) {
                                hasService = true;
                            }
                        } else if (WSDLConstants.QNAME_IMPORT.equals(name)) {
                            String importedDoc = reader.getAttributeValue(null, "location");
                            if (importedDoc != null) {
                                importedDocs.add(new URL(src.getSystemId(), importedDoc).toString());
                            }
                        } else if (SCHEMA_INCLUDE_QNAME.equals(name) || SCHEMA_IMPORT_QNAME.equals(name) ||
                                SCHEMA_REDEFINE_QNAME.equals(name)) {
                            String importedDoc = reader.getAttributeValue(null, "schemaLocation");
                            if (importedDoc != null) {
                                importedDocs.add(new URL(src.getSystemId(), importedDoc).toString());
                            }
                        }
                    }
                    return new WSDLImpl(
                        rootName,systemId,src,tns,hasPortType,hasService,importedDocs,allServices);
                } else {
                    return new SDDocumentImpl(rootName,systemId,src);
                }
            } finally {
                reader.close();
            }
        } catch (WebServiceException e) {
            throw new ServerRtException("runtime.parser.wsdl", systemId,e);
        } catch (IOException e) {
            throw new ServerRtException("runtime.parser.wsdl", systemId,e);
        } catch (XMLStreamException e) {
            throw new ServerRtException("runtime.parser.wsdl", systemId,e);
        }
    }

    protected SDDocumentImpl(QName rootName, URL url, SDDocumentSource source) {
        this(rootName, url, source, new HashSet<String>());
    }

    protected SDDocumentImpl(QName rootName, URL url, SDDocumentSource source, Set<String> imports) {
        if (url == null) {
            throw new IllegalArgumentException("Cannot construct SDDocument with null URL.");
        }
        this.rootName = rootName;
        this.source = source;
        this.url = url;
        this.imports = imports;
    }

    void setFilters(List<SDDocumentFilter> filters) {
        this.filters = filters;
    }

    void setResolver(SDDocumentResolver sddocResolver) {
        this.sddocResolver = sddocResolver;
    }

    public QName getRootName() {
        return rootName;
    }

    public boolean isWSDL() {
        return false;
    }

    public boolean isSchema() {
        return false;
    }

    public URL getURL() {
        return url;
    }

    public XMLStreamReader read(XMLInputFactory xif) throws IOException, XMLStreamException {
        return source.read(xif);
    }

    public XMLStreamReader read() throws IOException, XMLStreamException {
        return source.read();
    }

    public URL getSystemId() {
        return url;
    }

    public Set<String> getImports() {
        return imports;
    }

    public void writeTo(OutputStream os) throws IOException {
        XMLStreamWriter w = null;
        try {
            //generate the WSDL with utf-8 encoding and XML version 1.0
            w = XMLStreamWriterFactory.create(os, "UTF-8");
            w.writeStartDocument("UTF-8", "1.0");
            new XMLStreamReaderToXMLStreamWriter().bridge(source.read(), w);
            w.writeEndDocument();
        } catch (XMLStreamException e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } finally {
            try {
                if (w != null)
                    w.close();
            } catch (XMLStreamException e) {
                IOException ioe = new IOException(e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }
        }
    }


    public void writeTo(PortAddressResolver portAddressResolver, DocumentAddressResolver resolver, OutputStream os) throws IOException {
        XMLStreamWriter w = null;
        try {
            //generate the WSDL with utf-8 encoding and XML version 1.0
            w = XMLStreamWriterFactory.create(os, "UTF-8");
            w.writeStartDocument("UTF-8", "1.0");
            writeTo(portAddressResolver,resolver,w);
            w.writeEndDocument();
        } catch (XMLStreamException e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } finally {
            try {
                if (w != null)
                    w.close();
            } catch (XMLStreamException e) {
                IOException ioe = new IOException(e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }
        }
    }

    public void writeTo(PortAddressResolver portAddressResolver, DocumentAddressResolver resolver, XMLStreamWriter out) throws XMLStreamException, IOException {
        if (filters != null) {
            for (SDDocumentFilter f : filters) {
                out = f.filter(this,out);
            }
        }

        XMLStreamReader xsr = source.read();
        try {
            out.writeComment(VERSION_COMMENT);
            new WSDLPatcher(portAddressResolver, new DocumentLocationResolverImpl(resolver)).bridge(xsr,out);
        } finally {
            xsr.close();
        }
    }


    /**
     * {@link SDDocument.Schema} implementation.
     *
     * @author Kohsuke Kawaguchi
     */
    private static final class SchemaImpl extends SDDocumentImpl implements SDDocument.Schema {
        private final String targetNamespace;

        public SchemaImpl(QName rootName, URL url, SDDocumentSource source, String targetNamespace,
                          Set<String> imports) {
            super(rootName, url, source, imports);
            this.targetNamespace = targetNamespace;
        }

        public String getTargetNamespace() {
            return targetNamespace;
        }

        public boolean isSchema() {
            return true;
        }
    }


    private static final class WSDLImpl extends SDDocumentImpl implements SDDocument.WSDL {
        private final String targetNamespace;
        private final boolean hasPortType;
        private final boolean hasService;
        private final Set<QName> allServices;

        public WSDLImpl(QName rootName, URL url, SDDocumentSource source, String targetNamespace, boolean hasPortType,
                        boolean hasService, Set<String> imports,Set<QName> allServices) {
            super(rootName, url, source, imports);
            this.targetNamespace = targetNamespace;
            this.hasPortType = hasPortType;
            this.hasService = hasService;
            this.allServices = allServices;
        }

        public String getTargetNamespace() {
            return targetNamespace;
        }

        public boolean hasPortType() {
            return hasPortType;
        }

        public boolean hasService() {
            return hasService;
        }

        public Set<QName> getAllServices() {
            return allServices;
        }

        public boolean isWSDL() {
            return true;
        }
    }

    private class DocumentLocationResolverImpl implements DocumentLocationResolver {
        private DocumentAddressResolver delegate;

        DocumentLocationResolverImpl(DocumentAddressResolver delegate) {
            this.delegate = delegate;
        }

        public String getLocationFor(String namespaceURI, String systemId) {
            if (sddocResolver == null) {
                return systemId;
            }
            try {
                URL ref = new URL(getURL(), systemId);
                SDDocument refDoc = sddocResolver.resolve(ref.toExternalForm());
                if (refDoc == null)
                    return systemId;  // not something we know. just leave it as is.

                return delegate.getRelativeAddressFor(SDDocumentImpl.this, refDoc);
            } catch (MalformedURLException mue) {
                return null;
            }
        }
    }

}
