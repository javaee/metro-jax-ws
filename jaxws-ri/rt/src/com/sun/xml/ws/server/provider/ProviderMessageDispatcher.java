/**
 * $Id: ProviderMessageDispatcher.java,v 1.5 2005-07-24 01:34:57 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server.provider;

import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.ws.binding.soap.BindingImpl;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.handler.HandlerContext;
import com.sun.xml.ws.handler.LogicalMessageImpl;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.protocol.soap.server.SOAPMessageDispatcher;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.SOAPUtil;
import java.util.Map;

public class ProviderMessageDispatcher extends SOAPMessageDispatcher {

    /*
     * Fill the parameters, method in MessageInfo for Provider interface.
     * invoke(Source, HandlerContext) to Object[]
     * invoke(SOAPMessage, HandlerContext) to Object[]
     */
    @Override
    protected void toMessageInfo(MessageInfo messageInfo, HandlerContext context) {
        Object[] data = new Object[2];
        data[1] = new ProviderMsgContextImpl(context);
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        Provider provider = (Provider)endpointInfo.getImplementor();
        Class providerClass = provider.getClass();
        boolean isSource = isSource(providerClass);
        boolean isSoapMessage = isSoapMessage(providerClass);
        if (!(isSource || isSoapMessage)) {
            throw new UnsupportedOperationException(
                    "Endpoint should implement Provider<Source> or Provider<SOAPMessage>");
        }

        if (getServiceMode(providerClass) == Service.Mode.PAYLOAD) {
            if (isSource) {
                data[0] = new LogicalMessageImpl(context).getPayload();
            } else {
                throw new UnsupportedOperationException(
                        "Illeagal combination Mode.PAYLOAD and Provider<SOAPMessage>");
            }
        } else {
            InternalMessage internalMessage = context.getInternalMessage();
            SOAPMessage soapMessage = context.getSOAPMessage();
            try {
                if (internalMessage != null) {
                    // SOAPMessage's body is replaced by InternalMessage's BodyBlock
                    LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
                    SOAPEncoder encoder = eptf.getSOAPEncoder();
                    soapMessage = encoder.toSOAPMessage(internalMessage, soapMessage);
                }
                if (isSource) {
                    // Get SOAPMessage's SOAPPart as Source
                    data[0]= soapMessage.getSOAPPart().getContent();
                } else {
                    data[0] = soapMessage;
                }
            } catch(Exception e) {
                messageInfo.setResponseType(MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
                messageInfo.setResponse(e);
            }
        }
        ProviderMsgContextImpl providerContext = new ProviderMsgContextImpl(context);
        data[1] = providerContext;
        messageInfo.setData(data);
        messageInfo.setMethod(ProviderPeptTie.invoke_Method);
    }

    /*
     * MessageInfo contains the endpoint invocation results. If the endpoint
     * returns a SOAPMessage, just set the object in HandlerContext. If the
     * endpoint returns a Source in Mode.MESSAGE, it is converted to SOAPMessage
     * and set in HandlerContext. If the endpoint returns a Source in
     * Mode.PAYLOAD, it is set in InternalMessage, and InternalMessage is set
     * in HandlerContext
     */
    @Override
    protected void setResponseInContext(MessageInfo messageInfo,
            HandlerContext context) {
        Object obj = messageInfo.getResponse();
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        Provider provider = (Provider)endpointInfo.getImplementor();
        Class providerClass = provider.getClass();
        if (messageInfo.getResponseType() == MessageInfo.NORMAL_RESPONSE &&
                getServiceMode(providerClass) == Service.Mode.MESSAGE) {
            SOAPMessage soapMessage = null;
            if (obj instanceof SOAPMessage) {
                soapMessage = (SOAPMessage)obj;
            } else {
                // put Source into SOAPPart of SOAPMessage
                try {
                    Source source = (Source)obj;
                    String bindingId = ((BindingImpl)endpointInfo.getBinding()).getBindingId();
                    soapMessage = SOAPUtil.createMessage(bindingId);
                    soapMessage.getSOAPPart().setContent(source);
                } catch(SOAPException e) {
                    throw new ServerRtException("soapencoder.err", new Object[]{e});
                }
            }
            context.setSOAPMessage(soapMessage);
            context.setInternalMessage(null);
        } else {
            // set Source or any Exception in InternalMessage's BodyBlock
            LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
            InternalEncoder ine = eptf.getInternalEncoder();
            InternalMessage internalMessage =
                (InternalMessage)ine.toInternalMessage(messageInfo);
            // set handler context
            context.setInternalMessage(internalMessage);
            context.setSOAPMessage(null);
        }
    }

    /*
     * In this case, Oneway is known only after invoking the endpoint. For other
     * endpoints, the HTTP response code is sent before invoking the endpoint.
     * This is taken care here after invoking the endpoint.
     */
    @Override
    protected void invokeEndpoint(MessageInfo messageInfo, HandlerContext hc) {
        super.invokeEndpoint(messageInfo, hc);
        if (isOneway(messageInfo)) {
            sendResponseOneway(messageInfo);
        }
    }

    /*
     * Is it PAYLOAD or MESSAGE ??
     */
    private static Service.Mode getServiceMode(Class c) {
        ServiceMode mode = (ServiceMode)c.getAnnotation(ServiceMode.class);
        if (mode == null) {
            return Service.Mode.PAYLOAD;
        }
        return mode.value();
    }

    /*
     * Is it Provider<Source> ?
     */
    private static boolean isSource(Class c) {
        try {
            c.getMethod("invoke",  Source.class, Map.class);
            return true;
        } catch(NoSuchMethodException ne) {
            // ignoring intentionally
        }
        return false;
    }

    /*
     * Is it Provider<SOAPMessage> ?
     */
    private static boolean isSoapMessage(Class c) {
        try {
            c.getMethod("invoke",  SOAPMessage.class, Map.class);
            return true;
        } catch(NoSuchMethodException ne) {
            // ignoring intentionally
        }
        return false;
    }

}
