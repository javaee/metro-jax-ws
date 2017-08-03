/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package client.jaxws21.service_dispatch_features.client;

import client.common.client.DispatchTestCase;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import testutil.ClientServerTestUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.StringWriter;


/**
 * @author Arun Gupta
 *         Kathy walsh
 */
public class AddNumbersClient extends DispatchTestCase {
    //may be used for verification
    private static final QName SERVICE_QNAME = new QName("http://example.com/", "AddNumbersService");
    private static final QName PORT_QNAME = new QName("http://example.com/", "AddNumbersPort");
    private static final String ENDPOINT_ADDRESS = "http://localhost:8080/jaxrpc-client_jaxws21_service_dispatch_features/hello";
    //maybe used for firther tests
    private URL wsdl;

    public AddNumbersClient(String name) {
        super(name);
        try {
            wsdl = new URL("http://localhost:8080/jaxrpc-client_jaxws21_service_dispatch_features/hello?WSDL");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private Service createServiceWithWSDL() throws Exception {
        return Service.create(wsdl, SERVICE_QNAME);

    }

    private EndpointReference createEPRStubServiceWithWSDL(Service service) throws Exception {

        AddNumbersPortType port = service.getPort(PORT_QNAME, AddNumbersPortType.class);
        return ((BindingProvider) port).getEndpointReference();
    }

    private MemberSubmissionEndpointReference createMSEPRStubServiceWithWSDL(Service service) throws Exception {

        AddNumbersPortType port = service.getPort(PORT_QNAME, AddNumbersPortType.class);
        return ((BindingProvider) port).getEndpointReference(MemberSubmissionEndpointReference.class);
    }

     private EndpointReference createEPRDispatchService(Service service) throws Exception {

        AddNumbersPortType port = service.getPort(PORT_QNAME, AddNumbersPortType.class);
        return ((BindingProvider) port).getEndpointReference();
    }

    private MemberSubmissionEndpointReference createMSEPRDispatchService(Service service) throws Exception {

        AddNumbersPortType port = service.getPort(PORT_QNAME, AddNumbersPortType.class);
        return ((BindingProvider) port).getEndpointReference(MemberSubmissionEndpointReference.class);
    }

     private EndpointReference createEPRSDispatchServiceWithWSDL(Service service) throws Exception {

        AddNumbersPortType port = service.getPort(PORT_QNAME, AddNumbersPortType.class);
        return ((BindingProvider) port).getEndpointReference();
    }

    private MemberSubmissionEndpointReference createMSEPRDispatchServiceWithWSDL(Service service) throws Exception {

        AddNumbersPortType port = service.getPort(PORT_QNAME, AddNumbersPortType.class);
        return ((BindingProvider) port).getEndpointReference(MemberSubmissionEndpointReference.class);
    }


    private Service createService() throws Exception {
        return Service.create(SERVICE_QNAME);
    }


    JAXBContext createJAXBContext() {
        try {
            return JAXBContext.newInstance(client.jaxws21.service_dispatch_features.client.ObjectFactory.class);
        } catch (JAXBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }


    public void testProxyAddNumbers() throws AddNumbersFault_Exception {

        AddNumbersService service = new AddNumbersService();
        AddNumbersPortType port = service.getAddNumbersPort();
        int result = port.addNumbers(2, 4);

    }

    public void testCreateDispatchSMWsdl() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }
        RespectBindingFeature rbf = new RespectBindingFeature(false);
        WebServiceFeature[] wse = new WebServiceFeature[]{rbf};
        Service service = createServiceWithWSDL();
        Dispatch<SOAPMessage> dispatch = service.createDispatch(PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE, wse);
        SOAPMessage result = dispatch.invoke(getSOAPMessage(makeStreamSource(SMMsg)));

        result.writeTo(System.out);
    }

    //UsingAddressing wsdl:required=true
    public void testCreateDispatchSource() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }
        RespectBindingFeature rbf = new RespectBindingFeature(true);
        WebServiceFeature[] wse = new WebServiceFeature[]{rbf};
        Service service = createServiceWithWSDL();
        Dispatch<Source> dispatch = service.createDispatch(PORT_QNAME, Source.class, Service.Mode.PAYLOAD, wse);
        Source result = dispatch.invoke(makeStreamSource(MSGSrc));
        JAXBElement<AddNumbersResponse> addNumberResponse =  (JAXBElement<AddNumbersResponse>) createJAXBContext().createUnmarshaller().unmarshal(result);
        AddNumbersResponse response = addNumberResponse.getValue();
        assertEquals(response.getReturn(), 2 + 4);

    }

    //UsingAddressing wsdl:required=true
    //RespectBindingFeature Disabled - no effect - behavior undefined by specification
    //for backward compatability

    public void testCreateDispatchJAXB() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        RespectBindingFeature rbf = new RespectBindingFeature(true);
        WebServiceFeature[] wse = new WebServiceFeature[]{rbf};
        Service service = createServiceWithWSDL();
        Dispatch<Object> dispatch = service.createDispatch(PORT_QNAME, createJAXBContext(), Service.Mode.PAYLOAD, wse);

        AddNumbers addNumbers = factory.createAddNumbers();
        addNumbers.setNumber1(2);
        addNumbers.setNumber2(4);
        JAXBElement<AddNumbers> num = factory.createAddNumbers(addNumbers);
        JAXBElement<AddNumbersResponse> addNumbersResponse = (JAXBElement<AddNumbersResponse>) dispatch.invoke(num);
        AddNumbersResponse response = addNumbersResponse.getValue();
        assertEquals(response.getReturn(), 2 + 4);

    }

    public void testCreateDispatchSMWsdlWEPR() throws Exception {
        String eprString = "<EndpointReference xmlns=\"http://www.w3.org/2005/08/addressing\"><Address>" +
                "http://localhost:8080/jaxrpc-client_jaxws21_service_dispatch_features/hello</Address>" +
                "<Metadata><wsaw:ServiceName xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsdl\" xmlns:wsns=\"http://example.com/\" EndpointName=\"AddNumbersPort\">wsns:AddNumbersService</wsaw:ServiceName>" +
                "</Metadata></EndpointReference>";
        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        RespectBindingFeature rbf = new RespectBindingFeature(false);
        WebServiceFeature[] wse = new WebServiceFeature[]{rbf};
        Service service = createServiceWithWSDL();
        EndpointReference w3cEPR = createEPRStubServiceWithWSDL(service);
        //W3CEPRString = w3cEPR.toString();
        W3CEPRString = eprString;
        Dispatch<SOAPMessage> dispatch = service.createDispatch(EndpointReference.readFrom(makeStreamSource(W3CEPRString)), SOAPMessage.class, Service.Mode.MESSAGE);
        SOAPMessage sm = dispatch.invoke(getSOAPMessage(makeStreamSource(SMMsg)));
        sm.writeTo(System.out);
        //System.out.println("Adding numbers 2 and 4");
        // int result = dispatch.invoke(getSOAPMessage())
        // assert(result == 6);
        // System.out.println("Addinion of 2 and 4 successful");
    }

    //UsingAddressing wsdl:required=true
    public void testCreateDispatchSourceWEPR() throws Exception {
        String eprString = "<EndpointReference xmlns=\"http://www.w3.org/2005/08/addressing\"><Address>" +
                "http://localhost:8080/jaxrpc-client_jaxws21_service_dispatch_features/hello</Address>" +
                "<Metadata><wsaw:ServiceName xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsdl\" xmlns:wsns=\"http://example.com/\" EndpointName=\"AddNumbersPort\">wsns:AddNumbersService</wsaw:ServiceName>" +
                "</Metadata></EndpointReference>";
        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        RespectBindingFeature rbf = new RespectBindingFeature(true);
        WebServiceFeature[] wse = new WebServiceFeature[]{rbf};
        Service service = createServiceWithWSDL();

        EndpointReference w3cEPR = createEPRStubServiceWithWSDL(service);
        //W3CEPRString = w3cEPR.toString();
        W3CEPRString = eprString;        
        Dispatch<Source> dispatch = service.createDispatch(EndpointReference.readFrom(makeStreamSource(W3CEPRString)), Source.class, Service.Mode.PAYLOAD, wse);
        Source result = dispatch.invoke(makeStreamSource(MSGSrc));
        JAXBElement<AddNumbersResponse> addNumberResponse =  (JAXBElement<AddNumbersResponse>) createJAXBContext().createUnmarshaller().unmarshal(result);
        AddNumbersResponse response = addNumberResponse.getValue();
        assertEquals(response.getReturn(), 2 + 4);


    }

    //UsingAddressing wsdl:required=true
    //RespectBindingFeature Disabled - no effect - behavior undefined by specification
    //for backward compatability

    public void testCreateDispatchJAXBWEPR() throws Exception {
        String eprString = "<EndpointReference xmlns=\"http://www.w3.org/2005/08/addressing\"><Address>" +
                "http://localhost:8080/jaxrpc-client_jaxws21_service_dispatch_features/hello</Address>" +
                "<Metadata><wsaw:ServiceName xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsdl\" xmlns:wsns=\"http://example.com/\" EndpointName=\"AddNumbersPort\">wsns:AddNumbersService</wsaw:ServiceName>" +
                "</Metadata></EndpointReference>";
        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        RespectBindingFeature rbf = new RespectBindingFeature(true);
        WebServiceFeature[] wse = new WebServiceFeature[]{rbf};

        Service service = createServiceWithWSDL();

        EndpointReference w3cEPR = createEPRStubServiceWithWSDL(service);
        //W3CEPRString = w3cEPR.toString();
        W3CEPRString = eprString;
        Dispatch<Object> dispatch = service.createDispatch(EndpointReference.readFrom(makeStreamSource(W3CEPRString)), createJAXBContext(), Service.Mode.PAYLOAD, wse);
        AddNumbers addNumbers = factory.createAddNumbers();
        addNumbers.setNumber1(2);
        addNumbers.setNumber2(4);
        JAXBElement<AddNumbers> num = factory.createAddNumbers(addNumbers);
        JAXBElement<AddNumbersResponse> addNumbersResponse = (JAXBElement<AddNumbersResponse>) dispatch.invoke(num);
        AddNumbersResponse response = addNumbersResponse.getValue();
        assertEquals(response.getReturn(), 2 + 4);

    }


    public void testCreateDispatchSMWsdlMSEPR() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        RespectBindingFeature rbf = new RespectBindingFeature(false);
        WebServiceFeature[] wse = new WebServiceFeature[]{rbf};
        Service service = createServiceWithWSDL();
        EndpointReference msEPR = createMSEPRStubServiceWithWSDL(service);

        MSEPRString = msEPR.toString();

        Dispatch<SOAPMessage> dispatch = service.createDispatch(EndpointReference.readFrom(makeStreamSource(MSEPRString)), SOAPMessage.class, Service.Mode.MESSAGE, wse);
        SOAPMessage sm = dispatch.invoke(getSOAPMessage(makeStreamSource(SMMsg)));
        sm.writeTo(System.out);

        //System.out.println("Adding numbers 2 and 4");
        // int result = dispatch.invoke(getSOAPMessage())
        // assert(result == 6);
        // System.out.println("Addinion of 2 and 4 successful");
    }


    //UsingAddressing wsdl:required=true
    public void testCreateDispatchSourceMSEPR() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        RespectBindingFeature rbf = new RespectBindingFeature(true);
        WebServiceFeature[] wse = new WebServiceFeature[]{rbf};
        Service service = createServiceWithWSDL();

        EndpointReference msEPR = createMSEPRStubServiceWithWSDL(service);
        MSEPRString = msEPR.toString();
        Dispatch<Source> dispatch = service.createDispatch(EndpointReference.readFrom(makeStreamSource(MSEPRString)), Source.class, Service.Mode.PAYLOAD, wse);
        Source result = dispatch.invoke(makeStreamSource(MSGSrc));
        JAXBElement<AddNumbersResponse> addNumberResponse =  (JAXBElement<AddNumbersResponse>) createJAXBContext().createUnmarshaller().unmarshal(result);
        AddNumbersResponse response = addNumberResponse.getValue();
        assertEquals(response.getReturn(), 2 + 4);
      
    }

    //UsingAddressing wsdl:required=true
    //RespectBindingFeature Disabled - no effect - behavior undefined by specification
    //for backward compatability

    public void testCreateDispatchJAXBMSEPR() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        RespectBindingFeature rbf = new RespectBindingFeature(true);
        WebServiceFeature[] wse = new WebServiceFeature[]{rbf};
        Service service = createServiceWithWSDL();

        EndpointReference msEPR = createMSEPRStubServiceWithWSDL(service);
        MSEPRString = msEPR.toString();
        Dispatch<Object> dispatch = service.createDispatch(EndpointReference.readFrom(makeStreamSource(MSEPRString)), createJAXBContext(), Service.Mode.PAYLOAD, wse);
        AddNumbers addNumbers = factory.createAddNumbers();
        addNumbers.setNumber1(2);
        addNumbers.setNumber2(4);
        JAXBElement<AddNumbers> num = factory.createAddNumbers(addNumbers);
        JAXBElement<AddNumbersResponse> addNumbersResponse = (JAXBElement<AddNumbersResponse>) dispatch.invoke(num);
         AddNumbersResponse response = addNumbersResponse.getValue();
        assertEquals(response.getReturn(), 2 + 4);

    }

    public void testCreateDispatchSMWsdlMSEPRNoPortQName() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        RespectBindingFeature rbf = new RespectBindingFeature(false);
        WebServiceFeature[] wse = new WebServiceFeature[]{rbf};
        Service service = createServiceWithWSDL();
        MemberSubmissionEndpointReference msEPR = createMSEPRStubServiceWithWSDL(service);
        msEPR.portTypeName.name = null;
        MSEPRString = msEPR.toString();


        Dispatch<SOAPMessage> dispatch = service.createDispatch(EndpointReference.readFrom(makeStreamSource(MSEPRString)), SOAPMessage.class, Service.Mode.MESSAGE, wse);
        SOAPMessage sm = dispatch.invoke(getSOAPMessage(makeStreamSource(SMMsg)));
        sm.writeTo(System.out);

        //System.out.println("Adding numbers 2 and 4");
        // int result = dispatch.invoke(getSOAPMessage())
        // assert(result == 6);
        // System.out.println("Addinion of 2 and 4 successful");
    }


    //UsingAddressing wsdl:required=true
    public void testCreateDispatchSourceMSEPRNoPortQName() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        RespectBindingFeature rbf = new RespectBindingFeature(true);
        WebServiceFeature[] wse = new WebServiceFeature[]{rbf};
        Service service = createServiceWithWSDL();

        MemberSubmissionEndpointReference msEPR = createMSEPRStubServiceWithWSDL(service);
        //MemberSubmissionEndpointReference.AttributedQName portTypeName = msEPR.portTypeName;
        //QName portQName = portTypeName.name;
        msEPR.portTypeName.name = null;
        MSEPRString = msEPR.toString();
        Dispatch<Source> dispatch = service.createDispatch(EndpointReference.readFrom(makeStreamSource(MSEPRString)), Source.class, Service.Mode.PAYLOAD, wse);
        Source result = dispatch.invoke(makeStreamSource(MSGSrc));
        JAXBElement<AddNumbersResponse> addNumberResponse =  (JAXBElement<AddNumbersResponse>) createJAXBContext().createUnmarshaller().unmarshal(result);
        AddNumbersResponse response = addNumberResponse.getValue();
        assertEquals(response.getReturn(), 2 + 4);

    }

    //UsingAddressing wsdl:required=true
    //RespectBindingFeature Disabled - no effect - behavior undefined by specification
    //for backward compatability

    public void testCreateDispatchJAXBMSEPRNoPortQName() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        RespectBindingFeature rbf = new RespectBindingFeature(true);
        WebServiceFeature[] wse = new WebServiceFeature[]{rbf};
        Service service = createServiceWithWSDL();

        MemberSubmissionEndpointReference msEPR = createMSEPRStubServiceWithWSDL(service);        
        msEPR.portTypeName.name = null;

        MSEPRString = msEPR.toString();
        Dispatch<Object> dispatch = service.createDispatch(EndpointReference.readFrom(makeStreamSource(MSEPRString)), createJAXBContext(), Service.Mode.PAYLOAD, wse);
        AddNumbers addNumbers = factory.createAddNumbers();
        addNumbers.setNumber1(2);
        addNumbers.setNumber2(4);
        JAXBElement<AddNumbers> num = factory.createAddNumbers(addNumbers);
        JAXBElement<AddNumbersResponse> addNumbersResponse = (JAXBElement<AddNumbersResponse>) dispatch.invoke(num);
        AddNumbersResponse response = addNumbersResponse.getValue();
        assertEquals(response.getReturn(), 2 + 4);

    }


    public void xxxtestEPRGetPortIV() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }
    }


    //UsingAddressing wsdl:required=true
    //AddressingFeature Disabled expect Exception
    //Expect no valid addressingport created, so exception thrown
    public void xxxtestEPRGetPortV() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }


    }

    //UsingAddressing wsdl:required=true
    //AddressingFeature Disabled expect Exception
    //Expect no valid port created,
    public void xxxtestEPRGetPortVI() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }


    }


    //UsingAddressing wsdl:required=true
    //AddressingFeature Disabled expect Exception
    //Expect no valid port created, so exception thrown
    public void xxxtestEPRGetPortVII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
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


    }

