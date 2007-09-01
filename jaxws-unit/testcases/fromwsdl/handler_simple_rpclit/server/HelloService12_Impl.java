package fromwsdl.handler_simple_rpclit.server;

import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;

/**
 */
@javax.jws.WebService(portName="HelloPort12", endpointInterface="fromwsdl.handler_simple_rpclit.server.Hello12")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)

public class HelloService12_Impl implements Hello12 {

    public int hello12(int x) {
        System.out.println("Hello12Service_Impl received: " + x);
        return x;
    }

}
