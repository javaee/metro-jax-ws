/*
 * $Id: SOAPMsgCreateException.java,v 1.2 2005-05-25 20:16:26 kohlert Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;

/**
 * @author JAX-RPC Development Team
 */
public class SOAPMsgCreateException extends JAXWSExceptionBase {

    public SOAPMsgCreateException(String key, Object[] args) {
        super(key, args);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.soap";
    }
}
