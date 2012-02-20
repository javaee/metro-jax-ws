
package fromjava.inherited_annotations.server;

import fromjava.inherited_annotations.server.base3.*;
import javax.jws.*;

public class EchoBase4 extends EchoBase3 {
    @WebMethod
    public String echoJ(String str) {
        return "EchoBase4:"+str;
    }
}
