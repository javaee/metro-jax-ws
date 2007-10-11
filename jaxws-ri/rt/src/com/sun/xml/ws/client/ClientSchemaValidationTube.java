package com.sun.xml.ws.client;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.util.pipe.AbstractSchemaValidationTube;

import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
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

    public ClientSchemaValidationTube(WSBinding binding, Tube next) {
        super(binding, next);
        this.schema = null;
        this.validator = null;
        this.noValidation = true;
    }

    protected Validator getValidator() {
        return validator;
    }

    protected boolean isNoValidation() {
        return noValidation;
    }

   protected ClientSchemaValidationTube(ClientSchemaValidationTube that, TubeCloner cloner) {
        super(that,cloner);
        this.schema = that.schema;
        this.validator = schema.newValidator();
        this.noValidation = that.noValidation;
    }

    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new ClientSchemaValidationTube(this,cloner);
    }

}