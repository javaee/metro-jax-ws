/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.transport.http.client;

import java.io.OutputStream;
import com.sun.xml.ws.spi.runtime.WSConnection;
import java.util.Map;

import javax.xml.ws.soap.SOAPBinding;

import static com.sun.xml.ws.client.BindingProviderProperties.BINDING_ID_PROPERTY;
import com.sun.xml.ws.spi.runtime.ClientTransportFactory;
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

    /*
    public WSConnection create() {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(BINDING_ID_PROPERTY, SOAPBinding.SOAP11HTTP_BINDING);
        
        return new HttpClientTransport(_logStream, context);
    }
     */

    /**
     * Binding Id, Endpoint address and other metadata is in the property bag
     */
    public WSConnection create(Map<String, Object> context) {
        return new HttpClientTransport(_logStream, context);
    }

    private OutputStream _logStream;
}
