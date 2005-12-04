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

package com.sun.xml.ws.client.dispatch;

import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.client.*;
import static com.sun.xml.ws.client.BindingProviderProperties.DISPATCH_CONTEXT;
import static com.sun.xml.ws.client.dispatch.DispatchContext.DISPATCH_MESSAGE_CLASS;
import static com.sun.xml.ws.client.dispatch.DispatchContext.MessageType.HTTP_DATASOURCE_MESSAGE;
import static com.sun.xml.ws.client.dispatch.DispatchContext.MessageType.HTTP_SOURCE_PAYLOAD;
import static com.sun.xml.ws.client.dispatch.DispatchContext.MessageType.HTTP_JAXB_PAYLOAD;

import com.sun.xml.ws.client.dispatch.impl.DispatchContactInfoList;
import com.sun.xml.ws.client.dispatch.impl.DispatchDelegate;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.pept.Delegate;
import com.sun.xml.ws.pept.presentation.MessageStruct;
import com.sun.xml.ws.transport.http.client.HttpClientTransportFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;


import static com.sun.xml.ws.client.BindingProviderProperties.DISPATCH_CONTEXT;
import static com.sun.xml.ws.client.dispatch.DispatchContext.DISPATCH_MESSAGE_CLASS;
import com.sun.xml.ws.spi.runtime.ClientTransportFactory;

import javax.activation.DataSource;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;
import javax.xml.namespace.QName;


/**
 * The <code>javax.xml.ws.Dispatch</code> interface provides support
 * for the dynamic invocation of a service endpoint operation using XML
 * constructs or JAXB objects. The <code>javax.xml.ws.Service</code>
 * interface acts as a factory for the creation of <code>Dispatch</code>
 * instances.
 *
 * @author WS Development Team
 * @version 1.0
 */

