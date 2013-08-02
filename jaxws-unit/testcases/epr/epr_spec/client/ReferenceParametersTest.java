/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package epr.epr_spec.client;

import com.sun.xml.ws.addressing.W3CAddressingConstants;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.message.saaj.SAAJMessage;
import com.sun.xml.ws.util.DOMUtil;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import junit.framework.TestCase;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import javax.xml.ws.EndpointReference;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Rama Pulavarthi
 */

public class ReferenceParametersTest extends TestCase {
    public ReferenceParametersTest(String name) {
        super(name);
    }
     public void testReferenceParameters1() throws Exception {
        String xmlRefParam1 = "<myns:MyParam1 wsa:IsReferenceParameter='true' xmlns:myns=\"http://cptestservice.org/wsdl\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">Hello</myns:MyParam1>";
        String xmlRefParam2 = "<myns:MyParam2 wsa:IsReferenceParameter='true' xmlns:myns=\"http://cptestservice.org/wsdl\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">There</myns:MyParam2>";
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Header>" +
                xmlRefParam1 + xmlRefParam2 +
                "</S:Header><S:Body><DataType xmlns=\"http://cptestservice.org/xsd\"><param>foo bar</param></DataType></S:Body></S:Envelope>";
        MessageFactory messageFactory = MessageFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
        SOAPMessage soapMsg = messageFactory.createMessage(headers,
                new ByteArrayInputStream(request.getBytes()));
        //soapMsg.writeTo(System.out);
        Packet p = new Packet(new SAAJMessage(soapMsg));
        List<Element> refParams = p.getReferenceParameters();
        assertEquals("Did n't get expected ReferenceParameters",2,refParams.size());
        for(Element e: refParams) {
            assertTrue("isReferenceParameter attribute not present on header", e.getAttributeNodeNS(W3CAddressingConstants.WSA_NAMESPACE_NAME,"IsReferenceParameter") != null);
        }
    }

    public void testReferenceParameters2() throws Exception {
        String xmlParam1 = "<myns:MyParam1 xmlns:myns=\"http://cptestservice.org/wsdl\">Hello</myns:MyParam1>";
        String xmlParam2 = "<myns:MyParam2 xmlns:myns=\"http://cptestservice.org/wsdl\"><myns:InnerEl> Hello Hello </myns:InnerEl></myns:MyParam2>";
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><DataType xmlns=\"http://cptestservice.org/xsd\"><param>foo bar</param></DataType></S:Body></S:Envelope>";
        MessageFactory messageFactory = MessageFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
        SOAPMessage soapMsg = messageFactory.createMessage(headers,
                new ByteArrayInputStream(request.getBytes()));
        Packet p = new Packet(new SAAJMessage(soapMsg));
        List<Element> refparams = new ArrayList<Element>();
        Node n1 = DOMUtil.createDOMNode(new ByteArrayInputStream(xmlParam1.getBytes()));
        Node n2 = DOMUtil.createDOMNode(new ByteArrayInputStream(xmlParam2.getBytes()));
        refparams.add((Element)n1.getFirstChild());
        refparams.add((Element)n2.getFirstChild());
        WSEndpointReference wsepr = new WSEndpointReference(AddressingVersion.W3C,"http://foo.bar",null,null,null,null,null,refparams);
        wsepr.addReferenceParameters(p.getMessage().getHeaders());
        //p.getMessage().writeTo(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));

        List<Element> refParams = p.getReferenceParameters();
        assertEquals("Did n't get expected ReferenceParameters",2,refParams.size());
        for(Element e: refParams) {
            assertTrue("isReferenceParameter attribute not present on header", e.getAttributeNodeNS(W3CAddressingConstants.WSA_NAMESPACE_NAME,"IsReferenceParameter") != null);
        }


    }

