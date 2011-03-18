package server.session_ignorecase_portable1.client;

import java.net.URL;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.Service;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Address is set dynamically between the requests and see if session
 * is continued
 *
 * @author Jitendra Kotamraju
 */
public class HostIgnoreCaseTest extends TestCase {

    public HostIgnoreCaseTest(String name) {
        super(name);
    }
    
    /*
     * With maintain property set to true, session
     * should be maintained.
     */
    public void test3() throws Exception {
        Hello proxy = new HelloService().getHelloPort();

        // Set the adress with upper case hostname
        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
        proxy.introduce();

        String addr = (String)requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        URL url = new URL(addr);
        String host = url.getHost();
        addr = addr.replace(host, host.toUpperCase());
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, addr);

        assertTrue("client session should be maintained", proxy.rememberMe());
    }
    
}
