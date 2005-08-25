/*
 * EndpointFactoryImpl.java
 *
 * Created on April 13, 2005, 10:34 AM
 */

package com.sun.xml.ws.server;

import javax.xml.ws.EndpointFactory;
import javax.xml.ws.Endpoint;
import java.net.URI;
import com.sun.xml.ws.transport.http.server.EndpointImpl;

/**
 *
 * @author WS Development Team
 */
public class EndpointFactoryImpl extends EndpointFactory {
    
    @Override
    public Endpoint createEndpoint(URI bindingId, Object impl) {
        Endpoint endpoint = new EndpointImpl(bindingId, impl);
        return endpoint;
    }
    
    @Override
    public Endpoint publish(String address, Object impl) {
        Endpoint endpoint = new EndpointImpl(impl);
        endpoint.publish(address);
        return endpoint;
    }
    
}
