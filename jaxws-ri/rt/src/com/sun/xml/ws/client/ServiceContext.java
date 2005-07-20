/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.wsdl.WSDLContext;

import java.net.URL;

import javax.xml.namespace.QName;


/**
 * $author: JAXRPC Development Team
 */
public class ServiceContext {
    private WSDLContext wsdlContext; //from wsdlParsing
    private HandlerRegistryImpl registry; //from HandlerAnnotationProcessing
    private RuntimeContext runtimeContext; //from annotationPro ess
    private Class serviceInterface;
    private Class sei;
    private QName serviceName; //supplied on creation of service

    public ServiceContext(URL wsdlLocation, Class si, QName serviceName) {
    }

    public WSDLContext getWsdlContext() {
        return wsdlContext;
    }

    public void setWsdlContext(WSDLContext wsdlContext) {
        this.wsdlContext = wsdlContext;
    }

    public HandlerRegistryImpl getRegistry() {
        return registry;
    }

    public void setRegistry(HandlerRegistryImpl registry) {
        this.registry = registry;
    }

    public RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    public void setRuntimeContext(RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

    public Class getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public Class getSei() {
        return sei;
    }

    public void setSei(Class sei) {
        this.sei = sei;
    }

    public QName getServiceName() {
        if (serviceName != null)
            return serviceName;
        if (wsdlContext != null)
            return wsdlContext.getFirstServiceName();
        return null;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }
}
