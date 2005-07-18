/*
 * $Id: HttpClientTransportFactory.java,v 1.3 2005-07-18 16:52:25 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.client;

import java.io.OutputStream;

import com.sun.xml.ws.client.ClientTransport;
import com.sun.xml.ws.client.ClientTransportFactory;

import javax.xml.ws.soap.SOAPBinding;

/**
 * @author WS Development Team
 */
public class HttpClientTransportFactory implements ClientTransportFactory {

    public HttpClientTransportFactory() {
        this(null);
    }

    public HttpClientTransportFactory(OutputStream logStream) {
        _logStream = logStream;
    }

    public ClientTransport create() {
        return new HttpClientTransport(_logStream, SOAPBinding.SOAP11HTTP_BINDING);
    }

    //at present the HTTPClientTransport is binding aware so we need to pass the binding id to
    // chose appropriate binding
    public ClientTransport create(String bindingId) {
        return new HttpClientTransport(_logStream, bindingId);
    }

    private OutputStream _logStream;
}
