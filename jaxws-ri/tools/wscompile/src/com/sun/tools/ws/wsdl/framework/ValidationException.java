/*
 * $Id: ValidationException.java,v 1.1 2005-05-24 14:04:15 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

import com.sun.xml.ws.util.exception.JAXRPCExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * An exception signalling that validation of an entity failed.
 *
 * @author JAX-RPC Development Team
 */
public class ValidationException extends JAXRPCExceptionBase {

    public ValidationException(String key) {
        super(key);
    }

    public ValidationException(String key, String arg) {
        super(key, arg);
    }

    public ValidationException(String key, Localizable localizable) {
        super(key, localizable);
    }

    public ValidationException(String key, Object[] args) {
        super(key, args);
    }

    public String getResourceBundleName() {
        return "com.sun.tools.ws.resources.wsdl";
    }
}
