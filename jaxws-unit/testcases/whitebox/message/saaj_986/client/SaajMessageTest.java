package whitebox.message.saaj_986.client;

import com.sun.xml.ws.message.saaj.SAAJMessage;
import junit.framework.TestCase;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;

/**
 * Tests the SAAJMessage implementation for issue: 986
 *
 * @author Iaroslav Savytskyi
 */
public class SaajMessageTest extends TestCase {

    private static final String MESSAGE = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>" +
            "<S:Header>" +
            "<wsa:Action xmlns:wsa='http://www.w3.org/2005/08/addressing'>http://example.com/addNumbers</wsa:Action>" +
            "</S:Header>" +
            "<S:Body attr='value'>" +
            "<addNumbers xmlns='http://example.com/'/>" +
            "</S:Body></S:Envelope>";

    /**
     * Test for body attribute after SAAJMessage.copy()
     *
     * @throws Exception - error
     */
    public void testBodyAttr() throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage soapMessage = factory.createMessage();
        Source src = new StreamSource(new ByteArrayInputStream(MESSAGE.getBytes()));
        soapMessage.getSOAPPart().setContent(src);

        SAAJMessage saajMsg = new SAAJMessage(soapMessage);
        saajMsg.hasHeaders(); // breaks the underlying SOAPMessage
        saajMsg = (SAAJMessage) saajMsg.copy();
        SOAPBody soapBody = saajMsg.readAsSOAPMessage().getSOAPBody();
        assertEquals("value", soapBody.getAttribute("attr"));
    }
}
