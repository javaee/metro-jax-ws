
package whitebox.epr_2_1_x.server;

import javax.jws.WebService;

@WebService(endpointInterface = "whitebox.epr_2_1_x.server.Hello")
public class HelloImpl implements Hello {
    public HelloResponse hello( HelloRequest parameters) {
            //dummy impl
             return null;
    }

}
