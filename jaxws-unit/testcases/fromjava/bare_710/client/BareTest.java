package fromjava.bare_710.client;

import junit.framework.TestCase;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.io.InputStream;

/**
 * fromjava doc/bare testing
 *
 * @author Jitendra Kotamraju
 */
public class BareTest extends TestCase {
    EchoService service;
    Echo port;

    @Override
    public void setUp() {
        service = new EchoService();
        port = service.getEchoPort();
    }

    public void testAddNumbers() throws Exception {
        NumbersRequest numbersRequest = new NumbersRequest();
        numbersRequest.number1 = 10;
        numbersRequest.number2 = 20;
        numbersRequest.guess = 25;
        Holder<Integer> addNumbersResponse = new Holder<Integer>();
        port.addNumbers(numbersRequest, addNumbersResponse);
        assertEquals((Integer)30, addNumbersResponse.value);
    }

    // check wsdl:part for addNumbers()
    public void testAddNumbersParts() throws Exception {
        List<Part> in = getParts("addNumbers");
        assertEquals(1, in.size());
        Part inPart = in.get(0);
        assertEquals("addNumbers", inPart.partName);
        assertEquals("tns:addNumbers", inPart.elementName);

        List<Part> out = getParts("addNumbersResponse");
        assertEquals(1, out.size());
        Part outPart = out.get(0);
        assertEquals("addNumbersResponse", outPart.partName);
        assertEquals("tns:addNumbersResponse", outPart.elementName);
    }

    public void testEchoString() throws Exception {
        Holder<String> str = new Holder<String>("test");
        port.echoString(str);
        assertEquals("test", str.value);
    }

    // check wsdl:part for echoString()
    public void testEchoStringParts() throws Exception {
        List<Part> in = getParts("echoString");
        assertEquals(1, in.size());
        Part inPart = in.get(0);
        assertEquals("echoString", inPart.partName);
        assertEquals("tns:echoString", inPart.elementName);

        List<Part> out = getParts("echoStringResponse");
        assertEquals(1, out.size());
        Part outPart = out.get(0);
        assertEquals("echoString", outPart.partName);
        assertEquals("tns:echoString", outPart.elementName);
    }

    public void testEchoHeaders() throws Exception {
        Holder<String> str = new Holder<String>("test");
        Holder<String> outHeader = new Holder<String>();
        Holder<String> inoutHeader = new Holder<String>("inoutHeader");
        String returnHeader = port.echoHeaders(str, "inHeader", outHeader, inoutHeader);
        assertEquals("returnHeader", returnHeader);
        assertEquals("outHeader", outHeader.value);
        assertEquals("inoutHeader", inoutHeader.value);
    }

    // check wsdl:part for echoString()
    public void testEchoHeadersParts() throws Exception {
        List<Part> in = getParts("echoHeaders");
        assertEquals(3, in.size());
        Part inPart = in.get(0);
        assertEquals("echoHeaders", inPart.partName);
        assertEquals("tns:echoHeaders", inPart.elementName);
        inPart = in.get(1);
        assertEquals("arg1", inPart.partName);
        assertEquals("tns:arg1", inPart.elementName);
        inPart = in.get(2);
        assertEquals("arg3", inPart.partName);
        assertEquals("tns:arg3", inPart.elementName);

        List<Part> out = getParts("echoHeadersResponse");
        assertEquals(4, out.size());
        Part outPart = out.get(0);
        assertEquals("echoHeadersResponse", outPart.partName);
        assertEquals("tns:echoHeadersResponse", outPart.elementName);
        outPart = out.get(1);
        assertEquals("echoHeaders", outPart.partName);     // body part
        assertEquals("tns:echoHeaders", outPart.elementName);
        outPart = out.get(2);
        assertEquals("arg2", outPart.partName);
        assertEquals("tns:arg2", outPart.elementName);
        outPart = out.get(3);
        assertEquals("arg3", outPart.partName);
        assertEquals("tns:arg3", outPart.elementName);
    }

    private static final QName WSDL_MESSAGE = new QName("http://schemas.xmlsoap.org/wsdl/", "message");
    private static final QName WSDL_PART = new QName("http://schemas.xmlsoap.org/wsdl/", "part");

    // returns all the parts in a wsdl:message
    private List<Part> getParts(String messageName) throws Exception {
        URL url = service.getWSDLDocumentLocation();
        InputStream is = url.openStream();
        XMLStreamReader r = XMLInputFactory.newInstance().createXMLStreamReader(is);
        
        boolean inMsg = false;
        List<Part> parts = new ArrayList<Part>();
        while(r.hasNext()) {
            int event = r.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (r.getName().equals(WSDL_MESSAGE) &&
                    r.getAttributeValue(null, "name").equals(messageName)) {
                    inMsg = true;
                } else if (inMsg && r.getName().equals(WSDL_PART)) {
                    Part part = new Part(r.getAttributeValue(null, "name"),
                        r.getAttributeValue(null, "element"));
                    parts.add(part);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (r.getName().equals(WSDL_MESSAGE)) {
                    inMsg = false;
                }
            }
        }
        r.close();
        is.close();
        return parts;
    }

    private static class Part {
        final String partName;
        final String elementName;   // ideally, it should be QName

        Part(String partName, String elementName) {
            this.partName = partName;
            this.elementName = elementName;
        }

        @Override
        public String toString() {
            return "name="+partName+" element="+elementName;
        }
    }

}
