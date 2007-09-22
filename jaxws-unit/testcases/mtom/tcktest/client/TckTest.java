package mtom.tcktest.client;

import junit.framework.TestCase;
import testutil.AttachmentHelper;
import testutil.ClientServerTestUtil;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

import com.sun.xml.ws.developer.JAXWSProperties;

/**
 * @author Jitendra Kotamraju
 */
public class TckTest extends TestCase {

    private Hello proxy;

    public TckTest(String name) throws Exception{
        super(name);
        proxy = new HelloService().getHelloPort(new MTOMFeature());
        ClientServerTestUtil.setTransport(proxy);
    }

    public void testMtomIn() throws Exception {
        DataType dt = new DataType();
        dt.setDoc1(getSource("gpsXml.xml"));
        dt.setDoc2(getSource("gpsXml.xml"));
        // This not working since DCH is not registerd by JAX-WS
        //dt.setDoc3(new DataHandler(getSource("gpsXml.xml"), "text/xml"));
        dt.setDoc3(getDataHandler("gpsXml.xml"));
        dt.setDoc4(getImage("java.jpg"));

        String works = proxy.mtomIn(dt);
        assertEquals("works", works);
    }

    private Image getImage(String image) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(image);
        return javax.imageio.ImageIO.read(is);
    }

    private StreamSource getSource(String file) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(file);
        return new StreamSource(is);
    }

    private DataHandler getDataHandler(final String file) throws Exception {
        return new DataHandler(new DataSource() {
            public String getContentType() {
                return "text/html";
            }

            public InputStream getInputStream() {
                return getClass().getClassLoader().getResourceAsStream(file);
            }

            public String getName() {
                return null;
            }

            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException();
            }
        });
    }

}
