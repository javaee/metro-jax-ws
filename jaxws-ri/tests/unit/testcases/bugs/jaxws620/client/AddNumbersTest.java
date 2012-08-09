package bugs.jaxws620.client;

import bugs.jaxws620.client.soap11.Envelope;
import junit.framework.TestCase;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;


public class AddNumbersTest extends TestCase {
    
    private static final Logger LOGGER = Logger.getLogger(AddNumbersTest.class.getName());

    private static final QName portQName = new QName("http://duke.example.org", "AddNumbersPort");

    //SOAP MESSAGEs
    private static String messageRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><addNumbers xmlns=\"http://duke.example.org\"><arg0>10</arg0><arg1>20</arg1></addNumbers></soapenv:Body></soapenv:Envelope>";
    private static String messageResponse = "<?xml version=\"1.0\" ?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><addNumbersResponse xmlns=\"http://duke.example.org\"><return>30</return></addNumbersResponse></S:Body></S:Envelope>";

    // MESSAGE PAYLOADs
    private static String payloadRequest = "<addNumbers xmlns=\"http://duke.example.org\"><arg0>10</arg0><arg1>20</arg1></addNumbers>";
    private static String payloadResponse = "<ns0:addNumbersResponse xmlns:ns0=\"http://duke.example.org\"><return>30</return></ns0:addNumbersResponse>";

    public void testNonDispatch() {
        AddNumbers port = new AddNumbersImplService().getAddNumbersPort();
        int res = port.addNumbers(1, 2);
        LOGGER.finest("1+2 = " + res);
        assertTrue(res == 3);
    }

    @SuppressWarnings("unchecked")
    public void testModeMESSAGE() throws JAXBException {
        doDispatchTesting("bugs.jaxws620.client.soap11", messageRequest, Service.Mode.MESSAGE, Envelope.class);
    }

    @SuppressWarnings("unchecked")
    public void testModePAYLOAD() throws JAXBException {
        doDispatchTesting("bugs.jaxws620.client", payloadRequest, Service.Mode.PAYLOAD, AddNumbersResponse.class);
    }

    private void doDispatchTesting(String contextPath, String request, Service.Mode mode, Class expectedClass) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(contextPath);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        Object obj = u.unmarshal(new StreamSource(new StringReader(request)));

        LOGGER.finest("---Marshaller parsed and wrote back----\n" + reconstructMsg(jaxbContext, obj) + "\n---------\n");

        Service service = new AddNumbersImplService();

        Dispatch dispatch = service.createDispatch(portQName, jaxbContext, mode);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "run:tns:AddNumbersService");

        JAXBElement response = (JAXBElement) dispatch.invoke(obj);
        assertTrue(expectedClass.equals(response.getDeclaredType()));

        LOGGER.finest("---result:" + response.getValue().toString() + "---\n"
                + reconstructMsg(jaxbContext, response) + "\n---------\n");
    }

    private String reconstructMsg(JAXBContext jaxbContext, Object obj) throws JAXBException {
        Marshaller m = jaxbContext.createMarshaller();
        StringWriter writer = new StringWriter();
        m.marshal(obj, writer);
        return writer.toString();
    }

}


