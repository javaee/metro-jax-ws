/*
 * $Id: ClientTransportFactory.java,v 1.5 2005-08-31 04:26:19 kohlert Exp $
 */
/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.client;


import com.sun.xml.ws.spi.runtime.WSConnection;

import java.util.Map;

/**
 * @author WS Development Team
 */
public interface ClientTransportFactory
    extends com.sun.xml.ws.spi.runtime.ClientTransportFactory {
    public WSConnection create(Map<String, Object> context);
}
