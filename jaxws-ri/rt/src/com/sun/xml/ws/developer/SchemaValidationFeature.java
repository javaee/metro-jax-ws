package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.FeatureConstructor;
import com.sun.xml.ws.server.DraconianValidationErrorHandler;

import javax.xml.ws.WebServiceFeature;


/**
 * {@link WebServiceFeature} for schema validation.
 *
 * @since JAX-WS 2.1.3
 * @author Jitendra Kotamraju
 * @see SchemaValidation
 */
public class SchemaValidationFeature extends WebServiceFeature {
    /**
     * Constant value identifying the SchemaValidationFeature
     */
    public static final String ID = "http://jax-ws.dev.java.net/features/schema-validation";

    private Class<? extends ValidationErrorHandler> clazz;

    public SchemaValidationFeature() {
        this(DraconianValidationErrorHandler.class);
    }

    /**
     * Create an <code>SchemaValidationFeature</code>.
     * The instance created will be enabled.
     */
    @FeatureConstructor({"handler"})
    public SchemaValidationFeature(Class<? extends ValidationErrorHandler> clazz) {
        this.enabled = true;
        this.clazz = clazz;
    }

    public String getID() {
        return ID;
    }

    /**
     * Invalid schema instances are rejected, a SOAP fault message is created
     * for any invalid request and response message. If it is set to false, schema
     * validation messages are just logged.
     */
    public Class<? extends ValidationErrorHandler> getErrorHandler() {
        return clazz;
    }
}