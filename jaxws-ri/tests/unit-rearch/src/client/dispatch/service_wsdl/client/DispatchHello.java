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

package client.dispatch.service_wsdl.client;

import client.common.client.DispatchTestCase;
import testutil.ClientServerTestUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author JAX-RPC RI Development Team
 */
public class DispatchHello extends DispatchTestCase {

    private String helloSM = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><Hello xmlns=\"urn:test:types\"><argument>foo</argument><extra>Test </extra></Hello></soapenv:Body></soapenv:Envelope>";
    private String helloResponseSM = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><HelloResponse xmlns=\"urn:test:types\"><argument>foo</argument><extra>Test </extra></HelloResponse></soapenv:Body></soapenv:Envelope>";
    private String helloMsg = "<Hello xmlns=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></Hello>";
    private String helloMsgBadFoo = "<Hello xmlns=\"urn:test:types\"><argument>badfoo</argument><extra>bar</extra></Hello>";
    private String bad3helloMsg = "<Hello xmlns=\"urn:test\"><argument>foo</argument><extra>bar</extra></Hello>";
    //private String bad2helloMsg = "<Hello xmlns=\"urn:test:types\"><argument></argument><argument></extra></Hello>";
    //should give RemoteException with jaxbException cause
    private String badhelloMsg = "<Hello xmlns=\"urn:test:types\">.....<bar></Hello>";
    private String helloResponse = "<HelloResponse xmlns=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></HelloResponse>";
    private String helloResponseRequest = "<HelloResponse xmlns=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></HelloResponse>";

    private String voidMsg = "</VoidTest>";
    //should give RemoteException with jaxbException cause
    private String badvoidMsg = "<VoidTest xmlns=\"\"urn:test\"/>";
    private String voidResponse = "<VoidTestResponse xmlns=\"urn:test:types\"/>";

    //private String bugTest = <?xml version="1.0" encoding="UTF-8"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><soapenv:Body><GetUserByID1Response xmlns="http://example.com/auctiontraq/wsdl/rpclit"><Body xmlns="" userRating="5"><ns1:name xmlns:ns1="http://example.com/auctiontraq/schemas/rpclit"><ns1:fname>Rama</ns1:fname><ns1:lname>Pulavarthi</ns1:lname></ns1:name><ns2:dob xmlns:ns2="http://example.com/auctiontraq/schemas/rpclit">1978-08-30-07:00</ns2:dob><ns3:age xmlns:ns3="http://example.com/auctiontraq/schemas/rpclit">25</ns3:age><ns4:ssn xmlns:ns4="http://example.com/auctiontraq/schemas/rpclit">123 456 7890</ns4:ssn><ns5:address xmlns:ns5="http://example.com/auctiontraq/schemas/rpclit"><ns5:street>988 Henderson Ave</ns5:street><ns5:city>Sunnyvale</ns5:city><ns5:state>CA</ns5:state><ns5:country>USA</ns5:country><ns5:zipcode>94086</ns5:zipcode></ns5:address><ns6:email xmlns:ns6="http://example.com/auctiontraq/schemas/rpclit">rama.pulavarthi@sun.com</ns6:email><ns7:password xmlns:ns7="http://example.com/auctiontraq/schemas/rpclit">cGFzc3dvcmQ=</ns7:password><ns8:memberSince xmlns:ns8="http://example.com/auctiontraq/schemas/rpclit">2000-08-24-07:00</ns8:memberSince><ns9:authenticated xmlns:ns9="http://example.com/auctiontraq/schemas/rpclit">true</ns9:authenticated></Body></GetUserByID1Response></soapenv:Body></soapenv:Envelope>
    private String bugTest2 = "<ns2:GetUserByID1 xmlns:ns2=\"http://example.com/auctiontraq/wsdl/rpclit\" xmlns:ns3=\"http://example.com/auctiontraq/schemas/rpclit\"><Body>rama.pulavarthi@sun.com</Body></ns2:GetUserByID1>";

