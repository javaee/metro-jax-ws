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
    private boolean allMemory;
    private int memoryThreshold;

    @FeatureConstructor
    public StreamingAttachmentFeature() {
    }

    @FeatureConstructor({"dir","parseEagerly","allMemory","memoryThreshold"})
    public StreamingAttachmentFeature(@Nullable String dir, boolean parseEagerly, boolean allMemory, int memoryThreshold) {
        this.enabled = true;
        this.dir = dir;
        this.parseEagerly = parseEagerly;
        this.allMemory = allMemory;
        this.memoryThreshold = memoryThreshold;
    }

    public String getID() {
        return ID;
    }

    public MIMEConfig getConfig() {
        if (config == null) {
            config = new MIMEConfig();
            config.setDir(dir);
            config.setParseEagerly(parseEagerly);
            config.setOnlyMemory(allMemory);
            config.setInMemorySize(memoryThreshold);
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
     * All the attachments are kept in memory
     */
    public void setAllMemory(boolean allMemory) {
        this.allMemory = allMemory;
    }

    /**
     * After this threshold(no of bytes), large attachments are
     * written to file system
     */
    public void setMemoryThreshold(int memoryThreshold) {
        this.memoryThreshold = memoryThreshold;
    }

}