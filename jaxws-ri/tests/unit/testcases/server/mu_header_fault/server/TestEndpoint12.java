package server.mu_header_fault.server;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.BindingType;


@WebService(portName ="TestEndpoint12Port",serviceName="TestEndpoint12Service" )
@BindingType(value="http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")

public class TestEndpoint12
{
    @WebMethod()
    public String echo(String s) {
        return s;
    }
}