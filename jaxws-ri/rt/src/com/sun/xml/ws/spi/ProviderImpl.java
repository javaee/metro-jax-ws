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
package com.sun.xml.ws.spi;


import com.sun.xml.ws.client.WSServiceDelegate;
import com.sun.xml.ws.transport.http.server.EndpointImpl;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.spi.Provider;
import javax.xml.ws.spi.ServiceDelegate;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author WS Development Team
 */
public class ProviderImpl extends Provider {

    @Override
    public Endpoint createEndpoint(String bindingId, Object implementor) {
        return new EndpointImpl(bindingId, implementor);
    }

    @Override
    public ServiceDelegate createServiceDelegate(
         java.net.URL wsdlDocumentLocation,
         QName serviceName, Class serviceClass) {



         return new WSServiceDelegate(wsdlDocumentLocation, serviceName, serviceClass);
    }

    @Override
    public Endpoint createAndPublishEndpoint(String address,
                                             Object implementor) {
        Endpoint endpoint = new EndpointImpl(null, implementor);
        endpoint.publish(address);
        return endpoint;
    }

}
