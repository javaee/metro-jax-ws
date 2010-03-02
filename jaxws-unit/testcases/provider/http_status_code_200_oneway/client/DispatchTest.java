package provider.http_status_code_200_oneway.client;

import junit.framework.TestCase;

import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Issue WSIT:1354
 *
 * @author Jitendra Kotamraju
 */
public class DispatchTest extends TestCase {

    public DispatchTest(String name) throws Exception {
        super(name);
    }

    /*
     * Service sends status code 200+no envelope
     */
    public void testNoContentType() throws Exception {
        BindingProvider bp = (BindingProvider)new Hello_Service().getHelloPort();
        String address = (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

        Service service = Service.create(new QName("", ""));
        service.addPort(new QName("",""), SOAPBinding.SOAP11HTTP_BINDING, address);
        Dispatch<SOAPMessage> d = service.createDispatch(new QName("",""), SOAPMessage.class, Service.Mode.MESSAGE);
        String str = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body/></S:Envelope>";
        MessageFactory fact = MessageFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        SOAPMessage req = fact.createMessage(headers, new ByteArrayInputStream(str.getBytes("UTF-8")));
        d.invokeOneWay(req);
    }

}
