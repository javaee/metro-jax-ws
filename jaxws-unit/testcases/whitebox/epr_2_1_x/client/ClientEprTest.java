package whitebox.epr_2_1_x.client;

import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
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

public class ClientEprTest extends TestCase {
    public ClientEprTest(String name) {
        super(name);
    }

    private static final String endpointAddress = "http://helloservice.org/Hello";
    private static final QName serviceName = new QName("http://helloservice.org/wsdl", "HelloService");
    private static final QName portName = new QName("http://helloservice.org/wsdl", "HelloPort");
    private static final QName portTypeName = new QName("http://helloservice.org/wsdl", "Hello");

    public void testEprWithDispatchWithoutWSDL() throws Exception{
        Service service = Service.create(serviceName);
        service.addPort(portName, javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);
        Dispatch dispatch = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        w3cEprGettertest(dispatch, false);
        msEprGettertest(dispatch, false);
    }

    public void testEprWithDispatchWithWSDL() throws Exception{
        Service service = Service.create(getClass().getResource("../config/HelloService.wsdl"), serviceName);
        Dispatch dispatch = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        w3cEprGettertest(dispatch, true);
        msEprGettertest(dispatch, true);
    }

    public void testEprWithSEI() throws Exception{
        HelloService service = new HelloService();
        Hello hello = service.getHelloPort();
        w3cEprGettertest((BindingProvider)hello, true);
        msEprGettertest((BindingProvider)hello, true);
    }

    private void w3cEprGettertest(BindingProvider bp, boolean hasWSDL) throws Exception {
        //validate w3c epr
        W3CEndpointReference w3cEpr = (W3CEndpointReference) bp.getEndpointReference();
//        printEPR(w3cEpr);
        //assertTrue(EprUtil.validateEPR(w3cEpr,endpointAddress, serviceName, portName, portTypeName, hasWSDL));
        assertTrue(EprUtil.validateEPR(w3cEpr,endpointAddress, null,null,null, false));
        //validate ms epr
        MemberSubmissionEndpointReference msEpr = bp.getEndpointReference(MemberSubmissionEndpointReference.class);
//        printEPR(msEpr);
        assertTrue(EprUtil.validateEPR(msEpr,endpointAddress, serviceName, portName, portTypeName, hasWSDL));

    }

    private void msEprGettertest(BindingProvider bp, boolean hasWSDL) throws Exception {
        Service service = Service.create(serviceName);
        service.addPort(portName, javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);
        Dispatch dispatch = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);

        //validate ms epr
        MemberSubmissionEndpointReference msEpr = bp.getEndpointReference(MemberSubmissionEndpointReference.class);
//        printEPR(msEpr);
        assertTrue(EprUtil.validateEPR(msEpr,endpointAddress, serviceName, portName, portTypeName, hasWSDL));
        W3CEndpointReference w3cEpr = bp.getEndpointReference(W3CEndpointReference.class);
//        printEPR(w3cEpr);
//        assertTrue(EprUtil.validateEPR(w3cEpr,endpointAddress, serviceName, portName, portTypeName, hasWSDL));
        assertTrue(EprUtil.validateEPR(w3cEpr,endpointAddress, null,null,null, false));
    }

    private static void printEPR(EndpointReference epr) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult sr = new StreamResult(bos);
        epr.writeTo(sr);
        bos.flush();
        System.out.println(bos);
    }


}
