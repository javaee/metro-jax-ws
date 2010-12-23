package server.cookie_subdomain.client;

import junit.framework.TestCase;

import javax.xml.ws.BindingProvider;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Host in web service address is uppercase. That causes problems for
 * sticking cookies
 *
 * @author Jitendra Kotamraju
 */
public class SubDomainTest extends TestCase {

    public SubDomainTest(String name) {
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
        // proxy.introduce(); Doing this below as setting endpoint address
        // resets the cookie store. Will be handled in future revisions

        String addr = (String)requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        URL url = new URL(addr);
        String host = url.getHost();
        String fqdn = getFqdn(host);
        if (fqdn == null) {
            System.out.println("Cannot use this hostname="+host+" for the test");
            return;
        }

        addr = addr.replace(host, fqdn);
        System.out.println("Setting Web Service Addr="+addr);
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, addr);

        proxy.introduce(fqdn);
        assertTrue("client session should be maintained", proxy.rememberMe());
    }

    // return null if it cannot find a fqdn with two dots
    private String getFqdn(String host) {
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            System.out.println("UnknownHostException for host="+host);
            return null;
        }

        for(InetAddress tempAddr : addresses) {
            String fqdn = tempAddr.getCanonicalHostName();
            System.out.println("See if fqdn="+fqdn+" has two dots");
            int index = fqdn.indexOf('.');
            if (index == -1) {
                continue;
            }
            index = fqdn.indexOf('.', index+1);
            if (index != -1) {
                return fqdn;
            }
        }
        return null;
    }

}
