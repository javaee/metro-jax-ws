package whitebox.mtom_codec.client;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.binding.BindingImpl;
import junit.framework.TestCase;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.jvnet.staxex.Base64Data;

/**
 * @author Jitendra Kotamraju
 */
public class CharactersTest extends TestCase {
    
    public void testHandleCharacters() throws Throwable {
        //handleCharacters(12193);

        for(int i=0; i < 15000; i++) {
            try {
                handleCharacters(i);
            } catch(Throwable t) {
                System.out.println("Failed for the size="+i);
                throw t;
            }
        }
    }

    private void handleCharacters(int size) throws Exception {
        BindingID bid = BindingID.parse(SOAPBinding.SOAP11HTTP_MTOM_BINDING);
        BindingImpl binding = (BindingImpl)bid.createBinding();
        Codec codec = binding.createCodec();
        TestMessage msg = new TestMessage(size);

        Packet packet = new Packet();
        codec.decode(msg.getInputStream(), msg.getContentType(), packet);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XMLStreamWriter sw = XMLStreamWriterFactory.create(bout);
        packet.getMessage().writeTo(sw);
        sw.close();

        InputStream in = new ByteArrayInputStream(bout.toByteArray());
        XMLStreamReader sr = XMLStreamReaderFactory.create(null, in, true);
        while(sr.hasNext()) {
            sr.next();
            if(sr.getEventType() == XMLStreamReader.START_ELEMENT && sr.getLocalName().equals("doc1")) {
                assertEquals(msg.getEncodedText(), sr.getElementText().trim());
            }
        }
    }

    private static final class TestMessage {

        // keep whitespaces around <xop:Inclue> elements
        private static final String ENV =
    "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xop='http://www.w3.org/2004/08/xop/include'>"+
        "<S:Body>" +
            "<MTOMInOut xmlns='http://example.org/mtom'>"+
                "<doc1>  <xop:Include href='attachment-content-id'> </xop:Include> </doc1>"+
            "</MTOMInOut>" +
         "</S:Body>" +
    "</S:Envelope>";

        private static final String CT =
            "multipart/related;start=\"envelope-content-id\";type=\"application/xop+xml\";boundary=\"boundary\";start-info=\"text/xml\"";

        private final String attData;
        private final String attEncodedData;

        TestMessage(int size) {
            this.attData = getAttachmentData(size);
            this.attEncodedData = getEncodedAttachmentData(size);
        }

        private static String getAttachmentData(int size) {
            StringBuilder att = new StringBuilder();
            for(int i=0; i < size; i++) {
                att.append((char)('A'+i%26));
            }
            return att.toString();
        }

        private static String getEncodedAttachmentData(int size) {
            String str = getAttachmentData(size);
            Base64Data encoded = new Base64Data();
            encoded.set(str.getBytes(), null);
            return encoded.toString();
        }

        String getEncodedText() {
            return attEncodedData;
        }

        String getContentType() {
            return CT;
        }

        InputStream getInputStream() {
            String msg =
                "--boundary\r\n"+
                "Content-Type: application/xop+xml;charset=utf-8;type=\"text/xml\"\r\n"+
                "Content-Id: envelope-content-id\r\n\r\n"+
                ENV+"\r\n"+
                "--boundary\r\n"+
                "Content-Type: text/plain\r\n"+
                "Content-ID: attachment-content-id\r\n\r\n"+
                attData+"\r\n"+
                "--boundary--";

            //System.out.println(msg);
            return new ByteArrayInputStream(msg.getBytes());
        }
    }

}
