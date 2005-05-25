/*
 * $Id: ClientTransportException.java,v 1.2 2005-05-25 20:16:24 kohlert Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.client;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * @author JAX-RPC Development Team
 */
public class ClientTransportException extends JAXWSExceptionBase {
    public ClientTransportException(String key) {
        super(key);
    }

    public ClientTransportException(String key, String argument) {
        super(key, argument);
    }

    public ClientTransportException(String key, Object[] arguments) {
        super(key, arguments);
    }

    public ClientTransportException(String key, Localizable argument) {
        super(key, argument);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.client";
    }
}