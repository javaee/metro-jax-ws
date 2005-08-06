/*
 * $Id: XMLHandlerContext.java,v 1.4 2005-08-06 01:35:18 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.handler;

import javax.xml.ws.handler.LogicalMessageContext;
import com.sun.xml.ws.spi.runtime.MessageContext;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.xml.XMLMessage;
import java.lang.reflect.Method;


/**
 * @author WS Development Team
 */
public class XMLHandlerContext {

    private MessageInfo messageInfo;
    private InternalMessage internalMessage;
    private XMLMessage xmlMessage;
    private LogicalMessageContext logicalContext;
    private MessageContext msgContext;

    public XMLHandlerContext(MessageInfo messageInfo,
            InternalMessage internalMessage,
            XMLMessage xmlMessage) {
        this.messageInfo = messageInfo;
        this.internalMessage = internalMessage;
        this.xmlMessage = xmlMessage;
        this.msgContext = new MessageContextImpl();
    }

    public LogicalMessageContext getLogicalMessageContext() {
        if (logicalContext == null) {
            logicalContext = new XMLLogicalMessageContextImpl(this);
        }
        return logicalContext;
    }
    
    public MessageContext getMessageContext() {
        return msgContext;
    }
    
    public void setMessageContext(MessageContext msgContext) {
        this.msgContext = msgContext;
    }
    
    /**
     * @return Returns XMLMessage
     */
    public XMLMessage getXMLMessage() {
        return xmlMessage;
    }

    /**
     * @param xmlMessage The xmlMessage to set.
     */
    public void setXMLMessage(XMLMessage xmlMessage) {
        this.xmlMessage = xmlMessage;
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
