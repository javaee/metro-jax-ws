/*
 * $Id: SOAPProtocolViolationException.java,v 1.2 2005-05-25 20:16:27 kohlert Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap.streaming;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * @author JAX-RPC Development Team
 */
public class SOAPProtocolViolationException extends JAXWSExceptionBase {
    public SOAPProtocolViolationException(String key) {
        super(key);
    }

    public SOAPProtocolViolationException(String key, String argument) {
        super(key, argument);
    }

    public SOAPProtocolViolationException(String key, Object[] arguments) {
        super(key, arguments);
    }

    public SOAPProtocolViolationException(String key, Localizable argument) {
        super(key, argument);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.soap";
    }
}
