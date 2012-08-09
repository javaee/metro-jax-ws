package rt_wsdl_parser.client;

import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundFault;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import junit.framework.TestCase;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import static java.io.File.separator;

public class WSDLParserTest extends TestCase {

    public void testRuntimeWSDLParsing() {

        String path = System.getProperty("user.dir") + separator +
                "testcases" + separator +
                "rt_wsdl_parser" + separator +
                "wsdl" + separator;

        File wsdlDir = new File(path);
        File[] files = wsdlDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(".wsdl"))
                    return true;
                return false;
            }
        });
        for (File f : files) {
            //this is imported wsdl, exclude it
            if (f.getName().equals("W2JRLR2011ImportIndirectly.wsdl"))
                continue;
            try {
                com.sun.xml.ws.api.model.wsdl.WSDLModel doc = RuntimeWSDLParser.parse(f.toURL(), new StreamSource(f.toURL().toExternalForm()), getResolver(), true, null);
                validateBinding(f.getName(), doc);
            } catch (XMLStreamException e) {
                System.out.println("[FAILED] in RuntimeWSDLParser!");
                System.out.println("\t" + e.getMessage());
                assertTrue(false);
                continue;
            } catch (MalformedURLException e) {
                System.out.println("[ERROR] problem loading wsdl: " + f.getName() + "!");
                System.out.println("\t" + e.getMessage());
                assertTrue(false);
                continue;
            } catch (FileNotFoundException e) {
                System.out.println("[ERROR] problem loading wsdl: " + f.getName() + "!");
                System.out.println("\t" + e.getMessage());
                assertTrue(false);
                continue;
            } catch (IOException e) {
                System.out.println("[ERROR] problem loading wsdl: " + f.getName() + "!");
                System.out.println("\t" + e.getMessage());
                assertTrue(false);
                continue;
            } catch (SAXException e) {
                System.out.println("[FAILED] in RuntimeWSDLParser!");
                System.out.println("\t" + e.getMessage());
                assertTrue(false);
                continue;
            }
        }
    }

    private static void validateBinding(String wsdl, WSDLModel doc) {
        QName service = null;
        QName port = null;

        if (wsdl.equals("auctiontraq-doclit.wsdl")) {
            service = new QName("http://example.com/auctiontraq/wsdl/doclit", "AuctionTraqWSDLDoclitService");
            port = new QName("http://example.com/auctiontraq/wsdl/doclit", "AuctionTraqPort1");
        } else if (wsdl.equals("AddNumbers.wsdl")) {
            service = new QName("http://duke.org", "AddNumbersService");
            port = new QName("http://duke.org", "AddNumbersImplPort");
        } else if (wsdl.equals("bug.wsdl")) {
            service = new QName("http://server.hello/jaxws", "HelloImplService");
            port = new QName("http://server.hello/jaxws", "HelloImpl");
        } else if (wsdl.equals("HelloService.wsdl")) {
            service = new QName("http://helloservice.org/wsdl", "HelloService");
            port = new QName("http://helloservice.org/wsdl", "HelloPort");
        } else if (wsdl.equals("Issue500.wsdl")) {
            service = new QName("http://helloservice.org/wsdl", "HelloService");
            port = new QName("http://helloservice.org/wsdl", "HelloPort");
        } else if (wsdl.equals("hello_literal.wsdl")) {
            service = new QName("urn:test", "Hello");
            port = new QName("urn:test", "HelloPort");
        } else if (wsdl.equals("W2JRLR2011TestService.wsdl")) {
            service = new QName("http://w2jrlr2011testservice.org/W2JRLR2011TestService.wsdl", "W2JRLR2011TestService");
            port = new QName("http://w2jrlr2011testservice.org/W2JRLR2011TestService.wsdl", "W2JRLR2011TestPort");
        } else if (wsdl.equals("SimpleTest.wsdl")) {
            service = new QName("http://simpletestservice.org/wsdl", "SimpleTest");
            port = new QName("http://simpletestservice.org/wsdl", "SimpleEndpointPort");
        } else if (wsdl.equals("W2JDLR4002TestService.wsdl")) {
            service = new QName("http://w2jdlr4002testservice.org/W2JDLR4002TestService.wsdl", "W2JDLR4002TestService");
            port = new QName("http://w2jdlr4002testservice.org/W2JDLR4002TestService.wsdl", "W2JDLR4002TestPort");
        } else if (wsdl.equals("AddNumbers1.wsdl")) {
            service = new QName("http://example.com/", "AddNumbersService");
            port = new QName("http://example.com/", "AddNumbersPort");
        } else if (wsdl.equals("Neg_wsdl_extensions.wsdl")) {
            service = new QName("http://example.org", "AddNumbersService");
            port = new QName("http://example.org", "AddNumbersPort");
        } else if (wsdl.equals("jbi.wsdl")) {
            service = new QName("http://j2ee.netbeans.org/wsdl/newWSDL", "service1");
            port = new QName("http://j2ee.netbeans.org/wsdl/newWSDL", "port1");
            WSDLBoundPortType binding = doc.getBinding(service, port);
            assertNotNull(binding);
            WSDLBoundOperation op = binding.getOperation(null, "operation1");
            assertNull(op);
        } else if (wsdl.equals("policy.wsdl")) {
            service = new QName("http://wsit.test/", "FaultServiceService");
            port = new QName("http://wsit.test/", "FaultServicePort");
            WSDLBoundPortType binding = doc.getBinding(service, port);
            assertNotNull(binding);
            WSDLBoundOperation op = binding.get(new QName("http://wsit.test/", "echo"));
            assertNotNull(op);
            for (WSDLBoundFault bf : op.getFaults()) {
                if ((bf.getName().equals("EchoException"))) {
                    WSDLFault fault = bf.getFault();
                    assertNotNull(fault);
                    assertEquals(fault.getName(), bf.getName());
                } else {
                    assertEquals(bf.getName(), "Echo2Exception");
                }
            }
        }
        if (wsdl.equals("spews.wsdl")) {
            service = new QName("http://spews.pmg.net/", "SPEWS");
            port = new QName("http://spews.pmg.net/", "SPEWSSoap");
        }
        if (wsdl.equals("TestService.wsdl")) {
            service = new QName("http://service.test.policy.ws.xml.sun.com/", "TestServiceService");
            port = new QName("http://service.test.policy.ws.xml.sun.com/", "TestServicePort");
        } else {
            //no service port found, simply return
            return;
        }

        WSDLBoundPortType binding = doc.getBinding(service, port);
        if (binding == null) {
            System.out.println("[FAILED] No binding found for wsdl: " + wsdl + ", service: " + service + ", port: " + port + "!");
            assertTrue(false);
        }


        //test WSDLBoundPortType.getOperation()
        if (wsdl.equals("hello_literal.wsdl")) {
            WSDLBoundOperation op = binding.getOperation("urn:test:types", "Hello");
            assertTrue(op.getName().equals(new QName("urn:test", "hello")));
            assertTrue(op.getOperation().getInput().getName().equals("helloRequest"));
            assertTrue(op.getOperation().getOutput().getName().equals("helloResponse"));
            assertTrue(op.getOperation().getFaults().iterator().next().getName().equals("helloFault"));


            //check one way
            op = binding.getOperation("urn:test:types", "oneway");
            assertTrue(op.getName().equals(new QName("urn:test", "oneway")));
            assertTrue(op.getOperation().getInput().getName().equals("oneway"));
            assertTrue(op.getOperation().isOneWay());
            assertTrue(!op.getOperation().getFaults().iterator().hasNext());

            op = binding.getOperation("urn:test:types", "VoidTest");
            assertTrue(op.getName().equals(new QName("urn:test", "voidTest")));
            assertTrue(op.getOperation().getInput().getName().equals("voidInput"));
            assertTrue(op.getOperation().getOutput().getName().equals("voidOutput"));

        }

        if (wsdl.equals("AddNumbers1.wsdl")) {
            WSDLBoundOperation op = binding.getOperation("http://example.com/", "addNumbers");
            assertTrue(op.getName().equals(new QName("http://example.com/", "addNumbers")));
            assertTrue(op.getOperation().getInput().getName().equals("addNumbersRequest"));
            assertTrue(op.getOperation().getOutput().getName().equals("addNumbersResponse"));
            assertTrue(op.getOperation().getFaults().iterator().next().getName().equals("addNumbersFault"));
        }
        if (wsdl.equals("TestService.wsdl")) {
            WSDLBoundOperation op = binding.getOperation("http://service.test.policy.ws.xml.sun.com/", "echo");
            assertTrue(op.getName().equals(new QName("http://service.test.policy.ws.xml.sun.com/", "echo")));

        }
        if (wsdl.equals("Neg_wsdl_extensions.wsdl")) {
            WSDLPort wsdlPort = doc.getService(service).get(port);
            try {
                ((WSDLPortImpl) wsdlPort).areRequiredExtensionsUnderstood();
                assert (false);
            } catch (WebServiceException e) {
                System.out.println("Expected:");
                e.printStackTrace();
            }
        }

    }

    private static EntityResolver getResolver() {
        EntityResolver resolver = null;
        if (resolver == null) {
            // set up a manager
            CatalogManager manager = new CatalogManager();
            manager.setIgnoreMissingProperties(true);
            try {
                //if(System.getProperty(getClass().getName()+".verbose")!=null)
                manager.setVerbosity(0);
            } catch (SecurityException e) {
                // recover by not setting the debug flag.
            }

            // parse the catalog
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> catalogEnum;
            try {
                if (cl == null)
                    catalogEnum = ClassLoader.getSystemResources("/META-INF/jaxws-catalog.xml");
                else
                    catalogEnum = cl.getResources("/META-INF/jaxws-catalog.xml");

                while (catalogEnum.hasMoreElements()) {
                    URL url = catalogEnum.nextElement();
                    manager.getCatalog().parseCatalog(url);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            resolver = new CatalogResolver(manager);
        }

        return resolver;
    }
}