public class DispatchBase implements BindingProvider, InternalBindingProvider,
    Dispatch {

    public DispatchBase(PortInfoBase port, Class aClass, Service.Mode mode, WSServiceDelegate service) {
        this(port, mode, null, aClass, service);
    }

    public DispatchBase(PortInfoBase port, JAXBContext jaxbContext, Service.Mode mode, WSServiceDelegate service) {
        this(port, mode, jaxbContext, null, service);
    }

    DispatchBase(PortInfoBase port, Service.Mode mode, JAXBContext context, Class clazz, WSServiceDelegate service) {
        _delegate = new DispatchDelegate(new DispatchContactInfoList());
        _mode = mode;
        _portInfo = port;
        _jaxbContext = context;
        _clazz = clazz;
        _service = service;
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
     *            context must have the <code>javax.xml.ws.binding.context</code>
     *            property set.
     * @return The response to the operation invocation. The object is
     *         either an instance of <code>javax.xml.transform.Source</code>
     *         or a JAXB object.
     * @throws javax.xml.ws.WebServiceException
     *          If there is any error in the configuration of
     *          the <code>Dispatch</code> instance
     * @throws javax.xml.ws.WebServiceException
     *          If an error occurs when using a supplied
     *          JAXBContext to marshall msg or unmarshall the response. The cause of
     *          the WebServiceException is the original JAXBException.
     */
    public Object invoke(Object msg)
        throws WebServiceException {

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
     *            context must have the <code>javax.xml.ws.binding.context</code>
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
     *                context must have the <code>javax.xml.ws.binding.context</code>
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
        if (handler != null) {
            messageStruct.setMetaData(BindingProviderProperties.JAXWS_CLIENT_ASYNC_HANDLER, (Object) new AsyncHandlerService(handler, getCurrentExecutor()));
        } else
            throw new WebServiceException("AsyncHandler argument is null. " +
                "AsyncHandler is required for asynchronous callback invocations ");

        messageStruct.setMEP(MessageStruct.ASYNC_CALLBACK_MEP);
        Object result = sendAsync(messageStruct);
        if (result instanceof WSFuture)
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
     *            context must have the <code>javax.xml.ws.binding.context</code>
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
        _transportFactory =
            (ClientTransportFactory) getRequestContext().get(BindingProviderProperties.CLIENT_TRANSPORT_FACTORY);

        if (_transportFactory == null) {
            _transportFactory = new HttpClientTransportFactory();
        }
        return _transportFactory;
    }

    public void _setTransportFactory(com.sun.xml.ws.spi.runtime.ClientTransportFactory f) {
        getRequestContext().put(BindingProviderProperties.CLIENT_TRANSPORT_FACTORY, f);
        _transportFactory = (ClientTransportFactory) f;
    }

    private Object sendAndReceive(MessageStruct messageStruct) {
        Object response = null;

        _delegate.send(messageStruct);
        response = messageStruct.getResponse();
        updateResponseContext(messageStruct);
        //((ContextMap) getRequestContext()).clear();
        switch (messageStruct.getResponseType()) {

            case MessageStruct.NORMAL_RESPONSE:
                //not sure where this belongs yet - but for now-
                break;
            case MessageStruct.CHECKED_EXCEPTION_RESPONSE:
                if (response instanceof SOAPFaultException)
                    throw (SOAPFaultException) response;
                if (response instanceof SOAPFaultInfo) {
                    SOAPFaultInfo soapFaultInfo = (SOAPFaultInfo) response;
                    JAXBException jbe = null;
                    if (soapFaultInfo.getString().contains("javax.xml.bind")) {
                        jbe = new JAXBException(soapFaultInfo.getString());
                        //do I need to put this in a webservice exception
                        SOAPFaultException sfe = new SOAPFaultException(soapFaultInfo.getSOAPFault());
                        sfe.initCause(jbe);
                    } else
                        throw new SOAPFaultException(soapFaultInfo.getSOAPFault());
                } else if (response instanceof HTTPException) {
                    throw (HTTPException) response;
                } else if (response instanceof WebServiceException)
                    throw (WebServiceException) response;
            case MessageStruct.UNCHECKED_EXCEPTION_RESPONSE:
                if (response instanceof SOAPFaultException) {
                    throw (SOAPFaultException) response;
                } else if (response instanceof HTTPException) {
                    throw (HTTPException) response;
                } else throw (WebServiceException) response;
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
                throw new WebServiceException("Client side exception before invocation ", ex);

        } finally {
            _lock.unlock();
        }
        return response;
    }

    private Object sendOneWay(MessageStruct messageStruct) {

        _delegate.send(messageStruct);
        Object response = messageStruct.getResponse();
        //no exceptions should be returned from server but
        //exceptions may be returned from the client        
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
            MessageFactory factory = null;
            if (((msg instanceof Source) && _mode == Service.Mode.MESSAGE) &&
                (!_getBindingId().toString().equals(HTTPBinding.HTTP_BINDING)))
            {
                try {

                    if (_getBindingId().toString().equals(SOAPBinding.SOAP12HTTP_BINDING))
                        factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
                    else
                        factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);

                    SOAPMessage message = factory.createMessage();
                    message.getSOAPPart().setContent((Source) msg);
                    message.saveChanges();
                    msg = message;
                } catch (SOAPException se) {
                    throw new WebServiceException(se);
                }
            }

            //setMessageStruct(messageStruct, msg);

        } else {
            //todo - needs to be a get request
            if (!isValidHttpGETRequest(msg))
            //if (!_getBindingId().toString().equals(HTTPBinding.HTTP_BINDING))
                throw new WebServiceException("No Message to Send to web service");
            //else {
                //setMessageStruct(messageStruct, msg);

           //}
        }
        setMessageStruct(messageStruct, msg);
        return messageStruct;
    }

    private void setMessageStruct(MessageStruct messageStruct, Object msg) {
        messageStruct.setData(new Object[]{msg});
        setMetadata(getRequestContext(), msg, messageStruct);

        // Initialize content negotiation property
        ContentNegotiation.initialize(getRequestContext(), messageStruct);
    }

    private void updateResponseContext(MessageStruct messageStruct) {
        ResponseContext responseContext = (ResponseContext)
            messageStruct.getMetaData(BindingProviderProperties.JAXWS_RESPONSE_CONTEXT_PROPERTY);
        setResponseContext(responseContext);
    }

    private void setMetadata(Map jaxwsContext, Object obj, MessageStruct messageStruct) {

        jaxwsContext.put(BindingProviderProperties.JAXWS_CLIENT_HANDLE_PROPERTY, this);
        jaxwsContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, _portInfo.getTargetEndpoint());

        jaxwsContext.put(BindingProviderProperties.BINDING_ID_PROPERTY, _getBindingId().toString());
        if (_jaxbContext != null)
            jaxwsContext.put(BindingProviderProperties.JAXB_CONTEXT_PROPERTY, _jaxbContext);
        messageStruct.setMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY,
            jaxwsContext);

        messageStruct.setMetaData(DispatchContext.DISPATCH_MESSAGE_MODE, _mode);
        if (_clazz != null)
            messageStruct.setMetaData(DispatchContext.DISPATCH_MESSAGE_CLASS, _clazz);

        DispatchContext context = setDispatchContext(jaxwsContext, obj, _mode);
        messageStruct.setMetaData(BindingProviderProperties.DISPATCH_CONTEXT, context);
    }

    public Binding getBinding() {
        return (Binding) binding;
    }

    public void _setBinding(BindingImpl binding) {
        this.binding = binding;
    }

    // default for now is soap binding
    public String _getBindingId() {
        _bindingId = _portInfo.getBindingId();
        if (_bindingId == null) {
            _bindingId = SOAPBinding.SOAP11HTTP_BINDING;
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

    public DispatchContext setDispatchContext(Map jaxwsContext, Object obj, Service.Mode mode) {

        DispatchContext context = new DispatchContext();
        context.setProperty(DispatchContext.DISPATCH_MESSAGE_MODE, mode);

        if (obj != null) {
            if (obj instanceof Source){
                context.setProperty(DispatchContext.DISPATCH_MESSAGE_CLASS,
                    DispatchContext.MessageClass.SOURCE);
             }else if (obj instanceof SOAPMessage) {
                context.setProperty(DispatchContext.DISPATCH_MESSAGE_CLASS,
                    DispatchContext.MessageClass.SOAPMESSAGE);
            } else if  ((obj instanceof DataSource) &&
                _getBindingId().toString().equals(HTTPBinding.HTTP_BINDING)) {
                    context.setProperty(DispatchContext.DISPATCH_MESSAGE_CLASS,
                        DispatchContext.MessageClass.DATASOURCE);

            } else if (_jaxbContext != null) {
                context.setProperty(DispatchContext.DISPATCH_MESSAGE_CLASS,
                    DispatchContext.MessageClass.JAXBOBJECT);
            } else {
                if (!_getBindingId().toString().equals(HTTPBinding.HTTP_BINDING))
                    throw new WebServiceException("Object is not a javax.xml.transform.Source or there is no JAXB Context");
            }
        }

        if (_clazz != null) {
            if (_clazz.isAssignableFrom(Source.class)) {
                if (mode == Service.Mode.PAYLOAD) {
                    if (_getBindingId().toString().equals(HTTPBinding.HTTP_BINDING))
                        context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.HTTP_SOURCE_PAYLOAD);
                    else
                        context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.SOURCE_PAYLOAD);
                } else if (mode == Service.Mode.MESSAGE) {
                    if (_getBindingId().toString().equals(HTTPBinding.HTTP_BINDING))
                        context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.HTTP_SOURCE_MESSAGE);
                    else
                        context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.SOURCE_MESSAGE);
                }
            } else if (_clazz.isAssignableFrom(SOAPMessage.class)) {
                if (mode == Service.Mode.PAYLOAD) {
                    throw new WebServiceException("SOAPMessages must be Service.Mode.MESSAGE. ");
                } else if (mode == Service.Mode.MESSAGE)
                    context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.SOAPMESSAGE_MESSAGE);
            } else if (_clazz.isAssignableFrom(DataSource.class)) {
                if (mode == Service.Mode.PAYLOAD)
                    throw new WebServiceException("Can not have a Datahandler class with mode PAYLOAD");
                    //context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.HTTP_DATASOURCE_PAYLOAD);
                else if (mode == Service.Mode.MESSAGE)
                    context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.HTTP_DATASOURCE_MESSAGE);
            } else {               
                context.setProperty(DispatchContext.DISPATCH_MESSAGE_CLASS, _clazz);
            }
        } else if (hasJAXBContext(obj, null)) {
            if (mode == Service.Mode.PAYLOAD) {
                if (_getBindingId().toString().equals(HTTPBinding.HTTP_BINDING))
                    context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.HTTP_JAXB_PAYLOAD);
                else
                    context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.JAXB_PAYLOAD);
            } else if (mode == Service.Mode.MESSAGE) {
                if (_getBindingId().toString().equals(HTTPBinding.HTTP_BINDING))
                    throw new WebServiceException(" Can not have a JAXB object with mode MESSAGE");
                    //context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.HTTP_JAXB_MESSAGE);
                else
                    context.setProperty(DispatchContext.DISPATCH_MESSAGE, DispatchContext.MessageType.JAXB_MESSAGE);
            }
        }
               
        return context;
    }

    Executor getCurrentExecutor() {
        return _service.getExecutor();
    }

    public QName getServiceName() {
        if (_service != null)
            return _service.getServiceName();
        return null;
    }

    public QName getPortName() {
        if (_portInfo != null)
            return _portInfo.getName();
        return null;
    }

    private boolean isValidHttpGETRequest(Object msg){
       boolean isValid = false;

       String bindingId = _getBindingId().toString();
       String method = (String)getRequestContext().get(MessageContext.HTTP_REQUEST_METHOD);
       if (method != null){
          isValid = "GET".equalsIgnoreCase(method)?true:false &&
           (SOAPBinding.SOAP12HTTP_BINDING.equals(bindingId) ||
               HTTPBinding.HTTP_BINDING.equals(bindingId))?true:false;
       }
        return isValid;
    }

    private static ClientTransportFactory defaultTransportFactory = null;
    private static final Logger logger =
        Logger.getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());

    protected Map _requestContext;
    protected Map _responseContext;
    protected Service.Mode _mode;
    protected WSServiceDelegate _service;
    protected Class _clazz;
    protected JAXBContext _jaxbContext;

    protected Delegate _delegate = null;
    protected PortInfoBase _portInfo = null;

    protected String _bindingId = null;
    protected BindingImpl binding;

    private ClientTransportFactory _transportFactory;
    private Lock _lock;

}