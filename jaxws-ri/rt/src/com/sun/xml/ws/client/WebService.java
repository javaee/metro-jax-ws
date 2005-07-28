/*
 * $Id: WebService.java,v 1.16 2005-07-28 21:05:00 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.client.dispatch.DispatchBase;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.security.SecurityConfiguration;
import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * @author WS Development Team
 */
/**
 *  <code>Service</code> objects provide the client view of a Web service.
 *  <p><code>Service</code> acts as a factory of the following:
 *  <ul>
 *  <li>Proxies for a target service endpoint.
 *  <li>Instances of <code>javax.xml.ws.Dispatch</code> for
 *      dynamic message-oriented invocation of a remote
 *      operation.
 *  </li>
 *
 * <p>The ports available on a service can be enumerated using the
 * <code>getPorts</code> method. Alternatively, you can pass a
 * service endpoint interface to the unary <code>getPort</code> method
 * and let the runtime select a compatible port.
 *
 * <p>Handler chains for all the objects created by a <code>Service</code>
 * can be set by means of the provided <code>HandlerRegistry</code>.
 *
 * <p>An <code>Executor</code> may be set on the service in order
 * to gain better control over the threads used to dispatch asynchronous
 * callbacks. For instance, thread pooling with certain parameters
 * can be enabled by creating a <code>ThreadPoolExecutor</code> and
 * registering it with the service.
 *
 *  @since JAX-WS 2.0
 *
 *  @see javax.xml.ws.ServiceFactory
 *  @see javax.xml.ws.handler.HandlerRegistry
 *  @see java.util.concurrent.Executor
**/
public class WebService
    implements WebServiceInterface, Serializable, Referenceable {

    protected static final String GET = "get";

    protected HashSet<QName> ports;

    protected HashMap<QName, PortInfoBase> dispatchPorts;
    protected HandlerRegistryImpl handlerRegistry;

    protected Object serviceProxy;
    protected URL wsdlLocation;
    protected ServiceContext serviceContext;
    protected Executor executor;
    private Object seiProxy;

    public WebService(ServiceContext scontext) {
        serviceContext = scontext;
        this.dispatchPorts = new HashMap();
    }

    private void processServiceContext(QName portName, Class portInterface) throws WebServiceException {
        ServiceContextBuilder builder = new ServiceContextBuilder();
        serviceContext = builder.completeServiceContext(serviceContext, portName, portInterface);
    }

    public URL getWSDLLocation() {
        if (wsdlLocation == null)
            setWSDLLocation(getWsdlLocation());
        return wsdlLocation;
    }

    public void setWSDLLocation(URL location) {
        wsdlLocation = location;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        executor = this.executor;
    }

    public Object getPort(QName portName, Class portInterface)
        throws WebServiceException {
        seiProxy = createEndpointIFBaseProxy(portName, portInterface);
        if (portName != null) {
            addPort(portName);
        }

        return seiProxy;
    }

    public Object getPort(Class portInterface) throws WebServiceException {
        return createEndpointIFBaseProxy(null, portInterface);
    }

    //todo: rename addPort :spec tbd
    public void addPort(QName portName, URI bindingId, String endpointAddress) throws WebServiceException {

        if (!dispatchPorts.containsKey(portName)) {
            dispatchPorts.put(portName, new PortInfoBase(endpointAddress, portName, bindingId));
        } else
            throw new WebServiceException("Port " + portName.toString() + " already exists can not create a port with the same name.");
        // need to add port to list for HandlerRegistry
        addPort(portName);
    }

    public <T> Dispatch<T> createDispatch(QName qName, Class<T> aClass, Mode mode) throws WebServiceException {
        return createDispatchClazz(qName, aClass, mode);
    }

    public Dispatch<Object> createDispatch(QName qName, JAXBContext jaxbContext, Mode mode) throws WebServiceException {
        return createDispatchJAXB(qName, jaxbContext, mode);
    }

    public QName getServiceName() {
        return serviceContext.getServiceName();
    }

    public Iterator getPorts() throws WebServiceException {
        if (ports == null)
            populatePorts();

        if (ports.size() == 0)
            throw noWsdlException();
        return ports.iterator();
    }

    public java.net.URL getWSDLDocumentLocation() {
        return getWsdlLocation();
    }

    public SecurityConfiguration getSecurityConfiguration() {
        throw new UnsupportedOperationException("Security is not implemented for JAXWS 2.0 Early Access.");
    }

    public HandlerRegistryImpl getHandlerRegistry() {
        //need to return handlerRegistryImpl?
        if (handlerRegistry == null) {
            if (serviceContext.getRegistry() != null)
                handlerRegistry = serviceContext.getRegistry();
            else {
                handlerRegistry = new HandlerRegistryImpl(getPortsAsSet());
            }
        }

        return (HandlerRegistryImpl) handlerRegistry;
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


    protected void addPorts(QName[] ports) {
        if (ports != null) {
            for (int i = 0; i < ports.length; ++i) {
                addPort(ports[i]);
            }
        }
    }

    private void populatePorts() {
        if (ports == null)
            ports = new HashSet<QName>();

        if (serviceContext.getServiceName() == null) {
            if (serviceContext.getWsdlContext() != null) {
                serviceContext.setServiceName(serviceContext.getWsdlContext().getFirstServiceName());
            }
        }
        Set knownPorts = null;

        if (serviceContext.getWsdlContext() != null) {
            knownPorts =
                serviceContext.getWsdlContext().getPortsAsSet(serviceContext.getServiceName());
            if (knownPorts != null) {
                QName[] portz = (QName[]) knownPorts.toArray(new QName[knownPorts.size()]);
                addPorts(portz);
            }
        }
    }

    protected void addPort(QName port) {
        if (ports == null)
            populatePorts();

        ports.add(port);
        if (handlerRegistry != null) {
            handlerRegistry.addPort(port);
        }
    }

    protected WebServiceException noWsdlException() {
        return new WebServiceException("dii.service.no.wsdl.available");
    }

    private Object createEndpointIFBaseProxy(QName portName, Class portInterface) throws WebServiceException {

        processServiceContext(portName, portInterface);

        if (serviceContext.getWsdlContext().contains(serviceContext.getServiceName(), portName).size() < 1) {
            throw new WebServiceException("Port " + portName + "is not found in service " + serviceContext.getServiceName());
        }

        return buildEndpointIFProxy(portName, portInterface);
    }

    protected HashSet<QName> getPortsAsSet() {
        if (ports == null)
            populatePorts();
        return ports;
    }

    /*
     * Set the binding on the binding provider. Called by the service
     * class when creating the binding provider.
     */
    protected void setBindingOnProvider(InternalBindingProvider provider,
                                        QName portName, URI bindingId) {
        provider._setBinding(getHandlerRegistry().createBinding(portName, bindingId));
    }


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

    private URL getWsdlLocation() {
        return serviceContext.getWsdlContext().getWsdlLocation();
    }

    private Object buildEndpointIFProxy(QName portQName, Class portInterface)
        throws WebServiceException {
        EndpointIFInvocationHandler handler = new EndpointIFInvocationHandler(portInterface,
            serviceContext.getRuntimeContext(), serviceContext.getWsdlContext(), getServiceName()); //need handler registry passed in here
        setBindingOnProvider(handler, portQName, handler._getBindingId());

        Object proxy = Proxy.newProxyInstance(portInterface.getClassLoader(),
            new Class[]{
                portInterface, Remote.class, BindingProvider.class,
                BindingProviderProperties.class, AnnotatedElement.class,
                com.sun.xml.ws.spi.runtime.StubBase.class
            }, handler);
        handler.setProxy((Object) proxy);
        return (BindingProvider) proxy;
    }
}
