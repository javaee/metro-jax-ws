/*
 * $Id: ClientTransport.java,v 1.2 2005-07-18 16:52:04 kohlert Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.client;

import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;

/**
 * @author WS Development Team
 */
public interface ClientTransport {
    //todo add invoke async methods
    public void invoke(String endpoint, SOAPMessageContext context);

    public void invokeOneWay(String endpoint, SOAPMessageContext context);
}
