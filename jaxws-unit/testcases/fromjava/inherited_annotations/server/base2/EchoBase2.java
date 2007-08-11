
package fromjava.inherited_annotations.server.base2;

import fromjava.inherited_annotations.server.base.*;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;

@WebService
public class EchoBase2 extends EchoBase {
    public String echoA(String str) {
        return "EchoBase2:"+str;
    }

    public String echoD(String str) {
        return "EchoBase2:"+str;
    }

    public String echoE(String str) {
        return "EchoBase2:"+str;
    }
}
