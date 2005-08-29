/**
 * $Id: StubBase.java,v 1.2 2005-08-29 19:37:32 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;


/**
 * This class is implemented by
 * com.sun.xml.ws.client.StubBase
 */
public interface StubBase {
    public void _setTransportFactory(ClientTransportFactory f);
}
