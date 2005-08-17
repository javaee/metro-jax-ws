/*
 * $Id: ServiceFactoryImpl.java,v 1.7 2005-08-17 22:29:47 kohsuke Exp $
 */
/*
 * Copyright (c) 2005 Sun Microsystems. All Rights Reserved.
 */
package com.sun.xml.ws.client;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.EntityResolver;

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


/**
 * <p> A concrete factory for Service objects. </p>
 *
 * @author WS Development Team
 */
public class ServiceFactoryImpl extends ServiceFactory {

    /**
     * {@link CatalogResolver} to check META-INF/jaxws-catalog.xml.
     * Lazily created.
     */
    private EntityResolver resolver;

    public Service createService(URL wsdlDocumentLocation, QName name)
        throws WebServiceException {
        if (name == null)
            throw new WebServiceException("QName for the service must not be null");
        ServiceContext serviceContext = ServiceContextBuilder.build(wsdlDocumentLocation,null,getResolver());

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

        ServiceContext serviceContext = new ServiceContext(getResolver());
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
                wsdlDocumentLocation, serviceInterface, getResolver());

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

    /**
     * Gets the resolver that this {@link ServiceFactory} uses before
     * accessing remote WSDLs.
     */
    public EntityResolver getResolver() {
        if(resolver!=null) {
            // set up a manager
            CatalogManager manager = new CatalogManager();
            manager.setIgnoreMissingProperties(true);
            try {
                if(System.getProperty(getClass().getName()+".verbose")!=null)
                    manager.setVerbosity(999);
            } catch (SecurityException e) {
                // recover by not setting the debug flag.
            }

            // parse the catalog
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> catalogEnum;
            try {
                if(cl==null)
                    catalogEnum = ClassLoader.getSystemResources("/META-INF/jaxws-catalog.xml");
                else
                    catalogEnum = cl.getResources("/META-INF/jaxws-catalog.xml");

                while(catalogEnum.hasMoreElements()) {
                    URL url = catalogEnum.nextElement();
                    manager.getCatalog().parseCatalog(url);
                }
            } catch (IOException e) {
                throw new WebServiceException(e);
            }

            resolver = new CatalogResolver(manager);
        }

        return resolver;
    }

    /**
     * Overrides the resolver that this {@link ServiceFactoryImpl} uses.
     * To disable the catalog resolution, set a dummy entity resolver that
     * always return null.
     */
    public void setResolver(EntityResolver resolver) {
        this.resolver = resolver;
    }

    public String toString() {
        return "ServiceFactoryImpl{}";
    }
}
