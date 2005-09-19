/**
 * $Id: SOAPMessageDispatcher.java,v 1.40 2005-09-19 04:03:14 jitu Exp $
 */

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
package com.sun.xml.ws.protocol.soap.client;

import com.sun.pept.ept.EPTFactory;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.client.*;
import static com.sun.xml.ws.client.BindingProviderProperties.*;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.client.dispatch.ResponseImpl;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.encoding.soap.client.SOAP12XMLEncoder;
import com.sun.xml.ws.encoding.soap.client.SOAPXMLDecoder;
import com.sun.xml.ws.encoding.soap.client.SOAPXMLEncoder;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.internal.MessageInfoBase;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.handler.HandlerChainCaller.Direction;
import com.sun.xml.ws.handler.HandlerChainCaller.RequestOrResponse;
import com.sun.xml.ws.handler.SOAPHandlerContext;
import com.sun.xml.ws.handler.SOAPMessageContextImpl;
import com.sun.xml.ws.model.JavaMethod;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.spi.runtime.SystemHandlerDelegate;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.spi.runtime.InternalSoapEncoder;
import com.sun.xml.ws.transport.http.client.HttpClientTransportFactory;
import com.sun.xml.ws.util.Base64Util;
import com.sun.xml.ws.util.SOAPConnectionUtil;

import javax.xml.bind.JAXBException;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.*;
import static javax.xml.ws.BindingProvider.PASSWORD_PROPERTY;
import static javax.xml.ws.BindingProvider.USERNAME_PROPERTY;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Client-side SOAP protocol-specific {@link com.sun.pept.protocol.MessageDispatcher}
 *
 * @author WS Development Team
 */
public class SOAPMessageDispatcher implements MessageDispatcher {

    protected static final int MAX_THREAD_POOL_SIZE = 3;

    protected static final long AWAIT_TERMINATION_TIME = 10L;

    protected ExecutorService executorService;

    private final static String MUST_UNDERSTAND_FAULT_MESSAGE_STRING = "SOAP must understand error";

    /**
     * Default constructor
     */
    public SOAPMessageDispatcher() {
    }

    /*
     * Invokes doSendAsync method if the message exchange pattern is asynchronous, otherwise
     * invokes doSend method.
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

    /**
     * Orchestrates the sending of a synchronous request
     */
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
            SOAPHandlerContext handlerContext = null;
            InternalMessage im = encoder.toInternalMessage(messageInfo);

            HandlerChainCaller caller = getHandlerChainCaller(messageInfo);
            if (caller.hasHandlers()) {
                handlerContext = new SOAPHandlerContext(messageInfo, im, sm);
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
            }

            // Setting encoder here is necessary for calls to getBindingId()
            messageInfo.setEncoder(encoder);

            SystemHandlerDelegate systemHandlerDelegate =
                ((com.sun.xml.ws.spi.runtime.Binding) getBinding(messageInfo)).
                    getSystemHandlerDelegate();
            if (systemHandlerDelegate != null) {
                if (handlerContext == null) {
                    handlerContext = new SOAPHandlerContext(messageInfo, im, sm);
                    updateMessageContext(messageInfo, handlerContext);
                }
                //already used im, we can set that to null
                if ((sm != null) && (im != null))
                    handlerContext.setInternalMessage(null);

                handlerContext.getMessageContext().put(
                    MessageContext.MESSAGE_OUTBOUND_PROPERTY, Boolean.TRUE);
                handlerContext.getBindingId();
                handlerResult =
                    systemHandlerDelegate.processRequest(
                        (com.sun.xml.ws.spi.runtime.SOAPMessageContext)
                            handlerContext.getSOAPMessageContext());
                sm = handlerContext.getSOAPMessage();
            }

            if (sm == null)
                sm = encoder.toSOAPMessage(im, messageInfo);


            Map<String, Object> context = processMetadata(messageInfo, sm);

