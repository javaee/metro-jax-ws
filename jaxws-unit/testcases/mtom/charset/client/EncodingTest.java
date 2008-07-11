package mtom.charset.client;

import junit.framework.TestCase;
import testutil.AttachmentHelper;
import testutil.ClientServerTestUtil;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.Holder;
import java.util.*;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Jitendra Kotamraju
 */
public class EncodingTest extends TestCase {

    private HelloImpl proxy;
    private Random random = new Random();

    public EncodingTest(String name) throws Exception{
        super(name);
    }

    protected void setUp() throws Exception {
        proxy = new HelloImplService().getHelloImplPort(new MTOMFeature());
    }

    public void testEchoBinaryAsString() throws Exception {
        String exp = new String(new char[] { 0xfffb, 'a', 'b', '<' });
        String str = proxy.echoBinaryAsString(exp.getBytes("UTF-8"));
        assertEquals(exp, str);
    }

    public void testEchoBinaryAsString16() throws Exception {
        String exp = new String(new char[] { 0xfffb, 'a', 'b', '<' });
        String str = proxy.echoBinaryAsString16(exp.getBytes("UTF-16"));
        assertEquals(exp, str);
    }

    public void testEchoString() throws Exception {
        String exp = new String(new char[] { 0xfffb, 'a', 'b', '<' });
        String str = proxy.echoString(exp);
        assertEquals(exp, str);
    }

}
