package epr.w3ceprbuilder.client;

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
 *
 * This is somewhat similar to wsa.w3c.fromwsdl.w3cepr test, but tests JAX-WS 2.2 defined behavior
 */
public class W3CEPRTest extends TestCase {
    public W3CEPRTest(String name) throws Exception {
        super(name);
    }

    Hello getStub() throws Exception {
        return new Hello_Service().getHelloPort();
    }

    private String getEndpointAddress() throws Exception{

        BindingProvider bp = ((BindingProvider) getStub());
        return
            (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    }

    private static final String NAMESPACEURI = "urn:test";
    private static final String SERVICE_NAME = "Hello";
    private static final String PORT_NAME = "HelloPort";
    private QName SERVICE_QNAME = new QName(NAMESPACEURI, SERVICE_NAME);
    private QName PORT_QNAME = new QName(NAMESPACEURI, PORT_NAME);
    private static final QName INTERFACE_NAME =  new QName(NAMESPACEURI,"Hello");

    private static String xmlRefParam1 = "<myns1:MyParam1 wsa:IsReferenceParameter='true' xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" xmlns:myns1=\"http://example.com/myparam1\">Hello</myns1:MyParam1>";

    private static String xmlRefParam2 = "<myns2:MyParam2 xmlns:myns2=\"http://example.com/myparam2\">There</myns2:MyParam2>";

    public void xtestW3CEprBuilder_withWSDL_ServiceName() throws Exception {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder = builder.address(getEndpointAddress());
        builder = builder.serviceName(SERVICE_QNAME);
        builder = builder.endpointName(PORT_QNAME);
        builder = builder.wsdlDocumentLocation(getEndpointAddress()+"?wsdl");
        DOMSource domsrc = makeDOMSource(xmlRefParam1);
        Document document = (Document) domsrc.getNode();
        builder = builder.referenceParameter(document.getDocumentElement());
        domsrc = makeDOMSource(xmlRefParam2);
        document = (Document) domsrc.getNode();
        builder = builder.referenceParameter(document.getDocumentElement());
        W3CEndpointReference epr = builder.build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        epr.writeTo(new StreamResult(baos));
        baos.writeTo(System.out);        
    }

    public void xtestW3CEprBuilder_withWSDL() throws Exception {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder = builder.address(getEndpointAddress());
        builder = builder.wsdlDocumentLocation(getEndpointAddress()+"?wsdl");
        W3CEndpointReference epr = builder.build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        epr.writeTo(new StreamResult(baos));
        baos.writeTo(System.out);
    }

    public void testW3CEprBuilder_withWSDL_InterfaceName() throws Exception {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder = builder.address(getEndpointAddress());
        builder = builder.wsdlDocumentLocation(getEndpointAddress()+"?wsdl");
        builder = builder.interfaceName(INTERFACE_NAME);
        W3CEndpointReference epr = builder.build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        epr.writeTo(new StreamResult(baos));
        baos.writeTo(System.out);
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
