/*
 * $Id: ClientTransportException.java,v 1.1 2005-05-23 22:26:34 bbissett Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.client;

import com.sun.xml.ws.util.exception.JAXRPCExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * @author JAX-RPC Development Team
 */
public class ClientTransportException extends JAXRPCExceptionBase {
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