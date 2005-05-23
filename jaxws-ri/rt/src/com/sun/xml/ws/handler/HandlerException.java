/*
 * $Id: HandlerException.java,v 1.1 2005-05-23 22:37:25 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.handler;

import com.sun.xml.ws.util.exception.JAXRPCExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * @author JAX-WS Development Team
 */
public class HandlerException extends JAXRPCExceptionBase {

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
