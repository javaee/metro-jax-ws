package com.sun.xml.ws.transport.http.servlet;

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
public class JAXWSRIDeploymentProbeProvider {

    @Probe(name="deploy")
    public void deploy(@ProbeParam("adapter")ServletAdapter adpater) {
        // intentionally left empty.
    }

    @Probe(name="undeploy")
    public void undeploy(@ProbeParam("adapter")ServletAdapter adapter) {
        // intentionally left empty.
    }
    
}