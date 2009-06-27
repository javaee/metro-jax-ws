
package fromjava.dynamic_775.server;

import javax.jws.*;

import javax.xml.ws.Holder;
import javax.xml.ws.*;
import javax.xml.soap.*;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.rmi.RemoteException;

/**
 * Issue 775 test case
 *
 * @author Jitendra Kotamraju
 */
@WebService(name="Echo", serviceName="echoService", targetNamespace="http://echo.org/")
public class EchoImpl {
    public String echo(@WebParam(name="photo") byte[] photo,
                       @WebParam(name="groups") List<String> groups) {
        return null;
    }
}
