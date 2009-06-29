
package fromjava.dynamic_775.server;

import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.List;

/**
 * Issue 775 test case. Error in generating the wrapper bean dynamically
 * when the reflection type is GenericArrayType.
 *
 * @author Jitendra Kotamraju
 */
@WebService(name="Echo", serviceName="echoService", targetNamespace="http://echo.org/")
public class EchoImpl {

    // Reflection API gives "Class" for byte[] parameter
    public String echo(@WebParam(name="photo") byte[] photo) {
        return null;
    }

    // Reflection API gives "GenericArrayType" for byte[] parameter
    // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5041784
    public String echo1(@WebParam(name="photo") byte[] photo,
                        @WebParam(name="groups") List<String> groups) {
        return null;
    }

}