            // set the MIME headers on connection headers
            Map<String, List<String>> ch = new HashMap<String, List<String>>();
            for (Iterator iter = sm.getMimeHeaders().getAllHeaders(); iter.hasNext();) {
                List<String> h = new ArrayList<String>();
                MimeHeader mh = (MimeHeader) iter.next();

                h.clear();
                h.add(mh.getValue());
                ch.put(mh.getName(), h);
            }

            setConnection(messageInfo, context);
            ((WSConnection) messageInfo.getConnection()).setHeaders(ch);

            if (!isAsync(messageInfo)) {           
                WSConnection connection = (WSConnection) messageInfo.getConnection();

                logRequestMessage(sm, messageInfo);
                SOAPConnectionUtil.sendResponse(connection, sm);
            }

            // if handlerResult is false, the receive has already happened
            if (isRequestResponse && handlerResult) {            
                receive(messageInfo);
            } else if (isOneway(messageInfo) && handlerResult) {
                checkReturnStatus(messageInfo);              
            }
        
        } catch (Throwable e) {
            setResponseType(e, messageInfo);
            messageInfo.setResponse(e);
        }
        return sm;
    }

    private boolean isOneway(MessageInfo messageInfo) {
        return messageInfo.getMEP() == MessageStruct.ONE_WAY_MEP ? true : false;
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

        ContextMap properties = (ContextMap) messageInfo.getMetaData(JAXWS_CONTEXT_PROPERTY);

        if (messageInfo.getMEP() == MessageStruct.ONE_WAY_MEP)
            messageContext.put(ONE_WAY_OPERATION, "true");

        // process the properties
        if (properties != null) {
            for (Iterator names = properties.getPropertyNames(); names.hasNext();) {
                String propName = (String) names.next();

                // consume PEPT-specific properties
                if (propName.equals(ClientTransportFactory.class.getName())) {
                    messageContext.put(CLIENT_TRANSPORT_FACTORY, (ClientTransportFactory) properties.get(propName));
                } else if (propName.equals(BindingProvider.SESSION_MAINTAIN_PROPERTY)) {
                    Object maintainSession = properties.get(BindingProvider.SESSION_MAINTAIN_PROPERTY);
                    if (maintainSession != null && maintainSession.equals(Boolean.TRUE)) {
                        Object cookieJar = properties.get(HTTP_COOKIE_JAR);
                        if (cookieJar != null)
                            messageContext.put(HTTP_COOKIE_JAR, cookieJar);
                    }
                } else if (propName.equals(USERNAME_PROPERTY)) {
                    String credentials = (String) properties.get(USERNAME_PROPERTY);
                    if (credentials != null) {
                        credentials += ":";
                        String password = (String) properties.get(PASSWORD_PROPERTY);
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

        // Set accept header depending on content negotiation property
        String contentNegotiation = (String) messageInfo.getMetaData(CONTENT_NEGOTIATION_PROPERTY);

        String bindingId = getBindingId(messageInfo);
        if (bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
            soapMessage.getMimeHeaders().addHeader(ACCEPT_PROPERTY,
                contentNegotiation != "none" ? SOAP12_XML_FI_ACCEPT_VALUE : SOAP12_XML_ACCEPT_VALUE);
        } else {
            soapMessage.getMimeHeaders().addHeader(ACCEPT_PROPERTY,
                contentNegotiation != "none" ? XML_FI_ACCEPT_VALUE : XML_ACCEPT_VALUE);
        }

        messageContext.put(BINDING_ID_PROPERTY, bindingId);

        // SOAPAction: MIME header
        RuntimeContext runtimeContext = (RuntimeContext) messageInfo.getMetaData(JAXWS_RUNTIME_CONTEXT);
        if (runtimeContext != null) {
            JavaMethod javaMethod = runtimeContext.getModel().getJavaMethod(messageInfo.getMethod());
            if (javaMethod != null) {
                String soapAction = ((com.sun.xml.ws.model.soap.SOAPBinding) javaMethod.getBinding()).getSOAPAction();
                header.clear();
                if (soapAction == null) {
                    soapMessage.getMimeHeaders().addHeader("SOAPAction", "\"\"");
                } else {
                    soapMessage.getMimeHeaders().addHeader("SOAPAction", soapAction);
                }
            }
        }

        return messageContext;
    }

    protected void setConnection(MessageInfo messageInfo, Map<String, Object> context) {
        ClientTransportFactory clientTransportFactory = (ClientTransportFactory) context.get(CLIENT_TRANSPORT_FACTORY);
        WSConnection connection = null;
        if (clientTransportFactory == null) {
            clientTransportFactory = new HttpClientTransportFactory();
            context.put(CLIENT_TRANSPORT_FACTORY, clientTransportFactory);
        }
        connection = clientTransportFactory.create(context);
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

    public void checkReturnStatus(MessageInfo messageInfo) {
        WSConnection connection = (WSConnection)messageInfo.getConnection();
        Map<String, List<String>> headers = connection.getHeaders();
        if (connection.getStatus() != 202 && connection.getStatus() != 200) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "HTTP status code for oneway: expected 202 or 200, got " + connection.getStatus());
//            System.out.println("status: "+connection.getStatus());
        }        
    }
    
    /*
     * Orchestrates the receiving of a synchronous response
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

        // Content negotiation logic
        String contentNegotiationType = (String)messageInfo.getMetaData(CONTENT_NEGOTIATION_PROPERTY);
        // If XML request
        if (contentNegotiationType == "pessimistic") {
            try {
                // If FI response (TODO: remove dep with SAAJ RI)
                if (((com.sun.xml.messaging.saaj.soap.MessageImpl) sm).isFastInfoset()) {
                    Map requestContext = (Map)messageInfo.getMetaData(JAXWS_CONTEXT_PROPERTY);
                    // Further requests will be send using FI
                    requestContext.put(CONTENT_NEGOTIATION_PROPERTY, "optimistic");
                }
            }
            catch (ClassCastException e) {
                // Content negotiation fails
            }
        }

        try {
            logResponseMessage(sm, messageInfo);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        SOAPHandlerContext handlerContext = getInboundHandlerContext(messageInfo, sm);

        SystemHandlerDelegate systemHandlerDelegate =
            ((com.sun.xml.ws.spi.runtime.Binding) getBinding(messageInfo)).
                getSystemHandlerDelegate();
        if (systemHandlerDelegate != null) {
            handlerContext.getMessageContext().put(
                MessageContext.MESSAGE_OUTBOUND_PROPERTY, Boolean.FALSE);
            try {
            systemHandlerDelegate.processResponse((com.sun.xml.ws.spi.runtime.SOAPMessageContext)
                new SOAPMessageContextImpl(handlerContext));
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }

        try {
            decoder.doMustUnderstandProcessing(sm, messageInfo, handlerContext, false);
        } catch (SOAPException se) { // unusual saaj error
            throw new RuntimeException(se);
        } catch (IOException ie) { // unusual saaj error
            throw new RuntimeException(ie);
        } catch (SOAPFaultException sfe) {
            closeAllHandlers(handlerContext);
            throw sfe;
        }

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
            messageInfo.setResponse(sm);
            postReceiveAndDecodeHook(messageInfo);
        }
    }

    private SOAPHandlerContext getInboundHandlerContext(MessageInfo messageInfo, SOAPMessage sm) {
        SOAPHandlerContext handlerContext = (SOAPHandlerContext) messageInfo
            .getMetaData(BindingProviderProperties.JAXWS_HANDLER_CONTEXT_PROPERTY);
        if (handlerContext != null) {
            handlerContext.setSOAPMessage(sm);
            handlerContext.setInternalMessage(null);
        } else
            handlerContext = new SOAPHandlerContext(messageInfo, null, sm);
        return handlerContext;
    }

    /**
     * Orchestrates the sending of an asynchronous request
     */
    protected void doSendAsync(final MessageInfo messageInfo) {
        try { // should have already been caught
            preSendHook(messageInfo);
            SOAPMessage sm = doSend(messageInfo);
            postSendHook(messageInfo);

            //pass a copy of MessageInfo to the future task,so that no conflicts
            //due to threading happens
            Response r = sendAsyncReceive(MessageInfoBase.copy(messageInfo), sm);
            if (executorService == null) {
                executorService =
                    Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE, new DaemonThreadFactory());
            }

            AsyncHandlerService service = (AsyncHandlerService) messageInfo
                .getMetaData(BindingProviderProperties.JAXWS_CLIENT_ASYNC_HANDLER);
            WSFuture wsfuture = null;
            if (service != null) {
                wsfuture = service.setupAsyncCallback(r);
                ((ResponseImpl) r).setUID(service.getUID());
                ((ResponseImpl)r).setHandlerService(service);
            }

            executorService.execute((FutureTask) r);
            if (service == null)
                messageInfo.setResponse(r);
            else
                messageInfo.setResponse(wsfuture);
        } catch (Throwable e) {
            messageInfo.setResponse(e);
        }
    }

    /**
     * Orchestrates the receiving of an asynchronous response
     */
    protected Response<Object> sendAsyncReceive(final MessageInfo messageInfo, final SOAPMessage sm) {

        final AsyncHandlerService handler = (AsyncHandlerService) messageInfo
            .getMetaData(BindingProviderProperties.JAXWS_CLIENT_ASYNC_HANDLER);
        final boolean callback = (messageInfo.getMEP() == MessageStruct.ASYNC_CALLBACK_MEP) ? true
            : false;
        if (callback && (handler == null))
            throw new WebServiceException("Asynchronous callback invocation, but no handler - AsyncHandler required");

        final Response r = new ResponseImpl<Object>(new Callable<Object>() {

            public Object call() throws Exception {
                // get connection and do http.invoke()
                try {
                    final WSConnection connection = (WSConnection) messageInfo.getConnection();
                    logRequestMessage(sm, messageInfo);
                    SOAPConnectionUtil.sendResponse(connection, sm);
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

                if (messageInfo.getResponse() instanceof Exception)
                    throw (Exception) messageInfo.getResponse();
                return messageInfo.getResponse();
            }
        });
        return r;
    }

    private void makeSOAPMessage(MessageInfo messageInfo, SOAPHandlerContext context) {
        InternalMessage internalMessage = context.getInternalMessage();
        if (internalMessage != null) {
            LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
            SOAPEncoder encoder = eptf.getSOAPEncoder();
            SOAPMessage soapMesage = encoder.toSOAPMessage(internalMessage, messageInfo);
            context.setSOAPMessage(soapMesage);
            context.setInternalMessage(null);
        }
    }

    protected boolean callHandlersOnRequest(SOAPHandlerContext handlerContext) {
        HandlerChainCaller caller = getHandlerChainCaller(handlerContext.getMessageInfo());
        boolean responseExpected = (handlerContext.getMessageInfo().getMEP() != MessageStruct.ONE_WAY_MEP);
        return caller.callHandlers(Direction.OUTBOUND, RequestOrResponse.REQUEST, handlerContext,
            responseExpected);
    }

    protected boolean callHandlersOnResponse(SOAPHandlerContext handlerContext) {
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

    protected void updateMessageContext(MessageInfo messageInfo, SOAPHandlerContext context) {
        SOAPMessageContext messageContext = context.getSOAPMessageContext();
        messageInfo.setMetaData(BindingProviderProperties.JAXWS_HANDLER_CONTEXT_PROPERTY, context);
        RequestContext ctxt = (RequestContext) messageInfo
            .getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
        Iterator i = ctxt.copy().getPropertyNames();
        while (i.hasNext()) {
            String name = (String) i.next();
            Object value = ctxt.get(name);
            messageContext.put(name, value);
        }

        BindingProvider provider = (BindingProvider) context.getMessageContext()
            .get(BindingProviderProperties.JAXWS_CLIENT_HANDLE_PROPERTY);
        if (provider != null) {
            if (Proxy.isProxyClass(provider.getClass())) {
                EndpointIFInvocationHandler invocationHandler = (EndpointIFInvocationHandler) Proxy.getInvocationHandler(provider);
                EndpointIFContext endpointContext = invocationHandler.getEndpointContext();
                messageContext.put(MessageContext.WSDL_SERVICE, invocationHandler.getServiceQName());
                messageContext.put(MessageContext.WSDL_PORT, endpointContext.getPortName());
                //this should already be in messageContext String endpointAddress = endpointContext.getEndpointAddress();
            }
        }
        RuntimeContext rtContext = (RuntimeContext) messageInfo.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if (rtContext != null) {
            RuntimeModel model = rtContext.getModel();
            JavaMethod javaMethod = model.getJavaMethod(messageInfo.getMethod());
            if (javaMethod != null) {
                QName operationName = model.getQNameForJM(javaMethod);
                messageContext.put(MessageContext.WSDL_OPERATION, operationName);
            }
        }
        
        //now get value for ContentNegotiation
        Object prop = messageInfo.getMetaData(CONTENT_NEGOTIATION_PROPERTY);
        if (prop != null){
            messageContext.put(CONTENT_NEGOTIATION_PROPERTY, prop);
        }
    }

    protected void updateResponseContext(MessageInfo messageInfo, SOAPHandlerContext context) {

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

    /**
     * @return true if message exchange pattern indicates asynchronous, otherwise returns false
     */
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
                    SOAPFaultException sfe = new SOAPFaultException(soapFaultInfo.getSOAPFault());
                    if (jbe != null)
                        sfe.initCause(jbe);
                    messageInfo.setResponse((SOAPFaultException) sfe);
                }
                return;
            case MessageStruct.UNCHECKED_EXCEPTION_RESPONSE:
                if (response instanceof SOAPFaultException) {
                    messageInfo.setResponse((SOAPFaultException) response);
                } else {
                    WebServiceException jex = null;
                    if (response instanceof Exception) {
                        jex = new WebServiceException((Exception) response);
                        messageInfo.setResponse(jex);
                    }
                    messageInfo.setResponse(response);
                }
                return;
            default:
                messageInfo.setResponse(response);
        }
    }

    private void closeAllHandlers(SOAPHandlerContext context) {
        HandlerChainCaller caller = getHandlerChainCaller(context.getMessageInfo());
        if (caller != null && caller.hasHandlers()) {
            caller.forceCloseHandlers(context);
        }
    }

    /**
     * This method is used to create the appropriate SOAPMessage (1.1 or 1.2 using SAAJ api).
     *
     * @return the BindingId associated with messageInfo
     */
    protected String getBindingId(MessageInfo messageInfo) {
        SOAPEncoder encoder = (SOAPEncoder) messageInfo.getEncoder();
        if (encoder instanceof SOAP12XMLEncoder)
            return SOAPBinding.SOAP12HTTP_BINDING;
        else
            return SOAPBinding.SOAP11HTTP_BINDING;
    }

    /**
     * Logs the SOAP request message
     */
    protected void logRequestMessage(SOAPMessage soapMessage, MessageInfo messageInfo)
        throws IOException, SOAPException {

        OutputStream out = ((WSConnection) messageInfo.getConnection()).getDebug();

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

    /**
     * Logs the SOAP response message
     */
    protected void logResponseMessage(SOAPMessage response, MessageInfo messageInfo)
        throws IOException, SOAPException {

        OutputStream out = ((WSConnection) messageInfo.getConnection()).getDebug();
        if (out != null) {
            String s = "Response\n";
            out.write(s.getBytes());
            s =
                "Http Status Code: "
                    + ((WSConnection) messageInfo.getConnection()).getStatus()
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

    class DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread daemonThread = new Thread(r);
            daemonThread.setDaemon(Boolean.TRUE);
            return daemonThread;
        }
    }
}
