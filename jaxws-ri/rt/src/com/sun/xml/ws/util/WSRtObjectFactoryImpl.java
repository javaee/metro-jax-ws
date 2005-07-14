/*
 * $Id: WSRtObjectFactoryImpl.java,v 1.1 2005-07-14 23:39:50 jitu Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.util;

import com.sun.xml.ws.binding.soap.SOAPBindingImpl;
import java.io.OutputStream;

import com.sun.xml.ws.client.ClientTransportFactory;
import com.sun.xml.ws.transport.http.servlet.JAXRPCServletDelegate;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.Tie;

import com.sun.xml.ws.spi.runtime.ClientTransportFactoryTypes;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.transport.http.servlet.ServletConnectionImpl;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

/**
 * Singleton factory class to instantiate concrete objects.
 *
 * @author JAX-WS Development Team
 */
public class WSRtObjectFactoryImpl
    extends com.sun.xml.ws.spi.runtime.WSRtObjectFactory {
    
    public com.sun.xml.ws.spi.runtime.ClientTransportFactory
        createClientTransportFactory(int type, OutputStream outputStream) {
        
        ClientTransportFactory clientFactory = null;
        switch (type) {
            case ClientTransportFactoryTypes.HTTP :
                //return new HttpClientTransportFactory(outputStream);
                
            case ClientTransportFactoryTypes.LOCAL:         
                //return new LocalClientTransportFactory(null, outputStream);
        }
        return null;
    }
    
    public com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo createRuntimeEndpointInfo() {
        return new RuntimeEndpointInfo();
    }
    
    /**
     * Creates a connection for servlet transport
     */
    public WSConnection createWSConnection(HttpServletRequest req,
            HttpServletResponse res) {
        return new ServletConnectionImpl(req, res);
    }
    
    public com.sun.xml.ws.spi.runtime.SOAPMessageContext createSOAPMessageContext() {
        return null;
    }
    
    public com.sun.xml.ws.spi.runtime.ServletDelegate createServletDelegate() {
        return new JAXRPCServletDelegate();
    }
    
    public com.sun.xml.ws.spi.runtime.Tie createTie() {
        return new Tie();
    }
    
    public com.sun.xml.ws.spi.runtime.Binding createBinding(String bindingId) {
        if (bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING) ||
                bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
            return new SOAPBindingImpl(bindingId);
        } else if (bindingId.equals(HTTPBinding.HTTP_BINDING)) {
            return null;        // TODO
        }
        return new SOAPBindingImpl(SOAPBinding.SOAP11HTTP_BINDING);
    }
}
