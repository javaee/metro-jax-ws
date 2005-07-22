/*
 * $Id: HttpClientTransportFactory.java,v 1.5 2005-07-22 01:11:38 arungupta Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.client;

import java.io.OutputStream;
import com.sun.xml.ws.client.ClientTransportFactory;
import com.sun.xml.ws.spi.runtime.WSConnection;
import java.util.Map;

import javax.xml.ws.soap.SOAPBinding;

import static com.sun.xml.ws.client.BindingProviderProperties.BINDING_ID_PROPERTY;
import java.util.HashMap;

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
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(BINDING_ID_PROPERTY, SOAPBinding.SOAP11HTTP_BINDING);
        
        return new HttpClientTransport(_logStream, context);
    }

    /**
     * Binding Id, Endpoint address and other metadata is in the property bag
     */
    public WSConnection create(Map<String, Object> context) {
        return new HttpClientTransport(_logStream, context);
    }

    private OutputStream _logStream;
}
