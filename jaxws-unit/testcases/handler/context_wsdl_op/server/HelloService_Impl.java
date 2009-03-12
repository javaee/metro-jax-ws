package handler.context_wsdl_op.server;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.namespace.QName;
import javax.annotation.Resource;

/**
 * This tests the WSDL_OPERATION property in MessageContext
 * @author Rama Pulavarthi
 */
@javax.jws.HandlerChain(name="",file="handlers.xml")
@WebService(name="Hello", serviceName="HelloService", targetNamespace="urn:test")
public class HelloService_Impl {
    private final QName expected_wsdl_op = new QName("urn:test", "sayHello");
    @Resource
    private WebServiceContext wsc;

    @WebMethod
    public int sayHello(@WebParam(name="x")int x) {
        System.out.println("HelloService_Impl received: " + x);
        QName got_wsdl_op = (QName) (wsc.getMessageContext().get(MessageContext.WSDL_OPERATION));
        //System.out.println(got_wsdl_op);
        if (expected_wsdl_op.equals(got_wsdl_op))
            return x;
        else
            throw new WebServiceException("WSDL Operation property not available in Endpoint Implementation"); 


    }

}
