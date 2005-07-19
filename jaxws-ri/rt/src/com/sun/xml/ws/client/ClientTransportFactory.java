/*
 * $Id: ClientTransportFactory.java,v 1.3 2005-07-19 18:10:02 arungupta Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.client;

import com.sun.xml.ws.spi.runtime.WSConnection;

/**
 * @author WS Development Team
 */
public interface ClientTransportFactory
    extends com.sun.xml.ws.spi.runtime.ClientTransportFactory {
    public WSConnection create();
}
