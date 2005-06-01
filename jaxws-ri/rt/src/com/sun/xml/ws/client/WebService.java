/*
 * $Id: WebService.java,v 1.4 2005-06-01 00:12:37 kohlert Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.client.dispatch.DispatchBase;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.modeler.RuntimeAnnotationProcessor;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.wsdl.WSDLContext;
import com.sun.xml.ws.wsdl.parser.WSDLParser;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.security.SecurityConfiguration;
import java.io.BufferedInputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author JAX-RPC Development Team
 */

public class WebService
    implements WebServiceInterface, Serializable, Referenceable {

    protected static final String GET = "get";
    protected static final String PORT = "Port";
    protected static final int EXCLUDE_LEN = 7; //get len + Port len
    protected static final int GET_LEN = 3;
    protected static final int PORT_LEN = 4;
    protected static final String DEFAULT_OPERATION_STYLE = "rpc";
    protected QName name;
    protected WSDLContext wsdlContext;
    protected HashSet<QName> ports;
    protected URL wsdlLocation;
    //todo:will take out dispatch ports once bindingId
    //endpoint, QName determined in ConfiguredService
    protected HashMap<QName, PortInfoBase> dispatchPorts;

    protected HandlerRegistryImpl handlerRegistry;
    protected Class si;
    protected Object serviceProxy;


    private RuntimeContext rtContext;

    public void setWSDLLocation(URL location) {
        wsdlLocation = location;
    }

    protected void init(QName name, Class si) {
        this.name = name;
        this.si = si;
        this.ports = new HashSet<QName>();
        this.dispatchPorts = new HashMap();
    }

    //do we need this? //yes for proxy
    public WebService(RuntimeContext context, Class sinterface, URL wsdlDocumentLocation) {
        rtContext = context;
        wsdlLocation = wsdlDocumentLocation;
        init(null, sinterface);
    }

    public WebService(QName name, Class si) {
        init(name, si);
    }

    public WebService(QName name, Class si, URL wsdlDocumentLocation) {
        init(name, si);
        wsdlLocation = wsdlDocumentLocation;
    }

    private WSDLContext getWSDLContext() {
        if (wsdlContext == null)
            wsdlContext = new WSDLContext();
        return wsdlContext;
    }


    public WSDLContext parseWSDL(URL wsdlDocumentLocation) {
        //must get binding information
        WSDLParser parser = new WSDLParser();
        getWSDLContext().setOrigWSDLLocation(wsdlDocumentLocation);
        try {
            //return parser.parse(new BufferedInputStream(wsdlDocumentLocation.openStream()), getWSDLContext());
            return parser.parse(new BufferedInputStream(wsdlDocumentLocation.openStream()), getWSDLContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void preProcess(QName portName, Class portInterface) throws WebServiceException, MalformedURLException {

        if (rtContext == null) {
            RuntimeAnnotationProcessor processor =
                new RuntimeAnnotationProcessor(portName, portInterface, false);

            RuntimeModel model = processor.buildRuntimeModel();
            com.sun.xml.ws.wsdl.writer.WSDLGenerator wsdlGen = new com.sun.xml.ws.wsdl.writer.WSDLGenerator(model);
            if (wsdlLocation == null)
                wsdlLocation = new URL(model.getWSDLLocation());
            rtContext = new RuntimeContext(model);
        }

        //todo: if changed reprocess wsdl- track this
        if (wsdlLocation != null) {
            wsdlContext = parseWSDL(wsdlLocation);
        } else {
            noWsdlException();
        }

    }

    protected void addPorts(QName[] ports) {
        if (ports != null) {
            for (int i = 0; i < ports.length; ++i) {
                addPort(ports[i]);
            }
        }
    }

    public WebService(QName name, Iterator<QName> eachPort) {
        this.name = name;
        while (eachPort.hasNext()) {
            addPort(eachPort.next());
        }
    }

    protected void addPort(QName port) {
        ports.add(port);
        if (handlerRegistry != null) {
            handlerRegistry.addPort(port);
        }
    }

    protected WebServiceException noWsdlException() {
        return new WebServiceException("dii.service.no.wsdl.available");
    }

    public Object getPort(QName portName, Class portInterface)
        throws WebServiceException {

        if (portName != null) {
            addPort(portName);
        }

        return createEndpointIFBaseProxy(portName, portInterface);
    }

    private Object createEndpointIFBaseProxy(QName portName, Class portInterface) throws WebServiceException {

        try {
            preProcess(portName, portInterface);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        EndpointIFProxyBuilder builder = new EndpointIFProxyBuilder(getHandlerRegistry(), rtContext, wsdlContext);
        return builder.buildEndpointIFProxy(portName, portInterface);
    }

    public Object getPort(Class portInterface) throws WebServiceException {
        return createEndpointIFBaseProxy(null, portInterface);
    }

    //todo: rename addPort :spec tbd
    public void createPort(QName portName, URI bindingId, String endpointAddress) throws WebServiceException {
        //doesn't work for local transport
        //if (!isEndpointValidForBinding(bindingId, endpointAddress)) {
        //    throw new ServiceException("Endpoint address not valid for binding id " + bindingId.toString());
        //} else {
        if (!dispatchPorts.containsKey(portName)) {
            PortInfoBase info = new PortInfoBase(portName);
            info.setTargetEndpoint(endpointAddress);
            info.setBindingId(bindingId);
            //take out dispatch ports temp
            dispatchPorts.put(portName, info);
        } else
            throw new WebServiceException("Port " + portName.toString() + " already exists can not create a port with the same name.");

        // need to add port to list for HandlerRegistry
        addPort(portName);
        //}
    }

    public <T> Dispatch<T> createDispatch(QName qName, Class<T> aClass, Mode mode) throws WebServiceException {
        return createDispatchClazz(qName, aClass, mode);
    }

    public Dispatch<Object> createDispatch(QName qName, JAXBContext jaxbContext, Mode mode) throws WebServiceException {
        return createDispatchJAXB(qName, jaxbContext, mode);
    }


    public QName getServiceName() {
        return name;
    }

    protected HashSet<QName> getPortsAsSet() {
        return ports;
    }

    public Iterator getPorts() throws WebServiceException {
        if (ports.size() == 0)
            throw noWsdlException();
        return ports.iterator();

    }

    public java.net.URL getWSDLDocumentLocation() {
        return wsdlLocation;
    }


    public SecurityConfiguration getSecurityConfiguration() {
        throw new UnsupportedOperationException("Security is not implemented for JAXWS 2.0 Early Access.");
        // return null;
    }

    public HandlerRegistryImpl getHandlerRegistry() {
        //need to return handlerRegistryImpl?
        if (handlerRegistry == null) {
            handlerRegistry = new HandlerRegistryImpl(getPortsAsSet());
        }

        return (HandlerRegistryImpl) handlerRegistry;
    }


    /*
     * Called by generated service subclass in constructor to create
     * handler registry. This version should be used by 2.X services.
     */
    // protected void addHandler20(HandlerInfo info) {
    //     //getHandlerRegistry().addHandler20(info);
    // }

    /*
     * Called by generated service subclass in constructor to create
     * handler registry.
     */
    // public void addHandler(QName portName, HandlerInfo info) {
    //may be able to take out
    //getHandlerRegistry().addHandler(portName, info);
    // }

    /*
     * Called by generated service subclass in constructor to create
     * handler registry. The String that is passed in should already
     * have been validated at generation time, but need the try/catch
     * even though the exception should never happen. Moved the URI
     * creation here to keep it out of the generated code.
     */
    //public void addHandler(String bindingId, HandlerInfo info) {
    //    try {
    //        URI bindingIdURI = new URI(bindingId);
    //getHandlerRegistry().addHandler(bindingIdURI, info);
    //  } catch (java.net.URISyntaxException e) {
    // should not happen
    //     throw new RuntimeException(e);
    // }
    //}

    /*
     * Set the binding on the binding provider. Called by the service
     * class when creating the binding provider.
     */
    protected void setBindingOnProvider(InternalBindingProvider provider,
                                        QName portName, URI bindingId) {

        provider._setBinding(getHandlerRegistry().createBinding(portName, bindingId));
    }

    //todo: dispatch ports can be reused-
    //must have way to clear all except binding in dispatch
    private Dispatch createDispatchClazz(QName port, Class clazz, Mode mode) throws WebServiceException {
        PortInfoBase dispatchPort = dispatchPorts.get(port);
        if (dispatchPort != null) {
            DispatchBase dBase = new DispatchBase((PortInfoBase) dispatchPort, clazz, mode);
            setBindingOnProvider(dBase, port, dBase._getBindingId());
            return dBase;
        } else {
            throw new WebServiceException("Port must be defined in order to create Dispatch");
        }
    }

    private Dispatch createDispatchJAXB(QName port, JAXBContext jaxbContext, Mode mode) throws WebServiceException {
        PortInfoBase dispatchPort = dispatchPorts.get(port);
        if (dispatchPort != null) {
            DispatchBase dBase = new DispatchBase((PortInfoBase) dispatchPort, jaxbContext, mode);
            setBindingOnProvider(dBase, port, dBase._getBindingId());
            return dBase;
        } else {
            throw new WebServiceException("Port must be defined in order to create Dispatch");
        }
    }

    public Reference getReference() throws NamingException {
        Reference reference =
            new Reference(getClass().getName(),
                "com.sun.xml.rpc.naming.ServiceReferenceResolver",
                null);
        String serviceName = ServiceReferenceResolver.registerService(this);
        reference.add(new StringRefAddr("ServiceName", serviceName));
        return reference;
    }

    boolean isEndpointValidForBinding(URI bindingId, String endpoint) {
        if (bindingId.toString().equals("http://schemas.xmlsoap.org/wsdl/soap/http")) {
            if (endpoint.startsWith("http:") || endpoint.startsWith("file:"))
                return true;
        }
        return false;
    }

}
