/*
 * $Id: ClientTransportFactory.java,v 1.2 2005-07-18 16:52:04 kohlert Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.client;

/**
 * @author WS Development Team
 */
public interface ClientTransportFactory
    extends com.sun.xml.ws.spi.runtime.ClientTransportFactory {
    public ClientTransport create();
}
