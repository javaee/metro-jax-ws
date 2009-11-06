package com.sun.xml.ws.transport.http.servlet;

import org.glassfish.external.probe.provider.annotations.ProbeProvider;
import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

/**
 * Keeps track of webservice start and end for sun-jaxws.xml deployments.
 * A registered listener get to listen the emited events to track the time
 * spent in web services layer, no of requests for an endpoint etc
 *
 * @author Prashanth Abbagini
 * @author Jitendra Kotamraju
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="webservices", probeProviderName="servlet-ri")
public class JAXWSRIServletProbeProvider {

    @Probe(name="startedEvent")
    public void startedEvent(@ProbeParam("endpointAddress")String endpointAddress) {
        // intentionally left empty.
    }

    @Probe(name="endedEvent")
    public void endedEvent(@ProbeParam("endpointAddress")String endpointAddress) {
        // intentionally left empty.
    }

}