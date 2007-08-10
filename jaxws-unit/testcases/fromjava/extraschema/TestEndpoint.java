package fromjava.extraschema;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(targetNamespace = "http://echo.org/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class TestEndpoint {
    public String echoString(String param){
        return param;
    }

    public int echoInt(int param){
        return param;
    }
}
