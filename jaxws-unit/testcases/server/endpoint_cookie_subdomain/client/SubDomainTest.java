package server.endpoint_cookie_subdomain.client;

import junit.framework.TestCase;
import testutil.PortAllocator;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.util.Map;

/**
 * Service sends cookies for domain, and sub-domain. This tests whether
 * client sends the all cookies or not.
 *
 * @author Jitendra Kotamraju
 */
public class SubDomainTest extends TestCase {

    public SubDomainTest(String name) {
        super(name);
    }

    public void testSubDomainCookies() throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        String host = addr.getCanonicalHostName();
        if (!isFqdn(host)) {
            System.out.println("Cannot run test with hostname="+host+
                    " Need a hostname with two dots");
            return;
        }

        int port = PortAllocator.getFreePort();
        String address = "http://"+host+":"+port+"/hello";
        Endpoint endpoint = Endpoint.publish(address, new HelloServiceImpl());        
        invoke(host, address);
        endpoint.stop();
    }

    private void invoke(String host, String address) {
        // access service
        QName portName = new QName("", "");
        QName serviceName = new QName("", "");
        Service service = Service.create(serviceName);
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, address);
        Dispatch<Source> d = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        Map<String, Object> requestContext = d.getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);

        // introduce() request
        String body  = "<ns2:introduce xmlns:ns2='urn:test'><arg0>"+host+"</arg0></ns2:introduce>";
        Source request = new StreamSource(new ByteArrayInputStream(body.getBytes()));
        d.invoke(request);
        request = new StreamSource(new ByteArrayInputStream(body.getBytes()));
        d.invoke(request);

        // rememeberMe() request
        body  = "<ns2:rememberMe xmlns:ns2='urn:test'/>";
        request = new StreamSource(new ByteArrayInputStream(body.getBytes()));
        d.invoke(request);
        request = new StreamSource(new ByteArrayInputStream(body.getBytes()));
        d.invoke(request);
    }

    // return true if it is a fqdn with two dots
    private boolean isFqdn(String host) {
        System.out.println("See if host="+host+" has two dots");
        int index = host.indexOf('.');
        if (index == -1) {
            return false;
        }
        index = host.indexOf('.', index+1);
        return index != -1;
    }

}
