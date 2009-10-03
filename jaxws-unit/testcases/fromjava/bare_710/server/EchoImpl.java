
package fromjava.bare_710.server;

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
import javax.xml.ws.*;

@WebService(name="Echo", serviceName="EchoService", targetNamespace="http://echo.org/")
@SOAPBinding(parameterStyle=ParameterStyle.BARE)
public class EchoImpl {

    public void addNumbers(NumbersRequest numRequest,
                        @WebParam(mode=WebParam.Mode.OUT)
                        Holder<Integer> res) {
        if (numRequest.number1 != 10)
            throw new WebServiceException("numRequest.number1 expected=10"+" got"+numRequest.number1);
        if (numRequest.number2 != 20)
            throw new WebServiceException("numRequest.number2 expected=10"+" got"+numRequest.number2);
        res.value = numRequest.number1+numRequest.number2;
    }

}
