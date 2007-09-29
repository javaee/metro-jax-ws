package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.FeatureConstructor;

import javax.xml.ws.WebServiceFeature;


/**
 *
 * @since 2.1.3
 * @see SchemaValidation
 */
public class SchemaValidationFeature extends WebServiceFeature {
    /**
     * Constant value identifying the SchemaValidationFeature
     */
    public static final String ID = "http://jax-ws.dev.java.net/features/schema-validation";

    /**
     * Create an <code>SchemaValidationFeature</code>.
     * The instance created will be enabled.
     */
    @FeatureConstructor
    public SchemaValidationFeature() {
        this.enabled = true;
    }

    public String getID() {
        return ID;
    }
}