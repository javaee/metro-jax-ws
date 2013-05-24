/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package client.jaxws21.epr_get_port.client;

import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.util.DOMUtil;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import testutil.ClientServerTestUtil;
import testutil.XMLTestCase;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.*;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import javax.xml.transform.stream.StreamResult;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Element;


/**
 * @author Arun Gupta
 *         Kathy walsh
 */
public class AddNumbersClient extends XMLTestCase {
    //may be used for verification
    private static final QName SERVICE_QNAME = new QName("http://example.com/", "AddNumbersService");
    private static final QName PORT_QNAME = new QName("http://example.com/", "AddNumbersPort");
    private static final String ENDPOINT_ADDRESS = "http://localhost:8080/jaxrpc-client_jaxws21_epr_get_port/hello";
    //maybe used for firther tests
    // private static final String CORRECT_ACTION = "http://example.com/AddNumbersPortType/addNumbersRequest";

    public AddNumbersClient(String name) {
        super(name);
    }

    private Dispatch<SOAPMessage> createDispatchWithWSDL() throws Exception {
        AddNumbersService service = new AddNumbersService();
        return service.createDispatch(PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE);
    }

    private AddNumbersPortType createStub() throws Exception {
        AddNumbersService service = new AddNumbersService();
        return service.getAddNumbersPort();
    }

    //UsingAddressing wsdl:required=true
    public void testEPRGetPort() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }
        AddNumbersPortType proxy = createStub();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);

        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class);

        assertTrue(port != null);

        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");
    }

    //UsingAddressing wsdl:required=true
    public void testEPRGetPortII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }
        AddNumbersPortType proxy = createStub();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);

        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class);

        assertTrue(port != null);

        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");
    }

    //UsingAddressing wsdl:required=true
    //RespectBindingFeature Disabled - no effect - behavior undefined by specification
    //for backward compatability

    public void testEPRGetPortIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);

        RespectBindingFeature feature = new RespectBindingFeature(false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature};
        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);

        assertTrue(port != null);

        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");
    }

    //UsingAddressing wsdl:required=true
    //AddressingFeature Disabled expect Exception
    //Expect no valid port created, so exception thrown
    public void testEPRGetPortIV() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);

        //force addressing off
        AddressingFeature feature = new AddressingFeature(false, false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature};


        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);

        //expectation is that port is not configured for addressing and the invocation will fail
        try {
            System.out.println("Adding numbers 2 and 4");
            int result = port.addNumbers(2, 4);
        } catch (Exception ex) {
            assertTrue(ex instanceof SOAPFaultException);
            System.out.println(((SOAPFaultException) ex).getFault().getFaultString());

        }
    }


    //UsingAddressing wsdl:required=true
    //AddressingFeature Disabled expect Exception
    //Expect no valid addressingport created, so exception thrown
    public void testEPRGetPortV() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        //EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(W3CEndpointReference.class);
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);
        //force addressing off
        AddressingFeature feature = new AddressingFeature(false, false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature};


        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);

        //expectation is that port is not configured for addressing and the invocation will fail
        try {
            System.out.println("Adding numbers 2 and 4");
            int result = port.addNumbers(2, 4);
        } catch (Exception ex) {
            assertTrue(ex instanceof SOAPFaultException);
            System.out.println(((SOAPFaultException) ex).getFault().getFaultString());
        }

    }

    //UsingAddressing wsdl:required=true
    //AddressingFeature Disabled expect Exception
    //Expect no valid port created,
    public void testEPRGetPortVI() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);

        RespectBindingFeature feature = new RespectBindingFeature(true);
        MemberSubmissionAddressingFeature addr = new MemberSubmissionAddressingFeature(false, false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature, addr};


        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);

