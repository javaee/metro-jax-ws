/**
 * $Id: SOAPConnection.java,v 1.1 2005-05-23 22:50:25 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server;

import javax.xml.soap.SOAPMessage;

import com.sun.pept.ept.MessageInfo;

/**
 *
 */
public interface SOAPConnection {
    public void sendResponse(SOAPMessage soapMessage);
    public void sendResponseOneway();
    public void sendResponseError();
    public SOAPMessage getSOAPMessage();
    public SOAPMessage getSOAPMessage(MessageInfo messageInfo);
}
