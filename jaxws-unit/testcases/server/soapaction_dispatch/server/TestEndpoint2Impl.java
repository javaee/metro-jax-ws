package server.soapaction_dispatch.server;

import javax.jws.WebService;
import javax.xml.ws.BindingType;


/**
 * @author Rama Pulavarthi
 */
@WebService(portName = "TestEndpointPort2", targetNamespace = "http://server.soapaction_dispatch.server/", serviceName="TestEndpointService",
        endpointInterface = "server.soapaction_dispatch.server.TestEndpoint")
@BindingType("http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
public class TestEndpoint2Impl {
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