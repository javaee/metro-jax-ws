package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.FeatureConstructor;

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

    private boolean reject;

    /**
     * Create an <code>SchemaValidationFeature</code>.
     * The instance created will be enabled.
     */
    @FeatureConstructor({"reject"})
    public SchemaValidationFeature(boolean reject) {
        this.enabled = true;
        this.reject = reject;
    }

    public String getID() {
        return ID;
    }

    /**
     * Invalid schema instances are rejected, a SOAP fault message is created
     * for any invalid request and response message. If it is set to false, schema
     * validation messages are just logged.
     */
    public boolean getReject() {
        return reject;
    }
}