/*
 * $Id: SOAPConnectionBase.java,v 1.1 2005-05-23 22:50:25 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.server;

import javax.xml.soap.SOAPMessage;

import com.sun.pept.ept.MessageInfo;

/**
 * @author JAX-RPC RI Development Team
 */
public abstract class SOAPConnectionBase implements SOAPConnection {
    public void sendResponse(SOAPMessage soapMessage) {
    }

    public SOAPMessage getSOAPMessage() {
        return null;
    }

    public SOAPMessage getSOAPMessage(MessageInfo messageInfo) {
        return null;
    }

    public void sendResponseOneway() {
    }

    public void sendResponseError() {
    }

}
