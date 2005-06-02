/*
 * $Id: UtilException.java,v 1.1 2005-06-02 14:32:49 bbissett Exp $
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
 * @see com.sun.xml.rpc.util.exception.JAXWSExceptionBase
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
        return "com.sun.xml.ws.util";
    }

}
