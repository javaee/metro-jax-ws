package com.sun.xml.ws.util;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Represents the version information.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Version {
    /**
     * Represents the build id, which is a string like "b13" or "hudson-250".
     */
    public final String BUILD_ID;
    /**
     * Represents the complete version string, such as "JAX-WS RI 2.0-b19"
     */
    public final String BUILD_VERSION;
    /**
     * Represents the major JAX-WS version, such as "2.0".
     */
    public final String MAJOR_VERSION;

    private Version(String buildId, String buildVersion, String majorVersion) {
        this.BUILD_ID = fixNull(buildId);
        this.BUILD_VERSION = fixNull(buildVersion);
        this.MAJOR_VERSION = fixNull(majorVersion);
    }

    public static Version create(InputStream is) {
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (IOException e) {
            // ignore even if the property was not found. we'll treat everything as unknown
        }
        return new Version(
            props.getProperty("build-id"),
            props.getProperty("build-version"),
            props.getProperty("major-version"));
    }

    private String fixNull(String v) {
        if(v==null) return "unknown";
        return v;
    }
}
