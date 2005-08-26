/*
 * $Id: ServiceFactoryImpl.java,v 1.9 2005-08-26 00:12:26 arungupta Exp $
 */
/*
 * Copyright (c) 2005 Sun Microsystems. All Rights Reserved.
 */
package com.sun.xml.ws.client;

import javax.naming.Referenceable;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceFactory;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Enumeration;

import com.sun.xml.ws.util.xml.XmlUtil;



/**
 * <p> A concrete factory for Service objects. </p>
 *
 * @author WS Development Team
 */
public class ServiceFactoryImpl extends ServiceFactory {

    public Service createService(URL wsdlDocumentLocation, QName name)
        throws WebServiceException {
        if (name == null)
            throw new WebServiceException("QName for the service must not be null");
        ServiceContext serviceContext = ServiceContextBuilder.build(wsdlDocumentLocation,null,XmlUtil.createDefaultCatalogResolver());

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

        ServiceContext serviceContext = new ServiceContext(XmlUtil.createDefaultCatalogResolver());
        return new WebService(serviceContext);
    }

    public Service createService(URL wsdlDocumentLocation,
        Class serviceInterface) throws WebServiceException {
        //todo: for now just create the proxy-
        //todo: process here or process on getPort

        if (serviceInterface == null) {
            throw new WebServiceException();
        }

        //check to make sure this is a service
        if (!Service.class.isAssignableFrom(serviceInterface)) {
            throw new WebServiceException("service.interface.required" +
                serviceInterface.getName());
        }

        ServiceContext serviceContext = ServiceContextBuilder.build(
                wsdlDocumentLocation, serviceInterface, XmlUtil.createDefaultCatalogResolver());

        return createServiceProxy(serviceContext);
    }

    public Service createService(Class serviceInterface)
        throws WebServiceException {
        return createService(null, serviceInterface);
    }

    private WebServiceInterface createServiceProxy(
        ServiceContext serviceContext) {

        ServiceInvocationHandler handler = new ServiceInvocationHandler(serviceContext);

        try {
            WebServiceInterface serviceProxy = (WebServiceInterface) Proxy.newProxyInstance(
                serviceContext.getServiceInterface().getClassLoader(),
                    new Class[] {
                        serviceContext.getServiceInterface(),
                        com.sun.xml.ws.client.WebServiceInterface.class,
                        javax.xml.ws.Service.class, Serializable.class,
                        Referenceable.class
                    }, handler);
            handler.setProxy((Proxy) serviceProxy);
            return serviceProxy;
        } catch (Exception ex) {
            throw new WebServiceException(ex.getMessage(), ex);
        }
    }

    public String toString() {
        return "ServiceFactoryImpl{}";
    }
}
