/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.server.RuntimeContext;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/**
 * $author: WS Development Team
 */
public class EndpointIFContext {

    private RuntimeContext runtimeContext; //from annotationPro ess
    private Class serviceInterface;    //prop can take out
    private QName serviceName;
    private Class sei;
    private QName portName;
    private ArrayList<Handler> handlers;
    private String endpointAddress;
    private URI bindingId;


    public EndpointIFContext(Class sei) {
        this.sei = sei;
        handlers = new ArrayList();
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

    public QName getPortName() {
        return portName;
    }

    public String getEndpointAddress() {
        return endpointAddress;
    }

    public void setPortInfo(QName portQName, String endpoint, URI bindingID) {
        portName = portQName;
        endpointAddress = endpoint;
        this.bindingId = bindingID;
    }

    public URI getBindingID() {
        return bindingId;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public boolean contains(QName serviceName) {
        if (serviceName.equals(this.serviceName))
            return true;
        return false;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }
}