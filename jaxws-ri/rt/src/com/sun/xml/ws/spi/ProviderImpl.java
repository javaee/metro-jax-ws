/**
 * $Id: ProviderImpl.java,v 1.2 2005-09-01 02:46:05 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi;

import com.sun.xml.ws.transport.http.server.EndpointImpl;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.spi.Provider;
import javax.xml.ws.spi.ServiceDelegate;

/**
 *
 * @author WS Development Team
 */
public class ProviderImpl extends Provider {
    
    @Override
    public ServiceDelegate createServiceDelegate(URL wsdlDocumentLocation,
        QName serviceName, Class serviceClass) {
        return null;
    }
    
    @Override
    public Endpoint createEndpoint(String bindingId, Object implementor) {
        return new EndpointImpl(bindingId, implementor);
    }
    
    /*
    @Override
    public Endpoint publish(String address, Object impl) {
        // TODO : binding
        Endpoint endpoint = new EndpointImpl(null, impl);
        endpoint.publish(address);
        return endpoint;
    }
     */
    
}
