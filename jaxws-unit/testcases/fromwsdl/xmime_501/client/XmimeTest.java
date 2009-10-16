package fromwsdl.xmime_501.client;

import junit.framework.TestCase;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

/**
 * Test for issue 501
 *
 * @author Jitendra Kotamraju
 */
public class XmimeTest extends TestCase {

    private Hello proxy;

    public XmimeTest(String name) throws Exception{
        super(name);
    }

    protected void setUp() throws Exception {
        proxy = new HelloService().getHelloPort();
    }

    public void testMtomInOut() throws Exception {
    	proxy.mtomInOut(getSource("gpsXml.xml"));
    }

    private StreamSource getSource(String file) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(file);
        return new StreamSource(is);
    }

}
