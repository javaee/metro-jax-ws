/**
 * $Id: Tie.java,v 1.4 2005-08-09 00:55:04 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

public interface Tie {
    
    /**
     * Reads a Web Service request for RuntimeEndpointInfo from WSConnection
     * and sends a response. Set <code>WebServiceContext</code> with a filled-in
     * </code>MessageContext</code> on <code>RuntimeEndpointInfo</code> before
     * calling his.
     */
    public void handle(WSConnection con, RuntimeEndpointInfo endpoint)
    throws Exception;
    
}
