package com.sun.xml.ws.api.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;

import javax.xml.namespace.QName;

/**
 * Captures information about a Endpoint
 */
public class WSEndpointInfo<T> {
    QName serviceName, portName;
    Class<T> implementationClass;
    Container container;
    WSDLPort wsdlPort;
    
    public WSEndpointInfo(QName serviceName,QName portName, Class<T> implementationClass, Container container, WSDLPort wsdlPort){
        this.serviceName = serviceName;
        this.portName = portName;
        this.container = container;
        this.implementationClass = implementationClass;
        this.wsdlPort = wsdlPort;
    }
    /**
     * Gets the application endpoint's serviceName. It could be got from DD or annotations
     *
     * @return same as wsdl:service QName if WSDL exists or generated
     */
    public @NotNull QName getServiceName(){
        return serviceName;
    }

    /**
     * Gets the application endpoint's portName. It could be got from DD or annotations
     *
     * @return same as wsdl:port QName if WSDL exists or generated
     */
    public @NotNull QName getPortName() {
        return portName;
    }

    /**
     * Gets the application endpoint {@link Class} that eventually serves the request.
     *
     */
    public @NotNull Class<T> getImplementationClass(){
        return implementationClass;
    }

    /**
     * Gets the {@link Container} object.
     *
     * <p>
     * The components inside {@link WSEndpoint} uses this reference
     * to communicate with the hosting environment.
     *
     * @return
     *      always same object. If no "real" {@link Container} instance
     *      is given, {@link Container#NONE} will be returned.
     */
    public @NotNull Container getContainer(){
        return container;
    }

    /**
     * Gets the port that this endpoint is serving.
     *
     * <p>
     * A service is not required to have a WSDL, and when it doesn't,
     * this method returns null. Otherwise it returns an object that
     * describes the port that this {@link WSEndpoint} is serving.
     *
     * @return
     *      Possibly null, but always the same value.
     */
    public @Nullable WSDLPort getPort(){
        return wsdlPort;
    }
}
