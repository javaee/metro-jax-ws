
package fromjava.inherited_annotations.server.base;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;


@WebService
@SOAPBinding(parameterStyle=ParameterStyle.BARE)
public class EchoBase {
    public String echoA(String str) {
        return "EchoBase:"+str;
    }

    public String echoB(String str) {
        return "EchoBase:"+str;
    }

    public String echoC(String str) {
        return "EchoBase:"+str;
    }
}
