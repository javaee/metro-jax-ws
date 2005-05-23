/*
 * $Id: ServiceFactoryImpl.java,v 1.1 2005-05-23 22:26:38 bbissett Exp $
 */

/*
 * Copyright (c) 2005 Sun Microsystems. All Rights Reserved.
 */
package com.sun.xml.ws.client;


import com.sun.xml.ws.server.RuntimeContext;

import javax.naming.Referenceable;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceFactory;
import javax.xml.ws.WebServiceException;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.URL;


/**
 * <p> A concrete factory for Service objects. </p>
 *
 * @author JAX-RPC Development Team
 */
public class ServiceFactoryImpl extends ServiceFactory {

    public static final String SERVICE_IMPL_NAME = "com.sun.xml.rpc.service.impl.name";

    //needed as a delegate
    public ServiceFactoryImpl() {
    }

    //this will change
    public Service createService(java.net.URL wsdlDocumentLocation, QName name)
        throws WebServiceException {
        //todo:add back once done
        //if (wsdlDocumentLocation == null) {
        //    throw new IllegalArgumentException("wsdlDocumentLocation must not be null");
        //}
        return new WebService(name, (Class) null, wsdlDocumentLocation);
    }

    public Service createService(QName name) throws WebServiceException {
        //only qname is known, a BasicService is returned
        // return new BasicService(name);
        return new WebService(name, null, null);
    }

    public WebServiceInterface createService(URL wsdlDocumentLocation, Class serviceInterface)
        throws WebServiceException {

        //check for null arguments
        //todo:add basck in when done
        //if (wsdlDocumentLocation == null) {
        //    throw new IllegalArgumentException("wsdlDocumentLocation must not be null");
        //}

        if (serviceInterface == null) {
            throw new IllegalArgumentException();
        }

        //check to make sure this is a service
        if (!Service.class.isAssignableFrom(serviceInterface)) {
            // throw (WebServiceException)new WebServiceException("service.interface.required",
            //     serviceInterface.getName());
        }

        return (WebServiceInterface) bootStrap(serviceInterface, wsdlDocumentLocation);

    }

    public Service createService(Class serviceInterface)
        throws Exception {
        if (serviceInterface == null) {
            throw new IllegalArgumentException();
        }

        //load a service given the service interface
        if (!Service.class.isAssignableFrom(serviceInterface)) {
            throw new Exception("service.interface.required" +
                serviceInterface.getName());
        }

        return bootStrap(serviceInterface);
    }


    /* public Service createService(Class serviceInterface, QName name)
         throws Exception {
         //create a service using the service qname and an interface
         if (!Service.class.isAssignableFrom(serviceInterface)) {
             throw new WebServiceExceptionImpl("service.interface.required",
                 serviceInterface.getName());
         }

         return bootStrap(serviceInterface);
     }

     private Service createService(String serviceImplementationName)
         throws Exception {
         if (serviceImplementationName == null) {
             throw new IllegalArgumentException();
         }

         //load the service implementation class using classloader.loadClass()
         try {
             Class serviceImplementationClass =
                 Thread.currentThread().getContextClassLoader().loadClass(serviceImplementationName);

             Class<?> clazz = serviceImplementationClass.getDeclaringClass();

             if (clazz != null) {
                 return loadService(clazz);
             } else
                 throw new WebServiceExceptionImpl("Unable to createService");

         } catch (ClassNotFoundException e) {
             throw new WebServiceExceptionImpl("service.implementation.not.found",
                 serviceImplementationName);
         }

     }

     public Service loadService(Class serviceInterface)
         throws Exception {
         if (serviceInterface == null) {
             throw new IllegalArgumentException();
         }

         //load a service given the service interface
         if (!Service.class.isAssignableFrom(serviceInterface)) {
             throw new Exception("service.interface.required" +
                 serviceInterface.getName());
         }

         return bootStrap(serviceInterface);
     }

     //the below will definately change
     public Service loadService(java.net.URL wsdlDocumentLocation,
                                Class serviceInterface,
                                Properties properties)
         throws Exception {


         //check for null arguments
         if (wsdlDocumentLocation == null) {
             throw new IllegalArgumentException("wsdlDocumentLocation must not be null");
         }

         if (serviceInterface == null) {
             throw new IllegalArgumentException();
         }

         //check to make sure this is a service
         if (!Service.class.isAssignableFrom(serviceInterface)) {
             throw new WebServiceExceptionImpl("service.interface.required",
                 serviceInterface.getName());
         }

         return bootStrap(serviceInterface, wsdlDocumentLocation);
     }

     //this will change
     public Service loadService(java.net.URL wsdlDocumentLocation,
                                QName serviceName,
                                Properties properties)
         throws Exception {

         //check for null arguments
         if (wsdlDocumentLocation == null) {
             throw new IllegalArgumentException("wsdlDocumentLocation must not be null");
         }

         //get the service implementation class name
         String serviceImplementationName = null;
         if (properties != null) {
             serviceImplementationName =
                 properties.getProperty(SERVICE_IMPL_NAME);
         }
         if (serviceImplementationName == null) {
             throw new IllegalArgumentException("Properties should contain the property:"
                 + SERVICE_IMPL_NAME);
         }
         //create the service
         Service service = createService(serviceImplementationName);

         ((WebService) Proxy.getInvocationHandler(service)).setWSDLLocation(wsdlDocumentLocation);
         //introspect
         if (service.getServiceName().equals(ServiceName)) {
             return service;
         } else {
             throw new WebServiceExceptionImpl("service.implementation.not.found",
                     serviceImplementationName);
         }

         //return service;
         return service;
     }

    */
    private WebServiceInterface createServiceProxy(RuntimeContext context, Class si, URL wsdlLocation) {
        ServiceInvocationHandler handler =
            new ServiceInvocationHandler(context, si, wsdlLocation);

        Service serviceProxy = (Service) Proxy.newProxyInstance(si.getClassLoader(),
            new Class[]{si, com.sun.xml.ws.client.WebServiceInterface.class, javax.xml.ws.Service.class, Serializable.class, Referenceable.class},
            handler);
        handler.setProxy((Proxy) serviceProxy);

        return (com.sun.xml.ws.client.WebServiceInterface) serviceProxy;
    }

    private WebServiceInterface bootStrap(Class si, URL wsdlDocumentLocation) throws WebServiceException {
        //todo: for now just create the proxy-
        //todo: process here or process on getPort

        //get Methods on si-should only be one
        //Method[] methods = si.getDeclaredMethods();

        //for (Method method : methods) {
        //    method.setAccessible(true);
        //}


        //if (methods.length == 1) {
        //    Class seiClazz = methods[0].getReturnType();
        //    if ((seiClazz != null) && (!seiClazz.equals("void"))) {

        // RuntimeAnnotationProcessor processor =
        //     new RuntimeAnnotationProcessor(null, seiClazz);
        //
        // RuntimeModel model = processor.buildRuntimeModel();
        // RuntimeContext rtContext = new RuntimeContext(model);

        //return createServiceProxy(rtContext, si);
        return (WebServiceInterface) createServiceProxy(null, si, wsdlDocumentLocation);
        // } else throw new ServiceException(new IllegalArgumentException("No sei declared in service interface"));
        //} //is this a valid assumption?
        //return null;
    }

    private WebServiceInterface bootStrap(Class si) throws WebServiceException {
        return bootStrap(si, null);
    }


    public String toString() {
        return "ServiceFactoryImpl{}";
    }
}
