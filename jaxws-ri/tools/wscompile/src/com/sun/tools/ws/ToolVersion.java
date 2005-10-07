package com.sun.tools.ws;

import com.sun.xml.ws.util.Version;

/**
 * Obtains the version number of the JAX-WS tools.
 * @author Kohsuke Kawaguchi
 */
public abstract class ToolVersion {
    private ToolVersion() {}    // no instanciation please

    public static final Version VERSION = Version.create(ToolVersion.class.getResourceAsStream("version.properties"));
}
