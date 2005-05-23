/**
 * $Id: StubBase.java,v 1.1 2005-05-23 22:54:50 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;


/**
 * This class is implemented by
 * com.sun.xml.rpc.client.StubBase
 */
public interface StubBase {
    public void _setTransportFactory(ClientTransportFactory f);
}
