/*
 * $Id: LocalClientTransportFactory.java,v 1.2 2005-08-31 04:26:21 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.local.client;

import java.io.OutputStream;
import com.sun.xml.ws.client.ClientTransportFactory;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.spi.runtime.WSConnection;

import java.util.Map;

/**
 * @author WS Development Team
 */
public class LocalClientTransportFactory implements ClientTransportFactory {
    private RuntimeEndpointInfo endpointInfo;
    private OutputStream logStream;
    
    //this class is used primarily for debugging purposes
    public LocalClientTransportFactory(RuntimeEndpointInfo endpointInfo) {
        this(endpointInfo, null);
    }

    public LocalClientTransportFactory(RuntimeEndpointInfo endpointInfo,
        OutputStream logStream) {
        this.endpointInfo = endpointInfo;
        this.logStream = logStream;
    }

    public WSConnection create() {
        return create(null);        
    }
    
    public WSConnection create(Map<String, Object> context) {
        return new LocalClientTransport(endpointInfo, logStream);
    }

}
