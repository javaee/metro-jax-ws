/*
 * $Id: JAXRPCServletException.java,v 1.1 2005-05-23 23:01:39 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.servlet;

import com.sun.xml.ws.util.exception.JAXRPCExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.server.*;

/**
 *
 * @author JAX-RPC Development Team
 */
public class JAXRPCServletException extends JAXRPCExceptionBase {

    public JAXRPCServletException(String key) {
        super(key);
    }

    public JAXRPCServletException(String key, String arg) {
        super(key, arg);
    }

    public JAXRPCServletException(String key, Object[] args) {
        super(key, args);
    }

    public JAXRPCServletException(String key, Localizable arg) {
        super(key, arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.jaxrpcservlet";
    }
}
