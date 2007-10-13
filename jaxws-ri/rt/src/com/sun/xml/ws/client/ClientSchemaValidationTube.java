package com.sun.xml.ws.client;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.util.DOMUtil;
import com.sun.xml.ws.util.pipe.AbstractSchemaValidationTube;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.ws.WebServiceException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * {@link Tube} that does the schema validation on the client side.
 *
 * @author Jitendra Kotamraju
 */
public class ClientSchemaValidationTube extends AbstractSchemaValidationTube {

    private static final Logger LOGGER = Logger.getLogger(ClientSchemaValidationTube.class.getName());

    private final Schema schema;
    private final Validator validator;
    private final boolean noValidation;
    private final WSDLPort port;

    public ClientSchemaValidationTube(WSBinding binding, WSDLPort port, Tube next) {
        super(binding, next);
        this.port = port;
        Source[] sources = null;
        if (port != null) {
            String primaryWsdl = port.getOwner().getParent().getLocation().getSystemId();
            sources = getSchemaSources(primaryWsdl);
            for(Source source : sources) {
                LOGGER.fine("Constructing validation Schema from = "+source.getSystemId());
                //printDOM((DOMSource)source);
            }
        }
        if (sources != null) {
            noValidation = false;
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
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

    private Source[] getSchemaSources(String primaryWsdl) {
        InputStream is;
        try {
            is = new URL(primaryWsdl).openStream();
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
        Document dom = (Document)DOMUtil.createDOMNode(is);
        List<Source> list = new ArrayList<Source>();
        addSchemaFragmentSource(dom, primaryWsdl, list);
        return list.toArray(new Source[list.size()]) ;
    }

    protected Validator getValidator() {
        return validator;
    }

    protected boolean isNoValidation() {
        return noValidation;
    }

    protected ClientSchemaValidationTube(ClientSchemaValidationTube that, TubeCloner cloner) {
        super(that,cloner);
        this.port = that.port;
        this.schema = that.schema;
        this.validator = schema.newValidator();
        this.noValidation = that.noValidation;
    }

    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new ClientSchemaValidationTube(this,cloner);
    }

}