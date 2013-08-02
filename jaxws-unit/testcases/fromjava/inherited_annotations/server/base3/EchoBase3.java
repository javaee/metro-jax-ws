package fromjava.inherited_annotations.server.base3;


import fromjava.inherited_annotations.server.base2.EchoBase2;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;


@WebService
@SOAPBinding(parameterStyle=ParameterStyle.BARE)
public class EchoBase3 extends EchoBase2 {
    public String echoC(String str) {
        return "EchoBase3:"+str;
    }

    public String echoD(String str) {
        return "EchoBase3:"+str;
    }

    public String echoF(String str) {
        return "EchoBase3:"+str;
    }
    public String echoG(String str) {
        return "EchoBase3:"+str;
    }

    @WebMethod(exclude=true)
    public void badBase1(String str) {
    }
}
