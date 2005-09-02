/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.handler.HandlerResolverImpl;
import com.sun.xml.ws.handler.PortInfoImpl;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.modeler.RuntimeModeler;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.util.HandlerAnnotationInfo;
import com.sun.xml.ws.util.HandlerAnnotationProcessor;
import com.sun.xml.ws.util.JAXWSUtils;
import com.sun.xml.ws.wsdl.WSDLContext;
import org.xml.sax.EntityResolver;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.EntityResolver;

/**
 * $author: WS Development Team
 */
public abstract class ServiceContextBuilder {
    private ServiceContextBuilder() {}  // no instantication please

    //parses WSDL for service, Ports, endpoint, binding
    //returns wsdlcontext

    //nedd sei for next2 steps
    //runs handlerAnnotationProcessor

    //runs RuntimeAnnotationProcessor
    //returns runtime model

    /**
     * Creates a new {@link ServiceContext}.
     */
    public static ServiceContext build(URL wsdlLocation, Class service, EntityResolver er) throws WebServiceException {

        ServiceContext serviceContext = new ServiceContext(er);
        SIAnnotations serviceIFAnnotations;
        if ((service != null)) {

            serviceIFAnnotations = getSIAnnotations(service);
            if ((serviceIFAnnotations == null) && (service != javax.xml.ws.Service.class))
                throw new WebServiceException("Service Interface Annotations required, exiting...");
            serviceContext.setSiAnnotations(serviceIFAnnotations);

            if (wsdlLocation == null) {
                try {
                    //wsdlLocation = new URL(serviceIFAnnotations.wsdlLocation);
                    JAXWSUtils.getFileOrURLName(serviceIFAnnotations.wsdlLocation);

                    wsdlLocation = new URL(JAXWSUtils.getFileOrURLName(serviceIFAnnotations.wsdlLocation));
                } catch (MalformedURLException e) {
                    throw new WebServiceException(e);
                }
            }

            serviceContext.setWsdlContext(new WSDLContext(wsdlLocation, er));

            if (serviceIFAnnotations != null) {
                serviceContext.setServiceInterface(service);
                for (Class clazz : serviceIFAnnotations.classes) {
                    processAnnotations(serviceContext, clazz);
                }
            }
        }
        return serviceContext;
    }

    public static void completeServiceContext(ServiceContext serviceContext, Class portInterface) {
        if (serviceContext.getWsdlContext() == null) {
            URL wsdlLocation = null;
            try {
                wsdlLocation = getWSDLLocation(portInterface);
            } catch (MalformedURLException e) {
                new WebServiceException(e);
            }

            serviceContext.setWsdlContext(new WSDLContext(wsdlLocation, serviceContext.getEntityResolver()));
        }
    }

    private static QName getServiceName(Class serviceInterface) {
        WebServiceClient wsClient = (WebServiceClient) serviceInterface.getAnnotation(WebServiceClient.class);
        QName serviceName = null;
        if (wsClient != null) {
            String name = wsClient.name();
            String namespace = wsClient.targetNamespace();
            serviceName = new QName(namespace, name);
        }
        return serviceName;
    }

