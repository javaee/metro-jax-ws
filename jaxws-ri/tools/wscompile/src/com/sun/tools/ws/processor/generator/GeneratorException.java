/*
 * $Id: GeneratorException.java,v 1.1 2005-05-23 23:14:48 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.generator;

import com.sun.tools.ws.processor.ProcessorException;
import com.sun.xml.ws.util.localization.Localizable;

/**
 *
 * @author JAX-RPC Development Team
 */
public class GeneratorException extends ProcessorException {

    public GeneratorException(String key) {
        super(key);
    }

    public GeneratorException(String key, String arg) {
        super(key, arg);
    }

    public GeneratorException(String key, Object[] args) {
        super(key, args);
    }

    public GeneratorException(String key, Localizable arg) {
        super(key, arg);
    }

    public String getResourceBundleName() {
        return "com.sun.tools.ws.resources.generator";
    }
}
