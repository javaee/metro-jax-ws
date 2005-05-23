/*
 * $Id: EndpointIFProxyBuilder.java,v 1.1 2005-05-23 22:26:36 bbissett Exp $
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

    public EndpointIFProxyBuilder(HandlerRegistryImpl handlerRegistry, RuntimeContext context, WSDLContext wsContext) {
        this.handlerRegistry = handlerRegistry;
        rtContext = context;
        wsdlContext = wsContext;
    }

    public void setBindingOnProvider(InternalBindingProvider provider,
                                     QName portName, URI bindingId) {

        provider._setBinding(handlerRegistry.createBinding(portName, bindingId));
    }

    public Object buildEndpointIFProxy(QName portQName, Class portInterface)
        throws WebServiceException {

        EndpointIFInvocationHandler handler =
            new EndpointIFInvocationHandler(portInterface, rtContext, wsdlContext);   //need handler registry passed in here
        setBindingOnProvider(handler, portQName, handler._getBindingId());

        Object proxy = Proxy.newProxyInstance(portInterface.getClassLoader(),
            new Class[]{portInterface, Remote.class, BindingProvider.class, BindingProviderProperties.class, AnnotatedElement.class},
            handler);
        handler.setProxy((Object) proxy);
        handler.setModel(rtContext);
        return (BindingProvider) proxy;
    }

}
