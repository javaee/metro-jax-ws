package fromjava.webmethod.legacy.inheritance.server;

import javax.jws.WebService;
import javax.jws.WebMethod;

/**
 * @author Jitendra Kotamraju
 */
@WebService
public class TestImpl extends TestImplBase {

    @WebMethod
    public String method3(String str) {
        return str;
    }

    // This is not a WebMethod since legacy computation is set
    public String method4(String str) {
        return str;
    }

    // This is not a WebMethod since legacy computation is set
    public String method5(String str) {
        return str;
    }

    @WebMethod(exclude=true)
    public String method6(String str) {
        return str;
    }
}
