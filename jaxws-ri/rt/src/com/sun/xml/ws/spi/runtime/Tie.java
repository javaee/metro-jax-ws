/**
 * $Id: Tie.java,v 1.2 2005-07-13 21:21:16 jitu Exp $
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
    public void handle(WSConnection con, RuntimeEndpointInfo endpoint)
    throws Exception;
    
}
