package com.sun.tools.ws;

import com.sun.xml.ws.util.ASCIIUtility;

import java.io.IOException;

/**
 * Obtains the version number of the JAX-WS tools.
 * @author Kohsuke Kawaguchi
 */
public abstract class ToolVersion {
    private ToolVersion() {}    // no instanciation please

    public static final String ID = initID();

    private static String initID() {
        try {
            return new String(ASCIIUtility.getBytes(ToolVersion.class.getResourceAsStream("version.properties")),"UTF-8");
        } catch (IOException e) {
            return "unknown";
        }
    }
}
