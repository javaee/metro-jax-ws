package handler.messagehandler.server;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;

/**
 */
@javax.jws.HandlerChain(
    name="",
    file="handlers.xml"
)
@WebService(name="Hello", serviceName="HelloService", targetNamespace="urn:test")
@SOAPBinding(style=SOAPBinding.Style.RPC)
public class HelloService_Impl {

    @WebMethod
    public int hello(@WebParam(name="x")int x) {
        System.out.println("HelloService_Impl received: " + x);
        return x;
    }

}
