/*
 * $Id: LocalClientTransportFactory20.java,v 1.2 2005-07-18 16:52:29 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.local.client;

import java.io.OutputStream;

import com.sun.xml.ws.client.ClientTransport;
import com.sun.xml.ws.client.ClientTransportFactory;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.spi.runtime.Tie;

/**
 * @author WS Development Team
 */
public class LocalClientTransportFactory20 implements ClientTransportFactory {
    private RuntimeEndpointInfo endpointInfo;
    private OutputStream logStream;
    
    //this class is used primarily for debugging purposes
    public LocalClientTransportFactory20(RuntimeEndpointInfo endpointInfo) {
        this(endpointInfo, null);
    }

    public LocalClientTransportFactory20(RuntimeEndpointInfo endpointInfo,
        OutputStream logStream) {
        this.endpointInfo = endpointInfo;
        this.logStream = logStream;
    }

    public ClientTransport create() {
        return new LocalClientTransport20(endpointInfo, logStream);
    }

}
