package deployment.partial_webxml_multi.server;

import javax.jws.WebService;

/**
 * @author Rama Pulavarthi
 */
@WebService(targetNamespace = "http://com.example.hello", portName = "EchoPort")
public class HelloImpl {
    public String hello(String str) {
        return "Hello " +str;    
    }
}
