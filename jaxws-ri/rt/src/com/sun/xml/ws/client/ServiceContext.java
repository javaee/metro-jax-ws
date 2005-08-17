/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.wsdl.WSDLContext;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import javax.xml.namespace.QName;


/**
 * $author: WS Development Team
 */
public class ServiceContext {
    private WSDLContext wsdlContext; //from wsdlParsing
    private HandlerRegistryImpl registry; //from HandlerAnnotationProcessing
    private Class serviceInterface;
    private QName serviceName; //supplied on creation of service
    private SIAnnotations siAnnotations;
    private final HashSet<EndpointIFContext> seiContext = new HashSet<EndpointIFContext>();


    public SIAnnotations getSiAnnotations() {
        return siAnnotations;
    }

    public void setSiAnnotations(SIAnnotations siAnnotations) {
        this.siAnnotations = siAnnotations;
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

    public EndpointIFContext getEndpointIFContext(String className) {
        for (EndpointIFContext eif: seiContext){
            if (eif.getSei().getName().equals(className)){
                //this is the one
                return eif;
            }
        }
        return null;
    }

    public HashSet<EndpointIFContext> getEndpointIFContext() {
            return seiContext;
        }

    public void addEndpointIFContext(EndpointIFContext eifContext) {
        this.seiContext.add(eifContext);
    }

     public void addEndpointIFContext(List<EndpointIFContext> eifContexts) {
        this.seiContext.addAll(eifContexts);
    }

    public Class getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public QName getServiceName() {
        if (serviceName != null)
            return serviceName;
        if (wsdlContext != null){
            serviceName = wsdlContext.getFirstServiceName();
            return serviceName;
        }
        return null;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }
}
class SIAnnotations {
    String tns;
    QName serviceQName;
    ArrayList<QName> portQNames = new ArrayList<QName>();
    final ArrayList<Class> classes = new ArrayList<Class>();
    String wsdlLocation;
}