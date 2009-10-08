package provider.attachment_595.client;

import junit.framework.TestCase;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.soap.*;
import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;

/**
 *
 * @author Jitendra Kotamraju
 */
public class AttachmentTest extends TestCase {
    private Dispatch<SOAPMessage> dispatch;

    public AttachmentTest(String name) throws Exception {
        super(name);
    }

    protected void setUp() throws Exception {
        Service service = new Hello_Service();
        QName portName = new QName("urn:test", "HelloPort");
        dispatch = service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
    }

    public void testProxy() {
        Hello hello = new Hello_Service().getHelloPort();
        hello.hello(new Hello_Type());
    }

//    public void testSOAP() throws Exception {
//        dispatch.invoke(getSOAPMessage());
//    }

    private SOAPMessage getSOAPMessage() throws Exception {
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
        SOAPMessage msg = MessageFactory.newInstance().createMessage(headers, getResource("normal.envelope"));
        msg.saveChanges();
        return msg;
    }

    private InputStream getResource(String file) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(file);
        return is;
    }
}
