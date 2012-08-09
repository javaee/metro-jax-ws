package provider.xmlbind_649.client;

import junit.framework.TestCase;

import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Client sends a specific Content-Type
 *
 * @author Jitendra Kotamraju
 */
public class PutTest extends TestCase {

    public void testPut() {
        BindingProvider bp = (BindingProvider)(new Hello_Service().getHelloPort());
        String address =
            (String)bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

        // Use the above address for a new client
        QName serviceName = new QName("");
        QName portName = new QName("");
        Service service = Service.create(serviceName);
        service.addPort(portName, HTTPBinding.HTTP_BINDING, address);
        Dispatch<DataSource> d = service.createDispatch(portName, DataSource.class,
                Service.Mode.MESSAGE);
        setupHTTPHeaders(d);
        d.invoke(getDataSource());
        Map<String, Object> rc = d.getResponseContext();
        assertEquals(200, rc.get(MessageContext.HTTP_RESPONSE_CODE));
    }

    private void setupHTTPHeaders(Dispatch<DataSource> d) {
        Map<String, Object> requestContext = d.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, "PUT");
    }

    private DataSource getDataSource() {
        return new DataSource() {
            public String getContentType() {
                return "application/atom+xml";
            }

            public InputStream getInputStream() {
                return new ByteArrayInputStream("<a/>".getBytes());
            }

            public String getName() {
                return null;
            }

            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
