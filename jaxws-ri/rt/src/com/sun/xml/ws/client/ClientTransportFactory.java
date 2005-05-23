/*
 * $Id: ClientTransportFactory.java,v 1.1 2005-05-23 22:26:35 bbissett Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.client;

/**
 * @author JAX-RPC Development Team
 */
public interface ClientTransportFactory
    extends com.sun.xml.ws.spi.runtime.ClientTransportFactory {
    public ClientTransport create();
}
