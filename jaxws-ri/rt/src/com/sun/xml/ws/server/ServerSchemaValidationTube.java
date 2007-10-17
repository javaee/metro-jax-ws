package com.sun.xml.ws.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.*;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.MetadataUtil;
import com.sun.xml.ws.util.pipe.AbstractSchemaValidationTube;
import com.sun.xml.ws.util.xml.XmlUtil;
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
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Tube} that does the schema validation on the server side.
 *
 * @author Jitendra Kotamraju
 */
public class ServerSchemaValidationTube extends AbstractSchemaValidationTube {

    private static final Logger LOGGER = Logger.getLogger(ServerSchemaValidationTube.class.getName());

    //private final ServiceDefinition docs;
    private final Schema schema;
    private final Validator validator;
    
    private final boolean noValidation;

    public ServerSchemaValidationTube(WSEndpoint endpoint, WSBinding binding, Tube next) {
        super(binding, next);
        //docs = endpoint.getServiceDefinition();
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source[] sources = getSchemaSources(endpoint.getServiceDefinition());
        for(Source source : sources) {
            LOGGER.fine("Constructing validation Schema from = "+source.getSystemId());
            //printDOM((DOMSource)source);
        }
        if (sources.length != 0) {
            noValidation = false;
            sf.setResourceResolver(new MetadataResolverImpl(endpoint.getServiceDefinition()));
            try {
                schema = sf.newSchema(sources);
            } catch(SAXException e) {
                throw new WebServiceException(e);
            }
            validator = schema.newValidator();
        } else {
            noValidation = true;
            schema = null;
            validator = null;
        }
    }

    /**
     * Constructs list of schema documents as follows:
     *   - all <xsd:schema> fragements from all WSDL documents.
     *   - all schema documents in the application(from WAR etc)
     *
     * @return list of root schema documents
     */
    private Source[] getSchemaSources(ServiceDefinition sd) {
        String primary = sd.getPrimary().getURL().toExternalForm();
        MetadataUtil.MetadataResolver mdresolver = new MetadataResolverImpl(sd);
        Map<String, SDDocumentImpl> docs = MetadataUtil.getMetadataClosure(primary, mdresolver, true);

        List<Source> list = new ArrayList<Source>();
        for(Map.Entry<String, SDDocumentImpl> entry : docs.entrySet()) {
            SDDocumentImpl doc = entry.getValue();
            // Add all xsd:schema fragments from all WSDLs. That should form a closure of schemas.
            if (doc.isWSDL()) {
                Document dom = createDOM(doc);
                // Get xsd:schema node from WSDL's DOM
                addSchemaFragmentSource(dom, doc.getURL().toExternalForm(), list);
            } else if (doc.isSchema()) {
                // If there are multiple schemas with the same targetnamespace,
                // JAXP works only with the first one. Above, all schema fragments may have the same targetnamespace,
                // and that means it will not include all the schemas. Since we have a list of schemas, just add them.
                Document dom = createDOM(doc);
                list.add(new DOMSource(dom, doc.getURL().toExternalForm()));
            }
        }
        //addSchemaSource(list);
        return list.toArray(new Source[list.size()]) ;
    }

    private class MetadataResolverImpl implements MetadataUtil.MetadataResolver, LSResourceResolver {

        Map<String, SDDocumentImpl> docs = new HashMap<String, SDDocumentImpl>();

        MetadataResolverImpl(ServiceDefinition sd) {
            for(SDDocument doc : sd) {
                SDDocumentImpl sdi = (SDDocumentImpl)doc;
                docs.put(sdi.getSystemId().toExternalForm(), sdi);
            }
        }

        public SDDocumentImpl resolveEntity(String systemId) {
            SDDocumentImpl sdi = docs.get(systemId);
            if (sdi == null) {
                SDDocumentSource sds;
                try {
                    sds = SDDocumentSource.create(new URL(systemId));
                } catch(MalformedURLException e) {
                    throw new WebServiceException(e);
                }
                sdi = SDDocumentImpl.create(sds, new QName(""), new QName(""));
                docs.put(systemId, sdi);
            }
            return sdi;
        }

        public LSInput resolveResource(String type, String namespaceURI, String publicId, final String systemId, final String baseURI) {
            LOGGER.fine("type="+type+ " namespaceURI="+namespaceURI+" publicId="+publicId+" systemId="+systemId+" baseURI="+baseURI);
            try {
                URL base = baseURI == null ? null : new URL(baseURI);
                final URL rel = new URL(base, systemId);
                final SDDocumentImpl doc = docs.get(rel.toExternalForm());
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
                        return rel.toExternalForm();
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
                        return rel.toExternalForm();
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



            } catch(Exception e) {
                LOGGER.log(Level.WARNING, "Exception in LSResourceResolver impl", e);
            }
            LOGGER.fine("Don't know about systemId="+systemId+" baseURI="+baseURI);
            return null;
        }

    }


    protected Validator getValidator() {
        return validator;
    }

    protected boolean isNoValidation() {
        return noValidation;
    }

    /**
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
                if (!"xmlns".equals(a.getPrefix()) || !a.getLocalName().equals("prefix")) {
                    LOGGER.fine("Patching with xmlns:"+prefix+"="+nss.getURI(prefix));
                    elem.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:"+prefix, nss.getURI(prefix));
                }
            }
        }
    }




    protected ServerSchemaValidationTube(ServerSchemaValidationTube that, TubeCloner cloner) {
        super(that,cloner);
        //this.docs = that.docs;
        this.schema = that.schema;
        this.validator = schema.newValidator();
        this.noValidation = that.noValidation;
    }

    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new ServerSchemaValidationTube(this,cloner);
    }

}
