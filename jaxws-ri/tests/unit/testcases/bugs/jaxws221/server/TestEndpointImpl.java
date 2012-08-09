package bugs.jaxws221.server;

import javax.jws.WebService;

@WebService(endpointInterface="bugs.jaxws221.server.TestEndpoint")
public class TestEndpointImpl {
    public void oneWayMethod(String parameter){

    }
}
