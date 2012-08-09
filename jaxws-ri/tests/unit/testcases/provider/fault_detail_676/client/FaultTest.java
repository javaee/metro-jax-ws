package provider.fault_detail_676.client;

import junit.framework.TestCase;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Iterator;

/**
 * Client verifies multiple detail entries in soap fault
 *
 * @author Jitendra Kotamraju
 */
public class FaultTest extends TestCase {

    public void testPut() {
        Hello hello = new Hello_Service().getHelloPort();
        try {
            hello.voidTest(new VoidType());
            fail("Didn't receive an expected exception");
        } catch(SOAPFaultException se) {
            SOAPFault fault = se.getFault();
            assertEquals(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"),
                    fault.getFaultCodeAsQName());
            assertEquals("fault message", fault.getFaultString());
            assertEquals("http://example.org/actor", fault.getFaultActor());
            Detail detail = fault.getDetail();
            Iterator it = detail.getDetailEntries();
            DetailEntry de1 = (DetailEntry)it.next();
            assertEquals(new QName("", "entry1"), de1.getElementQName());
            DetailEntry de2 = (DetailEntry)it.next();
            assertEquals(new QName("", "entry2"), de2.getElementQName());
            assertFalse(it.hasNext());
        }
    }

}
