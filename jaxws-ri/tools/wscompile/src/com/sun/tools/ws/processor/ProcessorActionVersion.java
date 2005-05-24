/*
 * $Id: ProcessorActionVersion.java,v 1.1 2005-05-24 13:43:48 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor;

/**
 * @author JAX-RPC Development Team
 *
 * Typesafe enum class to hold the ProcessorActionVersion
 */
public enum ProcessorActionVersion {
    PRE_20("1.1.2"), VERSION_20("2.0");

    ProcessorActionVersion(String version) {
        this.version = version;
    }

    public String value() {
        return version;
    }

    private final String version;
}
