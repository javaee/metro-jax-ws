package bsh;

import javax.activation.*;
import javax.xml.ws.*;
import javax.xml.ws.soap.*;
import javax.xml.ws.handler.*;
import javax.xml.ws.handler.soap.*;
import javax.xml.bind.*;
import javax.xml.soap.*;
import javax.xml.namespace.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.util.*;
import java.util.concurrent.*;
import fromjava.innerclass.*;
import fromjava.innerclass.client.*;
import java.net.URL;
import java.net.URI;

import static junit.framework.TestCase.*;
import static bsh.Util.*;


public class Client5 {

    private static int DEPLOY_PORT = Integer.valueOf(System.getProperty("deployPort"));

    static String home = "/Users/miran/dev/clean/jaxws-ri/jaxws-ri/tests/unit/testcases/fromjava/innerclass";

    // TODO
    static List<URL> wsdlUrls = new ArrayList<URL>();

    static fromjava.innerclass.client.EchoInnerService echoInnerService = new fromjava.innerclass.client.EchoInnerService();
    static fromjava.innerclass.client.EchoInner echoInnerPort = echoInnerService.getEchoInnerPort();
    static URI echoInnerPortAddress = createUri("http://localhost:8080/fromjava.innerclass/EchoImpl$EchoInner");

    // injected from bsh scripts


    static URI createUri(String s) {
        try {
            return new URI(s);
        } catch(Throwable t){
            throw new RuntimeException(t);
        }
    }

    public static void main(String[] args) throws Throwable {
        // bsh script START
        
        
        Bar bar = new Bar();
        bar.setAge(33);

        assertNull(echoInnerPort.echoString(null));
        assertEquals("test", echoInnerPort.echoString("test"));
        assertEquals("Mary & Paul", echoInnerPort.echoString("Mary & Paul"));
        assertNull(echoInnerPort.echoBar(null));
        assertEquals(bar.getAge(), echoInnerPort.echoBar(bar).getAge());
        
    
        // bsh script END

        System.out.println("= TEST PASSED: Client5");
    }

}
