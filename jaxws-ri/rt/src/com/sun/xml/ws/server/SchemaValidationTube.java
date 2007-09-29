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
import com.sun.xml.ws.util.DOMUtil;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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

/**
 * @author Jitendra Kotamraju
 */
public class SchemaValidationTube extends AbstractFilterTubeImpl {

    private final WSBinding binding;
    private final ServiceDefinition docs;
    private final Schema schema;
    private final Validator validator;

    public SchemaValidationTube(WSEndpoint endpoint, WSBinding binding, Tube next) {
        super(next);
        this.binding = binding;
        docs = endpoint.getServiceDefinition();
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            schema = sf.newSchema(getSchemaSources());
        } catch(SAXException e) {
            throw new WebServiceException(e);
        }
        validator = schema.newValidator();
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
                Transformer trans = XmlUtil.newTransformer();
System.out.println("******** Data="+new String(bab.toByteArray()));               
                Source source = new StreamSource(bab.newInputStream(), doc.getURL().toExternalForm());
                DOMResult result = new DOMResult(DOMUtil.createDom());
                try {
                    trans.transform(source, result);
                } catch(TransformerException te) {
                    throw new WebServiceException(te);
                }
                //Document document = result.getNode().getOwnerDocument();
                Node schemaNode = getSchemaElement((Document)result.getNode());
                if (schemaNode != null) {
                    list.add(new DOMSource(schemaNode, doc.getURL().toExternalForm()));
                }
            }
        }
        return list.toArray(new Source[list.size()]) ;
    }

    /**
     * Locates xsd:schema element in the WSDL
     *
     * @param doc WSDL document
     * @return first xsd:schema element
     */
    private @Nullable Element getSchemaElement(Document doc) {

        Element e = doc.getDocumentElement();
        assert e.getNamespaceURI().equals(WSDLConstants.NS_WSDL);
        assert e.getLocalName().equals("definitions");

        /*
        node.
        if (!(node instanceof Element)) {
            return null;
        }
        Element e = (Element)node;
        */
        NodeList typesList = e.getElementsByTagNameNS(WSDLConstants.NS_WSDL, "types");
        for(int i=0; i < typesList.getLength(); i++) {
            if (typesList.item(i) instanceof Element) {
                NodeList schemaList = ((Element)typesList.item(i)).getElementsByTagNameNS(WSDLConstants.NS_XMLNS, "schema");
                for(int j=0; j < schemaList.getLength(); j++) {
                    if (schemaList.item(j) instanceof Element) {
                        return (Element)schemaList.item(j);
                    }
                }
            }
        }
        return null;
    }

    private static class ValidationDocumentAddressResolver implements DocumentAddressResolver {

        @Nullable
        public String getRelativeAddressFor(@NotNull SDDocument current, @NotNull SDDocument referenced) {
            System.out.println("Curent="+current+" ref="+referenced);
            System.out.println("Resolving to = "+referenced.getURL().toExternalForm());
            return referenced.getURL().toExternalForm();    // TODO
        }
    }

    protected SchemaValidationTube(SchemaValidationTube that, TubeCloner cloner) {
        super(that,cloner);
        this.binding = that.binding;
        this.docs = that.docs;
        this.schema = that.schema;
        this.validator = schema.newValidator();
    }

    @Override
    public NextAction processRequest(Packet request) {
        validator.reset();
        Message msg = request.getMessage().copy();
        Source source = msg.readPayloadAsSource();
        try {
            // Validator javadoc seems to allow ONLY SAX, and DOM Sources
            // But the impl seems to handle all kinds.
            validator.validate(source);
        } catch(IOException e) {
            throw new WebServiceException(e);
        } catch(SAXException e) {
            throw new WebServiceException(e);
        }
        return super.processRequest(request);
    }

    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new SchemaValidationTube(this,cloner);
    }

}
