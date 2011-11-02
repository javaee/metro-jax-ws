package fromjava.webmethod.privateValidation.server;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.rmi.Remote;

/**
 * 'non public' methods are not web methods and aren't validated
 */
@WebService
public class TestImpl {

    @WebMethod
    public String method1(String str) {
        return str;
    }

    // Not a web method
    private Remote method2() {
        return null;
    }

    // Not a web method
    Remote method3() {
        return null;
    }

    // Not a web method
    protected Remote method4() {
        return null;
    }
}
