/*
 * $Id: HandlerException.java,v 1.3 2005-08-08 19:32:30 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.handler;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * Exception thrown by handler-related code. Extends
 * {@link com.sun.xml.ws.util.exception.JAXWSExceptionBase}
 * using the appropriate resource bundle.
 *
 * @see com.sun.xml.ws.util.exception.JAXWSExceptionBase
 *
 * @author WS Development Team
 */
public class HandlerException extends JAXWSExceptionBase {

    public HandlerException(String key) {
        super(key);
    }

    public HandlerException(String key, String arg) {
        super(key, arg);
    }

    public HandlerException(String key, Object[] args) {
        super(key, args);
    }

    public HandlerException(String key, Localizable arg) {
        super(key, arg);
    }

    public HandlerException(Localizable arg) {
        super("handler.nestedError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.handler";
    }
}
