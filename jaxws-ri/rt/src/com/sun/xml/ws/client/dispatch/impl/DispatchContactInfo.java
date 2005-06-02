/*
 * $Id: DispatchContactInfo.java,v 1.2 2005-06-02 17:53:09 vivekp Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.client.dispatch.impl;

import com.sun.pept.encoding.Decoder;
import com.sun.pept.encoding.Encoder;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.pept.transport.Connection;
import com.sun.xml.ws.client.ContactInfoBase;

/**
 * @author JAX-RPC RI Development Team
 */
public class DispatchContactInfo extends ContactInfoBase {

    public DispatchContactInfo(Connection connection,
                               MessageDispatcher messageDispatcher,
                               Encoder encoder,
                               Decoder decoder) {
        super(connection, messageDispatcher, encoder, decoder,null);

    }

    public DispatchContactInfo() {
        super();
    }
}
