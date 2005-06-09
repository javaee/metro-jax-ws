/*
 * $Id: JaxRpcRtObjectFactoryImpl.java,v 1.2 2005-06-09 19:11:14 jitu Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.util;

import java.io.OutputStream;

import com.sun.xml.ws.client.ClientTransportFactory;
import com.sun.xml.ws.transport.http.servlet.JAXRPCServletDelegate;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.Tie;

import com.sun.xml.ws.spi.runtime.ClientTransportFactoryTypes;
import com.sun.xml.ws.spi.runtime.JaxrpcConnection;
import com.sun.xml.ws.transport.http.servlet.ServletConnectionImpl;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Singleton factory class to instantiate concrete objects.
 *
 * @author JAX-RPC Development Team
 */
public class JaxRpcRtObjectFactoryImpl
    extends com.sun.xml.ws.spi.runtime.JaxRpcRtObjectFactory {
    
    public JaxRpcRtObjectFactoryImpl() {
    }
    
    
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
    public JaxrpcConnection createJaxrpcConnection(HttpServletRequest req,
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
}
