package com.sun.xml.ws.api.server;

import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.probe.provider.annotations.ProbeListener;
import org.glassfish.probe.provider.annotations.ProbeParam;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jitendra Kotamraju
 */
@ManagedObject
@Description("Stats for Web Services deployed using RI deployment")
public class RIDeploymentStatsProvider {

    private final ConcurrentHashMap<String, RIDeploymentEndpointData> endpoints =
            new ConcurrentHashMap<String, RIDeploymentEndpointData>();

    @ProbeListener("glassfish:webservices:ri:deploy")
    public void deploy(
            @ProbeParam("name")String name,
            @ProbeParam("endpoint")WSEndpoint endpoint) {
        endpoints.put(name, new RIDeploymentEndpointData(endpoint));
    }

    @ProbeListener("glassfish:webservices:ri:undeploy")
    public void undeploy(@ProbeParam("name")String name) {
        endpoints.remove(name);
    }

    @ManagedAttribute
    @Description("Endpoints with sun-jaxws.xml deployment")
    public Collection<RIDeploymentEndpointData> getRIEndpoints() {
        return Collections.unmodifiableCollection(endpoints.values());
    }

    /*package*/ RIDeploymentEndpointData getEndpoint(String appName){
        return endpoints.get(appName);
    }

}