//expectation is that port is not configured for addressing and the invocation will fail
        try {
            System.out.println("Adding numbers 2 and 4");
            int result = port.addNumbers(2, 4);
        } catch (Exception ex) {
            assertTrue(ex instanceof SOAPFaultException);
            System.out.println(((SOAPFaultException) ex).getFault().getFaultString());
        }
    }


    //UsingAddressing wsdl:required=true
    //AddressingFeature Disabled expect Exception
    //Expect no valid port created, so exception thrown
    public void testEPRGetPortVII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        //EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(W3CEndpointReference.class);
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);
        RespectBindingFeature feature = new RespectBindingFeature(true);
        AddressingFeature addr = new AddressingFeature(false, false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature, addr};


        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);

//expectation is that port is not configured for addressing and the invocation will fail
        try {
            System.out.println("Adding numbers 2 and 4");
            int result = port.addNumbers(2, 4);
        } catch (Exception ex) {
            assertTrue(ex instanceof SOAPFaultException);
            System.out.println(((SOAPFaultException) ex).getFault().getFaultString());
        }

    }


    //UsingAddressing wsdl:required=true
    //AddressingFeature Disabled expect Exception
    //Expect no valid port created, so exception thrown
    public void xxtestEPRGetPortVIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);

        RespectBindingFeature feature = new RespectBindingFeature(true);
        MemberSubmissionAddressingFeature addr = new MemberSubmissionAddressingFeature(true, false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature, addr};


        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);


        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");


    }

//UsingAddressing wsdl:required=true
//AddressingFeature Disabled expect Exception
//Expect no valid port created, so exception thrown

    public void testEPRGetPortVIIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        //EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(W3CEndpointReference.class);
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);
        RespectBindingFeature feature = new RespectBindingFeature(true);
        AddressingFeature addr = new AddressingFeature(true, false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature, addr};


        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);

        //expectation is that port is not configured for addressing and the invocation will fail
        try {
            System.out.println("Adding numbers 2 and 4");
            int result = port.addNumbers(2, 4);
        } catch (Exception ex) {
            assertTrue(ex instanceof SOAPFaultException);
            System.out.println(((SOAPFaultException) ex).getFault().getFaultString());
        }

    }

    //UsingAddressing wsdl:required=true
    //AddressingFeature Disabled expect Exception
    //Expect no valid port created, so exception thrown
    public void testEPRGetPortVIIIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);

        RespectBindingFeature feature = new RespectBindingFeature(true);
        MemberSubmissionAddressingFeature addr = new MemberSubmissionAddressingFeature(true, true);
        WebServiceFeature[] features = new WebServiceFeature[]{feature, addr};


        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);

        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");

    }

//UsingAddressing wsdl:required=true
//AddressingFeature Disabled expect Exception
//Expect no valid port created, so exception thrown

    public void testEPRGetPortVIIIIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        //EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(W3CEndpointReference.class);
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);
        RespectBindingFeature feature = new RespectBindingFeature(true);
        AddressingFeature addr = new AddressingFeature(true, true);
        WebServiceFeature[] features = new WebServiceFeature[]{feature, addr};

        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);

        //expectation is that port is not configured for addressing and the invocation will fail

        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");

    }

    //UsingAddressing wsdl:required=true
    public void testDispatchEPRGetPort() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }
        Dispatch<SOAPMessage> dispatch = createDispatchWithWSDL();
        EndpointReference epr = dispatch.getEndpointReference(MemberSubmissionEndpointReference.class);

        //wsdl has addressing required
        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class);

        assertTrue(port != null);

        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");
    }

    /**
     * In this testcase, the Service instance is created with a
     * fake wsdl (incorrect endpoint adress). Normal invocation would have failed.
     * But the Dispatch is created with EPR which has valid endpoint address.
     * So, epr address shoudl take precedence and invocation should succeed.
     *
     */
    public void testDispatchWithEPRAddress() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }
