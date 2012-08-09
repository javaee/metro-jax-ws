package fromjava.webmethod.legacy.inheritance.server;

import javax.jws.WebService;

/**
 * @author Jitendra Kotamraju
 */
@WebService
public class TestImplBaseBase {

    // This is a WebMethod since legacy computation is set
    // and no @WebMethod's
    public String method1(String str) {
        return str;
    }

    // This is a WebMethod since legacy computation is set
    // and no @WebMethod's
    @Override
    public String toString() {
        return "TestImplBaseBase";
    }
}