    /**
     * Test where NS decl for ReferenceParameters is on the SOAP envelope
     * @throws Exception
     */
    public void testReferenceParametersNsDecl() throws Exception {
        String xmlRefParam1 = "<myns:MyParam1 wsa:IsReferenceParameter='true' xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">Hello</myns:MyParam1>";
        String xmlRefParam2 = "<myns:MyParam2 wsa:IsReferenceParameter='true' xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">There</myns:MyParam2>";
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:myns=\"http://cptestservice.org/wsdl\"><S:Header>" +
                xmlRefParam1 + xmlRefParam2 +
                "</S:Header><S:Body><DataType xmlns=\"http://cptestservice.org/xsd\"><param>foo bar</param></DataType></S:Body></S:Envelope>";
        MessageFactory messageFactory = MessageFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
        SOAPMessage soapMsg = messageFactory.createMessage(headers,
                new ByteArrayInputStream(request.getBytes()));
        //soapMsg.writeTo(System.out);
        Packet p = new Packet(new SAAJMessage(soapMsg));
        List<Element> refParams = p.getReferenceParameters();
        assertEquals("Did n't get expected ReferenceParameters",2,refParams.size());
        for(Element e: refParams) {
            assertTrue("isReferenceParameter attribute not present on header", e.getAttributeNodeNS(W3CAddressingConstants.WSA_NAMESPACE_NAME,"IsReferenceParameter") != null);
            assertEquals("NS Decl did not match", "http://cptestservice.org/wsdl", e.getNamespaceURI());
        }
    }
    /**
     * See if the ReferenceParamaters get added properly from ReplyTo EPR to response
     * @throws Exception
     */
    public void testReplyToReferenceParameters() throws Exception {
        String requestStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:user=\"http://foo.bar\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
                "<S:Header>" +
                "<wsa:Action>http://example.org/action/echoIn</wsa:Action>" +
                "<wsa:To>http://www.w3.org/2005/08/addressing/anonymous</wsa:To>" +
                "<wsa:MessageID>urn:uuid:1234567890</wsa:MessageID>" +
                "<wsa:ReplyTo>" +
                "<wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>" +
                "<wsa:ReferenceParameters>" +
                "<user:foo wsa:IsReferenceParameter='true'>bar</user:foo>" +
                "<user:bar>" +
                "<user:foobar>barfoo</user:foobar>" +
                "</user:bar>" +
                "</wsa:ReferenceParameters>" +
                "</wsa:ReplyTo>" +                
                "</S:Header>" +
                "<S:Body><DataType xmlns=\"http://foo.org/xsd\"><param>foo bar</param></DataType></S:Body></S:Envelope>";
        MessageFactory messageFactory = MessageFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
        SOAPMessage soapMsg = messageFactory.createMessage(headers,
                new ByteArrayInputStream(requestStr.getBytes()));
        Message message = new SAAJMessage(soapMsg);
        WSEndpointReference wsepr = message.getHeaders().getReplyTo(AddressingVersion.W3C, SOAPVersion.SOAP_11);

        String responseStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><DataTypeResponse xmlns=\"http://cptestservice.org/xsd\"><param>foo bar</param></DataTypeResponse></S:Body></S:Envelope>";
        MimeHeaders headers1 = new MimeHeaders();
        headers1.addHeader("Content-Type", "text/xml");
        SOAPMessage soapMsg1 = messageFactory.createMessage(headers1,
                        new ByteArrayInputStream(responseStr.getBytes()));
        Message m2 = new SAAJMessage(soapMsg1);
        wsepr.addReferenceParameters(m2.getHeaders());
        Packet response = new Packet(m2);
        List<Element> refParams = response.getReferenceParameters();
        assertEquals("Did n't get expected ReferenceParameters",2,refParams.size());
        for(Element e: refParams) {
            assertTrue("isReferenceParameter attribute not present on header", e.getAttributeNodeNS(W3CAddressingConstants.WSA_NAMESPACE_NAME,"IsReferenceParameter") != null);
            assertEquals("NS Decl did not match", "http://foo.bar", e.getNamespaceURI());
        }

    }

