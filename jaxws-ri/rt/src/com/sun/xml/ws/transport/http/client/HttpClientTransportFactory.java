/*
 * $Id: HttpClientTransportFactory.java,v 1.1 2005-05-23 23:01:38 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.client;

import java.io.OutputStream;

import com.sun.xml.ws.client.ClientTransport;
import com.sun.xml.ws.client.ClientTransportFactory;

/**
 * @author JAX-RPC Development Team
 */
public class HttpClientTransportFactory implements ClientTransportFactory {

    public HttpClientTransportFactory() {
        this(null);
    }

    public HttpClientTransportFactory(OutputStream logStream) {
        _logStream = logStream;
    }

    public ClientTransport create() {
        return new HttpClientTransport(_logStream);
    }

    private OutputStream _logStream;
}
