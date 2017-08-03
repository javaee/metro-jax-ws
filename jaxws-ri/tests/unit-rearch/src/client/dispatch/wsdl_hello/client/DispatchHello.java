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

package client.dispatch.wsdl_hello.client;

import client.common.client.DispatchTestCase;
import testutil.ClientServerTestUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;

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


    private String endpointAddress = "http://localhost:8080/jaxrpc-client_dispatch_wsdl_hello/hello";
    private static final String ENDPOINT_IMPL = "client.dispatch.wsdl_hello.server.Hello_PortType_Impl";
    private Service service;
    private Service serviceWithPorts;
    private Dispatch dispatch;


    public DispatchHello(String name) {
        super(name);

        // we'll fix the test harness correctly later,
        // so that test code won't have to hard code any endpoint address nor transport,
        // but for now let's just support local and HTTP to make unit tests happier.
        // this is not a good code, but it's just a bandaid solutino that works for now.
        if(ClientServerTestUtil.useLocal())
            endpointAddress = "local://"+new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\','/')+'?'+portQName.getLocalPart();
        else
            endpointAddress = "http://localhost:8080/jaxrpc-client_dispatch_wsdl_hello/hello";
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
            return javax.xml.bind.JAXBContext.newInstance(client.dispatch.wsdl_hello.client.ObjectFactory.class);
        } catch (javax.xml.bind.JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }



    //not working yet for dispatch
    public void tkkkestDispatchPropertySoapUseAction() {


        Dispatch dispatch = getDispatchSource();
        Source source = makeStreamSource(helloMsg);

        ((BindingProvider) dispatch).getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        ((BindingProvider) dispatch).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "urn:test:hello");
        Object result = dispatch.invoke(source);
        assertTrue(result instanceof Source);
        String xmlResult = sourceToXMLString((Source) result);
        System.out.println("Got result : " + xmlResult);

        // ((BindingProvider) dispatch).getRequestContext().get(BindingProvider.SOAPACTION_USE_PROPERTY);
        // ((BindingProvider) dispatch).getRequestContext().get(BindingProvider.SOAPACTION_URI_PROPERTY);
    }

    /* public void testStubPropertySoapUseActionNegative() {

        try {
            Hello stub = getDynamicStubDynamicServer();
            // get stub
            Boolean prop = (Boolean)((BindingProvider) stub).getRequestContext().get(BindingProvider.SOAPACTION_USE_PROPERTY);
             ((BindingProvider) stub).getRequestContext().get(BindingProvider.SOAPACTION_URI_PROPERTY);

        } catch (Exception ex) {

            if (ex instanceof WebServiceException) {
                System.out.println("Property test passes");
            }
        }
    }
    */


    public void testHelloRequestResponseJAXBandSource() throws Exception {
        //test for bug 6194159
        JAXBContext jc = null;

        HelloResponse helloResult = null;
        Hello_Type hello = new Hello_Type();

        jc = createJAXBContext();
        try {
            hello.setArgument("foo");
            hello.setExtra("Test ");


            Dispatch dispatch = getDispatchJAXB();
            Dispatch dispatch2 = getDispatchSource();

            dispatch.getRequestContext().put("com.sun.xml.pw.testprop", "test");

            Response response = dispatch.invokeAsync(hello);
            if (!response.isDone()) {
                dispatch2.invokeAsync(makeStreamSource(helloMsg));
            }
            Object result = response.get();

            assertEquals(((HelloResponse) result).getExtra(), hello.getExtra());
            assertEquals(((HelloResponse) result).getArgument(), hello.getArgument());
        } catch (Exception jex) {
            fail("Expected HelloResponse go Exception");
        }
    }

    public void testHelloRequestResponseJAXB() throws Exception {

        JAXBContext jc = null;

        HelloResponse helloResult = null;
        Hello_Type hello = new Hello_Type();

        jc = createJAXBContext();
        try {
            hello.setArgument("foo");
            hello.setExtra("Test ");

            Dispatch dispatch = getDispatchJAXB();

            helloResult = (HelloResponse) dispatch.invoke(hello);

            //assertEquals(((HelloResponse) result).getExtra(), hello.getExtra());
            //assertEquals(((HelloResponse) result).getArgument(), hello.getArgument());
        } catch (WebServiceException jex) {
            fail("testHelloRequestResponseJAXB FAILED");
        }
    }

    public void testHelloRequestResponseJAXBWPorts() throws Exception {

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

    public void testHelloRequestResponseJAXBPortsAvailable() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("http transport only exiting");
            return;
        }

        JAXBContext jc = null;

        HelloResponse helloResult = null;
        Hello_Type hello = new Hello_Type();

        jc = createJAXBContext();
        try {
            hello.setArgument("foo");
            hello.setExtra("Test ");

            Dispatch dispatch = getDispatchJAXBWithPorts();

            Object result = dispatch.invoke(hello);

            assertEquals(((HelloResponse) result).getExtra(), hello.getExtra());
            assertEquals(((HelloResponse) result).getArgument(), hello.getArgument());
        } catch (WebServiceException jex) {
            fail("testHelloRequestResponseJAXB FAILED");
        }
    }

    //todo:investigate failed assertion bad JAXB message should fail after invocation RemoteException?
    /* public void testHelloResponseBadRequestResponseJAXB() throws Throwable {

          JAXBContext jc = null;
          NameType hello = null;
          HelloResponse helloResult = null;

          jc = createJAXBContext();
          hello = new NameType();
          helloResult = new HelloResponse();

          try {
              //no remote method
              hello.getName().add("foo");

              Dispatch dispatch = getDispatchJAXB();

              Object result = dispatch.invoke(hello);
              System.out.println(" testHelloResponseBadRequestResponseJAXB FAILED");
          } catch (Exception e) {
              try {
              assertTrue(e instanceof SOAPFaultException);
              //assertTrue(e.getCause() instanceof SOAPFaultException);
              System.out.println(" testHelloResponseBadRequestResponseJAXB PASSED");
              } catch (Exception ex){
                System.out.println(" testHelloResponseBadRequestResponseJAXB FAILED");
              }
          }
      }
    */

    //todo:Investigate failed assertion invoking Result not request - expect remoteException w/SOAPFailtEx
    /*  public void testHelloResponseBad2RequestResponseJAXB() throws Throwable {

          JAXBContext jc = null;
          HelloResponse helloResult = null;

          try {
              jc = createJAXBContext();
              helloResult = new HelloResponse();
          } catch (Exception jbe) {
              jbe.printStackTrace();
          }

          try {
              helloResult.setExtra("Test ");
              helloResult.setArgument("Dispatch ");

              Dispatch dispatch = getDispatchJAXB();

              Object result = dispatch.invoke(helloResult);
              System.out.println(" testHelloResponseBadRequestResponseJAXB FAILED");
          } catch (Exception e) {
              try {
              assertTrue(e instanceof SOAPFaultException);
              //assertTrue(e.getCause() instanceof SOAPFaultException);
              System.out.println(" testHelloResponseBad3RequestResponseJAXB PASSED");
              } catch (Exception ex){
                 System.out.println(" testHelloResponseBadRequestResponseJAXB FAILED");
              }
          }
      }
      */

    public void testNoParamHelloRequestResponseJAXB() throws Throwable {

        JAXBContext jc = null;
        Hello_Type hello = null;
        HelloResponse helloResult = null;
        try {
            jc = createJAXBContext();
            hello = new Hello_Type();
            helloResult = new HelloResponse();
        } catch (Exception jbe) {
            assertTrue(jbe instanceof JAXBException);
        }

        try {
            //no hello args
            Dispatch dispatch = getDispatchJAXB();

            Object result = dispatch.invoke(null);
            assertTrue(result == null);
            //System.out.println("NoParamHelloRequestResponseJAXB FAILED");
        } catch (Exception e) {
            assertTrue(e instanceof SOAPFaultException);
            System.out.println("no param result is " + e.getClass().getName());
            //assertTrue(e instanceof WebServiceException);
            System.out.println("NoParamHelloRequestResponseJAXB PASSED");
        }
    }

    //no arg- extra
    public void testBadHelloRequestResponseXML() throws WebServiceException {

        System.err.println("----------------Expected Message ----------------------");
        Dispatch dispatch = getDispatchSource();
        Source source = null;
        Source response = null;
        try {
            source = makeStreamSource(badhelloMsg);
            response = makeStreamSource(helloResponse);
        } catch (Exception e) {
            System.out.println("Error making msg source exiting");
            return;
        }
        try {
            Object result = dispatch.invoke(source);
            assertTrue(result == null);
            //System.out.println("BadHelloRequestResponseXML FAILED");
        } catch (Exception e) {
                assertTrue(e instanceof WebServiceException);
                System.out.println("BadHelloRequestResponseXML PASSED");
        }
        System.err.println("----------------END Expected Message ----------------------");
    }

    //no arg- extra
    public void kkktestSQETestXML() throws WebServiceException, Exception {

        //System.err.println("----------------Expected Message ----------------------");
        Dispatch dispatch = getDispatchSource();
        Source source = null;
        Source response = null;
        source = makeStreamSource(sqeTest);

            Object result = dispatch.invoke(source);
            String xmlResult = sourceToXMLString((Source) result);
            System.out.println("Got result : " + xmlResult);
            assertTrue(result == null);
            System.out.println("sqeXML Passed");
    }

    public void kkktestSQETest2XML() throws WebServiceException, Exception {

        //System.err.println("----------------Expected Message ----------------------");
        Dispatch dispatch = getDispatchSource();
        Source source = null;
        Source response = null;

        source = makeDOMSource(bugTest2);

        Object result = dispatch.invoke(source);
        String xmlResult = sourceToXMLString((Source) result);
        System.out.println("Got result : " + xmlResult);
        assertTrue(result == null);
        System.out.println("sqeXML Passed");
    }


    public void testHelloRequestResponseXML() throws Exception {

        Dispatch dispatch = getDispatchSource();
        assertTrue(dispatch != null);

        //Source request = makeStreamSource(helloMsg);
        //Object result = dispatch.invoke(request);
        //assertTrue(result instanceof Source);
        //String xmlResult = sourceToXMLString((Source) result);
        //System.out.println("Got result : " + xmlResult);

        Collection<Source> sourceList = makeMsgSource(helloMsg);
        Collection<Source> responseList = makeMsgSource(helloResponse);
        try {
            for (Iterator iter = sourceList.iterator(); iter.hasNext();) {

                Object sourceObject = iter.next();
                Object result = dispatch.invoke(sourceObject);
                assertTrue(result instanceof Source);
                String xmlResult = sourceToXMLString((Source) result);
                System.out.println("Got result : " + xmlResult);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("testHelloRequestResponseXML FAILED");
        }
    }


    public void testHelloRequestResponseXMLWithWSDL() throws Exception {

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

//TODO:Investigate failed assertionResponse is sent instead as Request

    public void testBad3HelloResponseRequestResponseXML() {

        Dispatch dispatch = getDispatchSource();
        Source source = makeStreamSource(helloResponse);

        try {
            Object result = dispatch.invoke(source);
            assertTrue(result == null);
            fail("testBad3HelloResponseRequestResponseXML failed");
        } catch (Exception e) {
            assertTrue(e instanceof SOAPFaultException);
            System.out.println("testBad3HelloResponseRequestResponseXML Passed");
        }
    }

//bad namespace- expect remote exception

    public void testBad3HelloRequestResponseXML() {

        Dispatch dispatch = getDispatchSource();

        Source source = makeStreamSource(bad3helloMsg);
        Source response = makeStreamSource(helloResponse);

        try {
            Object result = dispatch.invoke(source);
            assertTrue(result == null);
            System.out.println("B3HelloRR invoke succeded");
        } catch (Exception e) {
            assertTrue(e instanceof SOAPFaultException);
            System.out.println("testBad3HelloRequestResponseXML Passed");
        }
    }

    public void testHelloRequestResponseHelloMsgBadFooXML() {
        //test for bug6323952
        Dispatch dispatch = getDispatchSource();

        Source source = makeStreamSource(helloMsgBadFoo);
        Source response = makeStreamSource(helloResponse);

        try {
            Source result = (Source) dispatch.invoke(source);
            assertTrue(result == null);

        } catch (Exception e) {
            assertTrue(e instanceof SOAPFaultException);
            System.out.println("e class " + e.getClass().getName());
            if (e.getCause() != null)
                System.out.println("Cause is " + e.getCause().getClass().getName());
            System.out.println("testHelloRequestBadFooResponseXML Passed");
        }
    }


    public void testHelloRequestResponseSOAPMessage() throws Exception {

        Dispatch dispatch = getDispatchSOAPMessage();
        assertTrue(dispatch != null);
        assertTrue(dispatch instanceof com.sun.xml.ws.client.dispatch.SOAPMessageDispatch);
        byte[] bytes = helloSM.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        Source source = makeStreamSource(helloSM);

        SOAPMessage message = getSOAPMessage(source);

        Object result = dispatch.invoke(message);
        //todo:need to check contents
        assertTrue(result instanceof SOAPMessage);
        ((SOAPMessage) result).writeTo(System.out);

    }

    public void testHelloRequestResponseSOAPMessageSource() throws Exception {

        Dispatch dispatch = getDispatchSOAPMessageSource();
        Source source = makeStreamSource(helloSM);
        Object result = dispatch.invoke(source);
        System.out.println("Result class is " + result.getClass().getName());
        //todo: need to check contents
        assertTrue(result instanceof Source);
        String xmlResult = sourceToXMLString((Source) result);
        System.out.println(xmlResult);
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
