/**
 * $Id: DispatchBase.java,v 1.3 2005-05-25 20:44:10 kohlert Exp $
 */
/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.client.dispatch;

import com.sun.pept.Delegate;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.ws.client.*;
import com.sun.xml.ws.client.dispatch.impl.DispatchContactInfoList;
import com.sun.xml.ws.client.dispatch.impl.DispatchDelegate;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.transport.http.client.HttpClientTransportFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPFaultException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static com.sun.xml.ws.client.BindingProviderProperties.DISPATCH_CONTEXT;
import static com.sun.xml.ws.client.dispatch.DispatchContext.DISPATCH_MESSAGE_CLASS;


/**
 * The <code>javax.xml.rpc.Dispatch</code> interface provides support
 * for the dynamic invocation of a service endpoint operation using XML
 * constructs or JAXB objects. The <code>javax.xml.rpc.Service</code>
 * interface acts as a factory for the creation of <code>Dispatch</code>
 * instances.
 *
 * @author JAXRPC Development Team
 * @version 1.0
 */

public class DispatchBase implements BindingProvider, InternalBindingProvider,
    Dispatch {

    public DispatchBase(PortInfoBase port, Class aClass, Service.Mode mode) {
        this(port, mode, null, aClass);
    }

    public DispatchBase(PortInfoBase port, JAXBContext jaxbContext, Service.Mode mode) {
        this(port, mode, jaxbContext, null);
    }

    DispatchBase(PortInfoBase port, Service.Mode mode, JAXBContext context, Class clazz) {
        _delegate = new DispatchDelegate(new DispatchContactInfoList());
        _mode = mode;
        _portInfo = port;
        _jaxbContext = context;
        _clazz = clazz;
    }

    /**
     * Invoke a service operation synchronously.
     * <p/>
     * The client is responsible for ensuring that the <code>msg</code> object
     * is formed according to the requirements of the protocol binding in use.
     *
     * @param msg An object that will form the payload of
     *            the message used to invoke the operation. Must be an instance of
     *            either <code>javax.xml.transform.Source</code> or a JAXB object. If
     *            <code>msg</code> is an instance of a JAXB object then the request
     *            context must have the <code>javax.xml.rpc.binding.context</code>
     *            property set.
     * @return The response to the operation invocation. The object is
     *         either an instance of <code>javax.xml.transform.Source</code>
     *         or a JAXB object.
     * @throws java.rmi.RemoteException If a fault occurs during communication with
     *                                  the service
     * @throws javax.xml.ws.WebServiceException
     *                                  If there is any error in the configuration of
     *                                  the <code>Dispatch</code> instance
     * @throws javax.xml.ws.WebServiceException
     *                                  If an error occurs when using a supplied
     *                                  JAXBContext to marshall msg or unmarshall the response. The cause of
     *                                  the WebServiceException is the original JAXBException.
     */
    public Object invoke(Object msg)
        throws RemoteException, WebServiceException {

        MessageStruct messageStruct = setupMessageStruct(msg);
        messageStruct.setMEP(MessageStruct.REQUEST_RESPONSE_MEP);
        return sendAndReceive(messageStruct);
    }

    /**
     * Invoke a service operation asynchronously.  The
     * method returns without waiting for the response to the operation
     * invocation, the results of the operation are obtained by polling the
     * returned <code>Response</code>.
     * <p/>
     * The client is responsible for ensuring that the <code>msg</code> object
     * when marshalled is formed according to the requirements of the protocol
     * binding in use.
     *
     * @param msg An object that, when marshalled, will form the payload of
     *            the message used to invoke the operation. Must be an instance of
     *            either <code>javax.xml.transform.Source</code> or a JAXB object. If
     *            <code>msg</code> is an instance of a JAXB object then the request
     *            context must have the <code>javax.xml.rpc.binding.context</code>
     *            property set.
     * @return The response to the operation invocation. The object
     *         returned by <code>Response.get()</code> is
     *         either an instance of <code>javax.xml.transform.Source</code>
     *         or a JAXB object.
     * @throws javax.xml.ws.WebServiceException
     *          If there is any error in the configuration of
     *          the <code>Dispatch</code> instance
     * @throws javax.xml.ws.WebServiceException
     *          If an error occurs when using a supplied
     *          JAXBContext to marshall msg. The cause of
     *          the WebServicException is the original JAXBException.
     */
    public Response<Object> invokeAsync(Object msg)
        throws WebServiceException {

        MessageStruct messageStruct = setupMessageStruct(msg);
        messageStruct.setMEP(MessageStruct.ASYNC_POLL_MEP);
        Object result = sendAsync(messageStruct);
        if (result instanceof Response)
            return (Response<Object>) result;
        else
            throw (WebServiceException) result;
    }


    /**
     * Invoke a service operation asynchronously. The
     * method returns without waiting for the response to the operation
     * invocation, the results of the operation are communicated to the client
     * via the passed in handler.
     * <p/>
     * The client is responsible for ensuring that the <code>msg</code> object
     * when marshalled is formed according to the requirements of the protocol
     * binding in use.
     *
     * @param msg     An object that, when marshalled, will form the payload of
     *                the message used to invoke the operation. Must be an instance of
     *                either <code>javax.xml.transform.Source</code> or a JAXB object. If
     *                <code>msg</code> is an instance of a JAXB object then the request
     *                context must have the <code>javax.xml.rpc.binding.context</code>
     *                property set.
     * @param handler The handler object that will receive the
     *                response to the operation invocation. The object
     *                returned by <code>Response.get()</code> is
     *                either an instance of
     *                <code>javax.xml.transform.Source</code> or a JAXB object.
     * @return A <code>Future</code> object that may be used to check the status
     *         of the operation invocation. This object must not be used to try to
     *         obtain the results of the operation - the object returned from
     *         <code>Future<?>.get()</code> is implementation dependent
     *         and any use of it will result in non-portable behaviour.
     * @throws javax.xml.ws.WebServiceException
     *          If there is any error in the configuration of
     *          the <code>Dispatch</code> instance
     * @throws javax.xml.ws.WebServiceException
     *          If an error occurs when using a supplied
     *          JAXBContext to marshall msg. The cause of
     *          the WebServiceException is the original JAXBException.
     */
    public Future<?> invokeAsync(java.lang.Object msg, AsyncHandler handler) {

        MessageStruct messageStruct = setupMessageStruct(msg);
        if (handler != null)
            messageStruct.setMetaData(BindingProviderProperties.JAXWS_CLIENT_ASYNC_HANDLER, handler);
        else
            throw new WebServiceException("AsyncHandler argument is null. " +
                "AsyncHandler is required for asynchronous callback invocations ");

        messageStruct.setMEP(MessageStruct.ASYNC_CALLBACK_MEP);
        Object result = sendAsync(messageStruct);
        if (result instanceof Response)
            return (Future<Object>) result;
        else
            throw (WebServiceException) result;
    }

    /**
     * Invokes a service operation using the one-way
     * interaction mode. The operation invocation is logically non-blocking,
     * subject to the capabilities of the underlying protocol, no results
     * are returned. When
     * the protocol in use is SOAP/HTTP, this method must block until
     * an HTTP response code has been received or an error occurs.
     * <p/>
     * The client is responsible for ensuring that the <code>msg</code> object
     * when marshalled is formed according to the requirements of the protocol
     * binding in use.
     *
     * @param msg An object that, when marshalled, will form the payload of
     *            the message used to invoke the operation. Must be an instance of
     *            either <code>javax.xml.transform.Source</code> or a JAXB object. If
     *            <code>msg</code> is an instance of a JAXB object then the request
     *            context must have the <code>javax.xml.rpc.binding.context</code>
     *            property set.
     * @throws javax.xml.ws.WebServiceException
     *          If there is any error in the configuration of
     *          the <code>Dispatch</code> instance or if an error occurs during the
     *          invocation.
     * @throws javax.xml.ws.WebServiceException
     *          If an error occurs when using a supplied
     *          JAXBContext to marshall msg. The cause of
     *          the WebServiceException is the original JAXBException.
     */

    public void invokeOneWay(Object msg) {

        MessageStruct messageStruct = setupMessageStruct(msg);
        messageStruct.setMEP(MessageStruct.ONE_WAY_MEP);
        sendOneWay(messageStruct);
    }

    private boolean hasJAXBContext(Object msg, MessageStruct messageStruct) {
        RequestContext requestContext = (RequestContext) getRequestContext();
        if (_jaxbContext != null) {
            requestContext.put(BindingProviderProperties.JAXB_CONTEXT_PROPERTY, _jaxbContext);
            return true;
        }
        return false;
    }

    public void _setDelegate(Delegate delegate) {
        _delegate = delegate;
    }

    public Delegate _getDelegate() {
        return _delegate;
    }

    public static void setDefaultTransportFactory(ClientTransportFactory factory) {
        defaultTransportFactory = factory;
    }

    public static ClientTransportFactory getDefaultTransportFactory() {
        if (defaultTransportFactory == null)
            defaultTransportFactory = new HttpClientTransportFactory();
        return defaultTransportFactory;
    }

    public ClientTransportFactory _getTransportFactory() {
        _transportFactory = null;
        //  (ClientTransportFactory)
        // getRequestContext().getProperty(ClientTransportFactory.class.getName());

        if (_transportFactory == null) {
            _transportFactory = defaultTransportFactory;
        }
        return _transportFactory;
    }

    public void _setTransportFactory(ClientTransportFactory f) {
         getRequestContext().put(ClientTransportFactory.class.getName(), f);
        _transportFactory = f;
    }

    private Object sendAndReceive(MessageStruct messageStruct) throws RemoteException {
        Object response = null;

        _delegate.send(messageStruct);
        response = messageStruct.getResponse();
        updateResponseContext(messageStruct);
        ((ContextMap) getRequestContext()).clear();
        switch (messageStruct.getResponseType()) {

            case MessageStruct.NORMAL_RESPONSE:
                //not sure where this belongs yet - but for now-
                break;
            case MessageStruct.CHECKED_EXCEPTION_RESPONSE:
                if (response instanceof SOAPFaultException)
                    throw new RemoteException("Exception from server", (SOAPFaultException) response);
                if (response instanceof SOAPFaultInfo) {
                    SOAPFaultInfo soapFaultInfo = (SOAPFaultInfo) response;
                    JAXBException jbe = null;
                    if (soapFaultInfo.getString().contains("javax.xml.bind")) {
                        jbe = new JAXBException(soapFaultInfo.getString());
                        //do I need to put this in a webservice exception
                    }
                    SOAPFaultException sfe = new SOAPFaultException(soapFaultInfo.getCode(), soapFaultInfo.getString(),
                        soapFaultInfo.getActor(), (Detail) soapFaultInfo.getDetail());
                    sfe.initCause(jbe);
                    throw new RemoteException(sfe.getFaultString(), sfe);
                } else if (response instanceof WebServiceException)
                    throw (WebServiceException) response;
            case MessageStruct.UNCHECKED_EXCEPTION_RESPONSE:
                if (response instanceof SOAPFaultException) {
                    throw new RemoteException("Exception from service " +
                        ((SOAPFaultException) response).getFaultString(), (Exception) response);
                } else
                //before invocation
                    throw new WebServiceException(((Exception) response).getMessage(),
                        (Exception) response);
            default:
                if (response != null) //must be some kind of exception
                    throw new WebServiceException("Client side exception - examine cause ", (Exception) response);
        }
        return response;
    }

    private Object sendAsync(MessageStruct messageStruct)
        throws WebServiceException {
        Object response = null;
        _lock = new ReentrantLock();
        _lock.lock();
        try {
            _delegate.send(messageStruct);
            response = messageStruct.getResponse();
        } catch (Exception ex) {
            if (ex instanceof WebServiceException)
                throw (WebServiceException) ex;
            else
                new WebServiceException("Client side exception before invocation ", ex);

        } finally {
            _lock.unlock();
        }
        return response;
    }

    private Object sendOneWay(MessageStruct messageStruct) {
        Object response = null;
        _delegate.send(messageStruct);
        //will exceptions be returned from the server-
        //if not then can take this out and just send
        switch (messageStruct.getResponseType()) {
            case MessageStruct.NORMAL_RESPONSE:
                break;
            case MessageStruct.CHECKED_EXCEPTION_RESPONSE:
            case MessageStruct.UNCHECKED_EXCEPTION_RESPONSE:
                //before invocation
                if (response instanceof WebServiceException)
                    throw (WebServiceException) response;
                else
                    throw new WebServiceException(((Exception) response).getMessage(),
                        (Exception) response);
            default:
                throw new WebServiceException("Client side Exception ", (Exception) response);
        }
        return response;
    }

    private MessageStruct setupMessageStruct(Object msg) throws WebServiceException {
        MessageStruct messageStruct = _delegate.getMessageStruct();

        if (msg != null) {
            Class objClass = msg.getClass();
            //Object data = null;
            if ((msg instanceof Source) && _mode == Service.Mode.MESSAGE) {
                try {
                    MessageFactory factory = MessageFactory.newInstance();
                    SOAPMessage message = factory.createMessage();
                    message.getSOAPPart().setContent((Source) msg);
                    message.saveChanges();
                    msg = message;
                } catch (SOAPException se) {
                    throw new WebServiceException(se);
                }
            }

            messageStruct.setData(new Object[]{msg});
            setMetadata(getRequestContext(), msg, messageStruct);

        } else {
            throw new WebServiceException("No Message to Send to web service");
        }
        return messageStruct;
    }

    private void updateResponseContext(MessageStruct messageStruct) {
        ResponseContext responseContext = (ResponseContext)
            messageStruct.getMetaData(BindingProviderProperties.JAXWS_RESPONSE_CONTEXT_PROPERTY);
        setResponseContext(responseContext);
    }

    private void setMetadata(Map jaxwsContext, Object obj, MessageStruct messageStruct) {

        jaxwsContext.put(BindingProviderProperties.JAXWS_CLIENT_HANDLE_PROPERTY, this);
        jaxwsContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, _portInfo.getTargetEndpoint());
        if (_jaxbContext != null)
            jaxwsContext.put(BindingProviderProperties.JAXB_CONTEXT_PROPERTY, _jaxbContext);
        messageStruct.setMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY,
            jaxwsContext);

        messageStruct.setMetaData(DispatchContext.DISPATCH_MESSAGE_MODE, _mode);
        if (_clazz != null)
            messageStruct.setMetaData(DispatchContext.DISPATCH_MESSAGE_CLASS, _clazz);

        DispatchContext context = setDispatchContext(obj, _mode);
        messageStruct.setMetaData(DISPATCH_CONTEXT, context);
    }

    public Binding getBinding() {
        return (Binding) binding;
    }

    public void _setBinding(BindingImpl binding) {
        this.binding = binding;
    }

    // default for now is soap binding
    public URI _getBindingId() {
        // _bindingId = _portInfo.getBindingId();
        if (_bindingId == null) {
            try {
                // this is a known string and should not cause error
                _bindingId = new URI(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING);
            } catch (java.net.URISyntaxException e) {
                // should never happen, but just in case
                throw new RuntimeException(e);
            }
        }
        return _bindingId;
    }


    /**
     * Get the jaxwsContext that is used in processing request messages.
     * <p/>
     * Modifications to the request context do not affect asynchronous
     * operations that have already been started.
     *
     * @return The jaxwsContext that is used in processing request messages.
     */
    public Map getRequestContext() {
        if (_requestContext == null)
        _requestContext = new RequestContext(this);

        return _requestContext;
    }

    private void setResponseContext(ResponseContext context) {
        _responseContext = context;
    }

    /**
     * Get the jaxwsContext that resulted from processing a response message.
     * <p/>
     * The returned context is for the most recently completed synchronous
     * operation. Subsequent synchronous operation invocations overwrite the
     * response context. Asynchronous operations return their response context
     * via the Response interface.
     *
     * @return The jaxwsContext that is used in processing request messages.
     */
    public Map getResponseContext() {
        if (_responseContext == null)
        _responseContext = new ResponseContext(this);
        return _responseContext;
    }

    public DispatchContext setDispatchContext(Object obj, Service.Mode mode) {

        DispatchContext context = new DispatchContext();
        context.setProperty(DispatchContext.DISPATCH_MESSAGE_MODE, mode);
        if (_clazz != null)
            context.setProperty(DISPATCH_MESSAGE_CLASS, _clazz);
        if (obj instanceof Source)
            context.setProperty(DISPATCH_MESSAGE_CLASS,
                DispatchContext.MessageClass.SOURCE);
        else if (obj instanceof SOAPMessage) {
            context.setProperty(DISPATCH_MESSAGE_CLASS,
                DispatchContext.MessageClass.SOAPMESSAGE);
        } else if (_jaxbContext != null) {
            context.setProperty(DISPATCH_MESSAGE_CLASS,
                DispatchContext.MessageClass.JAXBOBJECT);
        } else {
            throw new WebServiceException("Object is not a javax.xml.transform.Source or there is no JAXB Context");
        }

        if (_clazz != null) {
            if (_clazz.isAssignableFrom(Source.class)) {
                if (mode == Service.Mode.PAYLOAD)
                    context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.SOURCE_PAYLOAD);
                else if (mode == Service.Mode.MESSAGE)
                    context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.SOURCE_MESSAGE);
            } else if (_clazz.isAssignableFrom(SOAPMessage.class)) {
                if (mode == Service.Mode.PAYLOAD){
                    throw new WebServiceException("SOAPMessages must be Service.Mode.MESSAGE. ");
                }   else if (mode == Service.Mode.MESSAGE)
                    context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.SOAPMESSAGE_MESSAGE);
            }
        } else if (hasJAXBContext(obj, null)) {
            if (mode == Service.Mode.PAYLOAD)
                context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.JAXB_PAYLOAD);
            else if (mode == Service.Mode.MESSAGE)
                context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.JAXB_MESSAGE);
        }
        return context;
    }

    private static ClientTransportFactory defaultTransportFactory = null;
    private static final Logger logger =
        Logger.getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());

    protected Map _requestContext;
    protected Map _responseContext;
    protected Service.Mode _mode;
    protected Class _clazz;
    protected JAXBContext _jaxbContext;

    protected Delegate _delegate = null;
    protected PortInfoBase _portInfo = null;

    protected static URI _bindingId = null;
    protected BindingImpl binding;

    private ClientTransportFactory _transportFactory;
    private Lock _lock;

}