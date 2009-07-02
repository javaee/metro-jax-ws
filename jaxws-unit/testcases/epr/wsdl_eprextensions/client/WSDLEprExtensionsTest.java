package epr.wsdl_eprextensions.client;

import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import junit.framework.TestCase;
import testutil.EprUtil;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Tests runtime access of EPR extensions inside EPR specified under wsdl:port on server and client
 */
public class WSDLEprExtensionsTest extends TestCase {
    public WSDLEprExtensionsTest(String name) {
        super(name);
        Hello hello = new HelloService().getHelloPort();
        endpointAddress = (String) ((BindingProvider) hello).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    }

    private String endpointAddress = "http://helloservice.org/Hello";
    private static final QName serviceName = new QName("http://helloservice.org/wsdl", "HelloService");
    private static final QName portName = new QName("http://helloservice.org/wsdl", "HelloPort");
    private static final QName portTypeName = new QName("http://helloservice.org/wsdl", "Hello");

    /**
     * Tests client-side access to EPR extensions specified in WSDL
     * @throws Exception
     */
    public void testEprWithDispatchWithoutWSDL() throws Exception {
        Service service = Service.create(serviceName);
        service.addPort(portName, javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);
        Dispatch dispatch = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        WSEndpointReference wsepr = ((WSBindingProvider) dispatch).getWSEndpointReference();
        assertTrue(wsepr.getEPRExtensions().isEmpty());

    }

    /**
     * Tests client-side access to EPR extensions specified in WSDL
     * @throws Exception
     */
    public void testEprWithDispatchWithWSDL() throws Exception {
        Service service = new HelloService();
        Dispatch dispatch = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        WSEndpointReference wsepr = ((WSBindingProvider) dispatch).getWSEndpointReference();
        assertTrue(wsepr.getEPRExtensions().size() == 1);
        WSEndpointReference.EPRExtension idExtn = wsepr.getEPRExtension(new QName("http://schemas.xmlsoap.org/ws/2006/02/addressingidentity", "Identity"));
        assertTrue(idExtn != null && idExtn.getQName().equals(new QName("http://schemas.xmlsoap.org/ws/2006/02/addressingidentity", "Identity")));


    }

    /**
     * Tests client-side access to EPR extensions specified in WSDL
     * @throws Exception
     */
    public void testEprWithSEI() throws Exception {
        HelloService service = new HelloService();
        Hello hello = service.getHelloPort();
        WSEndpointReference wsepr = ((WSBindingProvider) hello).getWSEndpointReference();
        assertTrue(wsepr.getEPRExtensions().size() == 1);
        WSEndpointReference.EPRExtension idExtn = wsepr.getEPRExtension(new QName("http://schemas.xmlsoap.org/ws/2006/02/addressingidentity", "Identity"));
        assertTrue(idExtn != null && idExtn.getQName().equals(new QName("http://schemas.xmlsoap.org/ws/2006/02/addressingidentity", "Identity")));
    }

    /**
     * Tests server-side access to EPR extensions specified in WSDL
     * @throws Exception
     */
    public void testEprOnServerSide() throws Exception {
        HelloService service = new HelloService();
        Hello hello = service.getHelloPort();
        W3CEndpointReference serverEpr = hello.getW3CEPR();
        //printEPR(serverEpr);

        WSEndpointReference wsepr = new WSEndpointReference(serverEpr);
        assertTrue(wsepr.getEPRExtensions().size() == 1);
        WSEndpointReference.EPRExtension idExtn = wsepr.getEPRExtension(new QName("http://schemas.xmlsoap.org/ws/2006/02/addressingidentity", "Identity"));
        assertTrue(idExtn != null && idExtn.getQName().equals(new QName("http://schemas.xmlsoap.org/ws/2006/02/addressingidentity", "Identity")));
    }

    private static void printEPR(EndpointReference epr) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult sr = new StreamResult(bos);
        epr.writeTo(sr);
        bos.flush();
        System.out.println(bos);
    }


}