    private String bugTest = "<GetUserByID1Response xmlns=\"http://example.com/auctiontraq/wsdl/rpclit\"><Body xmlns=\"\" userRating=\"5\"><ns1:name xmlns:ns1=\"http://example.com/auctiontraq/schemas/rpclit\"><ns1:fname>Rama</ns1:fname><ns1:lname>Pulavarthi</ns1:lname></ns1:name><ns2:dob xmlns:ns2=\"http://example.com/auctiontraq/schemas/rpclit\">1978-08-30-07:00</ns2:dob><ns3:age xmlns:ns3=\"http://example.com/auctiontraq/schemas/rpclit\">25</ns3:age><ns4:ssn xmlns:ns4=\"http://example.com/auctiontraq/schemas/rpclit\">123 456 7890</ns4:ssn><ns5:address xmlns:ns5=\"http://example.com/auctiontraq/schemas/rpclit\"><ns5:street>988 Henderson Ave</ns5:street><ns5:city>Sunnyvale</ns5:city><ns5:state>CA</ns5:state><ns5:country>USA</ns5:country><ns5:zipcode>94086</ns5:zipcode></ns5:address><ns6:email xmlns:ns6=\"http://example.com/auctiontraq/schemas/rpclit\">rama.pulavarthi@sun.com</ns6:email><ns7:password xmlns:ns7=\"http://example.com/auctiontraq/schemas/rpclit\">cGFzc3dvcmQ=</ns7:password><ns8:memberSince xmlns:ns8=\"http://example.com/auctiontraq/schemas/rpclit\">2000-08-24-07:00</ns8:memberSince><ns9:authenticated xmlns:ns9=\"http://example.com/auctiontraq/schemas/rpclit\">true</ns9:authenticated></Body></GetUserByID1Response>";
    private String sqeTest = "<Person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://example.com/auctiontraq/schemas/doclit\" xsi:type=\"UserType\" userRating=\"5\"><name><fname>Rama</fname><lname>Pulavarthi</lname></name><ssn>123</ssn><ssn>456</ssn><ssn>7890</ssn><email>rama.pulavarthi@sun.com</email><password>cGFzc3dvcmQ=</password><memberSince>2000-08-24T11:05:35.000-07:00</memberSince><authenticated>true</authenticated></Person>";
    /*
<?xml version="1.0" encoding="UTF-8"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"><soapenv:Body><Person xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://example.com/auctiontraq/schemas/doclit" xsi:type="UserType" userRating="5"><name><fname>Rama</fname><lname>Pulavarthi</lname></name><dob>1978-08-30T00:00:00.000-07:00</dob><age>P25Y0M</age><ssn>123</ssn><ssn>456</ssn><ssn>7890</ssn><address><street>988 Henderson Ave</street><city>Sunnyvale</city><state>CA</state><country>USA</country><zipcode>94086</zipcode></address><email>rama.pulavarthi@sun.com</email><password>cGFzc3dvcmQ=</password><memberSince>2000-08-24T11:05:35.000-07:00</memberSince><authenticated>true</authenticated></Person></soapenv:Body></soapenv:Envelope>
 */


    //new QName("urn:test:types", "Hello")
    private QName serviceQName = new QName("urn:test", "Hello");
    private QName portQName = new QName("urn:test", "HelloPort");
    ;
    private String bindingIdString = "http://schemas.xmlsoap.org/wsdl/soap/http";


    private String endpointAddress = "http://localhost:8080/jaxrpc-client_dispatch_service_wsdl/hello";
    private Service service;
    private Service serviceWithPorts;
    private Dispatch dispatch;


    public DispatchHello(String name) {
        super(name);

        // we'll fix the test harness correctly later,
        // so that test code won't have to hard code any endpoint address nor transport,
        // but for now let's just support local and HTTP to make unit tests happier.
        // this is not a good code, but it's just a bandaid solutino that works for now.
        if (ClientServerTestUtil.useLocal())
            endpointAddress = "local://" + new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\', '/') + '?' + portQName.getLocalPart();
        else
            endpointAddress = "http://localhost:8080/jaxrpc-client_dispatch_service_wsdl/hello";
    }

