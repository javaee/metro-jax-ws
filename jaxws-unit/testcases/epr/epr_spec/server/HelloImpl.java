
package epr.epr_spec.server;

import javax.jws.WebService;

@WebService(endpointInterface = "epr.epr_spec.server.Hello")
public class HelloImpl implements Hello {
    public HelloResponse hello( HelloRequest parameters) {
            //dummy impl
             return null;
    }

}
