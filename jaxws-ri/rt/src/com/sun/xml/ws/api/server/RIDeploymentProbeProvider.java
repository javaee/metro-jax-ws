package com.sun.xml.ws.api.server;

import org.glassfish.probe.provider.annotations.ProbeProvider;
import org.glassfish.probe.provider.annotations.Probe;
import org.glassfish.probe.provider.annotations.ProbeParam;

/**
 * @author Jitendra Kotamraju
 */
@ProbeProvider(providerName="metro", moduleName="webservices")
public class RIDeploymentProbeProvider {

    @Probe(name="metro:jaxws:ri:deploy")
    public void deploy(@ProbeParam("name")String key, @ProbeParam("endpoint")WSEndpoint endpoint) {
        // intentionally left empty.
    }

    @Probe(name="metro:jaxws:ri:undeploy")
    public void undeploy(@ProbeParam("name")String key, @ProbeParam("endpoint")WSEndpoint endpoint) {
        // intentionally left empty.
    }
    
}