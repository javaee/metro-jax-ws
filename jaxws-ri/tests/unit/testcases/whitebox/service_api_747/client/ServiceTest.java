package whitebox.service_api_747.client;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.TestCase;

/**
 * @author Jitendra Kotamraju
 */
public class ServiceTest extends TestCase {

    public void test() throws Exception {
        Service service = Service.create(new QName("", ""));
        Iterator<QName> ports = service.getPorts();
        assertFalse(ports.hasNext());
        
        QName newPort = new QName("urn:test", "newPort");
        service.addPort(newPort, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost/service");
        ports = service.getPorts();
        assertEquals(newPort, ports.next());
    }

}
