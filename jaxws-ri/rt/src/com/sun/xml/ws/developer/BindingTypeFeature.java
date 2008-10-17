package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.BindingID;

import javax.xml.ws.WebServiceFeature;

/**
 * Using this feature, the application could override the binding used by
 * the runtime(usually determined from WSDL).
 *
 * @author Jitendra Kotamraju
 */
public final class BindingTypeFeature extends WebServiceFeature {

    public static final String ID = "http://jax-ws.dev.java.net/features/binding";

    private final String bindingId;

    public BindingTypeFeature(String bindingId) {
        this.bindingId = bindingId;
    }

    public String getID() {
        return ID;
    }

    public String getBindingId() {
        return bindingId;
    }

}