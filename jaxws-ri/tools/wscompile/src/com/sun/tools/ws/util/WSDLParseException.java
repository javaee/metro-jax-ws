/*
 * $Id: WSDLParseException.java,v 1.1 2005-05-24 13:49:45 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.util;

import com.sun.xml.ws.util.exception.JAXRPCExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
  * @author JAX-RPC Development Team
  */
public class WSDLParseException extends JAXRPCExceptionBase {

    public WSDLParseException(String key) {
        super(key);
    }

    public WSDLParseException(String key, String arg) {
        super(key, arg);
    }

    public WSDLParseException(String key, Localizable localizable) {
        super(key, localizable);
    }

    public WSDLParseException(String key, Object[] args) {
        super(key, args);
    }

    public String getResourceBundleName() {
        return "com.sun.tools.ws.resources.util";
    }
}
