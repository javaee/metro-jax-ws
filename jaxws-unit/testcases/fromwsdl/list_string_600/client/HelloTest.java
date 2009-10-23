package fromwsdl.list_string_600.client;

import junit.framework.TestCase;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

/**
 * Test for issue 600
 *
 * @author Jitendra Kotamraju
 */
public class HelloTest extends TestCase {

    private HelloServicePortType proxy;

    public HelloTest(String name) throws Exception{
        super(name);
    }

    protected void setUp() throws Exception {
        proxy = new HelloService().getHelloServicePort();
    }

    public void testList() throws Exception {
    	List<String> list = proxy.getAllGuestNames();
        assertEquals(1, list.size());
        assertEquals("sun", list.get(0));
    }

}
