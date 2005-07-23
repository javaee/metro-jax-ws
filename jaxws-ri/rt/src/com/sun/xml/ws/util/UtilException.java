/*
 * $Id: UtilException.java,v 1.3 2005-07-23 04:10:14 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * UtilException represents an exception that occurred while
 * one of the util classes is operating.
 * 
 * @see JAXWSExceptionBase
 * 
 * @author JAX-WS Development Team
 */
public class UtilException extends JAXWSExceptionBase {

    public UtilException(String key) {
        super(key);
    }

    public UtilException(String key, String arg) {
        super(key, arg);
    }

    public UtilException(String key, Object[] args) {
        super(key, args);
    }

    public UtilException(String key, Localizable arg) {
        super(key, arg);
    }

    public UtilException(Localizable arg) {
        super("nestedUtilError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.util";
    }

}
