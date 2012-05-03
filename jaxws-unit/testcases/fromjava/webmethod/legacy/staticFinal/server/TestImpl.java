package fromjava.webmethod.legacy.staticFinal.server;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.rmi.Remote;

/**
 * User: Iaroslav Savytskyi
 * Date: 01.12.11
 */
@WebService
public class TestImpl {

    // This is WebMethod
    @WebMethod
    public String method1(String str) {
        return str;
    }

    // This is not a WebMethod since static
    public static String method2(String str) {
        return str;
    }

    // This is not a WebMethod since final
    public final String method3(String str) {
        return str;
    }

    // This is not a WebMethod since package access
    String method4(String str) {
        return str;
    }

    // This is not a WebMethod since protected access
    protected String method5(String str) {
        return str;
    }

    // This is not a WebMethod since private access
    private String method6(String str) {
        return str;
    }

    // This method shouldn't be validated
    protected Remote method7(String str) {
        return null;
    }

    // this is not webMethod - since it's not annotated with @WebMethod
    public String method8(String str) {
        return str;
    }
}
