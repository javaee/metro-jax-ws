/*
 * $Id: SOAPProtocolViolationException.java,v 1.1 2005-05-23 22:30:18 bbissett Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap.streaming;

import com.sun.xml.ws.util.exception.JAXRPCExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * @author JAX-RPC Development Team
 */
public class SOAPProtocolViolationException extends JAXRPCExceptionBase {
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
