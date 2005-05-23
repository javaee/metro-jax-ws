/*
 * $Id: ConfigurationException.java,v 1.1 2005-05-23 23:13:24 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.config;

import com.sun.tools.ws.processor.ProcessorException;
import com.sun.xml.ws.util.localization.Localizable;

/**
 *
 * @author JAX-RPC Development Team
 */
public class ConfigurationException extends ProcessorException {

    public ConfigurationException(String key) {
        super(key);
    }

    public ConfigurationException(String key, String arg) {
        super(key, arg);
    }

    public ConfigurationException(String key, Object[] args) {
        super(key, args);
    }

    public ConfigurationException(String key, Localizable arg) {
        super(key, arg);
    }

    public String getResourceBundleName() {
        return "com.sun.tools.ws.resources.configuration";
    }

}
