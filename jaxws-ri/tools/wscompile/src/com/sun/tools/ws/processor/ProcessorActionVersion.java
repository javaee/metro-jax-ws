/*
 * $Id: ProcessorActionVersion.java,v 1.2 2005-07-18 18:13:54 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor;

/**
 * @author WS Development Team
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
