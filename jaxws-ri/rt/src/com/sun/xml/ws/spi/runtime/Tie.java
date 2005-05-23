/**
 * $Id: Tie.java,v 1.1 2005-05-23 22:54:51 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

import com.sun.xml.ws.spi.runtime.*;



public interface Tie {
    
    /* 
     * Reads a Web Service request for JaxrpcEndpoint from JaxrpcConnection
     * and sends a response.
     */
    public void handle(JaxrpcConnection con, RuntimeEndpointInfo endpoint)
    throws Exception;
    
}
