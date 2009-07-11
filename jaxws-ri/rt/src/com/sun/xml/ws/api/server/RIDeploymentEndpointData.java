package com.sun.xml.ws.api.server;

import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.Description;

/**
 * @author Jitendra Kotamraju
 */
@ManagedData
@Description("sun-jaxws.xml deployed endpoint info")
public class RIDeploymentEndpointData extends EndpointData {

    private final WSEndpoint endpoint;

    public RIDeploymentEndpointData(WSEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @ManagedAttribute
    @Description("Target Namespace of the Web Service")
    public String getNamespace() {
        return endpoint.getServiceName().getNamespaceURI();
    }

    @ManagedAttribute
    @Description("Web Service name")
    public String getServiceName() {
        return endpoint.getServiceName().getLocalPart();
    }

    @ManagedAttribute
    @Description("Web Service port name")
    public String getPortName() {
        return endpoint.getPortName().getLocalPart();
    }

    @ManagedAttribute
    @Description("Service Implementation Class")
    public String getImplClass() {
        return endpoint.getImplementationClass().getName();
    }

    @ManagedAttribute
    @Description("sun-jaxws.xml file")
    public String getSunJaxwsXml() {
        return null;
    }

}