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
 * @author jitu
 */
public class EndpointFactoryImpl extends EndpointFactory {
    
    public EndpointFactoryImpl() {
    }
    
    public Endpoint createEndpoint(URI bindingId, Object impl) {
        Endpoint endpoint = new EndpointImpl(bindingId, impl);
        return endpoint;
    }
    
    public Endpoint publish(String address, Object impl) {
        // TODO : binding
        Endpoint endpoint = new EndpointImpl(null, impl);
        endpoint.publish(address);
        return endpoint;
    }
    
}
