package fromjava.dynamic_775.client;

import java.util.*;
import junit.framework.TestCase;

/**
 * @author Jitendra Kotamraju
 */
public class EchoTest extends TestCase {
    Echo proxy;

    public void setUp() {
        proxy = new EchoService().getEchoPort();
    }

    public void testEcho() throws Exception {
        proxy.echo("test".getBytes());
    }

    public void testEcho1() throws Exception {
        List<String> list = new ArrayList<String>(); 
        list.add("list");
        proxy.echo1("test".getBytes(), list);
    }

}
