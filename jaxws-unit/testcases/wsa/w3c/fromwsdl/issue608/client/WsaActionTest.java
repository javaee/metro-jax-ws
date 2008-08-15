package wsa.w3c.fromwsdl.issue608.client;

import junit.framework.TestCase;

import javax.xml.ws.BindingProvider;

import testutil.HTTPResponseInfo;
import testutil.ClientServerTestUtil;

/**
 * Tests conformance of BP 1.2 conformace requirement R1144
 * (http://www.ws-i.org/Profiles/BasicProfile-1_2(WGAD).html#Valid_Range_of_Values_for_SOAPAction_When_WS-Addressing_is_Used)
 *
 * @author Rama Pulavarthi
 */
public class WsaActionTest extends TestCase {
    public WsaActionTest(String name) throws Exception {
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

    /**
     *   Useful for creating sample messages
     */
    public void testSimple() throws Exception {
        AddNumbersPortType proxy = getStub();
        assertEquals(4,proxy.addNumbers(1,3));
    }

    /**
     * SOAPAction HTTP header is not sent in HTTPRequest
     * @throws Exception
     */
    public void testNoSOAPAction() throws Exception {
    String address= getEndpointAddress();
    String message = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">"+address+"</To>" +
            "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">urn:com:example:action</Action>" +
            "<ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:4a7bee60-cd49-4345-a25b-35fb6a5f2e36</MessageID>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\"><number1>1</number1><number2>3</number2></addNumbers></S:Body></S:Envelope>";

    HTTPResponseInfo rInfo =
        ClientServerTestUtil.sendPOSTRequest( address, message,"text/xml", null );

    assertEquals(200, rInfo.getResponseCode());
    }

    /**
     * SOAPAction: "" is sent as HTTP header
     * @throws Exception
     */
    public void testEmptyStringSOAPAction() throws Exception {
    String address= getEndpointAddress();
    String message = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">"+address+"</To>" +
            "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">urn:com:example:action</Action>" +
            "<ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:4a7bee60-cd49-4345-a25b-35fb6a5f2e36</MessageID>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\"><number1>1</number1><number2>3</number2></addNumbers></S:Body></S:Envelope>";

    HTTPResponseInfo rInfo =
        ClientServerTestUtil.sendPOSTRequest( address, message,"text/xml", "\"\"" );

    assertEquals(200, rInfo.getResponseCode());
    }

    /**
     * SOAPAction:  (with no value) is sent as HTTP header
     * @throws Exception
     */
    public void testEmptySOAPAction() throws Exception {
    String address= getEndpointAddress();
    String message = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">"+address+"</To>" +
            "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">urn:com:example:action</Action>" +
            "<ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:4a7bee60-cd49-4345-a25b-35fb6a5f2e36</MessageID>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\"><number1>1</number1><number2>3</number2></addNumbers></S:Body></S:Envelope>";

    HTTPResponseInfo rInfo =
        ClientServerTestUtil.sendPOSTRequest( address, message,"text/xml", "" );

    assertEquals(200, rInfo.getResponseCode());
    }

    /**
     * SOAPAction HTTP header same as SOAPAction defined in WSDL, but sent as unquoted String 
     * @throws Exception
     */
    public void testNonEmptyUnquotedSOAPactionSameAsWSDLSOAPAction() throws Exception {
    String address= getEndpointAddress();
    String message = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">"+address+"</To>" +
            "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">urn:com:example:action</Action>" +
            "<ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:4a7bee60-cd49-4345-a25b-35fb6a5f2e36</MessageID>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\"><number1>1</number1><number2>3</number2></addNumbers></S:Body></S:Envelope>";

    HTTPResponseInfo rInfo =
        ClientServerTestUtil.sendPOSTRequest( address, message,"text/xml", "urn:com:example:action" );

    assertEquals(200, rInfo.getResponseCode());
    }

    /**
     * SOAPAction HTTP header same as SOAPAction defined in WSDL sent as quoted.
     * @throws Exception
     */
    public void testNonEmptyQuotedSOAPactionSameAsWSDLSOAPAction() throws Exception {
    String address= getEndpointAddress();
    String message = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">"+address+"</To>" +
            "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">urn:com:example:action</Action>" +
            "<ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:4a7bee60-cd49-4345-a25b-35fb6a5f2e36</MessageID>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\"><number1>1</number1><number2>3</number2></addNumbers></S:Body></S:Envelope>";

    HTTPResponseInfo rInfo =
        ClientServerTestUtil.sendPOSTRequest( address, message,"text/xml", "\"urn:com:example:action\"" );

    assertEquals(200, rInfo.getResponseCode());
    }

    /**
     * SOAPAction HTTP header different from SOAPAction defined in WSDL sent as quoted.
     *
     * Issue jax-ws-608: JAX-WS Runtime should validate wsa:Action with SOAPAction HTTP header instead of
     * WSDL soapAction
     *
     * @throws Exception
     */

    public void testNonEmptyQuotedSOAPactionDiffFromWSDLSOAPAction() throws Exception {
    String address= getEndpointAddress();
    String message = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">"+address+"</To>" +
            "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">urn:com:example:action</Action>" +
            "<ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:4a7bee60-cd49-4345-a25b-35fb6a5f2e36</MessageID>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\"><number1>1</number1><number2>3</number2></addNumbers></S:Body></S:Envelope>";

    HTTPResponseInfo rInfo =
        ClientServerTestUtil.sendPOSTRequest( address, message,"text/xml", "\"urn:com:different:action\"" );

    assertEquals(200, rInfo.getResponseCode());
    }

}
