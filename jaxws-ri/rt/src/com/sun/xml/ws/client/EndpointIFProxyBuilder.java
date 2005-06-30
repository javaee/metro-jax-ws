/*
 * $Id: EndpointIFProxyBuilder.java,v 1.2 2005-06-30 15:10:39 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.wsdl.WSDLContext;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.rmi.Remote;


/**
 * @author JAX-RPC Development Team
 */
public class EndpointIFProxyBuilder {

    protected HandlerRegistryImpl handlerRegistry;
    private RuntimeContext rtContext;
    private WSDLContext wsdlContext;
    private QName serviceQName;

    public EndpointIFProxyBuilder(HandlerRegistryImpl handlerRegistry,
                                  RuntimeContext context,
                                  WSDLContext wsContext,
                                  QName serviceName) {

        this.handlerRegistry = handlerRegistry;
        rtContext = context;
        wsdlContext = wsContext;
        serviceQName = serviceName;
    }

    public void setBindingOnProvider(InternalBindingProvider provider,
                                     QName portName, URI bindingId) {

        provider._setBinding(handlerRegistry.createBinding(portName, bindingId));
    }

    public Object buildEndpointIFProxy(QName portQName, Class portInterface)
        throws WebServiceException {

        EndpointIFInvocationHandler handler =
            new EndpointIFInvocationHandler(portInterface, rtContext, wsdlContext, serviceQName);   //need handler registry passed in here
        setBindingOnProvider(handler, portQName, handler._getBindingId());

        Object proxy = Proxy.newProxyInstance(portInterface.getClassLoader(),
            new Class[]{portInterface, Remote.class, BindingProvider.class, BindingProviderProperties.class, AnnotatedElement.class},
            handler);
        handler.setProxy((Object) proxy);
        handler.setModel(rtContext);
        return (BindingProvider) proxy;
    }

}
