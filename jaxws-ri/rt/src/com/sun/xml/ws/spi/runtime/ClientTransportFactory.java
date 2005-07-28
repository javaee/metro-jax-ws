/**
 * $Id: ClientTransportFactory.java,v 1.2 2005-07-28 20:59:30 kwalsh Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

/**
 * This class is implemented by
 * com.sun.xml.rpc.client.ClientTransportFactory
 */
public interface ClientTransportFactory {
    public WSConnection create();
}
