/*
 * $Id: XMLHandlerContext.java,v 1.2 2005-07-23 04:10:08 kohlert Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.handler;

import javax.xml.ws.handler.LogicalMessageContext;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.xml.XMLMessage;
import java.lang.reflect.Method;


/**
 * @author WS Development Team
 */
public class XMLHandlerContext extends MessageContextImpl {

    private MessageInfo messageInfo;
    private InternalMessage internalMessage;
    private XMLMessage xmlMessage;
    private LogicalMessageContext logicalContext;

    public XMLHandlerContext(MessageInfo messageInfo,
            InternalMessage internalMessage,
            XMLMessage xmlMessage) {
        this.messageInfo = messageInfo;
        this.internalMessage = internalMessage;
        this.xmlMessage = xmlMessage;
    }

    public LogicalMessageContext getLogicalMessageContext() {
        if (logicalContext == null) {
            //logicalContext = new LogicalMessageContextImpl(this);
        }
        return logicalContext;
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
    
    @Override
    public Method getMethod() {
        return messageInfo.getMethod();
    }

}
