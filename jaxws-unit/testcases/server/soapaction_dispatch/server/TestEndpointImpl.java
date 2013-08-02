package server.soapaction_dispatch.server;

import javax.jws.WebService;


/**
 * @author Rama Pulavarthi
 */
@WebService(portName = "TestEndpointPort1", targetNamespace = "http://server.soapaction_dispatch.server/", serviceName="TestEndpointService",
        endpointInterface = "server.soapaction_dispatch.server.TestEndpoint")
public class TestEndpointImpl {
    public EchoResponse echo(Echo e){
        EchoResponse r = new EchoResponse();
        r.setReturn("Hello "+ e.getArg0());
        return r;
    }

    public EchoResponse echo1(Echo e){
        EchoResponse r = new EchoResponse();
        r.setReturn("Hello1 "+ e.getArg0());
        return r;
    }
}