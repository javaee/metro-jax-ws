package server.mu_header_fault.server;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.BindingType;


@WebService(portName ="TestEndpointPort",serviceName="TestEndpointService" )

public class TestEndpoint
{
    @WebMethod()
    public String echo(String s) {
        return s;
    }
}