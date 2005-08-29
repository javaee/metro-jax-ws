/**
 * $Id: ClientTransportFactory.java,v 1.3 2005-08-29 19:37:32 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

/**
 * This class is implemented by
 * com.sun.xml.ws.client.ClientTransportFactory
 */
public interface ClientTransportFactory {
    public WSConnection create();
}