//UsingAddressing wsdl:required=true
//AddressingFeature Disabled expect Exception
//Expect no valid port created, so exception thrown

    public void xxxtestEPRGetPortVIIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }


    }

    //UsingAddressing wsdl:required=true
    //AddressingFeature Disabled expect Exception
    //Expect no valid port created, so exception thrown
    public void xxxtestEPRGetPortVIIIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }


    }

//UsingAddressing wsdl:required=true
//AddressingFeature Disabled expect Exception
//Expect no valid port created, so exception thrown

    public void xxxtestEPRGetPortVIIIIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }


    }

    //UsingAddressing wsdl:required=true
    public void xxxtestDispatchEPRGetPort() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

    }


    public static String W3CEPRString;
    public String MSEPRString;

    private ObjectFactory factory = new ObjectFactory();

    private String SMMsg = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">http://localhost:8080/jaxrpc-client_jaxws21_service_dispatch_features/hello</To><Action xmlns=\"http://www.w3.org/2005/08/addressing\">http://example.com/AddNumbersPortType/addNumbersRequest</Action><ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "<Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo><MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:a89abfcf-0b64-4f71-979e-9ee31ae75b6c</MessageID></S:Header><S:Body><addNumbers xmlns=\"http://example.com/\"><number1>2</number1><number2>4</number2></addNumbers></S:Body></S:Envelope>";

    private String SMMsgString = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">http://localhost:8080/jaxrpc-client_jaxws21_service_dispatch_features/hello</To><Action xmlns=\"http://www.w3.org/2005/08/addressing\">http://example.com/AddNumbersPortType/addNumbersRequest</Action><ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\"><Address>http://www.w3.org/2005/08/addressing/anonymous></Address></ReplyTo><MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:b63b8097-6ac9-4c6e-83f9-ab9f5b108f5c</MessageID></S:Header><S:Body><addNumbers xmlns=\"http://example.com/\"><number1>2</number1><number2>4</number2></addNumbers></S:Body></S:Envelope>";
    private String MSGSrc = "<addNumbers xmlns=\"http://example.com/\"><number1>2</number1><number2>4</number2></addNumbers>";
}
