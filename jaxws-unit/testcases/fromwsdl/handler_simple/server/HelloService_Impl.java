package fromwsdl.handler_simple.server;

import javax.jws.WebService;

/**
 */
@WebService(endpointInterface="fromwsdl.handler_simple.server.Hello",portName="HelloPort")
public class HelloService_Impl implements Hello {

    public int hello(int x) {
        System.out.println("HelloService_Impl received: " + x);
        return x;
    }

}
