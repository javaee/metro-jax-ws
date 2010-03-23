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

package com.sun.xml.ws.util.pipe;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.stream.buffer.XMLStreamBufferResult;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.server.DocumentAddressResolver;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.developer.SchemaValidationFeature;
import com.sun.xml.ws.developer.ValidationErrorHandler;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.MetadataUtil;
import com.sun.xml.ws.util.xml.MetadataDocument;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import org.w3c.dom.*;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Tube} that does the schema validation.
 *
 * @author Jitendra Kotamraju
 */
public abstract class AbstractSchemaValidationTube extends AbstractFilterTubeImpl {

    private static final Logger LOGGER = Logger.getLogger(AbstractSchemaValidationTube.class.getName());
    protected static final String HONOUR_ALL_SCHEMA_LOCATIONS_ID = "http://apache.org/xml/features/honour-all-schemaLocations";

    protected final WSBinding binding;
    protected final SchemaValidationFeature feature;
    protected final DocumentAddressResolver resolver = new ValidationDocumentAddressResolver();
    protected final SchemaFactory sf;
    protected final boolean honorAllSchemaLocations;

    public AbstractSchemaValidationTube(WSBinding binding, Tube next) {
        super(next);
        this.binding = binding;
        feature = binding.getFeature(SchemaValidationFeature.class);
        sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        boolean allLocations = false;
        try {
            sf.setFeature(HONOUR_ALL_SCHEMA_LOCATIONS_ID, true);
            allLocations = true;
       } catch(Exception e) {
            // This features is supported only xerces 2.7 onwards.
            // So just ignore the exception.
        }
        honorAllSchemaLocations = allLocations;
    }

    protected AbstractSchemaValidationTube(AbstractSchemaValidationTube that, TubeCloner cloner) {
        super(that, cloner);
        this.binding = that.binding;
        this.feature = that.feature;
        this.sf = that.sf;
        this.honorAllSchemaLocations = that.honorAllSchemaLocations;
    }

    protected abstract Validator getValidator();

    protected abstract boolean isNoValidation();

    private static class ValidationDocumentAddressResolver implements DocumentAddressResolver {

        @Nullable
        public String getRelativeAddressFor(@NotNull SDDocument current, @NotNull SDDocument referenced) {
            LOGGER.fine("Current = "+current.getURL()+" resolved relative="+referenced.getURL());
            return referenced.getURL().toExternalForm();
        }
    }

    protected Document createDOM(SDDocument doc) {
        // Get infoset
        ByteArrayBuffer bab = new ByteArrayBuffer();
        try {
            doc.writeTo(null, resolver, bab);
        } catch (IOException ioe) {
            throw new WebServiceException(ioe);
        }

        // Convert infoset to DOM
        Transformer trans = XmlUtil.newTransformer();
        Source source = new StreamSource(bab.newInputStream(), null); //doc.getURL().toExternalForm());
        DOMResult result = new DOMResult();
        try {
            trans.transform(source, result);
        } catch(TransformerException te) {
            throw new WebServiceException(te);
        }
        return (Document)result.getNode();
    }

    protected class MetadataResolverImpl implements MetadataUtil.MetadataResolver, LSResourceResolver {

        // systemID --> SDDocument
        final Map<String, SDDocument> docs = new HashMap<String, SDDocument>();

        // targetnamespace --> SDDocument
        final Map<String, SDDocument> nsMapping = new HashMap<String, SDDocument>();

        public MetadataResolverImpl() {
        }

        public MetadataResolverImpl(Iterable<SDDocument> it) {
            for(SDDocument doc : it) {
                if (doc.isSchema()) {
                    docs.put(doc.getURL().toExternalForm(), doc);
                    nsMapping.put(((SDDocument.Schema)doc).getTargetNamespace(), doc);
                }
            }
        }

        public void addInlinedSchemas(Map<String, DOMSource> inlineSchemas) {
            for(Map.Entry<String, DOMSource>e : inlineSchemas.entrySet()) {
                try {
                    XMLStreamBufferResult xsbr = XmlUtil.identityTransform(e.getValue(), new XMLStreamBufferResult());
                    String systemId = e.getKey();
                    SDDocumentSource sds = SDDocumentSource.create(new URL(systemId), xsbr.getXMLStreamBuffer());
                    SDDocument sdoc = MetadataDocument.create(sds, new QName(""), new QName(""));
                    docs.put(systemId, sdoc);
                    nsMapping.put(((SDDocument.Schema)sdoc).getTargetNamespace(), sdoc);
                } catch(Exception ex) {
                    LOGGER.log(Level.WARNING, "Exception in adding inlined schemas to resolver", e);
                }
            }
        }

        public SDDocument resolveEntity(String systemId) {
            SDDocument sdi = docs.get(systemId);
            if (sdi == null) {
                SDDocumentSource sds;
                try {
                    sds = SDDocumentSource.create(new URL(systemId));
                } catch(MalformedURLException e) {
                    throw new WebServiceException(e);
                }
                sdi = MetadataDocument.create(sds, new QName(""), new QName(""));
                docs.put(systemId, sdi);
            }
            return sdi;
        }

