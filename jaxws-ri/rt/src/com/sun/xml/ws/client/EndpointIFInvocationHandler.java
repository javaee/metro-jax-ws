/*
 * $Id: EndpointIFInvocationHandler.java,v 1.1 2005-05-23 22:26:36 bbissett Exp $
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

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;

public class EndpointIFInvocationHandler
    extends EndpointIFBase
    implements InvocationHandler, com.sun.xml.ws.client.BindingProviderProperties {

    Object _proxy;
    DelegateBase _delegate;
    Class _portInterface;

    RuntimeContext _rtcontext;
    WSDLContext _wsdlContext;
    boolean failure;
    URL wsdlDocumentLocation;

    /**
     * public constructor
     */
    public EndpointIFInvocationHandler(Class pi, RuntimeContext context, WSDLContext wscontext) {
        if (wscontext == null) {
            failure = true;
            return;
        }
        _portInterface = pi;
        _rtcontext = context;
        _wsdlContext = wscontext;
        if (wscontext.getEndpoint() != null)   //temp workaround for local transport kw
            getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wscontext.getEndpoint());

        //leave for now check on binding id
        // _bindingId = wscontext.getBindingID();

        ContactInfoListImpl cil = new ContactInfoListImpl();
        _delegate = new DelegateBase(cil);
    }

    public EndpointIFInvocationHandler(RuntimeContext context, Class si, URL wsdlLocation) {
        this(si, context, null);
        wsdlDocumentLocation = wsdlLocation;
    }


    public void setModel(RuntimeContext rtcontext) {
        _rtcontext = rtcontext;
    }

    public void setProxy(Object p) {
        _proxy = p;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (isSEIMethod(method, _portInterface)) {
            return implementSEIMethod(method, args);
        } else {
            //return implementStubMethod(method, args);
            return method.invoke(this, args);
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

        messageStruct.setMethod(method);
        messageStruct.setData(parameters);
        ((BindingProvider) _proxy).getRequestContext().put(JAXRPC_CLIENT_HANDLE_PROPERTY, _proxy);
        messageStruct.setMetaData(JAXRPC_RUNTIME_CONTEXT, _rtcontext);
        messageStruct.setMetaData(JAXRPC_CONTEXT_PROPERTY, ((BindingProvider) _proxy).getRequestContext());


        messageStruct.setMEP(mmep);
        _delegate.send(messageStruct);
        switch (messageStruct.getResponseType()) {
            case MessageStruct.NORMAL_RESPONSE:
                break;
            case MessageStruct.CHECKED_EXCEPTION_RESPONSE:
                if (_rtcontext.getModel().isCheckedException(method, messageStruct.getResponse().getClass()))
                    throw (Throwable) messageStruct.getResponse();
                // throw (RemoteException) messageStruct.getResponse();
                throw (Exception) messageStruct.getResponse();
            case MessageStruct.UNCHECKED_EXCEPTION_RESPONSE:
                throw (RuntimeException) messageStruct.getResponse();
        }
        return messageStruct.getResponse();
    }

    //don't think I need this- test later
    protected Object implementBindingProviderMethod(Method method, Object[] args) {

        String methodName = method.getName();

        if (methodName.equals("getRequestContext")) {
            return this.getRequestContext();
        } else if (methodName.equals("getResponseContext")) {
            return this.getResponseContext();
        } else if (methodName.equals("getBinding")) {
            return this.getBinding();
        } else
            throw new WebServiceException("No such method exception ");

    }


    boolean isSEIMethod(Method method, Class sei) {
        return (sei.equals(method.getDeclaringClass())) ? true : false;
    }

}
