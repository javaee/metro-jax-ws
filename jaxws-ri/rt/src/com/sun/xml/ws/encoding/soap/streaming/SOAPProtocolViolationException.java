/*
 * $Id: SOAPProtocolViolationException.java,v 1.3 2005-07-18 16:52:18 kohlert Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap.streaming;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * @author WS Development Team
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
