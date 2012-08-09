package fromjava.schema_inline.server;

import javax.jws.WebService;

/**
 * Tests in-lining of generated schema in WSDL
 *
 * @author Jitendra Kotamraju
 */
@WebService(name = "Echo", serviceName = "echoService",
        targetNamespace = "http://echo.org/")
public class EchoImpl {

    public Bar echoBarList(Bar bar) throws WSDLBarException, Fault1 {
        return bar;
    }

}
