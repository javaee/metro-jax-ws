package fromwsdl.client_from_sei.client;

import junit.framework.TestCase;

import javax.xml.ws.Service;
import java.net.URL;
import javax.xml.namespace.QName;

/**
 * Creating proxy from a SEI
 * 
 * @author Jitendra Kotamraju
 */
public class SEITest extends TestCase {

    public SEITest(String name) throws Exception{
        super(name);
    }

    public void testHello() throws Exception {
        URL wsdl = new Hello_Service().getWSDLDocumentLocation();

        QName sname = new QName("urn:test", "Hello");
        QName pname = new QName("urn:test", "HelloPort");
        Service service = Service.create(wsdl, sname);
        MyHello proxy = service.getPort(pname, MyHello.class);

        testProxy(proxy);
    }

    public void testHello1() throws Exception {;
        Service service = new Hello_Service();
        MyHello  proxy = service.getPort(MyHello.class);

        testProxy(proxy);
    }

    public void testHello2() throws Exception {
        QName pname = new QName("urn:test", "HelloPort");
        Service service = new Hello_Service();
        MyHello proxy = service.getPort(pname, MyHello.class);

        testProxy(proxy);
    }

    private void testProxy(MyHello proxy) {
        MyHello_Type req = new MyHello_Type();
        req.setArgument("arg");
        req.setExtra("extra");
        MyHelloResponse resp = proxy.hello(req);
        assertEquals("result", resp.getResult());
    }

}
