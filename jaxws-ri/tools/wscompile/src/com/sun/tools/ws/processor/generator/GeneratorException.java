/*
 * $Id: GeneratorException.java,v 1.2 2005-07-18 18:13:57 kohlert Exp $
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
 * @author WS Development Team
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
