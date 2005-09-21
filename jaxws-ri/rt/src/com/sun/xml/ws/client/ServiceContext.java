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

import com.sun.xml.ws.handler.HandlerResolverImpl;
import com.sun.xml.ws.wsdl.WSDLContext;

import java.net.URI;
import java.util.ArrayList;
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
    private Set<URI> roles;
    
    private QName serviceName; //supplied on creation of service
    private SCAnnotations SCAnnotations;
    private final HashSet<EndpointIFContext> seiContext = new HashSet<EndpointIFContext>();
    /**
     * To be used to resolve WSDL resources.
     */
    private final EntityResolver entityResolver;

    public ServiceContext(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public SCAnnotations getSCAnnotations() {
        return SCAnnotations;
    }

    public void setSCAnnotations(SCAnnotations SCAnnotations) {
        this.SCAnnotations = SCAnnotations;
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
    
    public Set<URI> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<URI> roles) {
        this.roles = roles;
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
            ", SCAnnotations=" + SCAnnotations +
            ", seiContext=" + seiContext +
            ", entityResolver=" + entityResolver +
            "}";
    }
}
class SCAnnotations {
    String tns;
    QName serviceQName;
    ArrayList<QName> portQNames = new ArrayList<QName>();
    final ArrayList<Class> classes = new ArrayList<Class>();
    String wsdlLocation;
}