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

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
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
        if ((wsdlLocation == null) && (si != null))
            try {
                wsdlLocation = getWSDLLocation(getSEI(si));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        serviceContext.setWsdlContext(parseWSDL(wsdlLocation));

        if (si != null) {
            serviceContext.setServiceInterface(si);
            ArrayList<Class> classez = getSEI(si);
            if (classez != null){
                for (Class clazz: classez){
                    processAnnotations(clazz);
                }
            //processAnnotations();
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

        WSDLParser parser = new WSDLParser();
        getWSDLContext().setOrigWSDLLocation(wsdlDocumentLocation);
        try {
            return parser.parse(new BufferedInputStream(wsdlDocumentLocation.openStream()), getWSDLContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getWSDLContext();
    }

    //todo: valid port in wsdl
    private void processAnnotations(Class portInterface) throws WebServiceException {
        EndpointIFContext eifc = serviceContext.getEndpointIFContext(portInterface.getName());
        if ( (eifc == null) || ((eifc != null) && (eifc.getRuntimeContext()== null))){

            if (eifc == null) {
                eifc = new EndpointIFContext(portInterface);
                serviceContext.addEndpointIFContext(eifc);
            }
            RuntimeModeler processor =
                new RuntimeModeler(portInterface, serviceContext.getWsdlContext().getBindingID().toString());

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
        ArrayList classes = new ArrayList(methods.length);
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

    public static URL getWSDLLocation(Class sei) throws MalformedURLException {
        if (sei != null) {
            ArrayList<Class> list= new ArrayList();
            list.add(sei);
            return getWSDLLocation(list);
        }
        return null;
    }

}
