/*
 * $Id: ModelException.java,v 1.3 2005-07-23 04:10:58 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.model;

import com.sun.tools.ws.processor.ProcessorException;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * ModelException represents an exception that occurred while
 * visiting service model.
 *
 * @see ProcessorException
 *
 * @author WS Development Team
 */
public class ModelException extends ProcessorException {

    public ModelException(String key) {
        super(key);
    }

    public ModelException(String key, String arg) {
        super(key, arg);
    }

    public ModelException(String key, Object[] args) {
        super(key, args);
    }

    public ModelException(String key, Localizable arg) {
        super(key, arg);
    }

    public ModelException(Localizable arg) {
        super("model.nestedModelError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.tools.ws.resources.model";
    }
}
