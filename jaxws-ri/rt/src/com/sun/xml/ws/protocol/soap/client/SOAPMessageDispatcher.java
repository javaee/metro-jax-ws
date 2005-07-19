/**
 * $Id: SOAPMessageDispatcher.java,v 1.5 2005-07-19 20:41:25 arungupta Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.protocol.soap.client;

import com.sun.pept.ept.EPTFactory;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.binding.soap.BindingImpl;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.client.ClientTransportFactory;
import com.sun.xml.ws.client.ContextMap;
import com.sun.xml.ws.client.RequestContext;
import com.sun.xml.ws.client.ResponseContext;
import com.sun.xml.ws.encoding.soap.client.SOAPXMLDecoder;
import com.sun.xml.ws.encoding.soap.client.SOAPXMLEncoder;
import com.sun.xml.ws.encoding.soap.client.SOAP12XMLEncoder;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.client.dispatch.ResponseImpl;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.internal.MessageInfoBase;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.handler.HandlerChainCaller.Direction;
import com.sun.xml.ws.handler.HandlerChainCaller.RequestOrResponse;
import com.sun.xml.ws.handler.HandlerContext;
import com.sun.xml.ws.handler.SOAPMessageContextImpl;
import com.sun.xml.ws.model.JavaMethod;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.spi.runtime.SystemHandlerDelegate;
import com.sun.xml.ws.server.SOAPConnection;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.util.SOAPConnectionUtil;
import com.sun.xml.ws.util.Base64Util;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;

import static javax.xml.ws.BindingProvider.SOAPACTION_URI_PROPERTY;
import static javax.xml.ws.BindingProvider.PASSWORD_PROPERTY;
import static javax.xml.ws.BindingProvider.USERNAME_PROPERTY;

import static com.sun.xml.ws.client.BindingProviderProperties.ACCEPT_ENCODING_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.ACCEPT_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.CONTENT_TYPE_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.FAST_ACCEPT_VALUE;
import static com.sun.xml.ws.client.BindingProviderProperties.FAST_CONTENT_TYPE_VALUE;
import static com.sun.xml.ws.client.BindingProviderProperties.FAST_ENCODING_VALUE;
import static com.sun.xml.ws.client.BindingProviderProperties.HTTP_COOKIE_JAR;
import static com.sun.xml.ws.client.BindingProviderProperties.JAXWS_CONTEXT_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.JAXWS_RUNTIME_CONTEXT;
import static com.sun.xml.ws.client.BindingProviderProperties.ONE_WAY_OPERATION;
import static com.sun.xml.ws.client.BindingProviderProperties.XMLFAST_ENCODING_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.XML_ACCEPT_VALUE;
import static com.sun.xml.ws.client.BindingProviderProperties.SOAP12_XML_ACCEPT_VALUE;
import static com.sun.xml.ws.client.BindingProviderProperties.XML_CONTENT_TYPE_VALUE;
import static com.sun.xml.ws.client.BindingProviderProperties.CLIENT_TRANSPORT_FACTORY;
import static com.sun.xml.ws.client.BindingProviderProperties.BINDING_ID_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.HTTP_STATUS_CODE;

import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.transport.http.client.HttpClientTransportFactory;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.soap.MimeHeader;


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

            SystemHandlerDelegate systemHandlerDelegate =
                ((com.sun.xml.ws.spi.runtime.Binding) getBinding(messageInfo)).
                getSystemHandlerDelegate();
            if (systemHandlerDelegate != null) {
                handlerResult = systemHandlerDelegate.processRequest(
                    (com.sun.xml.ws.spi.runtime.SOAPMessageContext)
                    new SOAPMessageContextImpl(
                    new HandlerContext(messageInfo, im, sm)));
            }
            Map<String, Object> context = processMetadata(messageInfo, sm);
            
            // set the MIME headers on connection headers: required for local transport for now
            Map<String, List<String>> ch = new HashMap<String, List<String>>();
            List<String> h = new ArrayList<String>();
            for (Iterator iter = sm.getMimeHeaders().getAllHeaders(); iter.hasNext(); ) {
                MimeHeader mh = (MimeHeader)iter.next();
                
                h.clear();
                h.add(mh.getValue());
                ch.put(mh.getName(), h);
            }
            
            setConnection(messageInfo, context);
            ((WSConnection)messageInfo.getConnection()).setHeaders(ch);
            
            if (!isAsync(messageInfo)) {
                WSConnection connection = (WSConnection) messageInfo.getConnection();

                logRequestMessage(sm, messageInfo);
                SOAPConnectionUtil.sendResponse (connection, sm);
//                connection.sendResponse(sm);
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

    /**
     * Process and classify the metadata in MIME headers or message context. <String,String> data
     * is copied into MIME headers and the remaining metadata is passed in message context to the
     * transport layer.
     * 
     * @param messageInfo
     * @param soapMessage
     */
    protected Map<String, Object> processMetadata(MessageInfo messageInfo, SOAPMessage soapMessage) {
        Map<String, Object> messageContext = new HashMap<String, Object>();
        List<String> header = new ArrayList<String>();

        ContextMap properties = (ContextMap) messageInfo
            .getMetaData(JAXWS_CONTEXT_PROPERTY);

        if (messageInfo.getMEP() == MessageStruct.ONE_WAY_MEP)
            messageContext.put(ONE_WAY_OPERATION, "true");

        boolean acceptPropertySet = false;
        boolean encodingPropertySet = false;
        // process the properties
        if (properties != null) {
            for (Iterator names = properties.getPropertyNames(); names.hasNext();) {
                String propName = (String) names.next();
                
                // consume PEPT-specific properties
                if (propName.equals(ClientTransportFactory.class.getName())) {
                    messageContext.put(CLIENT_TRANSPORT_FACTORY, (ClientTransportFactory)properties.get(propName));
                } else if (propName.equals(BindingProvider.SESSION_MAINTAIN_PROPERTY)) {
                    Object maintainSession = properties.get(BindingProvider.SESSION_MAINTAIN_PROPERTY);
                    if (maintainSession != null && maintainSession.equals(Boolean.TRUE)) {
                        Object cookieJar = properties.get(HTTP_COOKIE_JAR);
                        if (cookieJar != null)
                            messageContext.put(HTTP_COOKIE_JAR, cookieJar);
                    }
//                } else if (propName.equals(XMLFAST_ENCODING_PROPERTY)) {
//                    encodingPropertySet = true;
//                    String encoding = (String) properties.get(XMLFAST_ENCODING_PROPERTY);
//                    if (encoding != null) {
//                        if (encoding.equals(FAST_ENCODING_VALUE)) {
//                            mimeHeaders.addHeader(CONTENT_TYPE_PROPERTY, FAST_CONTENT_TYPE_VALUE);
//                        } else {
//                            mimeHeaders.addHeader(CONTENT_TYPE_PROPERTY, getContentType(messageInfo));
//                        }
//                    } else { // default is XML encoding
//                        mimeHeaders.addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
//                    }
//                } else if (propName.equals(ACCEPT_ENCODING_PROPERTY)) {
//                    acceptPropertySet = true;
//                    String accept = (String) properties.get(ACCEPT_ENCODING_PROPERTY);
//                    if (accept != null) {
//                        if (accept.equals(FAST_ENCODING_VALUE))
//                            mimeHeaders.addHeader(ACCEPT_PROPERTY, FAST_ACCEPT_VALUE);
//                        else
//                            mimeHeaders.addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
//                    } else { // default is XML encoding
//                        mimeHeaders.addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
//                    }
                } else if (propName.equals(USERNAME_PROPERTY)) {
                    String credentials = (String)properties.get(USERNAME_PROPERTY);
                    if (credentials != null) {
                        credentials += ":";
                        String password = (String)properties.get(PASSWORD_PROPERTY);
                        if (password != null)
                            credentials += password;

                        try {
                            credentials = Base64Util.encode(credentials.getBytes());
                        } catch (Exception ex) {
                            throw new WebServiceException(ex);
                        }
                        soapMessage.getMimeHeaders().addHeader("Authorization", "Basic " + credentials);
                    }
                } else {
                    messageContext.put(propName, properties.get(propName));
                }
            }
        }

        // default Content-Type is XML encoding: MIME header
        if (!encodingPropertySet) {
            soapMessage.getMimeHeaders().addHeader(XML_CONTENT_TYPE_VALUE, CONTENT_TYPE_PROPERTY);
        }

        // default Accept is XML encoding: MIME header
        if (!acceptPropertySet) {
            header.clear();
            if (getBindingId(messageInfo).equals(SOAPBinding.SOAP12HTTP_BINDING)){
                soapMessage.getMimeHeaders().addHeader(ACCEPT_PROPERTY, SOAP12_XML_ACCEPT_VALUE);
            }else{
                soapMessage.getMimeHeaders().addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
            }
        }
        
        messageContext.put(BINDING_ID_PROPERTY, getBindingId(messageInfo));

        // SOAPAction: MIME header
        RuntimeContext runtimeContext = (RuntimeContext) messageInfo.getMetaData (JAXWS_RUNTIME_CONTEXT);
        JavaMethod javaMethod = runtimeContext.getModel().getJavaMethod (messageInfo.getMethod ());
        if (javaMethod != null) {
            String soapAction = ((com.sun.xml.ws.model.soap.SOAPBinding)javaMethod.getBinding()).getSOAPAction ();
            header.clear();
            if (soapAction == null) {
                soapMessage.getMimeHeaders().addHeader("SOAPAction", "\"\"");
            } else {
                soapMessage.getMimeHeaders().addHeader("SOAPAction", soapAction);
            }
        }
        
        //TODO: How about connection.setHeaders() instead of setting MIME headers ?
        
        return messageContext;
    }
    
    protected void setConnection(MessageInfo messageInfo, Map<String, Object> context) {
        ClientTransportFactory clientTransportFactory = (ClientTransportFactory)context.get(CLIENT_TRANSPORT_FACTORY);
        WSConnection connection = null;
        if (clientTransportFactory == null){
            clientTransportFactory = new HttpClientTransportFactory();
        }
        if (clientTransportFactory instanceof HttpClientTransportFactory) {
            connection = ((HttpClientTransportFactory)clientTransportFactory).create(context);
        } else {
            //local transport
            connection = clientTransportFactory.create();
        }
        messageInfo.setConnection(connection);
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
        
        try {
            logResponseMessage(sm, messageInfo);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        // HandlerContext handlerContext = new HandlerContext(messageInfo,
        // null, sm);
        HandlerContext handlerContext = getInboundHandlerContext(messageInfo, sm);

        SystemHandlerDelegate systemHandlerDelegate =
            ((com.sun.xml.ws.spi.runtime.Binding) getBinding(messageInfo)).
            getSystemHandlerDelegate();
        if (systemHandlerDelegate != null) {
            systemHandlerDelegate.processResponse(
                (com.sun.xml.ws.spi.runtime.SOAPMessageContext)
                new SOAPMessageContextImpl(handlerContext));
        }

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

    protected Binding getBinding(MessageInfo messageInfo) {
        ContextMap context = (ContextMap) ((MessageInfoBase) messageInfo)
            .getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
        BindingProvider provider = (BindingProvider) context
            .get(BindingProviderProperties.JAXWS_CLIENT_HANDLE_PROPERTY);
        return provider.getBinding();
    }

    protected HandlerChainCaller getHandlerChainCaller(MessageInfo messageInfo) {
        BindingImpl binding = (BindingImpl) getBinding(messageInfo);
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

   /**
     * This method is used to create the appropriate SOAPMessage (1.1 or 1.2 using SAAJ api).
     * @return
     */
    protected String getBindingId(MessageInfo messageInfo){
        SOAPEncoder encoder = (SOAPEncoder)messageInfo.getEncoder();
        if (encoder instanceof SOAP12XMLEncoder)
            return SOAPBinding.SOAP12HTTP_BINDING;
        else
            return SOAPBinding.SOAP11HTTP_BINDING;
    }

    protected void logRequestMessage(SOAPMessage soapMessage, MessageInfo messageInfo)
            throws IOException, SOAPException {

        OutputStream out = ((WSConnection)messageInfo.getConnection()).getDebug();
        
        if (out != null) {
            String s = "******************\nRequest\n";
            out.write(s.getBytes());
            for (Iterator iter =
                    soapMessage.getMimeHeaders().getAllHeaders();
                 iter.hasNext();
                    ) {
                MimeHeader header = (MimeHeader) iter.next();
                s = header.getName() + ": " + header.getValue() + "\n";
                out.write(s.getBytes());
            }
            out.flush();
            soapMessage.writeTo(out);
            s = "\n";
            out.write(s.getBytes());
            out.flush();
        }
    }
    
    protected void logResponseMessage(SOAPMessage response, MessageInfo messageInfo)
            throws IOException, SOAPException {

        OutputStream out = ((WSConnection)messageInfo.getConnection()).getDebug();
        if (out != null) {
            String s = "Response\n";
            out.write(s.getBytes());
            s =
                    "Http Status Code: "
                    + ((WSConnection)messageInfo.getConnection()).getStatus ()
                    + "\n\n";
            out.write(s.getBytes());
            for (Iterator iter =
                    response.getMimeHeaders().getAllHeaders();
                 iter.hasNext();
                    ) {
                MimeHeader header = (MimeHeader) iter.next();
                s = header.getName() + ": " + header.getValue() + "\n";
                out.write(s.getBytes());
            }
            out.flush();
            response.writeTo(out);
            s = "******************\n\n";
            out.write(s.getBytes());
        }
    }



}
