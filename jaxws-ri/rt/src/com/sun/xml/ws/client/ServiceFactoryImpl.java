/*
 * $Id: ServiceFactoryImpl.java,v 1.5 2005-08-04 02:32:21 kwalsh Exp $
 */
/*
 * Copyright (c) 2005 Sun Microsystems. All Rights Reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.server.RuntimeContext;

import java.io.Serializable;

import java.lang.reflect.Proxy;

import java.net.URL;

import javax.naming.Referenceable;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceFactory;
import javax.xml.ws.WebServiceException;


/**
 * <p> A concrete factory for Service objects. </p>
 *
 * @author WS Development Team
 */
public class ServiceFactoryImpl extends ServiceFactory {
    public Service createService(java.net.URL wsdlDocumentLocation, QName name)
        throws WebServiceException {
        if (name == null)
            throw new WebServiceException("QName for the service must not be null");
        ServiceContextBuilder builder = new ServiceContextBuilder();
        ServiceContext serviceContext = builder.buildServiceContext(wsdlDocumentLocation,
                (Class) null, name);

        if (serviceContext.getWsdlContext().contains(name).size() > 1) {
            throw new WebServiceException(" Service " + name +
                " is not a valid service. Valid " + " Services are " +
                serviceContext.getWsdlContext().contains(name).toString());
        }
        return new WebService(serviceContext);
    }

    public Service createService(QName name) throws WebServiceException {
        if (name == null) {
            throw new WebServiceException(
                "QName for the service must not be null");
        }

        ServiceContext serviceContext = new ServiceContext(null, null, name);
        return new WebService(serviceContext);
    }

    public Service createService(URL wsdlDocumentLocation,
        Class serviceInterface) throws WebServiceException {
        return (Service) bootStrap(serviceInterface, wsdlDocumentLocation);
    }

    public Service createService(Class serviceInterface)
        throws WebServiceException {
        return createService(null, serviceInterface);
    }

    private WebServiceInterface createServiceProxy(
        ServiceContext serviceContext) {

        ServiceInvocationHandler handler = new ServiceInvocationHandler(serviceContext);
        Service serviceProxy = null;

        try {
            serviceProxy = (Service) Proxy.newProxyInstance(serviceContext.getServiceInterface()
                                                                          .getClassLoader(),
                    new Class[] {
                        serviceContext.getServiceInterface(),
                        com.sun.xml.ws.client.WebServiceInterface.class,
                        javax.xml.ws.Service.class, Serializable.class,
                        Referenceable.class
                    }, handler);
            handler.setProxy((Proxy) serviceProxy);
        } catch (Exception ex) {
            throw new WebServiceException(ex.getMessage(), ex);
        }

        if (serviceProxy == null) {
            throw new WebServiceException("Failed to create ServiceProxy.");
        }
        return (com.sun.xml.ws.client.WebServiceInterface) serviceProxy;
    }

    private WebServiceInterface bootStrap(Class si, URL wsdlDocumentLocation)
        throws WebServiceException {
        //todo: for now just create the proxy-
        //todo: process here or process on getPort
        ServiceContextBuilder serviceContextBuilder = new ServiceContextBuilder();

        if (si == null) {
            throw new WebServiceException();
        }

        //check to make sure this is a service
        if (!Service.class.isAssignableFrom(si)) {
            throw new WebServiceException("service.interface.required" +
                si.getName());
        }

        ServiceContext serviceContext = serviceContextBuilder.buildServiceContext(wsdlDocumentLocation,
                si, null);

        return (WebServiceInterface) createServiceProxy(serviceContext);
    }

    public String toString() {
        return "ServiceFactoryImpl{}";
    }
}