    /**
     * See if the ReferenceParamaters get added proeprly from FaultToEPR to fault response
     * @throws Exception
     */
    public void testFaultToReferenceParameters() throws Exception {
        String requestStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:user=\"http://foo.bar\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
                "<S:Header>" +
                "<wsa:Action>http://example.org/action/echoIn</wsa:Action>" +
                "<wsa:To>http://www.w3.org/2005/08/addressing/anonymous</wsa:To>" +
                "<wsa:MessageID>urn:uuid:1234567890</wsa:MessageID>" +
                "<wsa:FaultTo>" +
                "<wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>" +
                "<wsa:ReferenceParameters>" +
                "<user:foo wsa:IsReferenceParameter='true'>bar</user:foo>" +
                "<user:bar>" +
                "<user:foobar>barfoo</user:foobar>" +
                "</user:bar>" +
                "</wsa:ReferenceParameters>" +
                "</wsa:FaultTo>" +
                "</S:Header>" +
                "<S:Body><DataType xmlns=\"http://foo.org/xsd\"><param>foo bar</param></DataType></S:Body></S:Envelope>";
        MessageFactory messageFactory = MessageFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
        SOAPMessage soapMsg = messageFactory.createMessage(headers,
                new ByteArrayInputStream(requestStr.getBytes()));
        Message message = new SAAJMessage(soapMsg);
        WSEndpointReference wsepr = message.getHeaders().getFaultTo(AddressingVersion.W3C, SOAPVersion.SOAP_11);

        String responseStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><S:Fault>" +
                "<faultcode>echo:EmptyEchoString</faultcode>" +
                "<faultstring>The echo string was empty.</faultstring>" +
                "</S:Fault></S:Body></S:Envelope>";
        MimeHeaders headers1 = new MimeHeaders();
        headers1.addHeader("Content-Type", "text/xml");
        SOAPMessage soapMsg1 = messageFactory.createMessage(headers1,
                        new ByteArrayInputStream(responseStr.getBytes()));
        Message m2 = new SAAJMessage(soapMsg1);
        wsepr.addReferenceParameters(m2.getHeaders());
        Packet response = new Packet(m2);
        List<Element> refParams = response.getReferenceParameters();
        assertEquals("Did n't get expected ReferenceParameters",2,refParams.size());
        for(Element e: refParams) {
            assertTrue("isReferenceParameter attribute not present on header", e.getAttributeNodeNS(W3CAddressingConstants.WSA_NAMESPACE_NAME,"IsReferenceParameter") != null);
            assertEquals("NS Decl did not match", "http://foo.bar", e.getNamespaceURI());
        }

    }

    /**
     * See if the ReferenceParamaters get added proeprly from FaultToEPR to fault response
     * @throws Exception
     */
    public void testFaultToReferenceParametersStreamMessage1() throws Exception {
        String requestStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:user=\"http://foo.bar\" " +
                "xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
                "<S:Header>" +
                "<wsa:Action>http://example.org/action/echoIn</wsa:Action>" +
                "<wsa:To>http://www.w3.org/2005/08/addressing/anonymous</wsa:To>" +
                "<wsa:MessageID>urn:uuid:1234567890</wsa:MessageID>" +
                "<wsa:FaultTo>" +
                "<wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>" +
                "<wsa:ReferenceParameters>" +
                "<user:foo xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" wsa:IsReferenceParameter='true'>bar</user:foo>" +
                "<user:bar xmlns:user=\"http://foo.bar\">" +
                "<user:foobar>barfoo</user:foobar>" +
                "</user:bar>" +
                "</wsa:ReferenceParameters>" +
                "</wsa:FaultTo>" +
                "</S:Header>" +
                "<S:Body><DataType xmlns=\"http://foo.org/xsd\"><param>foo bar</param></DataType></S:Body></S:Envelope>";
        Message message = useStreamCodec(requestStr);
        WSEndpointReference wsepr = message.getHeaders().getFaultTo(AddressingVersion.W3C, SOAPVersion.SOAP_11);

        String responseStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><S:Fault>" +
                "<faultcode>echo:EmptyEchoString</faultcode>" +
                "<faultstring>The echo string was empty.</faultstring>" +
                "</S:Fault></S:Body></S:Envelope>";
        Message m2 = useStreamCodec(responseStr);
        wsepr.addReferenceParameters(m2.getHeaders());
        ByteArrayBuffer baos = new ByteArrayBuffer();
        XMLStreamWriter writer = XMLStreamWriterFactory.create(baos);
        m2.writeTo(writer);
        writer.flush();
        XMLInputFactory readerFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = readerFactory.createXMLStreamReader(baos.newInputStream());
        Message sm = Messages.create(reader);
        Packet sm_packet = new Packet(sm);
        List<Element> refParams = sm_packet.getReferenceParameters();
        assertEquals("Did n't get expected ReferenceParameters",2,refParams.size());
        for(Element e: refParams) {
            assertTrue("isReferenceParameter attribute not present on header", e.getAttributeNodeNS(W3CAddressingConstants.WSA_NAMESPACE_NAME,"IsReferenceParameter") != null);
            assertEquals("NS Decl did not match", "http://foo.bar", e.getNamespaceURI());
        }
    }

