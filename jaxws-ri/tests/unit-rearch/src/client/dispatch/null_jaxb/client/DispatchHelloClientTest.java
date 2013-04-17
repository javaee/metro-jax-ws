/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2013 Oracle and/or its affiliates. All rights reserved.
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

package client.dispatch.null_jaxb.client;

import client.dispatch.null_jaxb.client.HelloRequest;
import client.dispatch.null_jaxb.client.HelloResponse;
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
import javax.xml.ws.soap.SOAPFaultException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author JAX-RPC RI Development Team
 */
public class DispatchHelloClientTest extends DispatchTestCase {

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

    private String delayTest = "<delay xmlns=\"urn:test:types\"><interval>10000</interval></delay>";

    //new QName("urn:test:types", "Hello")
    private QName serviceQName = new QName("urn:test", "HelloService");
    private QName portQName = new QName("urn:test", "HelloPort");
    ;
    private String bindingIdString = SOAPBinding.SOAP11HTTP_BINDING;

    private String endpointAddress;
    private Service service;
    private Service serviceWithPorts;
    private Dispatch dispatch;


    public DispatchHelloClientTest(String name) {
        super(name);

        // we'll fix the test harness correctly later,
        // so that test code won't have to hard code any endpoint address nor transport,
        // but for now let's just support local and HTTP to make unit tests happier.
        // this is not a good code, but it's just a bandaid solutino that works for now.
        if(ClientServerTestUtil.useLocal())
            endpointAddress = "local://"+new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\','/')+'?'+portQName.getLocalPart();
        else
            endpointAddress = "http://localhost:/jaxrpc-client_dispatch_null_jaxb/hello";
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

    private void createServiceWithWSDL() {
        URL serviceURL = null;
        try {
            serviceURL = new URL(endpointAddress + "?wsdl");
        } catch (MalformedURLException e) {
            fail("Error creating service with wsdl");
        }
        try {
            serviceWithPorts = Service.create(serviceURL, serviceQName);
        } catch (WebServiceException e) {
            e.printStackTrace();
        }
    }


    private Dispatch createDispatchJAXB() {
        try {
            JAXBContext context = DispatchHelloClientTest.createJAXBContext();
            dispatch = service.createDispatch(portQName, context, Service.Mode.PAYLOAD);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return dispatch;
    }

    private Dispatch createDispatchJAXBPortsAvailable() {
        try {
            JAXBContext context = DispatchHelloClientTest.createJAXBContext();
            dispatch = serviceWithPorts.createDispatch(portQName, context,
                Service.Mode.PAYLOAD);


        } catch (WebServiceException e) {
            e.printStackTrace();
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

    private Dispatch getDispatchSOAPMessage() {
        createService();
        return createDispatchSOAPMessage();
    }

    private Dispatch getDispatchSOAPMessageSource() {
        createService();
        return createDispatchSOAPMessageSource();
    }


    private static JAXBContext createJAXBContext() {
        try {
            return JAXBContext.newInstance(client.dispatch.null_jaxb.client.ObjectFactory.class);
        } catch (javax.xml.bind.JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }


    //should send empty SOAPBody here - no argument returned or perhaps server side exception
    //nillable allowed message
    public void testNoParamHelloRequestResponseJAXBNegative() throws Throwable {

        JAXBContext jc = null;
        HelloRequest hello = null;
        HelloResponse helloResult = null;
        try {
            jc = createJAXBContext();

        } catch (Exception jbe) {
            fail("Problem creating jaxb context");
        }

        try {
            //no hello args
            Dispatch dispatch = getDispatchJAXB();

            Object result = dispatch.invoke(null);
            assertTrue(result == null);

        } catch (Exception e) {

            assertTrue(e instanceof SOAPFaultException);
            System.out.println("NoParamHelloRequestResponseJAXBNegative PASSED");
        }
    }

    public void testNoParamRequestResponseXMLNegative() throws Exception {

        Dispatch dispatch = getDispatchSource();

        try {
            Source src = makeStreamSource(helloMsg);
            Object result = dispatch.invoke(null);
            assertTrue(result == null);
        } catch (Exception ex) {
            assertTrue(ex instanceof SOAPFaultException);
            System.out.println("NoParamHelloRequestResponseXMLNegative PASSED");
            ex.printStackTrace();
        }
    }


    public void testNOParamSOAPMessage() throws Exception {

        Dispatch dispatch = getDispatchSOAPMessage();
        byte[] bytes = helloSM.getBytes();
        Source source = makeStreamSource(helloSM);

        SOAPMessage message = getSOAPMessage(source);
        try {
            Object result = dispatch.invoke(null);
            assertTrue(result == null);
        } catch (Exception e) {
            assertTrue(e instanceof WebServiceException);
            System.out.println("Exception message " + e.getMessage());
        }
    }

    public void testNoParamSOAPMessageSource() throws Exception {

        Dispatch dispatch = getDispatchSOAPMessageSource();
        Source source = makeStreamSource(helloSM);
        try {
            Object result = dispatch.invoke(null);
            assertTrue(result == null);
        } catch (Exception e) {
            assertTrue(e instanceof WebServiceException);
            System.out.println("Exception message " + e.getMessage());
        }
    }

    /*
    * for debugging
    */
    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "optimistic");
            DispatchHelloClientTest testor = new DispatchHelloClientTest("TestClient");
            //testor.testHelloRequestResponseXML();
            //testor.testDelay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
