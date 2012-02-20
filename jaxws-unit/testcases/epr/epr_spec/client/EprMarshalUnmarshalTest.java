package epr.epr_spec.client;

import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.util.DOMUtil;
import junit.framework.TestCase;
import testutil.EprUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBResult;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

public class EprMarshalUnmarshalTest extends TestCase {
    public EprMarshalUnmarshalTest(String name) {
        super(name);
        Hello hello = new HelloService().getHelloPort();
        endpointAddress = (String)((BindingProvider)hello).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

    }

    public void testMSEprMarshalling1() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(com.sun.xml.ws.developer.MemberSubmissionEndpointReference.class);
        JAXBResult res = new JAXBResult(ctx);
        com.sun.xml.ws.developer.MemberSubmissionEndpointReference mepr =
                new com.sun.xml.ws.developer.MemberSubmissionEndpointReference();
        mepr.writeTo(res);
    }
    
    public void testW3CEprMarshalling() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(W3CEndpointReference.class);
        JAXBResult res = new JAXBResult(ctx);
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address("http://example.com");
        W3CEndpointReference epr = builder.build();
        // You will get the NPE
        epr.writeTo(res);
    }
    public void testMSEprMarshalling() throws JAXBException {
        HelloService service = new HelloService();
        BindingProvider bp = (BindingProvider) service.getHelloPort();

        //validate w3c epr
        W3CEndpointReference w3cEpr = (W3CEndpointReference) bp.getEndpointReference();
        w3cEpr.writeTo(new StreamResult(System.out));
        //assertTrue(EprUtil.validateEPR(w3cEpr, endpointAddress, serviceName, portName, portTypeName, true));
        assertTrue(EprUtil.validateEPR(w3cEpr, endpointAddress, null,null,null,false));
        Marshaller m = jaxbCtx.createMarshaller();

        DOMResult w3cResult = new DOMResult();
        m.marshal(w3cEpr, w3cResult);
//      assertTrue(EprUtil.validateEPR(w3cResult.getNode(), W3CEndpointReference.class, endpointAddress, serviceName, portName, portTypeName, true));
        assertTrue(EprUtil.validateEPR(w3cResult.getNode(), W3CEndpointReference.class, endpointAddress, null,null,null,false));


        //validate ms epr
        MemberSubmissionEndpointReference msEpr = bp.getEndpointReference(MemberSubmissionEndpointReference.class);
        w3cEpr.writeTo(new StreamResult(System.out));
        assertTrue(EprUtil.validateEPR(msEpr, endpointAddress, serviceName, portName, portTypeName, false));
        DOMResult msResult = new DOMResult();

        m.marshal(msEpr, msResult);
        assertTrue(EprUtil.validateEPR(msResult.getNode(), MemberSubmissionEndpointReference.class, endpointAddress, serviceName, portName, portTypeName, true));


    }

    public void testW3CEPRBuilder() throws Exception {
            try {
                String xmlParam1 = "<myns:MyParam1 xmlns:myns=\"http://cptestservice.org/wsdl\">Hello</myns:MyParam1>";
                Node n1 = DOMUtil.createDOMNode(new ByteArrayInputStream(xmlParam1.getBytes()));
                String metadata = "<myMetadata>This is not useful metadata</myMetadata>";
                Node n2 = createDOMNodeNoNS(new ByteArrayInputStream(metadata.getBytes()));
                W3CEndpointReferenceBuilder eprBuilder = new W3CEndpointReferenceBuilder();
                eprBuilder.address(endpointAddress);
                eprBuilder.referenceParameter((Element) n1.getFirstChild());
                eprBuilder.metadata((Element)n2.getFirstChild());
                W3CEndpointReference epr = eprBuilder.build();
                epr.writeTo(new StreamResult(System.out));
            } catch (Exception e) {
                e.printStackTrace();
                assertTrue(false);
            }
    }
    
    public static Node createDOMNodeNoNS(InputStream inputStream) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            try {
                return builder.parse(inputStream);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ParserConfigurationException pce) {
            IllegalArgumentException iae = new IllegalArgumentException(pce.getMessage());
            iae.initCause(pce);
            throw iae;
        }
        return null;
    }

    private String endpointAddress = "http://helloservice.org/Hello";
    private static final QName serviceName = new QName("http://helloservice.org/wsdl", "HelloService");
    private static final QName portName = new QName("http://helloservice.org/wsdl", "HelloPort");
    private static final QName portTypeName = new QName("http://helloservice.org/wsdl", "Hello");

    private static JAXBContext jaxbCtx;

    static {
        try {
            jaxbCtx = JAXBContext.newInstance(W3CEndpointReference.class, MemberSubmissionEndpointReference.class);
        } catch (JAXBException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

}