        public LSInput resolveResource(String type, String namespaceURI, String publicId, final String systemId, final String baseURI) {
            LOGGER.fine("type="+type+ " namespaceURI="+namespaceURI+" publicId="+publicId+" systemId="+systemId+" baseURI="+baseURI);
            try {
                final SDDocument doc;
                final URL rel;
                if (systemId == null) {
                    doc = nsMapping.get(namespaceURI);
                    rel = null;
                } else {
                    URL base = baseURI == null ? null : new URL(baseURI);
                    rel = new URL(base, systemId);
                    doc = docs.get(rel.toExternalForm());
                }
                if (doc != null) {
                    return new LSInput() {

                        public Reader getCharacterStream() {
                            return null;
                        }

                        public void setCharacterStream(Reader characterStream) {
                            throw new UnsupportedOperationException();
                        }

                        public InputStream getByteStream() {
                            ByteArrayBuffer bab = new ByteArrayBuffer();
                            try {
                                doc.writeTo(null, resolver, bab);
                            } catch (IOException ioe) {
                                throw new WebServiceException(ioe);
                            }
                            return bab.newInputStream();
                        }

                        public void setByteStream(InputStream byteStream) {
                            throw new UnsupportedOperationException();
                        }

                        public String getStringData() {
                            return null;
                        }

                        public void setStringData(String stringData) {
                            throw new UnsupportedOperationException();
                        }

                        public String getSystemId() {
                            return doc.getURL().toExternalForm();
                        }

                        public void setSystemId(String systemId) {
                            throw new UnsupportedOperationException();
                        }

                        public String getPublicId() {
                            return null;
                        }

                        public void setPublicId(String publicId) {
                            throw new UnsupportedOperationException();
                        }

                        public String getBaseURI() {
                            return doc.getURL().toExternalForm();
                        }

                        public void setBaseURI(String baseURI) {
                            throw new UnsupportedOperationException();
                        }

                        public String getEncoding() {
                            return null;
                        }

                        public void setEncoding(String encoding) {
                            throw new UnsupportedOperationException();
                        }

                        public boolean getCertifiedText() {
                            return false;
                        }

                        public void setCertifiedText(boolean certifiedText) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            } catch(Exception e) {
                LOGGER.log(Level.WARNING, "Exception in LSResourceResolver impl", e);
            }
            LOGGER.fine("Don't know about systemId="+systemId+" baseURI="+baseURI);
            return null;
        }

    }

    protected Source[] getSchemaSources(Iterable<SDDocument> docs, MetadataResolverImpl mdresolver) {
        // All schema fragments in WSDLs are used to create inlinedSchemas schema
        Map<String, DOMSource> inlinedSchemas = new HashMap<String, DOMSource>();

        for(SDDocument sdoc: docs) {
            if (sdoc.isWSDL()) {
                Document dom = createDOM(sdoc);
                // Get xsd:schema node from WSDL's DOM
                addSchemaFragmentSource(dom, sdoc.getURL().toExternalForm(), inlinedSchemas);
            }
        }
        LOGGER.fine("WSDL inlined schema fragment documents(these are used to create a pseudo schema) = "+ inlinedSchemas.keySet());
        if (inlinedSchemas.isEmpty()) {
            return new Source[0];   // WSDL doesn't have any schema fragments
        }else if (inlinedSchemas.size() == 1) {
            // Every type definition is traversable from this inlined schema
            return new Source[] {inlinedSchemas.values().iterator().next()};
        }

        Source pseudoSchema = getPseudoSchemaSources(inlinedSchemas);
        List<Source> sources = new ArrayList<Source>();
        sources.add(pseudoSchema);
        mdresolver.addInlinedSchemas(inlinedSchemas);
        if (honorAllSchemaLocations) {
            // Just return pseudo schema since every other type definition
            // is traversable from this pseudo schema.
            return new Source[] {pseudoSchema};
        }

        // If multiple schemas have the same namespace, JAXP in JDK5 considers
        // only one of those schemas. So some type definitions cannot be reached
        // The only thing we can do is to add all the schemas and hope for best
        for(SDDocument sdoc: docs) {
            if (sdoc.isSchema()) {
                Document dom = createDOM(sdoc);
                sources.add(new DOMSource(dom, sdoc.getURL().toExternalForm()));
            }
        }
        return sources.toArray(new Source[sources.size()]);
    }

    /**
     * Locates xsd:schema elements in the WSDL and creates DOMSource and adds them to the list
     *
     * @param doc WSDL document
     * @param systemId systemId for WSDL document
     * @param list xsd:schema DOMSource list
     */
    protected @Nullable void addSchemaFragmentSource(Document doc, String systemId, List<Source> list) {

        Element e = doc.getDocumentElement();
        assert e.getNamespaceURI().equals(WSDLConstants.NS_WSDL);
        assert e.getLocalName().equals("definitions");

        NodeList typesList = e.getElementsByTagNameNS(WSDLConstants.NS_WSDL, "types");
        for(int i=0; i < typesList.getLength(); i++) {
            NodeList schemaList = ((Element)typesList.item(i)).getElementsByTagNameNS(WSDLConstants.NS_XMLNS, "schema");
            for(int j=0; j < schemaList.getLength(); j++) {
                Element elem = (Element)schemaList.item(j);
                NamespaceSupport nss = new NamespaceSupport();
                buildNamespaceSupport(nss, elem);
                patchDOMFragment(nss, elem);
                list.add(new DOMSource(elem, systemId+"#schema"+j));
            }
        }
    }

    protected @Nullable void addSchemaFragmentSource(Document doc, String systemId, Map<String, DOMSource> map) {
        Element e = doc.getDocumentElement();
        assert e.getNamespaceURI().equals(WSDLConstants.NS_WSDL);
        assert e.getLocalName().equals("definitions");

        NodeList typesList = e.getElementsByTagNameNS(WSDLConstants.NS_WSDL, "types");
        for(int i=0; i < typesList.getLength(); i++) {
            NodeList schemaList = ((Element)typesList.item(i)).getElementsByTagNameNS(WSDLConstants.NS_XMLNS, "schema");
            for(int j=0; j < schemaList.getLength(); j++) {
                Element elem = (Element)schemaList.item(j);
                NamespaceSupport nss = new NamespaceSupport();
                buildNamespaceSupport(nss, elem);
                patchDOMFragment(nss, elem);
                String docId = systemId+"#schema"+j;
                map.put(docId, new DOMSource(elem, docId));
            }
        }
    }
    

    /*
     * Recursively visit ancestors and build up {@link org.xml.sax.helpers.NamespaceSupport} oject.
     */
    private void buildNamespaceSupport(NamespaceSupport nss, Node node) {
        if(node==null || node.getNodeType()!=Node.ELEMENT_NODE)
            return;

        buildNamespaceSupport( nss, node.getParentNode() );

        nss.pushContext();
        NamedNodeMap atts = node.getAttributes();
        for( int i=0; i<atts.getLength(); i++ ) {
            Attr a = (Attr)atts.item(i);
            if( "xmlns".equals(a.getPrefix()) ) {
                nss.declarePrefix( a.getLocalName(), a.getValue() );
                continue;
            }
            if( "xmlns".equals(a.getName()) ) {
                nss.declarePrefix( "", a.getValue() );
                continue;
            }
        }
    }

    /**
     * Adds inscope namespaces as attributes to  <xsd:schema> fragment nodes.
     *
     * @param nss namespace context info
     * @param elem that is patched with inscope namespaces
     */
    private @Nullable void patchDOMFragment(NamespaceSupport nss, Element elem) {
        NamedNodeMap atts = elem.getAttributes();
        for( Enumeration en = nss.getPrefixes(); en.hasMoreElements(); ) {
            String prefix = (String)en.nextElement();

            for( int i=0; i<atts.getLength(); i++ ) {
                Attr a = (Attr)atts.item(i);
                if (!"xmlns".equals(a.getPrefix()) || !a.getLocalName().equals(prefix)) {
                    LOGGER.fine("Patching with xmlns:"+prefix+"="+nss.getURI(prefix));
                    elem.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:"+prefix, nss.getURI(prefix));
                }
            }
        }
    }

    protected @Nullable Source getPseudoSchemaSources(Map<String, DOMSource> pseudo) {
        assert pseudo.size() > 1;
        StringBuilder sb = new StringBuilder("<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema' targetNamespace='urn:x-jax-ws'>");
        for(Map.Entry<String, DOMSource> e : pseudo.entrySet()) {
            DOMSource src = e.getValue();
            Element elem = (Element)src.getNode();
            String ns = elem.getAttribute("targetNamespace");
            sb.append("<xsd:import schemaLocation='").append(e.getKey()).append("'");
            if (!ns.equals("")) {
                sb.append(" namespace='").append(ns).append("'");
            }
            sb.append("/>");
        }
        sb.append("</xsd:schema>");
        LOGGER.fine("Pseudo Schema="+sb);

        return new StreamSource(new StringReader(sb.toString()), "http://pseudo");
    }

    protected void doProcess(Packet packet) throws SAXException {
        getValidator().reset();
        Class<? extends ValidationErrorHandler> handlerClass = feature.getErrorHandler();
        ValidationErrorHandler handler;
        try {
            handler = handlerClass.newInstance();
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
        handler.setPacket(packet);
        getValidator().setErrorHandler(handler);
        Message msg = packet.getMessage().copy();
        Source source = msg.readPayloadAsSource();
        try {
            // Validator javadoc allows ONLY SAX, and DOM Sources
            // But the impl seems to handle all kinds.
            getValidator().validate(source);
        } catch(IOException e) {
            throw new WebServiceException(e);
        }
    }

    protected static void printSource(Source src) {
        try {
            ByteArrayBuffer bos = new ByteArrayBuffer();
            StreamResult sr = new StreamResult(bos );
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(src, sr);
            LOGGER.info("**** src ******"+bos.toString());
            bos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}