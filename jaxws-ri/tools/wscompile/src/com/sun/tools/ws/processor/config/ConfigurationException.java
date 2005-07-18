/*
 * $Id: ConfigurationException.java,v 1.2 2005-07-18 18:13:55 kohlert Exp $
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
 * @author WS Development Team
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
