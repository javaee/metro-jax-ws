package whitebox.header.client;

import junit.framework.TestCase;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.message.StringHeader;

/**
 * Tests the StringHeader impl
 *
 * @author Rama Pulavarthi
 */
public class StringHeaderTest extends TestCase {
    public void test1() {
        Header h = new StringHeader(AddressingVersion.W3C.actionTag,"http://example.com/action");
        assertEquals(h.getStringContent(),"http://example.com/action");
    }
}