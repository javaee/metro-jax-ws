/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

package wsa.fromwsdl_api.service.client;

import client.common.client.DispatchTestCase;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import testutil.ClientServerTestUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author JAX-RPC RI Development Team
 */
public class HelloLiteralTest extends DispatchTestCase {


    private static Hello stub;

    private QName serviceQName = new QName("urn:test", "Hello");
    private QName portQName = new QName("urn:test", "HelloPort");

    private String bindingIdString = SOAPBinding.SOAP11HTTP_BINDING;


    private String endpointAddress = "http://localhost:8080/jaxrpc-wsa_fromwsdl_api_service/hello";
    private Service service;
    private Service serviceWithPorts;

// main method added for debugging

    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            HelloLiteralTest test = new HelloLiteralTest("HelloLiteralTest");
            //test.testGetMSEPRFromClass();
            //test.testHello();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public HelloLiteralTest(String name) throws Exception {
        super(name);
        Hello_Service service = new Hello_Service();

        stub = service.getHelloPort();
    }


    public EndpointReference getEPRFromStub() throws Exception {

        Hello_Service service = new Hello_Service();

        stub = service.getHelloPort();

        return ((BindingProvider) stub).getEndpointReference(MemberSubmissionEndpointReference.class);
    }

    public EndpointReference getW3CEPRFromStub() throws Exception {

        Hello_Service service = new Hello_Service();

        stub = service.getHelloPort();

        return ((BindingProvider) stub).getEndpointReference();
    }

    private void dispatchLocalTransport() {

        // we'll fix the test harness correctly later,
        // so that test code won't have to hard code any endpoint address nor transport,
        // but for now let's just support local and HTTP to make unit tests happier.
        // this is not a good code, but it's just a bandaid solutino that works for now.
        if (ClientServerTestUtil.useLocal())
            endpointAddress = "local://" + new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\', '/') + '?' + "hello_literal.wsdl";
        //else
        //    endpointAddress = "http://localhost:8080/jaxrpc-client_dispatch_wsdl_hello/hello";
    }

    //TODO:fix
    public void kwtestServiceCreateDispatchWithMSEPR() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }
        createService();  //no wsdl
        Dispatch<Source> dispatch = getDispatchSourceWithEPR(service);
        assert dispatch != null;
    }

    //TODO:fix
    public void kwtestInvokeDispatchSourceWithEPR() {
        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }
        createService();  //no wsdl
        Dispatch<Source> dispatch = getDispatchSourceWithEPR(service);
        assert dispatch != null;
        Source result = (Source) dispatch.invoke(makeStreamSource(helloMsg));
        assert(result != null);
        System.out.println(sourceToXMLString(result));

    }

     //TODO:fix
    public void kwtestServiceWithWSDLCreateDispatchWithMSEPR() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }
        createServiceWithWSDL();
        Dispatch<Source> dispatch = getDispatchSourceWithEPR(serviceWithPorts);
        assertTrue(dispatch != null);

    }

    //TODO:fix
    public void kwtestInvokeDispatchSourceWithWSDL() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }
        createServiceWithWSDL();
        Dispatch<Source> dispatch = getDispatchSourceWithEPR(serviceWithPorts);
        assertTrue(dispatch != null);
        Source result = dispatch.invoke(makeStreamSource(helloMsg));
        assert(result != null);
        System.out.println(sourceToXMLString(result));


    }
     //TODO:fix
    public void kwtestGenertatedServiceCreateDispatchWithMSEPR() {


        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }

        Hello_Service service = new Hello_Service();
        Dispatch<Source> dispatch = getDispatchSourceWithEPR(service);
        assertTrue(dispatch != null);

    }
     //TODO:fix
    public void kwtestInvokeDispatchSourceWithGeneratedService(){

            if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }

        Hello_Service service = new Hello_Service();
        Dispatch<Source> dispatch = getDispatchSourceWithEPR(service);
        assertTrue(dispatch != null);

        Source result = (Source) dispatch.invoke(makeStreamSource(helloMsg));
        assert(result != null);
        System.out.println(sourceToXMLString(result));

    }
     //TODO:fix
    public void kwtestServiceCreateDispatchWithW3CEPR() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }

        createService();  //no wsdl
        Dispatch<Source> dispatch = getDispatchSourceWithW3CEPR(service);
        assert dispatch != null;

    }
     //TODO:fix
    public void kwtestServiceWithWSDLCreateDispatchWithW3CEPR() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }

        createServiceWithWSDL();  //no wsdl
        Dispatch<Source> dispatch = getDispatchSourceWithW3CEPR(serviceWithPorts);
        assertTrue(dispatch != null);

    }
     //TODO:fix
    public void kwtestGenertatedServiceCreateDispatchWithW3CPR() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }

        Hello_Service service = new Hello_Service();
        Dispatch<Source> dispatch = getDispatchSourceWithW3CEPR(service);
        assertTrue(dispatch != null);

    }

     //TODO:fix
    public void kwtestServiceCreateJAXBispatchWithMSEPR() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }

        createService();  //no wsdl
        Dispatch<Object> dispatch = getDispatchSourceWithEPR(service);
        assert dispatch != null;

    }

     //TODO:fix
    public void kwtestServiceWithWSDLCreateJAXBDispatchWithMSEPR() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }

        createServiceWithWSDL();  //no wsdl
        Dispatch<Object> dispatch = getDispatchSourceWithEPR(serviceWithPorts);
        assertTrue(dispatch != null);

    }

     //TODO:fix
    public void kwtestGenertatedServiceCreateJAXBDispatchWithMSEPR() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }

        Hello_Service service = new Hello_Service();
        Dispatch<Object> dispatch = getDispatchSourceWithEPR(service);
        assertTrue(dispatch != null);

    }
     //TODO:fix
    public void kwtestServiceCreateJAXBDispatchWithW3CEPR() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }

        createService();  //no wsdl
        Dispatch<Object> dispatch = getDispatchSourceWithW3CEPR(service);
        assert dispatch != null;

    }
     //TODO:fix
    public void kwtestServiceCreateSOAPMSGDispatchWithW3CEPR() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
                   System.out.println("HTTP Transport only exiting");
                   return;
               }

        Dispatch<SOAPMessage> dispatch = getDispatchSOAPMessageWithW3CEPR();
        assert (dispatch != null);
    }
     //TODO:fix
    public void kwtestServiceWithWSDLCreateJAXBDispatchWithW3CEPR() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }

        createServiceWithWSDL();  //no wsdl
        Dispatch<Object> dispatch = getDispatchSourceWithW3CEPR(serviceWithPorts);
        assertTrue(dispatch != null);
    }
     //TODO:fix
    public void kwtestGenertatedServiceCreateJAXBDispatchWithW3CPR() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport only exiting");
            return;
        }

        Hello_Service service = new Hello_Service();
        Dispatch<Object> dispatch = getDispatchSourceWithW3CEPR(service);
        assertTrue(dispatch != null);

    }


    public void testGetEPRMethodNoArg() {

        EndpointReference epr = ((BindingProvider) stub).getEndpointReference();

        assertTrue(epr != null);
        assertTrue(epr instanceof W3CEndpointReference);
    }

    //functionality needs to be clarrified
    public void testGetMSEPRFromClass() {

        EndpointReference epr = ((BindingProvider) stub).getEndpointReference(MemberSubmissionEndpointReference.class);

        assertTrue(epr != null);
        assertTrue(epr instanceof MemberSubmissionEndpointReference);
    }

    public void testGetW3CEPRFromClass() {

        EndpointReference epr = ((BindingProvider) stub).getEndpointReference(W3CEndpointReference.class);

        assertTrue(epr != null);
        assertTrue(epr instanceof W3CEndpointReference);
    }
    //functionality above needs to be clarified


    public void testDispatchGetEPRMethodNoArg() {

        EndpointReference epr = ((BindingProvider) getDispatchJAXB()).getEndpointReference();

        assertTrue(epr != null);
        assertTrue(epr instanceof W3CEndpointReference);
    }

    //functionality needs to be clarrified
    public void testDispatchGetMSEPRFromClass() {

        EndpointReference epr = ((BindingProvider) getDispatchSource()).getEndpointReference(MemberSubmissionEndpointReference.class);

        assertTrue(epr != null);
        assertTrue(epr instanceof MemberSubmissionEndpointReference);
    }

    public void testDispatchGetW3CEPRFromClass() {

        EndpointReference epr = ((BindingProvider) getDispatchSOAPMessage()).getEndpointReference(W3CEndpointReference.class);

        assertTrue(epr != null);
        assertTrue(epr instanceof W3CEndpointReference);
    }


    public void testDispatchWithPortsGetEPRMethodNoArg() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("http transport only exiting");
            return;
        }

        EndpointReference epr = ((BindingProvider) getDispatchJAXBWithPorts()).getEndpointReference();

        assertTrue(epr != null);
        assertTrue(epr instanceof W3CEndpointReference);
    }


    public void testDispatchWithPortsGetMSEPRFromClass() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("http transport only exiting");
            return;
        }

        EndpointReference epr = ((BindingProvider) getDispatchSourceWithPorts()).getEndpointReference(MemberSubmissionEndpointReference.class);

        assertTrue(epr != null);
        assertTrue(epr instanceof MemberSubmissionEndpointReference);
    }

    public void testDispatchWithPortsGetW3CEPRFromClass() {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("http transport only exiting");
            return;
        }

        EndpointReference epr = ((BindingProvider) getDispatchSOAPMessageSourceWithPorts()).getEndpointReference(W3CEndpointReference.class);

        assertTrue(epr != null);
        assertTrue(epr instanceof W3CEndpointReference);
    }

    public void xxxtestHelloProxyI() throws Exception {
        try {

            Hello proxy = createGeneratedStubRespectBindingFeature(true);
            String arg = "foo";
            String extra = "bar";
            Hello_Type req = new Hello_Type();
            req.setArgument(arg);
            req.setExtra(extra);
            HelloResponse response = proxy.hello(req);
            assertEquals(arg, response.getArgument());
            assertEquals(extra, response.getExtra());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }


    public void xxxtestHelloProxyIII() throws Exception {
        try {

            Hello proxy = createGeneratedStubRespectBindingFeature(false);
            String arg = "foo";
            String extra = "bar";
            Hello_Type req = new Hello_Type();
            req.setArgument(arg);
            req.setExtra(extra);
            HelloResponse response = proxy.hello(req);
            assertEquals(arg, response.getArgument());
            assertEquals(extra, response.getExtra());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void xxxtestHelloProxyII() throws Exception {
        try {

            Hello proxy = createGeneratedStubNullFeatures();
            String arg = "foo";
            String extra = "bar";
            Hello_Type req = new Hello_Type();
            req.setArgument(arg);
            req.setExtra(extra);
            HelloResponse response = proxy.hello(req);
            assertEquals(arg, response.getArgument());
            assertEquals(extra, response.getExtra());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    //dispatch without wsdl
    private void createService() {
        try {
            service = Service.create(serviceQName);
            service.addPort(portQName, bindingIdString, endpointAddress);
        } catch (WebServiceException e) {
            e.printStackTrace();
        }
    }


    //getProxy
    private Hello createGeneratedStubNullFeatures() {
        try {
            Hello_Service helloService = new Hello_Service();
            return helloService.getPort(portQName, Hello.class, null);
        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    //getProxy
    private Hello createGeneratedStubRespectBindingFeature(boolean enabled) {
        try {
            Hello_Service helloService = new Hello_Service();
            RespectBindingFeature bindingFeature = new RespectBindingFeature(enabled);
            WebServiceFeature[] features = new WebServiceFeature[]{bindingFeature};
            return helloService.getPort(portQName, Hello.class, features);
        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    //fix- why not working with harness
    private void createServiceWithWSDL() {
        URI serviceURI = null;

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("http transport only exiting");
            return;
        }


        try {
            serviceURI = new URI(endpointAddress + "?wsdl");
            serviceWithPorts = Service.create(serviceURI.toURL(), serviceQName);
        } catch (WebServiceException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    private Dispatch getDispatchJAXB() {
        createService();

        try {
            JAXBContext context = createJAXBContext();
            return service.createDispatch(portQName, context, Service.Mode.PAYLOAD);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return null;

    }

    private Dispatch getDispatchJAXBWithPorts() {


        createServiceWithWSDL();

        try {

            JAXBContext context = createJAXBContext();
            return serviceWithPorts.createDispatch(portQName, context,
                    Service.Mode.PAYLOAD);

        } catch (WebServiceException e) {
            fail("error creating service with ports");
        }
        return null;
    }

    private Dispatch getDispatchSource() {
        createService();

        try {
            return service.createDispatch(portQName, Source.class,
                    Service.Mode.PAYLOAD);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return null;

    }


    private Dispatch getDispatchJAXBWithEPR(Service service) {
        //createService();

        JAXBContext jc = createJAXBContext();

        try {
            return service.createDispatch((EndpointReference) getEPRFromStub(), jc,
                    Service.Mode.PAYLOAD, null);

        } catch (WebServiceException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
        return null;

    }


    private Dispatch getDispatchJAXBWithW3CEPR(Service service) {
        //createService();

        JAXBContext jc = createJAXBContext();

        try {
            return service.createDispatch((EndpointReference) getW3CEPRFromStub(), jc,
                    Service.Mode.PAYLOAD, null);

        } catch (WebServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    private Dispatch getDispatchSourceWithEPR(Service service) {
        //createService();

        try {
            return service.createDispatch((EndpointReference) getEPRFromStub(), Source.class,
                    Service.Mode.PAYLOAD, null);

        } catch (WebServiceException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
        return null;

    }


    private Dispatch getDispatchSourceWithW3CEPR(Service service) {
        //createService();

        try {
            return service.createDispatch((EndpointReference) getW3CEPRFromStub(), Source.class,
                    Service.Mode.PAYLOAD, null);

        } catch (WebServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    private Dispatch getDispatchSourceWithPorts() {
        createServiceWithWSDL();
        try {
            return serviceWithPorts.createDispatch(portQName, Source.class,
                    Service.Mode.PAYLOAD);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return null;

    }


     private Dispatch getDispatchSOAPMessageWithW3CEPR() throws Exception{
        createService();

        try {
            return service.createDispatch(getW3CEPRFromStub(), SOAPMessage.class,
                    Service.Mode.MESSAGE, null);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Dispatch getDispatchSOAPMessage() {
        createService();

        try {
            return service.createDispatch(portQName, SOAPMessage.class,
                    Service.Mode.MESSAGE);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Dispatch getDispatchSOAPMessageSourceWithPorts() {
        createServiceWithWSDL();
        try {

            return serviceWithPorts.createDispatch(portQName, Source.class,
                    Service.Mode.MESSAGE);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static javax.xml.bind.JAXBContext createJAXBContext() {
        try {
            return javax.xml.bind.JAXBContext.newInstance(wsa.fromwsdl_api.service.client.ObjectFactory.class);
        } catch (javax.xml.bind.JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }


    public Source makeStreamSource(String msg) {

        byte[] bytes = msg.getBytes();
        ByteArrayInputStream sinputStream = new ByteArrayInputStream(bytes);
        return new StreamSource(sinputStream);
    }

    public String sourceToXMLString(Source result) {

        String xmlResult = null;
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            OutputStream out = new ByteArrayOutputStream();
            StreamResult streamResult = new StreamResult();
            streamResult.setOutputStream(out);
            transformer.transform(result, streamResult);
            xmlResult = streamResult.getOutputStream().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return xmlResult;
    }

    private String helloMsg = "<Hello xmlns=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></Hello>";
    //all that is required here is the wsa:Address- other data optional on set EPR
    final static String metadata = "<definitions name=\"HelloTest\" targetNamespace=\"urn:test\" xmlns=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:types=\"urn:test:types\"  xmlns:tns=\"urn:test\"><types><xsd:schema targetNamespace=\"urn:test:types\" attributeFormDefault=\"unqualified\" elementFormDefault=\"unqualified\"><xsd:element name=\"Hello\"><xsd:complexType><xsd:sequence><xsd:element name=\"argument\" type=\"xsd:string\"/><xsd:element name=\"extra\" type=\"xsd:string\"/></xsd:sequence></xsd:complexType></xsd:element><xsd:complexType name=\"HelloType\"><xsd:sequence><xsd:element name=\"argument\" type=\"xsd:string\"/><xsd:element name=\"extra\" type=\"xsd:string\"/></xsd:sequence></xsd:complexType><xsd:element name=\"HelloResponse\"><xsd:complexType><xsd:sequence><xsd:sequence><xsd:element name=\"name\" type=\"xsd:string\"/></xsd:sequence><xsd:element name=\"argument\" type=\"xsd:string\"/><xsd:element name=\"extra\" type=\"xsd:string\"/></xsd:sequence></xsd:complexType></xsd:element></xsd:schema></types><message name=\"HelloRequest\"><part name=\"parameters\" element=\"types:Hello\"/></message><message name=\"HelloResponse\"><part name=\"parameters\" element=\"types:HelloResponse\"/></message><portType name=\"Hello\"><operation name=\"hello\"><input message=\"tns:HelloRequest\"/><output message=\"tns:HelloResponse\"/></operation></portType><binding name=\"HelloBinding\" type=\"tns:Hello\"><soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/><operation name=\"hello\"><soap:operation soapAction=\"urn:test:hello\"/><input><soap:body use=\"literal\"/></input><output><soap:body use=\"literal\"/></output></operation></binding><service name=\"Hello\"><port name=\"HelloPort\" binding=\"tns:HelloBinding\"><soap:address location=\"http://test.org/hello\"/></port></service></definitions>";
    final static String msEprString =
            "<wsa:EndpointReference xmlns:wsa = \"http://schemas.xmlsoap.org/ws/2004/08/addressing\"><wsa:Address>http://localhost:8080/jaxrpc-fromwsdl_wsdl_hello_lit/hello</wsa:Address><wsa:ReferenceProperties></wsa:ReferenceProperties><wsa:ReferenceParameters>" + metadata + "</wsa:ReferenceParameters></wsa:EndpointReference>";

    final static String w3cEprString =
            "<wsa:EndpointReference xmlns:wsa = \"http://www.w3.org/2005/08/addressing\"><wsa:Address>http://localhost:8080/jaxrpc-fromwsdl_wsdl_hello_lit/hello</wsa:Address><wsa:ReferenceParameters></wsa:ReferenceParameters><wsa:Metadata>" + metadata + "</wsa:Metadata></wsa:EndpointReference>";

}
