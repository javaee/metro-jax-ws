/**
 * $Id: SOAPMessageDispatcher.java,v 1.6 2005-06-23 02:09:55 jitu Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.client;

import com.sun.pept.ept.EPTFactory;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.client.dispatch.ResponseImpl;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.internal.MessageInfoBase;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.handler.HandlerChainCaller.Direction;
import com.sun.xml.ws.handler.HandlerChainCaller.RequestOrResponse;
import com.sun.xml.ws.handler.HandlerContext;
import com.sun.xml.ws.server.SOAPConnection;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.soap.*;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class SOAPMessageDispatcher implements MessageDispatcher {

    //RuntimeContext rtContext;

    protected static final int MAX_THREAD_POOL_SIZE = 2;

    protected static final long AWAIT_TERMINATION_TIME = 10L;

    protected ExecutorService executorService = null;

    private final static String MUST_UNDERSTAND_FAULT_MESSAGE_STRING = "SOAP must understand error";

    public SOAPMessageDispatcher() {
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.pept.protocol.MessageDispatcher#send(com.sun.pept.ept.MessageInfo)
     */
    public void send(MessageInfo messageInfo) {
        if (isAsync(messageInfo)) {
            doSendAsync(messageInfo);
        } else {
            doSend(messageInfo);
        }
    }

    protected SOAPMessage doSend(MessageInfo messageInfo) {
        //change from LogicalEPTFactory to ContactInfoBase - should be changed back when we have things working
        EPTFactory contactInfo = messageInfo.getEPTFactory();
        SOAPXMLEncoder encoder = (SOAPXMLEncoder) contactInfo.getEncoder(messageInfo);
        SOAPMessage sm = null;
        boolean handlerResult = true;
        boolean isRequestResponse = (messageInfo.getMEP() == MessageStruct.REQUEST_RESPONSE_MEP);

        if (messageInfo.getMetaData(DispatchContext.DISPATCH_MESSAGE_MODE) == Service.Mode.MESSAGE) {
            sm = (SOAPMessage) messageInfo.getData()[0];
        }
        try {
            InternalMessage im = encoder.toInternalMessage(messageInfo);

            HandlerChainCaller caller = getHandlerChainCaller(messageInfo);
            if (caller.hasHandlers()) {
                HandlerContext handlerContext = new HandlerContext(messageInfo, im, sm);
                updateMessageContext(messageInfo, handlerContext);
                handlerResult = callHandlersOnRequest(handlerContext);
                sm = handlerContext.getSOAPMessage();
                if (sm == null) {
                    sm = encoder.toSOAPMessage(handlerContext.getInternalMessage(), messageInfo);
                }

                // the only case where no message is sent
                if (isRequestResponse && !handlerResult) {
                    SOAPXMLDecoder decoder = (SOAPXMLDecoder) contactInfo.getDecoder(messageInfo);
                    im = decoder.toInternalMessage(sm, messageInfo);
                    decoder.toMessageInfo(im, messageInfo);
                    return sm;
                }
            } else {
                if (sm == null)
                    sm = encoder.toSOAPMessage(im, messageInfo);
            }

            if (!isAsync(messageInfo)) {
                SOAPConnection connection = (SOAPConnection) messageInfo.getConnection();
                connection.sendResponse(sm);
            } // else return sm;

            // if handlerResult is false, the receive has already happened
            if (isRequestResponse && handlerResult) {
                receive(messageInfo);
            }
        } catch (Throwable e) {
            setResponseType(e, messageInfo);
            messageInfo.setResponse(e);
        }
        return sm;
    }

    protected void setResponseType(Throwable e, MessageInfo messageInfo) {
        if (e instanceof RuntimeException) {
            messageInfo.setResponseType(MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
            if (e instanceof ClientTransportException) {
                Throwable temp = e;
                e = new RemoteException(temp.getMessage(), temp);
            }
        } else {
            messageInfo.setResponseType(MessageStruct.CHECKED_EXCEPTION_RESPONSE);
        }
        messageInfo.setResponse(e);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.pept.protocol.MessageDispatcher#receive(com.sun.pept.ept.MessageInfo)
     *
     * todo: exception handling with possible saaj error below
     */
    public void receive(MessageInfo messageInfo) {
        // change from LogicalEPTFactory to ContactInfoBase - should be changed back when we have things working
        EPTFactory contactInfo = messageInfo.getEPTFactory();
        //LogicalEPTFactory contactInfo = (LogicalEPTFactory) messageInfo.getEPTFactory();

        SOAPXMLDecoder decoder = (SOAPXMLDecoder) contactInfo.getDecoder(messageInfo);

        SOAPMessage sm = decoder.toSOAPMessage(messageInfo);
        // HandlerContext handlerContext = new HandlerContext(messageInfo,
        // null, sm);
        HandlerContext handlerContext = getInboundHandlerContext(messageInfo, sm);

        try {
            decoder.doMustUnderstandProcessing(sm, messageInfo, handlerContext, false);
            //checkMustUnderstandHeaders(handlerContext);
        } catch (SOAPException se) { // unusual saaj error
            throw new RuntimeException(se);
        } catch (IOException ie) { // unusual saaj error
            throw new RuntimeException(ie);
        } catch (SOAPFaultException sfe) {
            closeAllHandlers(handlerContext);
            throw sfe;
        }


        // TODO Check for null context in Dispatch and then uncomment
        // TODO the if/else for inbound handlers infrastructure
//        HandlerChainCaller caller = getHandlerChainCaller(messageInfo);
//        if (caller.hasHandlers()) {
//            callHandlersOnResponse(handlerContext);
//            updateResponseContext(messageInfo, handlerContext);
//            // handlerContext.toJAXBBean(util.getJAXBContext());
//            InternalMessage im = handlerContext.getInternalMessage();
//            if (im == null) {
//                im = decoder.toInternalMessage(sm, messageInfo);
//            } else {
//                im = decoder.toInternalMessage(sm, im, messageInfo);
//            }
//            decoder.toMessageInfo(im, messageInfo);
//        } else {
//            decoder.receiveAndDecode(messageInfo);
//            postReceiveAndDecodeHook(messageInfo);
//        }


        HandlerChainCaller caller = getHandlerChainCaller(messageInfo);
        if (caller.hasHandlers()) {
            callHandlersOnResponse(handlerContext);
            updateResponseContext(messageInfo, handlerContext);
        }

        InternalMessage im = handlerContext.getInternalMessage();
        if (im == null) {
            im = decoder.toInternalMessage(sm, messageInfo);
        } else {
            im = decoder.toInternalMessage(sm, im, messageInfo);
        }
        decoder.toMessageInfo(im, messageInfo);
        if (messageInfo.getMetaData(DispatchContext.DISPATCH_MESSAGE_MODE) ==
                Service.Mode.MESSAGE) {
            sm = decoder.toSOAPMessage(messageInfo);
            messageInfo.setResponse(sm);
            postReceiveAndDecodeHook(messageInfo);
        }
    }

    private HandlerContext getInboundHandlerContext(MessageInfo messageInfo, SOAPMessage sm) {
        HandlerContext handlerContext = (HandlerContext) messageInfo
            .getMetaData(BindingProviderProperties.JAXWS_HANDLER_CONTEXT_PROPERTY);
        if (handlerContext != null) {
            handlerContext.setSOAPMessage(sm);
            handlerContext.setInternalMessage(null);
        } else
            handlerContext = new HandlerContext(messageInfo, null, sm);
        return handlerContext;
    }

    protected void doSendAsync(final MessageInfo messageInfo) {
        try { // should have already been caught
            preSendHook(messageInfo);
            SOAPMessage sm = doSend(messageInfo);
            postSendHook(messageInfo);

            //Response r = sendAsyncReceive(messageInfo, sm);

            //pass a copy of MessageInfo to the future task,so that no conflicts 
            //due to threading happens 
            Response r = sendAsyncReceive(MessageInfoBase.copy(messageInfo), sm);
            if (executorService == null) {
                executorService = Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE);
                /*
                 * try {
                 * executorService.awaitTermination(AWAIT_TERMINATION_TIME,
                 * TimeUnit.MILLISECONDS); } catch (InterruptedException e) {
                 * throw new JAXRPCException(e); }
                 */
            }

            executorService.execute((FutureTask) r);
            executorService.shutdown();
            executorService = null;
            messageInfo.setResponse(r);
        } catch (Throwable e) {
            System.out.println("Exception is " + e.getClass().getName());
            messageInfo.setResponse(e);

        }
    }

    protected Response<Object> sendAsyncReceive(final MessageInfo messageInfo, final SOAPMessage sm) {

        final AsyncHandler handler = (AsyncHandler) messageInfo
            .getMetaData(BindingProviderProperties.JAXWS_CLIENT_ASYNC_HANDLER);
        final boolean callback = (messageInfo.getMEP() == MessageStruct.ASYNC_CALLBACK_MEP) ? true
            : false;
        if (callback && (handler == null))
            throw new WebServiceException("Asynchronous callback invocation, but no handler - AsyncHandler required");

        final Response r = new ResponseImpl<Object>(new Callable<Object>() {

            public Object call() throws Exception {
                // get connection and do http.invoke()
                try {
                    final SOAPConnection connection = (SOAPConnection) messageInfo.getConnection();
                    connection.sendResponse(sm);
                } catch (Throwable t) {
                    messageInfo.setResponse(t);
                    messageInfo.setResponseType(MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
                }
                // receive response
                preReceiveHook(messageInfo);
                try {
                    receive(messageInfo);
                } catch (Exception ex) {
                    messageInfo.setResponse(ex);
                }
                postReceiveHook(messageInfo);

                if (callback) {
                    ResponseImpl res = new ResponseImpl(new Callable<Object>() {
                        public Object call() {
                            return null;
                        }
                    });
                    setResponse(messageInfo, res);
                    handler.handleResponse(res);
                    return null;
                }

                // for poll case
                if (messageInfo.getResponse() instanceof Exception)
                    throw (Exception) messageInfo.getResponse();
                return messageInfo.getResponse();
            }
        });
        return r;
    }

    protected boolean callHandlersOnRequest(HandlerContext handlerContext) {
        HandlerChainCaller caller = getHandlerChainCaller(handlerContext.getMessageInfo());
        boolean responseExpected = (handlerContext.getMessageInfo().getMEP() != MessageStruct.ONE_WAY_MEP);
        return caller.callHandlers(Direction.OUTBOUND, RequestOrResponse.REQUEST, handlerContext,
            responseExpected);
    }

    protected boolean callHandlersOnResponse(HandlerContext handlerContext) {
        HandlerChainCaller caller = getHandlerChainCaller(handlerContext.getMessageInfo());
        return caller.callHandlers(Direction.INBOUND, RequestOrResponse.RESPONSE, handlerContext,
            false);
    }

    protected HandlerChainCaller getHandlerChainCaller(MessageInfo messageInfo) {
        ContextMap context = (ContextMap) ((MessageInfoBase) messageInfo)
            .getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
        BindingProvider provider = (BindingProvider) context
            .get(BindingProviderProperties.JAXWS_CLIENT_HANDLE_PROPERTY);
        BindingImpl binding = (BindingImpl) provider.getBinding();
        return binding.getHandlerChainCaller();
    }

    protected void updateMessageContext(MessageInfo messageInfo, HandlerContext context) {
        SOAPMessageContext messageContext = context.createSOAPMessageContext();
        messageInfo.setMetaData(BindingProviderProperties.JAXWS_HANDLER_CONTEXT_PROPERTY, context);
        RequestContext ctxt = (RequestContext) messageInfo
            .getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
        Iterator i = ctxt.copy().getPropertyNames();
        while (i.hasNext()) {
            String name = (String) i.next();
            Object value = ctxt.get(name);
            messageContext.put(name, value);
        }
    }

    protected void updateResponseContext(MessageInfo messageInfo, HandlerContext context) {

        ResponseContext responseContext = new ResponseContext(null);
        javax.xml.ws.handler.soap.SOAPMessageContext messageContext = (javax.xml.ws.handler.soap.SOAPMessageContext) context
            .getSOAPMessageContext();
        Iterator i = messageContext.keySet().iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            MessageContext.Scope scope = messageContext.getScope(name);
            if (MessageContext.Scope.APPLICATION == scope) {
                Object value = messageContext.get(name);
                responseContext.put(name, value);
            }
        }

        messageInfo.setMetaData(BindingProviderProperties.JAXWS_RESPONSE_CONTEXT_PROPERTY,
            responseContext.copy());
    }

    protected boolean isAsync(MessageInfo messageInfo) {
        if ((messageInfo.getMEP() == MessageStruct.ASYNC_POLL_MEP)
            || (messageInfo.getMEP() == MessageStruct.ASYNC_CALLBACK_MEP)) {
            return true;
        }
        return false;
    }

    private void setResponse(MessageInfo messageInfo, ResponseImpl res) {
        Object result = messageInfo.getResponse();
        ResponseContext context = (ResponseContext) messageInfo
            .getMetaData(BindingProviderProperties.JAXWS_RESPONSE_CONTEXT_PROPERTY);
        if (context != null)
            res.setResponseContext(context);
        // need to set responseContext on Response
        // asyncHandler does the exception processing
        if (result instanceof Exception)
            res.setException((Exception) result);
        else
            res.set(result);
    }

    private void preSendHook(MessageInfo messageInfo) {
    }

    private void preReceiveHook(MessageInfo messageInfo) {
    }

    private void postSendHook(MessageInfo messageInfo) {
        if (messageInfo.getResponseType() != MessageStruct.NORMAL_RESPONSE) {
            postReceiveHook(messageInfo);
            throw (WebServiceException) messageInfo.getResponse();
        }
    }

    private void postReceiveAndDecodeHook(MessageInfo messageInfo) {
        DispatchContext dispatchContext = (DispatchContext) messageInfo
            .getMetaData(BindingProviderProperties.DISPATCH_CONTEXT);
        if ((messageInfo.getMetaData(DispatchContext.DISPATCH_MESSAGE_MODE) == Service.Mode.MESSAGE)
            && (dispatchContext.getProperty(DispatchContext.DISPATCH_MESSAGE) == DispatchContext.MessageType.SOURCE_MESSAGE)) {
            Object response = messageInfo.getResponse();
            if (response instanceof SOAPMessage) {
                SOAPPart part = ((SOAPMessage) response).getSOAPPart();
                try {
                    messageInfo.setResponse(part.getContent());
                } catch (SOAPException e) {
                    throw new WebServiceException(e);
                }
            }
        }
    }

    private void postReceiveHook(MessageInfo messageInfo) {
        // postReceiveHook exaimines the result for an exception
        // or SOAPFaultInfo - it will set appropriate
        // asynchronous exceptions
        Object response = messageInfo.getResponse();
        switch (messageInfo.getResponseType()) {
            case MessageStruct.NORMAL_RESPONSE:
                // not sure where this belongs yet - but for now-
                return;
            case MessageStruct.CHECKED_EXCEPTION_RESPONSE:
                if (response instanceof SOAPFaultInfo) {
                    SOAPFaultInfo soapFaultInfo = (SOAPFaultInfo) response;
                    JAXBException jbe = null;
                    if (soapFaultInfo.getString().contains("javax.xml.bind")) {
                        jbe = new JAXBException(soapFaultInfo.getString());
                        // do I need to put this in a jaxws exception
                    }
                    SOAPFaultException sfe = new SOAPFaultException(soapFaultInfo.getCode(),
                        soapFaultInfo.getString(), soapFaultInfo.getActor(),
                        (Detail) soapFaultInfo.getDetail());
                    if (jbe != null)
                        sfe.initCause(jbe);
                    messageInfo.setResponse(new RemoteException(sfe.getFaultString(), sfe));
                }
                return;
            case MessageStruct.UNCHECKED_EXCEPTION_RESPONSE:
                if (response instanceof SOAPFaultException) {
                    messageInfo.setResponse(new RemoteException("Exception from service "
                        + ((SOAPFaultException) response).getFaultString(),
                        (Exception) response));
                } else {
                    WebServiceException jex = new WebServiceException(((Exception) response).getMessage(),
                        (Exception) response);
                    messageInfo.setResponse(jex);
                }
                return;
            default:
                // this is mostlikey and exception

                // todo:may need to throw jaxwsexceptionhere
        }
    }

    private void closeAllHandlers(HandlerContext context) {
        HandlerChainCaller caller = getHandlerChainCaller(context.getMessageInfo());
        if (caller != null && caller.hasHandlers()) {
            caller.forceCloseHandlers(context);
        }
    }

    /*
     * Try to create as few objects as possible, thus carry around null sets
     * when possible and check if MU headers are found. Also assume handler
     * chain caller is null unless one is found.
     *
     * todo -- cleanup
     *
    private void checkMustUnderstandHeaders(HandlerContext context) throws SOAPException {
        SOAPMessage message = context.getSOAPMessage();
        SOAPHeader header = message.getSOAPHeader();
        if (header == null) {
            return;
        }

        // start with the mandatory roles
        Set<String> roles = new HashSet<String>();
        roles.add("http://schemas.xmlsoap.org/soap/actor/next");
        roles.add("");
        HandlerChainCaller hcCaller = getHandlerChainCaller(context.getMessageInfo());
        if (hcCaller != null) {
            roles.addAll(hcCaller.getRoles());
        }

        // keep set=null if there are no understood headers
        Set<QName> understoodHeaders = null;
        RuntimeContext rtContext = (RuntimeContext) context.getMessageInfo().getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if (rtContext != null && rtContext.getModel() != null) {
            understoodHeaders = new HashSet<QName>(((SOAPRuntimeModel) rtContext.getModel()).getKnownHeaders());
        }
        if (understoodHeaders == null) {
            if (hcCaller != null) {
                understoodHeaders = hcCaller.getUnderstoodHeaders();
            }
        } else {
            if (hcCaller != null) {
                understoodHeaders.addAll(hcCaller.getUnderstoodHeaders());
            }
        }

        // check MU headers for each role
        for (String role : roles) {
            Iterator<SOAPHeaderElement> iter = header.examineMustUnderstandHeaderElements(role);
            while (iter.hasNext()) {
                SOAPHeaderElement element = iter.next();
                QName qName = new QName(element.getNamespaceURI(), element.getLocalName());
                if (understoodHeaders == null || !understoodHeaders.contains(qName)) {
                    throw new SOAPFaultException(SOAPConstants.FAULT_CODE_MUST_UNDERSTAND,
                        MUST_UNDERSTAND_FAULT_MESSAGE_STRING, role, null);
                }
            }
        }
    }
     */

}
