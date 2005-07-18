/*
 * $Id: JAXRPCServletException.java,v 1.3 2005-07-18 16:52:27 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.servlet;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.server.*;

/**
 *
 * @author WS Development Team
 */
public class JAXRPCServletException extends JAXWSExceptionBase {

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
