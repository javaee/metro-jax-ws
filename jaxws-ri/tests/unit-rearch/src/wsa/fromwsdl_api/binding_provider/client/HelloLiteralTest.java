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

package wsa.fromwsdl_api.binding_provider.client;

import client.common.client.DispatchTestCase;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import testutil.ClientServerTestUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.ByteArrayInputStream;
import java.io.File;
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


    private String endpointAddress = "http://localhost:/jaxrpc-wsa_fromwsdl_api_binding_provider/hello";
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

    private void dispatchLocalTransport() {

        // we'll fix the test harness correctly later,
        // so that test code won't have to hard code any endpoint address nor transport,
        // but for now let's just support local and HTTP to make unit tests happier.
        // this is not a good code, but it's just a bandaid solutino that works for now.
        if (ClientServerTestUtil.useLocal())
            endpointAddress = "local://" + new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\', '/') + '?' + portQName.getLocalPart();
        //else
        //    endpointAddress = "http://localhost:/jaxrpc-client_dispatch_wsdl_hello/hello";
    }


    public void xxtestW3CReadFrom() {
        Source w3csrc = makeStreamSource(w3cEprString);
        EndpointReference result = EndpointReference.readFrom(w3csrc);

        assertTrue(result != null);
        assertTrue(result instanceof W3CEndpointReference);
        //result.writeTo(new StreamResult(System.out));
    }

    public void xxtestMSReadFrom() {
        Source mssrc = makeStreamSource(msEprString);
        EndpointReference result = EndpointReference.readFrom(mssrc);

        assertTrue(result != null);
        assertTrue(result instanceof MemberSubmissionEndpointReference);

        //result.writeTo(new StreamResult(System.out));
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

    //functionality needs to be clarrified
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

    public void testHello() throws Exception {
        try {
            String arg = "foo";
            String extra = "bar";
            Hello_Type req = new Hello_Type();
            req.setArgument(arg);
            req.setExtra(extra);
            HelloResponse response = stub.hello(req);
            assertEquals(arg, response.getArgument());
            assertEquals(extra, response.getExtra());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

     public void testDispatchHelloI() throws Exception {
        try {
            String arg = "foo";
            String extra = "bar";
            Hello_Type req = new Hello_Type();
            req.setArgument(arg);
            req.setExtra(extra);
            HelloResponse response = stub.hello(req);
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
            return javax.xml.bind.JAXBContext.newInstance(wsa.fromwsdl_api.binding_provider.client.ObjectFactory.class);
        } catch (javax.xml.bind.JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }


    public Source makeStreamSource(String msg) {

        byte[] bytes = msg.getBytes();
        ByteArrayInputStream sinputStream = new ByteArrayInputStream(bytes);
        return new StreamSource(sinputStream);
    }

    private final static String helloMsg = "<Hello xmlns=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></Hello>";
    //all that is required here is the wsa:Address- other data optional on set EPR
    final static String metadata = "<definitions name=\"HelloTest\" targetNamespace=\"urn:test\" xmlns=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:types=\"urn:test:types\"  xmlns:tns=\"urn:test\"><types><xsd:schema targetNamespace=\"urn:test:types\" attributeFormDefault=\"unqualified\" elementFormDefault=\"unqualified\"><xsd:element name=\"Hello\"><xsd:complexType><xsd:sequence><xsd:element name=\"argument\" type=\"xsd:string\"/><xsd:element name=\"extra\" type=\"xsd:string\"/></xsd:sequence></xsd:complexType></xsd:element><xsd:complexType name=\"HelloType\"><xsd:sequence><xsd:element name=\"argument\" type=\"xsd:string\"/><xsd:element name=\"extra\" type=\"xsd:string\"/></xsd:sequence></xsd:complexType><xsd:element name=\"HelloResponse\"><xsd:complexType><xsd:sequence><xsd:sequence><xsd:element name=\"name\" type=\"xsd:string\"/></xsd:sequence><xsd:element name=\"argument\" type=\"xsd:string\"/><xsd:element name=\"extra\" type=\"xsd:string\"/></xsd:sequence></xsd:complexType></xsd:element></xsd:schema></types><message name=\"HelloRequest\"><part name=\"parameters\" element=\"types:Hello\"/></message><message name=\"HelloResponse\"><part name=\"parameters\" element=\"types:HelloResponse\"/></message><portType name=\"Hello\"><operation name=\"hello\"><input message=\"tns:HelloRequest\"/><output message=\"tns:HelloResponse\"/></operation></portType><binding name=\"HelloBinding\" type=\"tns:Hello\"><soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/><operation name=\"hello\"><soap:operation soapAction=\"urn:test:hello\"/><input><soap:body use=\"literal\"/></input><output><soap:body use=\"literal\"/></output></operation></binding><service name=\"Hello\"><port name=\"HelloPort\" binding=\"tns:HelloBinding\"><soap:address location=\"http://test.org/hello\"/></port></service></definitions>";
    final static String msEprString =
            "<wsa:EndpointReference xmlns:wsa = \"http://schemas.xmlsoap.org/ws/2004/08/addressing\"><wsa:Address>http://localhost:/jaxrpc-fromwsdl_wsdl_hello_lit/hello</wsa:Address><wsa:ReferenceProperties></wsa:ReferenceProperties><wsa:ReferenceParameters>" + metadata + "</wsa:ReferenceParameters></wsa:EndpointReference>";

    final static String w3cEprString =
            "<wsa:EndpointReference xmlns:wsa = \"http://www.w3.org/2005/08/addressing\"><wsa:Address>http://localhost:/jaxrpc-fromwsdl_wsdl_hello_lit/hello</wsa:Address><wsa:ReferenceParameters></wsa:ReferenceParameters><wsa:Metadata>" + metadata + "</wsa:Metadata></wsa:EndpointReference>";

}
