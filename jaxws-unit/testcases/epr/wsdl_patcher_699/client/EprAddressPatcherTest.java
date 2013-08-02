package epr.wsdl_patcher_699.client;

import junit.framework.TestCase;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.net.URL;
import java.util.Iterator;

/**
 * Tests if wsa:Address is patched correctly
 *
 * @author Jitendra Kotamraju
 */
public class EprAddressPatcherTest extends TestCase {
    Hello_Service service;

    public EprAddressPatcherTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        service = new Hello_Service();
    }

    public void testAddress() throws Exception {
        URL url = service.getWSDLDocumentLocation();
        String DONOT_MODIFY = "DONOT_MODIFY_THIS";
        String MODIFY = "REPLACE_WITH_URL";
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContextImpl());

        XPathExpression xpe = xpath.compile("/wsdl:definitions/sp:Issuer/wsa:Address/text()");
        InputSource inputSource = new InputSource(url.openStream());
        assertEquals(DONOT_MODIFY, xpe.evaluate(inputSource).trim());

        xpe = xpath.compile("/wsdl:definitions/wsdl:service/wsdl:port/soap:address/@location");
        inputSource = new InputSource(url.openStream());
        assertNotSame(MODIFY, xpe.evaluate(inputSource).trim());

        xpe = xpath.compile("/wsdl:definitions/wsdl:service/wsdl:port/wsa:EndpointReference/wsa:Address/text()");
        inputSource = new InputSource(url.openStream());
        assertNotSame(MODIFY, xpe.evaluate(inputSource).trim());

        xpe = xpath.compile("/wsdl:definitions/wsdl:service/wsdl:port/sp:Issuer/wsa:Address/text()");
        inputSource = new InputSource(url.openStream());
        assertEquals(DONOT_MODIFY, xpe.evaluate(inputSource).trim());

        xpe = xpath.compile("/wsdl:definitions/sp:Issuer[2]/wsa:Address/text()");
        inputSource = new InputSource(url.openStream());
        assertEquals(DONOT_MODIFY, xpe.evaluate(inputSource).trim());
    }

    private static class NamespaceContextImpl implements NamespaceContext {
        public String getNamespaceURI(String prefix) {
            if ("wsdl".equals(prefix))
                return "http://schemas.xmlsoap.org/wsdl/";
            else if ("soap".equals(prefix))
                return "http://schemas.xmlsoap.org/wsdl/soap/";
            else if ("wsa".equals(prefix))
                return "http://www.w3.org/2005/08/addressing";
            else if ("sp".equals(prefix))
                return "urn:test:sp";
            return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String namespaceURI) {
            throw new UnsupportedOperationException();
        }

        public Iterator getPrefixes(String namespaceURI) {
            throw new UnsupportedOperationException();
        }
    }

}
