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
package com.sun.xml.ws.client;


import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.ws.encoding.soap.internal.DelegateBase;
import com.sun.xml.ws.model.JavaMethod;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.wsdl.WSDLContext;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.Executor;

public class EndpointIFInvocationHandler
    extends EndpointIFBase
    implements InvocationHandler, com.sun.xml.ws.client.BindingProviderProperties {

    Object _proxy;
    DelegateBase _delegate;



    EndpointIFContext _endpointContext;

    Class _portInterface;
    QName _serviceQName;

    RuntimeContext _rtcontext;
    WSDLContext _wsdlContext;
    boolean failure;
    URL wsdlDocumentLocation;
    WSServiceDelegate _service;

    /**
     * public constructor
     */

    public EndpointIFInvocationHandler(Class portInterface, EndpointIFContext eif, WSServiceDelegate service, QName serviceName) {

        if ((eif.getBindingID() == null) || (eif.getRuntimeContext() == null)) {
            failure = true;
            return;
        }
        _endpointContext = eif;
        _portInterface = portInterface;
        _rtcontext = eif.getRuntimeContext();
        _bindingId = eif.getBindingID();
        _service = service;

        if (serviceName != null) {
            if (eif.contains(serviceName))
                _serviceQName = serviceName;
            else
                throw new WebServiceException("Supplied service QName " +
                    serviceName + " does not exist in this wsdl.");
        } else
            _serviceQName =
                eif.getServiceName();

        if (eif.getEndpointAddress() != null)   //temp workaround for local transport kw
            getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                eif.getEndpointAddress());

        ContactInfoListImpl cil = new ContactInfoListImpl();
        //not sure I need this service argument
        _delegate = new DelegateBase(cil, service);
    }

    public void setModel(RuntimeContext rtcontext) {
        _rtcontext = rtcontext;
    }

    public void setProxy(Object p) {
        _proxy = p;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws WebServiceException, Throwable {

        try {
            if (isSEIMethod(method, _portInterface)) {
                return implementSEIMethod(method, args);
            } else {
                return method.invoke(this, args);
            }
        } catch (IllegalAccessException e) {
            throw new WebServiceException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (java.lang.reflect.UndeclaredThrowableException ex) {
            throw new WebServiceException(ex.getMessage(), ex.getCause());
        } catch (java.lang.reflect.GenericSignatureFormatError ex) {
            throw new WebServiceException(ex.getMessage(), ex);
        } catch (java.lang.reflect.MalformedParameterizedTypeException ex) {
            throw new WebServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * Gets a new {@link com.sun.pept.presentation.MessageStruct} from the Delegate, copies
     * the data and metadata into the newly created MessageStruct, invokes Delegate.send, and
     * returns the response.
     */
    public Object implementSEIMethod(Method method, Object[] parameters) throws Throwable {

        MessageStruct messageStruct = _delegate.getMessageStruct();
        int mmep = 0;
        if (_rtcontext != null) {
            JavaMethod jmethod = _rtcontext.getModel().getJavaMethod(method);
            if (jmethod != null) {
                int mep = jmethod.getMEP();
                mmep = (mep == MessageStruct.REQUEST_RESPONSE_MEP) ?
                    MessageStruct.REQUEST_RESPONSE_MEP : (mep == MessageStruct.ONE_WAY_MEP) ?
                    MessageStruct.ONE_WAY_MEP : ((mep == MessageStruct.ASYNC_POLL_MEP) ?
                    MessageStruct.ASYNC_POLL_MEP : MessageStruct.ASYNC_CALLBACK_MEP);
            } else throw new WebServiceException("runtime model information for java Method " + method.getName() + " is not known .");
        } //need to check if this is dispatch invocation

        if (mmep == MessageStruct.ASYNC_CALLBACK_MEP) {
            for (Object param : parameters) {
                if (AsyncHandler.class.isAssignableFrom(param.getClass())) {
                    //messageStruct.setMetaData(BindingProviderProperties.JAXWS_CLIENT_ASYNC_HANDLER, param);
                    messageStruct.setMetaData(BindingProviderProperties.JAXWS_CLIENT_ASYNC_HANDLER,
                        new AsyncHandlerService((AsyncHandler)param, getCurrentExecutor()));
                }
            }
        }

        messageStruct.setMethod(method);
        messageStruct.setData(parameters);
        RequestContext requestContext = (RequestContext) ((BindingProvider) _proxy).getRequestContext();
        requestContext.put(JAXWS_CLIENT_HANDLE_PROPERTY, _proxy);
        messageStruct.setMetaData(JAXWS_RUNTIME_CONTEXT, _rtcontext);
        messageStruct.setMetaData(JAXWS_CONTEXT_PROPERTY, requestContext);

        //set mtom threshold value to
        Object mtomThreshold = requestContext.get(MTOM_THRESHOLOD_VALUE);
        messageStruct.setMetaData(MTOM_THRESHOLOD_VALUE, mtomThreshold);
        
        messageStruct.setMEP(mmep);

        // Initialize content negotiation property
        ContentNegotiation.initialize(requestContext, messageStruct);

        // Set MTOM processing for XML requests only
        if (_rtcontext != null && _rtcontext.getModel() != null) {
            javax.xml.ws.soap.SOAPBinding sb = (binding instanceof javax.xml.ws.soap.SOAPBinding) ? (javax.xml.ws.soap.SOAPBinding) binding : null;
            if (sb != null) {
                _rtcontext.getModel().enableMtom(sb.isMTOMEnabled());
            }
        }

        _delegate.send(messageStruct);
        switch (messageStruct.getResponseType()) {
            case MessageStruct.NORMAL_RESPONSE:
                break;
            case MessageStruct.CHECKED_EXCEPTION_RESPONSE:
                if (_rtcontext.getModel().isCheckedException(method, messageStruct.getResponse().getClass()))
                    throw (Throwable) messageStruct.getResponse();
                throw (Exception) messageStruct.getResponse();
            case MessageStruct.UNCHECKED_EXCEPTION_RESPONSE:
                throw (RuntimeException) messageStruct.getResponse();
        }
        return messageStruct.getResponse();
    }

    boolean isSEIMethod(Method method, Class sei) {
        return (sei.equals(method.getDeclaringClass())) ? true : false;
    }

    public EndpointIFContext getEndpointContext() {
        return _endpointContext;
    }

    public QName getServiceQName() {
        return _serviceQName;
    }

    public Class getPortInterface(){
        return _portInterface;
    }

     Executor getCurrentExecutor(){
        return _service.getExecutor();
    }
}
