package fromjava.webmethod.declclass.server;

import javax.jws.WebService;
import javax.jws.WebMethod;

/**
 * @author Jitendra Kotamraju
 */
@WebService
public class TestImpl {

    @WebMethod
    public String method1(String str) {
        return str;
    }

    // This is also a WebMethod since declaring class
    // has WebService
    public String method2(String str) {
        return str;
    }
}
