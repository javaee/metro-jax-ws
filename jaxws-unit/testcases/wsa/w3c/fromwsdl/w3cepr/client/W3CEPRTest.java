package wsa.w3c.fromwsdl.w3cepr.client;

import junit.framework.TestCase;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Tests W3CEndpointReferenceBuilder
 * @author Rama Pulavarthi
 */
public class W3CEPRTest extends TestCase {
    public W3CEPRTest(String name) throws Exception {
        super(name);
    }

    AddNumbersPortType getStub() throws Exception {
        return new AddNumbersService().getAddNumbersPort();
    }

    private String getEndpointAddress() throws Exception{

        BindingProvider bp = ((BindingProvider) getStub());
        return
            (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    }

   private static final String NAMESPACEURI = "http://example.com";
    private static final String SERVICE_NAME = "AddNumbersService";
    private static final String PORT_NAME = "AddNumbersPort";
    private QName SERVICE_QNAME = new QName(NAMESPACEURI, SERVICE_NAME);
    private QName PORT_QNAME = new QName(NAMESPACEURI, PORT_NAME);
    private static String xmlInterfaceName = "<wsaw:InterfaceName xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsdl\" xmlns:myns=\"http://example.com\">myns:AddNumbersPortType</wsaw:InterfaceName>";

    private static String xmlRefParam1 = "<myns1:MyParam1 wsa:IsReferenceParameter='true' xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" xmlns:myns1=\"http://example.com/myparam1\">Hello</myns1:MyParam1>";

    private static String xmlRefParam2 = "<myns2:MyParam2 xmlns:myns2=\"http://example.com/myparam2\">There</myns2:MyParam2>";
    private URL wsdlurl = null;
    private String url = null;

    public void testEPRWithReferenceParameters() throws Exception {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder = builder.address(getEndpointAddress());
        DOMSource domsrc = makeDOMSource(xmlInterfaceName);
        Document document = (Document) domsrc.getNode();
        builder = builder.metadata(document.getDocumentElement());
        builder = builder.serviceName(SERVICE_QNAME);
        builder = builder.endpointName(PORT_QNAME);
        //builder = builder.wsdlDocumentLocation(wsdlurl.toString());
        domsrc = makeDOMSource(xmlRefParam1);
        document = (Document) domsrc.getNode();
        builder = builder.referenceParameter(document.getDocumentElement());
        domsrc = makeDOMSource(xmlRefParam2);
        document = (Document) domsrc.getNode();
        builder = builder.referenceParameter(document.getDocumentElement());
        W3CEndpointReference epr = builder.build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        epr.writeTo(new StreamResult(baos));
        AddNumbersService service = new AddNumbersService();
        AddNumbersPortType myport = (AddNumbersPortType) service.getPort(
                epr, AddNumbersPortType.class, new AddressingFeature(true, true));
        myport.addNumbers(10, 10);

    }



    public DOMSource makeDOMSource(String msg) {

        byte[] bytes = msg.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        return new DOMSource(createDOMNode(inputStream));
    }

    public Node createDOMNode(InputStream inputStream) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            try {
                return builder.parse(inputStream);
            } catch (SAXException e) {
                fail("Error creating Dom Document");
            } catch (IOException e) {
                fail("Error creating Dom Document");
                fail("Error creating JABDispatch");
            }
        } catch (ParserConfigurationException pce) {
            fail("Error creating Dom Document");
            //IllegalArgumentException iae = new IllegalArgumentException(pce.getMessage());
            //iae.initCause(pce);
            //throw iae;
        }
        return null;
    }
}
