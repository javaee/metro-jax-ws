package fromwsdl.handler_simple.server;

/**
 */
@javax.jws.WebService(endpointInterface="fromwsdl.handler_simple.server.Hello12")
public class HelloService12_Impl implements Hello12 {

    public int hello12(int x) {
        System.out.println("Hello12Service_Impl received: " + x);
        return x;
    }

}
