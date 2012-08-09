package fromwsdl.handler_simple_rpclit.server;

/**
 */
@javax.jws.WebService(portName="HelloPort", endpointInterface="fromwsdl.handler_simple_rpclit.server.Hello")
public class HelloService_Impl implements Hello {

    public int hello(int x) {
        System.out.println("HelloService_Impl received: " + x);
        return x;
    }

}
