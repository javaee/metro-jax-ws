/*
 * $Id: HandlerContext.java,v 1.6 2005-08-05 21:53:36 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.handler;

import javax.xml.ws.handler.LogicalMessageContext;
import com.sun.xml.ws.spi.runtime.SOAPMessageContext;
import com.sun.xml.ws.spi.runtime.MessageContext;
import javax.xml.soap.SOAPMessage;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import java.lang.reflect.Method;

/**
 * @author WS Development Team
 */
public class HandlerContext {

    private MessageInfo messageInfo;
    private InternalMessage internalMessage;
    private SOAPMessage soapMessage;
    private SOAPMessageContext soapContext;
    private LogicalMessageContext logicalContext;
    private MessageContext msgContext;

    public HandlerContext(MessageInfo messageInfo,
            InternalMessage internalMessage,
            SOAPMessage soapMessage) {
        this.messageInfo = messageInfo;
        this.internalMessage = internalMessage;
        this.soapMessage = soapMessage;
        this.msgContext = new MessageContextImpl();
    }

    public SOAPMessageContext getSOAPMessageContext() {
        if (soapContext == null) {
            soapContext = new SOAPMessageContextImpl(this);
        }
        return soapContext;
    }

    public LogicalMessageContext getLogicalMessageContext() {
        if (logicalContext == null) {
            logicalContext = new LogicalMessageContextImpl(this);
        }
        return logicalContext;
    }

    /**
     * @return Returns the soapMessage.
     */
    public SOAPMessage getSOAPMessage() {
        return soapMessage;
    }
    
    /**
     * @return Returns the soapMessage.
     */
    public MessageContext getMessageContext() {
        return msgContext;
    }
    
    public void setMessageContext(MessageContext msgContext) {
        this.msgContext = msgContext;
    }


    /**
     * @param soapMessage The soapMessage to set.
     */
    public void setSOAPMessage(SOAPMessage soapMessage) {
        this.soapMessage = soapMessage;
    }

    public InternalMessage getInternalMessage() {
        return internalMessage;
    }

    /**
    * @param internalMessage The internalMessage to set.
    */
    public void setInternalMessage(InternalMessage internalMessage) {
        this.internalMessage = internalMessage;
    }

    public MessageInfo getMessageInfo() {
        return messageInfo;
    }

    /**
    * @param messageInfo The messageInfo to set.
    */
    public void setMessageInfo(MessageInfo messageInfo) {
        this.messageInfo = messageInfo;
    }

}
