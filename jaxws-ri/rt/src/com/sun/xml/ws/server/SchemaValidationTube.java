package com.sun.xml.ws.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.DocumentAddressResolver;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Jitendra Kotamraju
 */
public class SchemaValidationTube extends AbstractFilterTubeImpl {

    private static final Logger LOGGER = Logger.getLogger(SchemaValidationTube.class.getName());

    private final WSBinding binding;
    private final ServiceDefinition docs;
    private final Schema schema;
    private final Validator validator;

    public SchemaValidationTube(WSEndpoint endpoint, WSBinding binding, Tube next) {
        super(next);
        this.binding = binding;
        docs = endpoint.getServiceDefinition();
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source[] sources = getSchemaSources();
        for(Source source : sources) {
            LOGGER.fine("Constructing validation Schema from = "+source.getSystemId());
        }
        try {
            schema = sf.newSchema(sources);
        } catch(SAXException e) {
            throw new WebServiceException(e);
        }
        validator = schema.newValidator();
        validator.setResourceResolver(new ResourceResolver());
    }

    private Source[] getSchemaSources() {
        List<Source> list = new ArrayList<Source>();
        DocumentAddressResolver resolver = new ValidationDocumentAddressResolver();
        for(SDDocument doc : docs) {
            if (doc.isWSDL()) {
                ByteArrayBuffer bab = new ByteArrayBuffer();
                try {
                    doc.writeTo(null, resolver, bab);
                } catch (IOException ioe) {
                    throw new WebServiceException(ioe);
                }

                // Get WSDL infoset as DOM
                Transformer trans = XmlUtil.newTransformer();
                Source source = new StreamSource(bab.newInputStream(), doc.getURL().toExternalForm());
                DOMResult result = new DOMResult();
                try {
                    trans.transform(source, result);
                } catch(TransformerException te) {
                    throw new WebServiceException(te);
                }

                // Get xsd:schema node from WSDL's DOM
                addSchemaSource((Document)result.getNode(), doc, list);
            }
        }
        return list.toArray(new Source[list.size()]) ;
    }

    private static class ResourceResolver implements LSResourceResolver {

        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            LOGGER.fine("type="+type+ " namespaceURI="+namespaceURI+" publicId="+publicId+" systemId="+systemId+" baseURI="+baseURI);
            throw new UnsupportedOperationException(" TODO implement it");
        }

    }

    /**
     * Locates xsd:schema elements in the WSDL and creates DOMSource and adds them to the list
     *
     * @param doc WSDL document
     * @param sddoc SDDocument for WSDL document
     * @param list xsd:schema DOMSource list
     */
    private @Nullable void addSchemaSource(Document doc, SDDocument sddoc, List<Source> list) {

        Element e = doc.getDocumentElement();
        assert e.getNamespaceURI().equals(WSDLConstants.NS_WSDL);
        assert e.getLocalName().equals("definitions");

        NodeList typesList = e.getElementsByTagNameNS(WSDLConstants.NS_WSDL, "types");
        for(int i=0; i < typesList.getLength(); i++) {
            if (typesList.item(i) instanceof Element) {
                NodeList schemaList = ((Element)typesList.item(i)).getElementsByTagNameNS(WSDLConstants.NS_XMLNS, "schema");
                for(int j=0; j < schemaList.getLength(); j++) {
                    if (schemaList.item(j) instanceof Element) {
                        list.add(new DOMSource(schemaList.item(j), sddoc.getURL().toExternalForm()));
                    }
                }
            }
        }
    }

    private static class ValidationDocumentAddressResolver implements DocumentAddressResolver {

        @Nullable
        public String getRelativeAddressFor(@NotNull SDDocument current, @NotNull SDDocument referenced) {
            LOGGER.fine("Current = "+current.getURL()+" resolved relative="+referenced.getURL());
            return referenced.getURL().toExternalForm();
        }
    }

    protected SchemaValidationTube(SchemaValidationTube that, TubeCloner cloner) {
        super(that,cloner);
        this.binding = that.binding;
        this.docs = that.docs;
        this.schema = that.schema;
        this.validator = schema.newValidator();
        validator.setResourceResolver(new ResourceResolver());
    }

    @Override
    public NextAction processRequest(Packet request) {
        if (!request.getMessage().hasPayload() || request.getMessage().isFault()) {
            return super.processRequest(request);
        }
        validator.reset();
        Message msg = request.getMessage().copy();
        Source source = msg.readPayloadAsSource();
        try {
            // Validator javadoc allows ONLY SAX, and DOM Sources
            // But the impl seems to handle all kinds.
            validator.validate(source);
        } catch(IOException e) {
            throw new WebServiceException(e);
        } catch(SAXException e) {
            throw new WebServiceException(e);
        }
        return super.processRequest(request);
    }

    @Override
    public NextAction processResponse(Packet response) {
        if (response.getMessage() == null || !response.getMessage().hasPayload() || response.getMessage().isFault()) {
            return super.processResponse(response);
        }
        validator.reset();
        Message msg = response.getMessage().copy();
        Source source = msg.readPayloadAsSource();
        try {
            // Validator javadoc allows ONLY SAX, and DOM Sources
            // But the impl seems to handle all kinds.
            validator.validate(source);
        } catch(IOException e) {
            throw new WebServiceException(e);
        } catch(SAXException e) {
            throw new WebServiceException(e);
        }
        return super.processResponse(response);
    }


    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new SchemaValidationTube(this,cloner);
    }

}
