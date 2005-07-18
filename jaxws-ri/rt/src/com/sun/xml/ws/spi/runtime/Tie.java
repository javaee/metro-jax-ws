/**
 * $Id: Tie.java,v 1.3 2005-07-18 18:21:50 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

import com.sun.xml.ws.spi.runtime.*;



public interface Tie {
    
    /* 
     * Reads a Web Service request for RuntimeEndpointInfo from WSConnection
     * and sends a response.
     */
    public void handle(WSConnection con, RuntimeEndpointInfo endpoint)
    throws Exception;
    
}
