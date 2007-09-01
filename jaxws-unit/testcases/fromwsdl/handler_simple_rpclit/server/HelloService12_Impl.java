package fromwsdl.handler_simple_rpclit.server;

/**
 */
@javax.jws.WebService(portName="HelloPort12", endpointInterface="fromwsdl.handler_simple_rpclit.server.Hello12")
public class HelloService12_Impl implements Hello12 {

    public int hello12(int x) {
        System.out.println("Hello12Service_Impl received: " + x);
        return x;
    }

}
