/*
 * $Id: WSServletException.java,v 1.2 2005-07-24 01:34:59 kohlert Exp $
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
public class WSServletException extends JAXWSExceptionBase {

    public WSServletException(String key) {
        super(key);
    }

    public WSServletException(String key, String arg) {
        super(key, arg);
    }

    public WSServletException(String key, Object[] args) {
        super(key, args);
    }

    public WSServletException(String key, Localizable arg) {
        super(key, arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.wsservlet";
    }
}
