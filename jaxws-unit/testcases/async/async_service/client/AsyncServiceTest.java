package async.async_service.client;

import junit.framework.TestCase;

/**
 * @author Rama Pulavarthi
 */
public class AsyncServiceTest extends TestCase {

    public AsyncServiceTest(String name) throws Exception{
        super(name);
    }

    public void testAsync() throws Exception {
    Hello helloPort = new Hello_Service().getHelloPort();
    Hello_Type req = new Hello_Type();
	req.setArgument("sync");
	req.setExtra("source");
	HelloResponse response = helloPort.hello(req, req);
	assertEquals("foo", response.getArgument());
	assertEquals("bar", response.getExtra());
    }

}