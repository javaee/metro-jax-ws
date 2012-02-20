
package fromjava.nosei_bare_466.server;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

@WebService(name="Echo", serviceName="EchoService", targetNamespace="http://echo.org/")
@SOAPBinding(parameterStyle=ParameterStyle.BARE)
public class EchoImpl {

    /**
     * Testcase to test generation of default name and partName in wsdl for headers.
     * Issue:466 
     */
    @WebMethod
    public Long echoIn3Header(Integer age, @WebParam(header=true)Long num,
                              @WebParam(header=true)String name) {
        return num+age;
    }
}
