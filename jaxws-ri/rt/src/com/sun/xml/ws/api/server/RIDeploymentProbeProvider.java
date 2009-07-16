package com.sun.xml.ws.api.server;

import org.glassfish.external.probe.provider.annotations.ProbeProvider;
import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

/**
 * sun-jaxws.xml deployment probe. A registered listener get to listen the emited
 * sun-jaxws.xml deployment/undepolyment events.
 *
 * @author Jitendra Kotamraju
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="webservices", probeProviderName="ri")
public class RIDeploymentProbeProvider {

    @Probe(name="deploy")
    public void deploy(@ProbeParam("name")String name, @ProbeParam("endpoint")WSEndpoint endpoint) {
        // intentionally left empty.
    }

    @Probe(name="undeploy")
    public void undeploy(@ProbeParam("name")String name) {
        // intentionally left empty.
    }
    
}