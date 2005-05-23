/*
 * $Id: HandlerContext.java,v 1.1 2005-05-23 22:37:25 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.handler;

import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPMessage;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;

/**
 * @author JAX-WS RI Development Team
 */
public class HandlerContext extends MessageContextImpl {

    private MessageInfo messageInfo;
    private InternalMessage internalMessage;
    private SOAPMessage soapMessage;
    private SOAPMessageContext soapContext;
    private LogicalMessageContext logicalContext;

    public HandlerContext(MessageInfo messageInfo,
            InternalMessage internalMessage,
            SOAPMessage soapMessage) {
        this.messageInfo = messageInfo;
        this.internalMessage = internalMessage;
        this.soapMessage = soapMessage;
    }

    public SOAPMessageContext createSOAPMessageContext() {
        if (soapContext == null) {
            soapContext = new SOAPMessageContextImpl(this);
        }
        return soapContext;
    }

    public LogicalMessageContext createLogicalMessageContext() {
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
    public SOAPMessageContext getSOAPMessageContext() {
        return soapContext;
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

    /*
     * Makes body as JAXBBean with the provided JAXBContext
     *
    public void toJAXBBean(JAXBContext newCtxt) {
        if (internalMessage == null) {
            return;
        }
        BodyBlock bodyBlock = internalMessage.getBody();
        if (bodyBlock != null) {
            LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
            LogicalEncoder encoder = eptf.getLogicalEncoder();
            Object value = bodyBlock.getValue();
            if (value instanceof Source) {
                Object bean = encoder.toJAXBBean((Source)value, newCtxt);
                bodyBlock.setJaxbBean(bean, newCtxt);
            } else if (value instanceof SOAPFaultInfo) {
                // TODO
            } else {
                JAXBContext oldCtxt = bodyBlock.getJAXBContext();
                Object bean = encoder.toJAXBBean(value, oldCtxt, newCtxt);
                bodyBlock.setJaxbBean(bean, newCtxt);
            }
        }
    }
     */

}
