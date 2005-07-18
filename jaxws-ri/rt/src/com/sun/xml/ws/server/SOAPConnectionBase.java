/*
 * $Id: SOAPConnectionBase.java,v 1.2 2005-07-18 16:52:20 kohlert Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.server;

import javax.xml.soap.SOAPMessage;

import com.sun.pept.ept.MessageInfo;

/**
 * @author WS Development Team
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
