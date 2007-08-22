package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.FeatureConstructor;

import javax.xml.ws.WebServiceFeature;

import org.jvnet.mimepull.MIMEConfig;

/**
 * @author Jitendra Kotamraju
 */
public final class MIMEFeature extends WebServiceFeature {
    /**
     * Constant value identifying the {@link @MIME} feature.
     */
    public static final String ID = "http://jax-ws.dev.java.net/features/mime";

    private final String dir;


    @FeatureConstructor
    public MIMEFeature() {
        this(null);
    }

    @FeatureConstructor
    public MIMEFeature(String dir) {
        this.enabled = true;
        this.dir = dir;
    }

    public String getID() {
        return ID;
    }

    public String getDir() {
        return dir;
    }

    public MIMEConfig getConfig() {
        throw new UnsupportedOperationException();
    }

}
