/*
 * $Id: HttpClientTransportFactory.java,v 1.4 2005-07-19 18:10:05 arungupta Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.client;

import java.io.OutputStream;

import com.sun.xml.ws.client.ClientTransport;
import com.sun.xml.ws.client.ClientTransportFactory;
import com.sun.xml.ws.spi.runtime.WSConnection;
import java.util.Map;

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

    public WSConnection create() {
        return new HttpClientTransport(_logStream, SOAPBinding.SOAP11HTTP_BINDING);
    }

    // at present the HTTPClientTransport is binding aware so we need to pass the binding id to
    // chose appropriate binding
    public WSConnection create(String bindingId) {
        return new HttpClientTransport(_logStream, bindingId);
    }

    /**
     * Binding Id, Endpoint address and other metadata is in the property bag
     */
    public WSConnection create(Map<String, Object> context) {
        return new HttpClientTransport(_logStream, context);
    }

    private OutputStream _logStream;
}
