
package epr.wsdl_eprextensions.server;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

@WebService(endpointInterface = "epr.wsdl_eprextensions.server.Hello")
public class HelloImpl implements Hello {
    @Resource
    WebServiceContext wsContext;

    public HelloResponse hello( HelloRequest parameters) {
            //dummy impl
             return null;
    }

    public W3CEndpointReference getW3CEPR() {
        return (W3CEndpointReference) wsContext.getEndpointReference();
    }
}