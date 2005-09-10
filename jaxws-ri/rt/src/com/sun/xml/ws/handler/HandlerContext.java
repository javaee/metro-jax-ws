/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
 * The HandlerContext is used in the client and server runtime
 * in {@link com.sun.xml.ws.protocol.soap.client.SOAPMessageDispatcher} and 
 * {@link com.sun.xml.ws.protocol.soap.server.SOAPMessageDispatcher} to hold
 * information about the current message.
 *
 * <p>It stores a {@link com.sun.pept.ept.MessageInfo} and
 * {@link com.sun.xml.ws.encoding.soap.internal.InternalMessage}
 * which are used by the rest of the runtime, and provides a bridge
 * between these and the soap and logical message contexts that
 * are used by the handlers.
 *
 * @see LogicalMessageContextImpl
 * @see MessageContextImpl
 * @see SOAPMessageContextImpl
 *
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