    private void createService() {

        try {
            service = Service.create(serviceQName);
            //does service.addPort(portQName, bindingIdString, endpointAddress
            addPort(service, portQName, bindingIdString, endpointAddress);
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

        /*try {

            if (ClientServerTestUtil.useLocal())
                serviceURI = new URI("file","c:/rearch/jaxws-ri/test/src/client/dispatch/wsdl_hello/config/hello_literal.wsdl", null);
            else
                serviceURI = new URI(endpointAddress + "?wsdl");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
         */
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


    private Dispatch createDispatchJAXB() {
        try {
            JAXBContext context = createJAXBContext();
            dispatch = service.createDispatch(portQName, context, Service.Mode.PAYLOAD);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return dispatch;
    }

    private Dispatch createDispatchJAXBPortsAvailable() {
        try {

            JAXBContext context = createJAXBContext();
            dispatch = serviceWithPorts.createDispatch(portQName, context,
                    Service.Mode.PAYLOAD);

        } catch (WebServiceException e) {
            fail("error creating service with ports");
        }
        return dispatch;
    }

    private Dispatch createDispatchSource() {
        try {
            dispatch = service.createDispatch(portQName, Source.class,
                    Service.Mode.PAYLOAD);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return dispatch;
    }

    private Dispatch createDispatchSourceWithPorts() {
        try {
            dispatch = serviceWithPorts.createDispatch(portQName, Source.class,
                    Service.Mode.PAYLOAD);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return dispatch;
    }


    private Dispatch createDispatchSOAPMessage() {
        try {
            dispatch = service.createDispatch(portQName, SOAPMessage.class,
                    Service.Mode.MESSAGE);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return dispatch;
    }

    private Dispatch createDispatchSOAPMessageSource() {
        try {

            dispatch = service.createDispatch(portQName, Source.class,
                    Service.Mode.MESSAGE);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return dispatch;
    }

    private Dispatch getDispatchJAXB() {
        createService();
        return createDispatchJAXB();
    }

    private Dispatch getDispatchJAXBWithPorts() {
        createServiceWithWSDL();
        return createDispatchJAXBPortsAvailable();
    }

    private Dispatch getDispatchSource() {
        createService();
        return createDispatchSource();
    }

    private Dispatch getDispatchSourceWithPorts() {
        createServiceWithWSDL();
        return createDispatchSourceWithPorts();
    }

    private Dispatch getDispatchSOAPMessage() {
        createService();
        return createDispatchSOAPMessage();
    }

    private Dispatch getDispatchSOAPMessageSource() {
        createService();
        return createDispatchSOAPMessageSource();
    }


    private static javax.xml.bind.JAXBContext createJAXBContext() {
        try {
            return javax.xml.bind.JAXBContext.newInstance(client.dispatch.service_wsdl.client.ObjectFactory.class);
        } catch (javax.xml.bind.JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }


    /*
     *  Create a Service with a WSDL. For a Dispatch Client add a Dummy port.
     *  A Dispatch client should always be able to add a port unless it already
     *  even though it is unknown to the WSDL.   Issue 136.
     */
    public void testServiceWithWSDLandAddNonExistantPort() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("http transport only exiting");
            return;
        }

        JAXBContext jc = null;

        Hello_Type hello = new Hello_Type();

        jc = createJAXBContext();
        try {
            hello.setArgument("foo");
            hello.setExtra("Test");

            createServiceWithWSDL();
            QName dummyPort = new QName("DummyPort");
            serviceWithPorts.addPort(dummyPort, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);

            JAXBContext context = createJAXBContext();
            dispatch = serviceWithPorts.createDispatch(dummyPort, context,
                    Service.Mode.PAYLOAD);

            HelloResponse result = (HelloResponse) dispatch.invoke(hello);

            assertEquals(result.getExtra(), hello.getExtra());
            assertEquals(result.getArgument(), hello.getArgument());
        } catch (WebServiceException jex) {
            jex.printStackTrace();
            fail("testServiceWithWsdlandNonExistentPort failed");
        }
    }

    /*
     *  Create a Service with a WSDL. For a Dispatch Client add a Dummy port.
     *  A Dispatch client should always be able to add a port unless it already
     *  even though it is unknown to the WSDL.   Issue 136.
     */
    public void xxtestServiceNoWSDLandAddDummyPort() throws Exception {

        JAXBContext jc = null;

        Hello_Type hello = new Hello_Type();

        jc = createJAXBContext();
        try {
            hello.setArgument("foo");
            hello.setExtra("Test ");

            createService();
            service.addPort(new QName("DummyPort"), SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);

            JAXBContext context = createJAXBContext();
            dispatch = service.createDispatch(portQName, context,
                    Service.Mode.PAYLOAD);

            HelloResponse result = (HelloResponse) dispatch.invoke(hello);

            assertEquals(result.getExtra(), hello.getExtra());
            assertEquals(result.getArgument(), hello.getArgument());
        } catch (WebServiceException jex) {
            fail("testHelloRequestResponseJAXB FAILED");
        }
    }

    public void xxtestHelloRequestResponseJAXBWPorts() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("http transport only exiting");
            return;
        }
        JAXBContext jc = null;

        HelloResponse result = null;
        Hello_Type hello = new Hello_Type();

        jc = createJAXBContext();
        try {
            hello.setArgument("foo");
            hello.setExtra("Test ");

            Dispatch dispatch = getDispatchJAXBWithPorts();

            result = (HelloResponse) dispatch.invoke(hello);

            assertEquals(((HelloResponse) result).getExtra(), hello.getExtra());
            assertEquals(((HelloResponse) result).getArgument(), hello.getArgument());
        } catch (WebServiceException jex) {
            fail("testHelloRequestResponseJAXB FAILED");
        }
    }

    public void xxtestHelloRequestResponseXMLWithWSDL() throws Exception {

        if (ClientServerTestUtil.useLocal())
            return;

        Dispatch dispatch = getDispatchSourceWithPorts();
        assertTrue(dispatch != null);

        Source request = makeStreamSource(helloMsg);
        Object result = dispatch.invoke(request);
        assertTrue(result instanceof Source);
        String xmlResult = sourceToXMLString((Source) result);
        System.out.println("Got result : " + xmlResult);
    }

    /*
     * for debugging
     */
    public static void main(String [] args) {
        try {
            if (ClientServerTestUtil.useLocal()) {
                System.out.println("http transport only exiting");
                return;
            }
            System.setProperty("uselocal", "true");
            System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "optimistic");
            DispatchHello testor = new DispatchHello("TestClient");
            //testor.testHelloRequestResponseSOAPMessageSource();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
