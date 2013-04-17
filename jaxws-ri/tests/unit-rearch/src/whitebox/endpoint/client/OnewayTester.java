package whitebox.endpoint.client;

import java.io.StringReader;
import java.net.URL;
 
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.Oneway;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.BindingProvider;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import javax.xml.stream.*;
import java.io.*;
import java.util.*;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import junit.framework.TestCase;

/**
 * @author Jitendra Kotamraju
 */

public class OnewayTester extends TestCase
{
    private static final String NS = "http://echo.org/";

    public void testOneway() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:"+port+"/oneway";
        Endpoint endpoint = Endpoint.create(new OnewayEndpoint());
        endpoint.publish(address);

        int status = getHttpStatus(address);
        assertEquals(202, status);

        assertTrue(verify(address).contains("12345"));
        
        endpoint.stop();
    }

    private int getHttpStatus(String address) throws Exception {
        QName portName = new QName(NS, "OnewayEndpointPort");
        QName serviceName = new QName(NS, "OnewayEndpointService");
        Service service = Service.create(new URL(address+"?wsdl"), serviceName);
        Dispatch<Source> d = service.createDispatch(portName, Source.class,
            Service.Mode.PAYLOAD);
        String body = "<ns0:echoInteger xmlns:ns0='"+NS+
            "'><arg0>12345</arg0></ns0:echoInteger>";
        d.invokeOneWay(new StreamSource(new StringReader(body)));
        Map<String, Object> rc = ((BindingProvider)d).getResponseContext();
        return (Integer)rc.get(MessageContext.HTTP_RESPONSE_CODE);
    }

    private String verify(String address) throws Exception {
        QName portName = new QName(NS, "OnewayEndpointPort");
        QName serviceName = new QName(NS, "OnewayEndpointService");
        Service service = Service.create(new URL(address+"?wsdl"), serviceName);
        Dispatch<Source> d = service.createDispatch(portName, Source.class,
            Service.Mode.PAYLOAD);
        String body = "<ns0:verifyInteger xmlns:ns0='"+NS+"'/>";
        Source response = d.invoke(new StreamSource(new StringReader(body)));

        ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(response, new StreamResult(bos));
        bos.close();
        return new String(bos.toByteArray());
    }
   
    @WebService(targetNamespace="http://echo.org/")
    @SOAPBinding(style=Style.RPC)
    public static class OnewayEndpoint {
        int prev;

        @Oneway
        public void echoInteger(int arg0) {
            prev = arg0;
        }

        public int verifyInteger() {
            return prev;
        }
    }
}
