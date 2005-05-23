/*
 * $Id: SOAPMsgFactoryCreateException.java,v 1.1 2005-05-23 22:30:18 bbissett Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.util.exception.JAXRPCExceptionBase;

/**
 * @author JAX-RPC Development Team
 */
public class SOAPMsgFactoryCreateException extends JAXRPCExceptionBase {

    public SOAPMsgFactoryCreateException(String key, Object[] args) {
        super(key, args);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.soap";
    }
}
