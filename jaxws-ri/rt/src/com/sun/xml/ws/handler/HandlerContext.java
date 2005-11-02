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
import javax.xml.ws.handler.MessageContext;

import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.AttachmentBlock;
import com.sun.xml.ws.spi.runtime.InternalSoapEncoder;
import com.sun.xml.ws.spi.runtime.Invoker;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

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
    private MessageContext msgContext;

    private Method method;
    private Invoker invoker;
    private String algorithm;
    private String bindingId;

    public HandlerContext(MessageInfo messageInfo,
                          InternalMessage internalMessage) {
        this.messageInfo = messageInfo;
        this.internalMessage = internalMessage;
        this.msgContext = new MessageContextImpl();
        populateAttachmentMap();
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

    public InternalMessage getInternalMessage() {
        return internalMessage;
    }

    /**
    * @param internalMessage The internalMessage to set.
    */
    public void setInternalMessage(InternalMessage internalMessage) {
        this.internalMessage = internalMessage;
        populateAttachmentMap();
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
     * Returns the invocation method
     */
    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    /*
    * Returns InternalMessage's BodyBlock value. It can be null for empty body.
    */
    public Object getBody() {
        return (internalMessage == null) ? null : ((internalMessage.getBody() == null)?null:internalMessage.getBody().getValue());
    }

    /*
    * Returns InternalMessage's HeaderBlock values
    */
    public List getHeaders() {
        List<HeaderBlock> headerBlocks =
            (internalMessage == null) ? null : internalMessage.getHeaders();
        if (headerBlocks != null) {
             List headers = new ArrayList();
             for (HeaderBlock headerBlock : headerBlocks) {
                if (headerBlock.getValue() != null) {
                    headers.add(headerBlock.getValue());                             
                }
             }
             return headers;
        }
        return null;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingID) {
        bindingId = bindingID;
    }

    public void setCanonicalization(String algorithm) {
        this.algorithm = algorithm;
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    private void populateAttachmentMap(){
        //populate the attachment map
        if(internalMessage != null){
            for(AttachmentBlock ab: internalMessage.getAttachments().values()){
                MessageContextUtil.addMessageAttachment(msgContext, ab.getId(), ab.asDataHandler());
            }
        }
    }

}
