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
package com.sun.xml.ws.spi.runtime;

/**
 * Entry point to JAXWS server side runtime
 */
public interface Tie {
    
    /**
     * Reads a Web Service request for RuntimeEndpointInfo from WSConnection
     * and sends a response. Set <code>WebServiceContext</code> with a filled-in
     * </code>MessageContext</code> on <code>RuntimeEndpointInfo</code> before
     * calling this.
     */
    public void handle(WSConnection con, RuntimeEndpointInfo endpoint)
    throws Exception;
    
}