    private static QName getPortName(Class portInterface, Class serviceInterface) {
        QName portName = null;
        WebServiceClient wsClient = (WebServiceClient) serviceInterface.getAnnotation(WebServiceClient.class);
        for (Method method : serviceInterface.getMethods()) {
            if (!method.getDeclaringClass().equals(serviceInterface))
                continue;
            WebEndpoint webEndpoint = method.getAnnotation(WebEndpoint.class);
            if (webEndpoint == null) {
                continue;
            if (method.getGenericReturnType().equals(portInterface)) {
                if (method.getName().startsWith("get")) {
                    portName = new QName(wsClient.targetNamespace(), webEndpoint.name());
                    break;
                }
            }
        }
        return portName;
    }

    //does any necessagy checking and validation

    //todo: valid port in wsdl
    private static void processAnnotations(ServiceContext serviceContext, Class portInterface) throws WebServiceException {
        EndpointIFContext eifc = serviceContext.getEndpointIFContext(portInterface.getName());
        if ((eifc == null) || (eifc.getRuntimeContext() == null)) {

            if (eifc == null) {
                eifc = new EndpointIFContext(portInterface);
                serviceContext.addEndpointIFContext(eifc);
            }

            //toDo:
            QName serviceName = getServiceName(serviceContext.getServiceInterface());
            QName portName = getPortName(portInterface, serviceContext.getServiceInterface());
            //todo:Use SI ANNotations and put in map
            RuntimeModeler modeler = new RuntimeModeler(portInterface, serviceName,
                serviceContext.getWsdlContext().getBindingID().toString());
            modeler.setPortName(portName);
            RuntimeModel model = modeler.buildRuntimeModel();

            eifc.setRuntimeContext(new RuntimeContext(model));
            //serviceContext.addEndpointIFContext(eifc);

            // get handler information
            HandlerAnnotationInfo chainInfo =
                HandlerAnnotationProcessor.buildHandlerInfo(portInterface);

            if (serviceContext.getServiceName() == null)
                serviceContext.setServiceName(serviceContext.getWsdlContext().getFirstServiceName());

            if (chainInfo != null) {
                HandlerResolverImpl resolver =
                    getHandlerResolver(serviceContext);
                resolver.setHandlerChain(new PortInfoImpl(
                    modeler.getBindingId(),
                    model.getPortName(),
                    model.getServiceQName()),
                    chainInfo.getHandlers());
                serviceContext.setResolver(resolver);

                // todo: need a place to store role information to
                // place in binding
                //chainInfo.getRoles();
            }
        }
    }

    private static HandlerResolverImpl getHandlerResolver(
        ServiceContext serviceContext) {
        if (serviceContext.getResolver() == null) {
            serviceContext.setResolver(new HandlerResolverImpl());
        }
        return serviceContext.getResolver();
    }

    private ArrayList<Class> getSEI(Class si) {

        if (si == null) {
            throw new WebServiceException();
        }

        //check to make sure this is a service
        if (!Service.class.isAssignableFrom(si)) {
            throw new WebServiceException("service.interface.required" +
                si.getName());
        }

        Method[] methods = si.getDeclaredMethods();
        ArrayList<Class> classes = new ArrayList<Class>(methods.length);
        for (Method method : methods) {
            method.setAccessible(true);
            Class seiClazz = method.getReturnType();
            if ((seiClazz != null) && (!seiClazz.equals("void"))) {
                classes.add(seiClazz);
            }
        }

        return classes;
    }

    /**
     * Utility method to get wsdlLocation attribute from @WebService annotation on sei.
     *
     * @return the URL of the location of the WSDL for the sei, or null if none was found.
     */
    //this will change
    private static URL getWSDLLocation(Class<?> sei) throws MalformedURLException {
        WebService ws = sei.getAnnotation(WebService.class);
        if (ws == null)
            return null;
        return new URL(ws.wsdlLocation());
    }

    //this will change
    private static SIAnnotations getSIAnnotations(Class si) {
       
        SIAnnotations siAnnotations = new SIAnnotations();
        ArrayList<QName> portQNames = new ArrayList<QName>();
        if (si != null) {
            WebServiceClient wsc = (WebServiceClient) si.getAnnotation(WebServiceClient.class);
            if (wsc != null) {
                String name = wsc.name();
                String tns = wsc.targetNamespace();
                siAnnotations.tns = tns;
                if (name != null)
                    siAnnotations.serviceQName = new QName(tns, name);
                siAnnotations.wsdlLocation = wsc.wsdlLocation();

                Method[] methods = si.getDeclaredMethods();
                if (methods != null) {
                    ArrayList<Class<?>> classes = new ArrayList<Class<?>>(methods.length);
                    for (Method method : methods) {
                        method.setAccessible(true);
                        WebEndpoint webEndpoint = method.getAnnotation(WebEndpoint.class);
                        if (webEndpoint != null) {
                            String endpointName = webEndpoint.name();
                            QName portQName = new QName(tns, endpointName);
                            portQNames.add(portQName);
                        }
                        Class<?> seiClazz = method.getReturnType();
                        if ((seiClazz != null) && (!seiClazz.equals("void"))) {
                            classes.add(seiClazz);
                        }
                    }
                    siAnnotations.portQNames.addAll(portQNames);
                    siAnnotations.classes.addAll(classes);
                }
            }
        }
        return siAnnotations;
    }
}
