/*
 * $Id: WSDLParseException.java,v 1.2 2005-05-25 20:16:36 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.util;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
  * @author JAX-RPC Development Team
  */
public class WSDLParseException extends JAXWSExceptionBase {

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
