package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.FeatureConstructor;
import com.sun.istack.Nullable;

import javax.xml.ws.WebServiceFeature;

import org.jvnet.mimepull.MIMEConfig;

/**
 * Proxy needs to be created with this feature to configure StreamingAttachment
 * attachments behaviour.
 * 
 * <pre>
 * for e.g.: To configure all StreamingAttachment attachments to be kept in memory
 * <p>
 *
 * StreamingAttachmentFeature feature = new StreamingAttachmentFeature();
 * feature.setAllMemory(true);
 *
 * proxy = HelloService().getHelloPort(feature);
 *
 * </pre>
 *
 * @author Jitendra Kotamraju
 */
public final class StreamingAttachmentFeature extends WebServiceFeature {
    /**
     * Constant value identifying the {@link @StreamingAttachment} feature.
     */
    public static final String ID = "http://jax-ws.dev.java.net/features/mime";

    private MIMEConfig config;

    private String dir;
    private boolean parseEagerly;
    private long memoryThreshold;

    @FeatureConstructor
    public StreamingAttachmentFeature() {
    }

    @FeatureConstructor({"dir","parseEagerly","memoryThreshold"})
    public StreamingAttachmentFeature(@Nullable String dir, boolean parseEagerly, int memoryThreshold) {
        this.enabled = true;
        this.dir = dir;
        this.parseEagerly = parseEagerly;
        this.memoryThreshold = memoryThreshold;
    }

    public String getID() {
        return ID;
    }

    /**
     * Returns the configuration object. Once this is called, you cannot
     * change the configuration.
     *
     * @return
     */
    public MIMEConfig getConfig() {
        if (config == null) {
            config = new MIMEConfig();
            config.setDir(dir);
            config.setParseEagerly(parseEagerly);
            config.setMemoryThreshold(memoryThreshold);
            config.validate();
        }
        return config;
    }

    /**
     * Directory in which large attachments are stored
     */
    public void setDir(String dir) {
        this.dir = dir;
    }

    /**
     * StreamingAttachment message is parsed eagerly
     */
    public void setParseEagerly(boolean parseEagerly) {
        this.parseEagerly = parseEagerly;
    }

    /**
     * After this threshold(no of bytes), large attachments are
     * written to file system
     */
    public void setMemoryThreshold(int memoryThreshold) {
        this.memoryThreshold = memoryThreshold;
    }

}