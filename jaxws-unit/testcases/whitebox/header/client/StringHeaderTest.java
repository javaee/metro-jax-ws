package whitebox.header.client;

import junit.framework.TestCase;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.message.StringHeader;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Tests the StringHeader impl
 *
 * @author Rama Pulavarthi
 */
public class StringHeaderTest extends TestCase {
    String TEST_MESSAGE  =
        "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>"+
        "<S:Body xmlns:SOAP-ENV=\"xxx\" attr='value' xmlns=\"foo\" xmlns:wsa=\"bar\">" +
        "<addNumbers xmlns='http://example.com/'/>" +
        "</S:Body></S:Envelope>";

    public void test1() {
        Header h = new StringHeader(AddressingVersion.W3C.actionTag,"http://example.com/action");
        assertEquals(h.getStringContent(),"http://example.com/action");
    }

    public void test2() throws SOAPException, IOException {
        Header h = new StringHeader(AddressingVersion.W3C.actionTag,"http://example.com/action");
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage(new MimeHeaders(),new ByteArrayInputStream(TEST_MESSAGE.getBytes()));
        h.writeTo(message);
    }
}