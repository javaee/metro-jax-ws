package fromwsdl.handler_simple.server;

import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;

/**
 */
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
@WebService(portName="HelloPort12", endpointInterface="fromwsdl.handler_simple.server.Hello12",portName="HelloPort12")
public class HelloService12_Impl implements Hello12 {

    public int hello12(int x) {
        System.out.println("Hello12Service_Impl received: " + x);
        return x;
    }

}
