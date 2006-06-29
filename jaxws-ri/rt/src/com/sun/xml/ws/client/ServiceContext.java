/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.handler.HandlerResolverImpl;
import com.sun.xml.ws.wsdl.WSDLContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.xml.sax.EntityResolver;


/**
 * $author: WS Development Team
 */
public class ServiceContext {
    private WSDLContext wsdlContext; //from wsdlParsing
    
    private Class serviceClass;
    private HandlerResolverImpl handlerResolver;
    private QName serviceName; //supplied on creation of service
    private final HashSet<EndpointIFContext> seiContext = new HashSet<EndpointIFContext>();
    /**
     * To be used to resolve WSDL resources.
     */
    private final EntityResolver entityResolver;
    private HashMap<QName,Set<String>> rolesMap = new HashMap<QName,Set<String>>();
    public ServiceContext(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public ServiceContext(Class serviceClass, QName serviceName, EntityResolver entityResolver) {
        this.serviceClass = serviceClass;
        this.serviceName = serviceName;
        this.entityResolver = entityResolver;
    }

    public WSDLContext getWsdlContext() {
        return wsdlContext;
    }

    public void setWsdlContext(WSDLContext wsdlContext) {
        this.wsdlContext = wsdlContext;
    }

    public HandlerResolverImpl getHandlerResolver() {
        return handlerResolver;
    }

    public void setHandlerResolver(HandlerResolverImpl resolver) {
        this.handlerResolver = resolver;
    }
    
    public Set<String> getRoles(QName portName) {
        return rolesMap.get(portName);
    }
    
    public void setRoles(QName portName,Set<String> roles) {
        rolesMap.put(portName,roles);
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

    public Class getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class serviceClass) {
        this.serviceClass = serviceClass;
    }

    public QName getServiceName() {
        if (serviceName == null) {
            if (wsdlContext != null) {
                setServiceName(wsdlContext.getFirstServiceName());
            }
        }
        return serviceName;
    }

    public void setServiceName(QName serviceName) {
        assert(serviceName != null);
        this.serviceName = serviceName;
    }

    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    public String toString() {
        return "ServiceContext{" +
            "wsdlContext=" + wsdlContext +
            ", handleResolver=" + handlerResolver +
            ", serviceClass=" + serviceClass +
            ", serviceName=" + serviceName +
            ", seiContext=" + seiContext +
            ", entityResolver=" + entityResolver +
            "}";
    }
}
