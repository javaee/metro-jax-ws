package fromjava.bare_710.client;

import junit.framework.TestCase;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.io.InputStream;

/**
 * @author Jitendra Kotamraju
 */
public class BareTest extends TestCase {

    public void testAddNumbers() throws Exception {
        System.out.println("**** PARTS *****"+getParts("addNumbers"));
    }

    private static final QName WSDL_MESSAGE = new QName("http://schemas.xmlsoap.org/wsdl/", "message");
    private static final QName WSDL_PART = new QName("http://schemas.xmlsoap.org/wsdl/", "part");

    // returns
    private List<Part> getParts(String messageName) throws Exception {
        EchoService service = new EchoService();
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
        final String elementName;

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
