package provider.rootpart_charset_772.client;

import junit.framework.TestCase;

import javax.xml.soap.*;
import javax.xml.ws.BindingProvider;
import java.io.ByteArrayInputStream;

/**
 * Client needs to verify whether the Content-Type header for the soap part
 * contains "charset" or not.
 *
 * @author Jitendra Kotamraju
 */
public class AttachmentTest extends TestCase {

    public AttachmentTest(String name) throws Exception {
        super(name);
    }

    // verifies if the root(soap envelope) part's Content-Type has charset
    public void testRootPartCharset() throws Exception {
        // Create request message
        String str = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body><VoidTest xmlns='urn:test:types'/></S:Body></S:Envelope>";
        MessageFactory fact = MessageFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        SOAPMessage req = fact.createMessage(headers, new ByteArrayInputStream(str.getBytes("UTF-8")));

        // Get address
        Hello hello = new Hello_Service().getHelloPort();
        BindingProvider bp = (BindingProvider)hello;
        String address = (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

        // Get response message
        SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
        SOAPConnection con = factory.createConnection();
        SOAPMessage res = con.call(req, address);
        con.close();

        // verifies root part's Content-Type
        String[] cts = res.getSOAPPart().getMimeHeader("Content-Type");
        assertTrue(cts[0].contains("charset"));
    }

}
