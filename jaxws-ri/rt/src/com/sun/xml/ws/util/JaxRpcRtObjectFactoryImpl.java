/*
 * $Id: JaxRpcRtObjectFactoryImpl.java,v 1.1 2005-05-23 23:06:25 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.util;

import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import com.sun.xml.ws.client.ClientTransportFactory;
import com.sun.xml.ws.transport.http.client.HttpClientTransportFactory;
import com.sun.xml.ws.transport.http.servlet.JAXRPCServletDelegate;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.Tie;

import com.sun.xml.ws.spi.runtime.ClientTransportFactoryTypes;

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
