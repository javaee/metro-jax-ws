/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.modeler.RuntimeModeler;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.util.HandlerAnnotationInfo;
import com.sun.xml.ws.util.HandlerAnnotationProcessor;
import com.sun.xml.ws.wsdl.WSDLContext;
import com.sun.xml.ws.wsdl.parser.WSDLParser;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import com.sun.xml.ws.wsdl.parser.WSDLDocument;
import com.sun.xml.ws.wsdl.parser.Binding;
import com.sun.java_cup.internal.parser;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import java.io.BufferedInputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * $author: WS Development Team
 */
public class ServiceContextBuilder {

    ServiceContext serviceContext;

    //parses WSDL for service, Ports, endpoint, binding
    //returns wsdlcontext

    //nedd sei for next2 steps
    //runs handlerAnnotationProcessor
    //returns handlerregistry

    //runs RuntimeAnnotationProcessor
    //returns runtime model


    public ServiceContext buildServiceContext(URL wsdlLocation, Class si, QName serviceName) throws WebServiceException {

        serviceContext = new ServiceContext(wsdlLocation, si, serviceName);
        SIAnnotations serviceIFAnnotations;
        if (si != null) {

            serviceIFAnnotations = getSIAnnotations(si);
            if (serviceIFAnnotations == null )
                throw new WebServiceException("Service Interface Annotations required, exiting...");
            else serviceContext.setSiAnnotations(serviceIFAnnotations);
            
            if(wsdlLocation == null)
                try {
                    wsdlLocation = new URL(serviceIFAnnotations.wsdlLocation);
                } catch (MalformedURLException e) {
                    throw new WebServiceException(e);
                }
            serviceContext.setWsdlContext(parseWSDL(wsdlLocation));

            if (si != null) {
                serviceContext.setServiceInterface(si);
                ArrayList<Class> classez = serviceIFAnnotations.classes;
                if (classez != null) {
                    for (Class clazz : classez) {
                        processAnnotations(clazz);
                    }
                }
            }
        }
        return serviceContext;
    }

    public ServiceContext completeServiceContext(ServiceContext serviceContext, QName portName, Class portInterface) {
        URL wsdlLocation = null;
        if (serviceContext.getWsdlContext() == null) {
            try {
                wsdlLocation = getWSDLLocation(portInterface);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            serviceContext.setWsdlContext(parseWSDL(wsdlLocation));
        }

        //if ((serviceContext.getRuntimeContext() == null) && (portInterface != null)) {
        //    processAnnotations(portInterface);
        //}
        return serviceContext;
    }

    //does any necessagy checking and validation
    public WSDLContext parseWSDL(URL wsdlDocumentLocation) {
        //must get binding information

        if (wsdlDocumentLocation == null)
            throw new WebServiceException("No WSDL location Information present, error");

        //WSDLParser parser = new WSDLParser();
        getWSDLContext().setOrigWSDLLocation(wsdlDocumentLocation);
        try {
            //return parser.parse(new BufferedInputStream(wsdlDocumentLocation.openStream()), getWSDLContext());
            WSDLDocument wsdlDoc = RuntimeWSDLParser.parse(wsdlDocumentLocation);
            WSDLContext wsdlContext = getWSDLContext();
            wsdlContext.setWSDLDocument(wsdlDoc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getWSDLContext();
    }

    //todo: valid port in wsdl
    private void processAnnotations(Class portInterface) throws WebServiceException {
        EndpointIFContext eifc = serviceContext.getEndpointIFContext(portInterface.getName());
        if ((eifc == null) || ((eifc != null) && (eifc.getRuntimeContext() == null))) {

            if (eifc == null) {
                eifc = new EndpointIFContext(portInterface);
                serviceContext.addEndpointIFContext(eifc);
            }

//            Binding binding = serviceContext.getWsdlContext().getWsdlBinding(serviceContext.getServiceName(),
//                                serviceContext.getWsdlContext().getPortName());



            RuntimeModeler processor = new RuntimeModeler(portInterface, serviceContext.getWsdlContext().getBindingID().toString());
//            RuntimeModeler processor = null;
//            if(binding != null)
//                processor = new RuntimeModeler(portInterface, binding);
//            else
//                processor = new RuntimeModeler(portInterface, serviceContext.getWsdlContext().getBindingID().toString());

            RuntimeModel model = processor.buildRuntimeModel();

            eifc.setRuntimeContext(new RuntimeContext(model));
            //serviceContext.addEndpointIFContext(eifc);

            // get handler information
            HandlerAnnotationInfo chainInfo =
                HandlerAnnotationProcessor.buildHandlerInfo(portInterface);
            if (chainInfo != null)
                eifc.setHandlers(chainInfo.getHandlers());

            if (serviceContext.getServiceName() == null)
                serviceContext.setServiceName(serviceContext.getWsdlContext().getFirstServiceName());

            if (chainInfo != null) {
                HandlerRegistryImpl registry = getHandlerRegistry(serviceContext.getServiceName());
                registry.setHandlerChain(chainInfo.getHandlers());
                serviceContext.setRegistry(registry);

                // todo: need a place to store role information to
                // place in binding
                //chainInfo.getRoles();
            }
        }

//        //todo: if changed reprocess wsdl- track this
//        if (wsdlLocation != null) {
//            wsdlContext = parseWSDL(wsdlLocation);
//        } else {
//            noWsdlException();
//        }
    }

    private HandlerRegistryImpl getHandlerRegistry(QName serviceName) {
        //need to return handlerRegistryImpl?
        if (serviceContext.getRegistry() == null) {
            Set knownPorts = serviceContext.getWsdlContext().getPortsAsSet(serviceName);
            HashSet portz = new HashSet(knownPorts.size());
            portz.addAll(knownPorts);
            serviceContext.setRegistry(
                new HandlerRegistryImpl(portz));
        }

        return (HandlerRegistryImpl) serviceContext.getRegistry();
    }

    private WSDLContext getWSDLContext() {
        if (serviceContext.getWsdlContext() == null)
            serviceContext.setWsdlContext(new WSDLContext());

        return serviceContext.getWsdlContext();
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
     * @param sei
     * @return the URL of the location of the WSDL for the sei.
     */
    //this will change
    public static URL getWSDLLocation(ArrayList<Class> seis) throws MalformedURLException {
        if (seis != null) {
            if (seis.size() > 0) {
                javax.jws.WebService ws = (WebService) seis.get(0).getAnnotation(WebService.class);
                if (ws == null)
                    return null;
                String wsdlLocation = ws.wsdlLocation();
                if (wsdlLocation == null)
                    return null;
                return new URL(wsdlLocation);
            }
        }
        return null;
    }

    //this will change
    public SIAnnotations getSIAnnotations(Class si) {
       
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

    public static URL getWSDLLocation(Class sei) throws MalformedURLException {
        if (sei != null) {
            ArrayList<Class> list = new ArrayList<Class>();
            list.add(sei);
            return getWSDLLocation(list);
        }
        return null;
    }


}