//        W3CEndpointReferenceBuilder eprBuilder = new W3CEndpointReferenceBuilder();
//        eprBuilder.address(ENDPOINT_ADDRESS);
//        eprBuilder.serviceName(SERVICE_QNAME);
//        eprBuilder.endpointName(PORT_QNAME);
//        W3CEndpointReference epr = eprBuilder.build();
        WSEndpointReference wsepr = new WSEndpointReference(AddressingVersion.MEMBER, ENDPOINT_ADDRESS,SERVICE_QNAME,PORT_QNAME,null,null,null,null);
        MemberSubmissionEndpointReference epr = wsepr.toSpec(MemberSubmissionEndpointReference.class);
        URL fakeWsdlUrl = Thread.currentThread().getContextClassLoader().
                getResource("wsdl/AddNumbersFake.wsdl");
        Service service = Service.create(fakeWsdlUrl,SERVICE_QNAME);
        JAXBContext jc = createJAXBContext();
        Dispatch<Object> dispatch = service.createDispatch(epr,jc,Service.Mode.PAYLOAD, new AddressingFeature());
        AddNumbers input = new AddNumbers();
        input.setNumber1(2);
        input.setNumber2(4);
        JAXBElement<AddNumbersResponse> o = (JAXBElement<AddNumbersResponse>) dispatch.invoke(new ObjectFactory().createAddNumbers(input));
        int result = o.getValue().getReturn();
        assert(result == 6);
    }

    public void testDispatchWithRefParams() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        String xmlRefParam1 = "<myns1:MyParam1 xmlns:myns1=\"http://cptestservice.org/wsdl\">Hello</myns1:MyParam1>";
        String xmlRefParam2 = "<myns2:MyParam2 xmlns:myns2=\"http://cptestservice.org/wsdl\">There</myns2:MyParam2>";
        String request = "<?xml version=\"1.0\" ?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><DataType xmlns=\"http://cptestservice.org/xsd\"><param>{0}</param></DataType></S:Body></S:Envelope>";
//        W3CEndpointReferenceBuilder eprBuilder = new W3CEndpointReferenceBuilder();
//        eprBuilder.address(ENDPOINT_ADDRESS);
//        eprBuilder.serviceName(SERVICE_QNAME);
//        eprBuilder.endpointName(PORT_QNAME);
        List<Element> refParams = new ArrayList<Element>();
        Element n1 = (Element) DOMUtil.createDOMNode(new ByteArrayInputStream(xmlRefParam1.getBytes())).getFirstChild();
        Element n2 = (Element) DOMUtil.createDOMNode(new ByteArrayInputStream(xmlRefParam2.getBytes())).getFirstChild();
        refParams.add(n1);
        refParams.add(n2);
//        eprBuilder.referenceParameter(n1);
//        eprBuilder.referenceParameter(n2);
//        W3CEndpointReference epr = eprBuilder.build();

        WSEndpointReference wsepr = new WSEndpointReference(AddressingVersion.MEMBER, ENDPOINT_ADDRESS,SERVICE_QNAME,PORT_QNAME,null,null,null,refParams);
        MemberSubmissionEndpointReference epr = wsepr.toSpec(MemberSubmissionEndpointReference.class);
        printEPR(epr);
        
        URL fakeWsdlUrl = Thread.currentThread().getContextClassLoader().
                getResource("wsdl/AddNumbersFake.wsdl");
        Service service = Service.create(fakeWsdlUrl,SERVICE_QNAME);
        JAXBContext jc = createJAXBContext();
        Dispatch<Object> dispatch = service.createDispatch(epr,jc,Service.Mode.PAYLOAD, new AddressingFeature());
        AddNumbers input = new AddNumbers();
        input.setNumber1(2);
        input.setNumber2(4);
        JAXBElement<AddNumbersResponse> o = (JAXBElement<AddNumbersResponse>) dispatch.invoke(new ObjectFactory().createAddNumbers(input));
        int result = o.getValue().getReturn();
        assert(result == 6);
    }
    private static void printEPR(EndpointReference epr) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult sr = new StreamResult(bos);
        epr.writeTo(sr);
        bos.flush();
        System.out.println(bos);
    }
    private static javax.xml.bind.JAXBContext createJAXBContext() {
        try {
            return javax.xml.bind.JAXBContext.newInstance(client.jaxws21.epr_get_port.client.ObjectFactory.class);
        } catch (javax.xml.bind.JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }
}
