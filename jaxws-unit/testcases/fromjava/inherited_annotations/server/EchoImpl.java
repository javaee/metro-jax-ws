
package fromjava.inherited_annotations.server;

import javax.jws.*;

@WebService(portName="EchoPort")
public class EchoImpl extends EchoBase4 {
    @WebMethod
    public String echoF(String str) {
        return "EchoImpl:"+str;
    }
    @WebMethod	
    public String echoH(String str) {
        return "EchoImpl:"+str;
    }

    public void badImpl1(String str) {
    }	
}
