package fromjava.webmethod.legacy.withWebMethod.server;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * User: Iaroslav Savytskyi
 * Date: 01.12.11
 */
@WebService
public class TestImpl {

    @WebMethod
    public String method1(String str) {
        return str;
    }

    // This is not a WebMethod since legacy is set to "true"
    public String method2(String str) {
        return str;
    }

    @WebMethod
    @Override
    public String toString() {
        return "str2";
    }
}
