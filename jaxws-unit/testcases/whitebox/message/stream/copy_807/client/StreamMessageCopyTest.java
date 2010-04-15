package whitebox.message.stream.copy_807.client;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.message.stream.StreamMessage;
import com.sun.xml.ws.test.VersionRequirement;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Tests the StreamMessage copy implementation
 *
 * @author Jitendra Kotamraju
 */
public class StreamMessageCopyTest extends TestCase {

    private StreamMessage createSOAP11StreamMessage(String msg) throws IOException {
        Codec codec = Codecs.createSOAPEnvelopeXmlCodec(SOAPVersion.SOAP_11);
        Packet packet = new Packet();
        ByteArrayInputStream in = new ByteArrayInputStream(msg.getBytes("utf-8"));
        codec.decode(in, "text/xml", packet);
        return (StreamMessage) packet.getMessage();
    }

    public void testMessageForestCopy() throws Exception {
        for (int i = 0; i < 2500; i++) {
            try {
                testMessageForestCopy(i);
            } catch (Exception e) {
                System.out.println("Failed for i " + i);
                throw e;
            }
        }
    }

    private void testMessageForestCopy(int i) throws Exception {
        String soapMsgStart = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body>";
        String soapMsgEnd = "</S:Body></S:Envelope>";
        StringBuilder sb = new StringBuilder(soapMsgStart);
        while (i-- > 0) {
            sb.append("<a/>");
        }
        sb.append(soapMsgEnd);
        StreamMessage msg = createSOAP11StreamMessage(sb.toString());
        Message msg1 = msg.copy();
        msg1.copy();
    }

    public void testMessageCopy() throws Exception {
        for (int i = 0; i < 2500; i++) {
            try {
                testMessageCopy(i);
            } catch (Exception e) {
                System.out.println("Failed for i " + i);
                throw e;
            }
        }
    }

    private void testMessageCopy(int i) throws Exception {
        String soapMsgStart = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body>";
        String soapMsgEnd = "</S:Body></S:Envelope>";
        StringBuilder sb = new StringBuilder(soapMsgStart);
        int j = i;
        while (j-- > 0) {
            sb.append("<a>");
        }
        j = i;
        while (j-- > 0) {
            sb.append("</a>");
        }
        sb.append(soapMsgEnd);
        StreamMessage msg = createSOAP11StreamMessage(sb.toString());
        Message msg1 = msg.copy();
        msg1.copy();
    }
}
