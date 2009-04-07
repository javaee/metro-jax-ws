
package fromjava.xmlelement.server;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

@WebService(name="Echo", serviceName="echoService", targetNamespace="http://echo.org/")
public class EchoImpl {

    @XmlElement(required=true)
    public int echoInt(@XmlElement(nillable=true) int a) {
        return a;
    }

    @XmlElement(required=true)
    public String echoString(@XmlElement(nillable=true) String a) {
        return a;
    }

    @XmlElement(nillable=true)
    public Integer echoInteger(@XmlElement(required=true) Integer a) {
        return a;
    }

    @XmlElement(name="result")
    public String echoName(@XmlElement(name="input") String a) {
        return a;
    }

    @WebResult(name="result")
    @XmlElement(name="result")
    public String echoWebParamName(
            @WebParam(name="input")
            @XmlElement(name="input") String a) {
        return a;
    }
      
}
