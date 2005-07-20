/*
 * $Id: EndpointIFInvocationHandler.java,v 1.8 2005-07-20 20:28:22 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

public class EndpointIFInvocationHandler
    extends EndpointIFBase
    implements InvocationHandler, com.sun.xml.ws.client.BindingProviderProperties {

    Object _proxy;
    DelegateBase _delegate;
    Class _portInterface;
    QName _serviceQName;

    RuntimeContext _rtcontext;
    WSDLContext _wsdlContext;
    boolean failure;
    URL wsdlDocumentLocation;

    /**
     * public constructor
     */
    public EndpointIFInvocationHandler(Class pi, RuntimeContext context, WSDLContext wscontext, QName serviceName) {
        if (wscontext == null) {
            failure = true;
            return;
        }
        _portInterface = pi;
        _rtcontext = context;
        _wsdlContext = wscontext;
        _bindingId = wscontext.getBindingID();

        if (serviceName != null) {
            if (wscontext.getServiceQName(serviceName) != null)
                _serviceQName = serviceName;
            else
                throw new WebServiceException("Supplied service QName " +
                    serviceName + " does not exist in this wsdl.");
        } else
            _serviceQName =
                wscontext.getServiceQName();

        if (wscontext.getEndpoint(_serviceQName) != null)   //temp workaround for local transport kw
            getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                wscontext.getEndpoint(_serviceQName));

        ContactInfoListImpl cil = new ContactInfoListImpl();
        _delegate = new DelegateBase(cil);
    }

    public EndpointIFInvocationHandler(RuntimeContext context, Class si, URL wsdlLocation) {
        this(si, context, null, null);
        wsdlDocumentLocation = wsdlLocation;
    }

    public void setModel(RuntimeContext rtcontext) {
        _rtcontext = rtcontext;
    }

    public void setProxy(Object p) {
        _proxy = p;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws WebServiceException, Throwable{

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

    public Object implementSEIMethod(Method method, Object[] parameters) throws Throwable {

        MessageStruct messageStruct = _delegate.getMessageStruct();
        int mmep = 0;
        if (_rtcontext != null) {
            JavaMethod jmethod = _rtcontext.getModel().getJavaMethod(method);
            int mep = jmethod.getMEP();
            mmep = (mep == MessageStruct.REQUEST_RESPONSE_MEP) ?
                MessageStruct.REQUEST_RESPONSE_MEP : (mep == MessageStruct.ONE_WAY_MEP) ?
                MessageStruct.ONE_WAY_MEP : ((mep == MessageStruct.ASYNC_POLL_MEP) ?
                MessageStruct.ASYNC_POLL_MEP : MessageStruct.ASYNC_CALLBACK_MEP);
        }

        if (mmep == MessageStruct.ASYNC_CALLBACK_MEP) {
            for (Object param : parameters) {
                if (AsyncHandler.class.isAssignableFrom(param.getClass())) {
                    messageStruct.setMetaData(BindingProviderProperties.JAXWS_CLIENT_ASYNC_HANDLER, param);
                }
            }
        }

        messageStruct.setMethod(method);
        messageStruct.setData(parameters);
        ((BindingProvider) _proxy).getRequestContext().put(JAXWS_CLIENT_HANDLE_PROPERTY, _proxy);
        messageStruct.setMetaData(JAXWS_RUNTIME_CONTEXT, _rtcontext);
        messageStruct.setMetaData(JAXWS_CONTEXT_PROPERTY, ((BindingProvider) _proxy).getRequestContext());

        messageStruct.setMEP(mmep);

        //set mtom processing
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
}
