/*
 * $Id: HandlerException.java,v 1.2 2005-05-25 20:16:28 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.handler;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * @author JAX-WS Development Team
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