    public void testFaultToReferenceParametersJAXBMessage1() throws Exception {
        String requestStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:user=\"http://foo.bar\" " +
                "xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
                "<S:Header>" +
                "<wsa:Action>http://example.org/action/echoIn</wsa:Action>" +
                "<wsa:To>http://www.w3.org/2005/08/addressing/anonymous</wsa:To>" +
                "<wsa:MessageID>urn:uuid:1234567890</wsa:MessageID>" +
                "<wsa:FaultTo>" +
                "<wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>" +
                "<wsa:ReferenceParameters>" +
                "<user:foo xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" wsa:IsReferenceParameter='true'>bar</user:foo>" +
                "<user:bar xmlns:user=\"http://foo.bar\">" +
                "<user:foobar>barfoo</user:foobar>" +
                "</user:bar>" +
                "</wsa:ReferenceParameters>" +
                "</wsa:FaultTo>" +
                "</S:Header>" +
                "<S:Body><DataType xmlns=\"http://foo.org/xsd\"><param>foo bar</param></DataType></S:Body></S:Envelope>";
        Message message = useStreamCodec(requestStr);
        WSEndpointReference wsepr = message.getHeaders().getFaultTo(AddressingVersion.W3C, SOAPVersion.SOAP_11);
        Message m2 = Messages.create("Test Unsupported",AddressingVersion.W3C,SOAPVersion.SOAP_11);
        wsepr.addReferenceParameters(m2.getHeaders());
        ByteArrayBuffer baos = new ByteArrayBuffer();
        XMLStreamWriter writer = XMLStreamWriterFactory.create(baos);
        m2.writeTo(writer);
        writer.flush();
        
        XMLInputFactory readerFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = readerFactory.createXMLStreamReader(baos.newInputStream());
        Message sm = Messages.create(reader);
        Packet sm_packet = new Packet(sm);
        List<Element> refParams = sm_packet.getReferenceParameters();
        assertEquals("Did n't get expected ReferenceParameters",2,refParams.size());
        for(Element e: refParams) {
            assertTrue("isReferenceParameter attribute not present on header", e.getAttributeNodeNS(W3CAddressingConstants.WSA_NAMESPACE_NAME,"IsReferenceParameter") != null);
            assertEquals("NS Decl did not match", "http://foo.bar", e.getNamespaceURI());
        }
    }

