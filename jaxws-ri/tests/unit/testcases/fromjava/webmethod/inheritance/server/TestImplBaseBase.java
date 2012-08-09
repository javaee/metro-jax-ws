package fromjava.webmethod.inheritance.server;

import javax.jws.WebService;
import javax.jws.WebMethod;

/**
 * @author Jitendra Kotamraju
 */
@WebService
public class TestImplBaseBase {

    // This is also a WebMethod since declaring class
    // has WebService
    public String method1(String str) {
        return str;
    }

    // This is also a WebMethod since declaring class
    // has WebService
    @Override 
    public String toString() {
        return "TestImplBaseBase";
    }
}
