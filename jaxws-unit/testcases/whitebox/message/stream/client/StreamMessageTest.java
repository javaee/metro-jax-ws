package whitebox.message.stream.client;

import junit.framework.TestCase;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.message.stream.StreamMessage;

import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * Tests the StreamMessage implementation of Message API.
 *
 * @author Rama Pulavarthi
 */
public class StreamMessageTest extends TestCase {
    public void testMessageWriteTo() throws Exception {

    String soapMsg = "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<Header>" +
                    "</Header>" +
                    "<Body>" +
                    "<GetCountryCodesResponse xmlns='http://www.strikeiron.com'> <GetCountryCodesResult/></GetCountryCodesResponse>" +
                    "</Body>" +
                    "</Envelope>";
            Message message = createSOAP11StreamMessage(soapMsg);
            ByteArrayBuffer baos = new ByteArrayBuffer();
            XMLStreamWriter writer = XMLStreamWriterFactory.create(baos);
            message.writeTo(writer);
            writer.flush();
            baos.writeTo(System.out);
}
    private StreamMessage createSOAP11StreamMessage(String msg) throws IOException {
        Codec codec = Codecs.createSOAPEnvelopeXmlCodec(SOAPVersion.SOAP_11);
        Packet packet = new Packet();
        ByteArrayInputStream in = new ByteArrayInputStream(msg.getBytes());
        codec.decode(in, "text/xml", packet);
        return (StreamMessage) packet.getMessage();
    }
}