    /*
     * Whitebox test for issue564   
     */
    /*
    This test has been moved to jaxws-unit test harness so that 2.1.5 tag can be used.
    
    public void testFaultToReferenceParametersJAXBMessage2() throws Exception {
        String requestStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<S:Envelope xmlns:S=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:user=\"http://foo.bar\" " +
                "xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
                "<S:Header>" +
                "<wsa:Action>http://example.org/action/echoIn</wsa:Action>" +
                "<wsa:To>http://www.w3.org/2005/08/addressing/anonymous</wsa:To>" +
                "<wsa:MessageID>urn:uuid:1234567890</wsa:MessageID>" +
                "<wsa:ReplyTo>" +
                "<wsa:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:Address>" +
                "<wsa:ReferenceParameters>" +
                "<user:foo>bar</user:foo>" +
                "<user:bar xmlns:user=\"http://foo.bar\">" +
                "<user:foobar>barfoo</user:foobar>" +
                "</user:bar>" +
                "</wsa:ReferenceParameters>" +
                "</wsa:ReplyTo>" +
                "<wsa:FaultTo>" +
                "<wsa:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:Address>" +
                "<wsa:ReferenceParameters>" +
                "<user:foo>bar</user:foo>" +
                "<user:bar xmlns:user=\"http://foo.bar\">" +
                "<user:foobar>barfoo</user:foobar>" +
                "</user:bar>" +
                "</wsa:ReferenceParameters>" +
                "</wsa:FaultTo>" +
                "</S:Header>" +
                "<S:Body>" +
                "<addNumbers xmlns=\"http://example.com/\">" +
                "<number1>10</number1>" +
                "<number2>10</number2>" +
                "</addNumbers>" +
                "</S:Body></S:Envelope>";
        Message message = useStream12Codec(requestStr);
        WSEndpointReference wsepr = message.getHeaders().getFaultTo(AddressingVersion.MEMBER, SOAPVersion.SOAP_12);
        Message m2 = Messages.create("Test Unsupported",AddressingVersion.MEMBER,SOAPVersion.SOAP_12);
        wsepr.addReferenceParameters(m2.getHeaders());
        ByteArrayBuffer baos = new ByteArrayBuffer();
        XMLStreamWriter writer = XMLStreamWriterFactory.create(baos);
        m2.writeTo(writer);
        writer.flush();

        XMLInputFactory readerFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = readerFactory.createXMLStreamReader(baos.newInputStream());
        Message sm = Messages.create(reader);
        Packet sm_packet = new Packet(sm);
        List<Element> refParams = sm_packet.getReferenceParameters();
        assertEquals("Did n't get expected ReferenceParameters",2,refParams.size());
        for(Element e: refParams) {
            assertEquals("NS Decl did not match", "http://foo.bar", e.getNamespaceURI());
        }
    }
    */
    public void testReferenceParametersConversion1() throws Exception{
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder db = docFactory.newDocumentBuilder();
        Document doc = db.newDocument();
        Element el1 = doc.createElementNS("http:foo.bar", "Element1");        

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        EndpointReference epr =
        builder.address("http://foo.bar").referenceParameter(el1).build();
        System.out.println("EPR " + epr);

        WSEndpointReference wsepr = new WSEndpointReference(epr);
        MemberSubmissionEndpointReference translated =
        wsepr.toSpec(MemberSubmissionEndpointReference.class);
        //System.out.println(translated);
        assert(translated.referenceParameters.elements.size() == 1);

    }

    public void testReferenceParametersConversion2() throws Exception{
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder db = docFactory.newDocumentBuilder();
        Document doc = db.newDocument();
        Element el1 = doc.createElementNS("http:foo.bar", "Element1");
        Element el2 = doc.createElementNS("http:foo.bar", "Element1");

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        EndpointReference epr =
        builder.address("http://foo.bar").referenceParameter(el1).
                referenceParameter(el2).build();
        System.out.println("EPR " + epr);

        WSEndpointReference wsepr = new WSEndpointReference(epr);
        MemberSubmissionEndpointReference translated =
        wsepr.toSpec(MemberSubmissionEndpointReference.class);
        assert(translated.referenceParameters.elements.size() == 2);

    }

    public void testReferenceParametersConversion3() throws Exception{
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder db = docFactory.newDocumentBuilder();
        Document doc = db.newDocument();
        Element el1 = doc.createElementNS("http:foo.bar", "Element1");
        Element el2 = doc.createElementNS("http:foo.bar", "Element1");
        Element el3 = doc.createElementNS("http:foo.bar", "Element1");

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        EndpointReference epr =
        builder.address("http://foo.bar").referenceParameter(el1).
                referenceParameter(el2).referenceParameter(el3).build();
        System.out.println("EPR " + epr);

        WSEndpointReference wsepr = new WSEndpointReference(epr);
        MemberSubmissionEndpointReference translated =
        wsepr.toSpec(MemberSubmissionEndpointReference.class);
        assert(translated.referenceParameters.elements.size() == 3);
    }

    Message useStreamCodec(String msg) throws IOException {
        Codec codec = Codecs.createSOAPEnvelopeXmlCodec(SOAPVersion.SOAP_11);
        Packet packet = new Packet();
        ByteArrayInputStream in = new ByteArrayInputStream(msg.getBytes());
        codec.decode(in, "text/xml", packet);
        return packet.getMessage();
    }

    Message useStream12Codec(String msg) throws IOException {
        Codec codec = Codecs.createSOAPEnvelopeXmlCodec(SOAPVersion.SOAP_12);
        Packet packet = new Packet();
        ByteArrayInputStream in = new ByteArrayInputStream(msg.getBytes());
        codec.decode(in, "application/soap+xml", packet);
        return packet.getMessage();
    }
}
