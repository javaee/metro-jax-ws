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

package com.sun.xml.ws.transport.local.client;

import java.io.OutputStream;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.spi.runtime.ClientTransportFactory;
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
