/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.util;

import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.handler.HandlerContext;
import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.server.RuntimeContext;

import javax.xml.ws.handler.MessageContext;
import javax.xml.bind.Marshaller;
import java.util.Set;

/**
 * @author WS RI Development Team
 */
public class MessageInfoUtil {

    public static void setRuntimeContext(MessageInfo messageInfo,
                                         RuntimeContext runtimeContext) {
        messageInfo.setMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT, runtimeContext);
    }

    public static RuntimeContext getRuntimeContext(MessageInfo messageInfo) {
        return (RuntimeContext) messageInfo.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
    }

    public static MessageContext getMessageContext(MessageInfo messageInfo) {
        RuntimeContext rtCtxt = getRuntimeContext(messageInfo);
        HandlerContext hdCtxt = null;
        if (rtCtxt != null)
            hdCtxt = rtCtxt.getHandlerContext();
        else
            hdCtxt = (HandlerContext)
                messageInfo.getMetaData(BindingProviderProperties.JAXWS_HANDLER_CONTEXT_PROPERTY);
        return (hdCtxt == null) ? null : hdCtxt.getMessageContext();
    }

    public static HandlerChainCaller getHandlerChainCaller(
        MessageInfo messageInfo) {
        return (HandlerChainCaller) messageInfo.getMetaData(
            HandlerChainCaller.HANDLER_CHAIN_CALLER);
    }

    public static void setHandlerChainCaller(MessageInfo messageInfo,
                                             HandlerChainCaller caller) {
        messageInfo.setMetaData(HandlerChainCaller.HANDLER_CHAIN_CALLER,
            caller);
    }

    public static JAXWSAttachmentMarshaller getAttachmentMarshaller(MessageInfo messageInfo) {
        Object rtc = messageInfo.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if (rtc != null) {
            BridgeContext bc = ((RuntimeContext) rtc).getBridgeContext();
            if (bc != null) {
                return (JAXWSAttachmentMarshaller) bc.getAttachmentMarshaller();
            }
        } else {
             Marshaller m = (Marshaller)messageInfo.getMetaData(BindingProviderProperties.DISPATCH_MARSHALLER);
            if (m != null) {
                return (JAXWSAttachmentMarshaller) m.getAttachmentMarshaller();
            }
        }
        return null;
    }

    public static void setNotUnderstoodHeaders(MessageInfo messageInfo,
                                               Set<HeaderBlock> headers) {

        messageInfo.setMetaData(SOAPDecoder.NOT_UNDERSTOOD_HEADERS, headers);
    }

    public static Set<HeaderBlock> getNotUnderstoodHeaders(
        MessageInfo messageInfo) {

        return (Set<HeaderBlock>) messageInfo.getMetaData(
            SOAPDecoder.NOT_UNDERSTOOD_HEADERS);
    }
}
