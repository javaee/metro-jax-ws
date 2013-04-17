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

package client.dispatch.header.doclit.client;

import testutil.ClientServerTestUtil;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.*;
import javax.xml.soap.SOAPMessage;
import java.io.*;

//import client.dispatch.TestCaseBase;

/**
 * @author JAX-RPC RI Development Team
 */
public class DispatchHeaderTest extends TestCaseBase {

    QName serviceQName = new QName("urn:test", "Hello");
    QName portQName = new QName("urn:test", "HelloPort");

    private String endpointAddress;


    public DispatchHeaderTest(String name) {
        super(name);

        // we'll fix the test harness correctly later,
        // so that test code won't have to hard code any endpoint address nor transport,
        // but for now let's just support local and HTTP to make unit tests happier.
        // this is not a good code, but it's just a bandaid solutino that works for now.
        if(ClientServerTestUtil.useLocal())
            endpointAddress = "local://"+new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\','/')+'?'+portQName.getLocalPart();
        else
            endpointAddress = "http://localhost:/jaxrpc-client_dispatch_header_doclit/hello";
    }

    public void init(String endpointAddress, QName portQName, QName serviceQName, JAXBContext context) {
        super.init(endpointAddress, portQName, serviceQName, context);
        setLog(System.out);
    }


    private static javax.xml.bind.JAXBContext createJAXBContext() {
        try {
            return JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void testEchoSource() throws Exception {
        init(endpointAddress, portQName, serviceQName, null);

        Source source = makeStreamSource(echoRequest);
        Dispatch dispatch = getDispatchSource();
        try {
            System.err.println("************Expected EXCEPTION***************");
            Object result = dispatch.invoke(source);

            assertTrue(result == null);
        } catch (Exception sfe) {
            System.err.println("************End Expected EXCEPTION***************");
            assertTrue(sfe instanceof SOAPFaultException);

        }
    }
    
    public void testEcho5JAXB() throws Exception {
        init(endpointAddress, portQName, serviceQName, createJAXBContext());
        Dispatch dispatch = getDispatchJAXB();

        EchoType body = new EchoType();
        body.setReqInfo("Hello World!");
        ObjectFactory of = new ObjectFactory();
        JAXBElement<EchoType> echo5Request = of.createEcho5Request(body);
        JAXBElement<String> resp = (JAXBElement<String>) dispatch.invoke(echo5Request);
        assertTrue(resp == null);
    }


    public void testEcho4SOAPMessage() throws Exception {
        init(endpointAddress, portQName, serviceQName, null);
        Dispatch dispatch = getDispatchSOAPMessage();
        assertTrue(dispatch!= null);
        assertTrue(dispatch instanceof com.sun.xml.ws.client.dispatch.SOAPMessageDispatch);
        //not sure why doing this
        byte[] bytes = echo4RequestSM.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        //not sure why doint that above

        Source source = makeStreamSource(echo4RequestSM);
        SOAPMessage message = getSOAPMessage(source);

        Object result = dispatch.invoke(message);
        //todo:need to check contents

        System.out.println("Printing out received header");
        assertTrue(result instanceof SOAPMessage);
        ((SOAPMessage)result).writeTo(System.out);

    }

    //todo: other tests to be converted

    /*public void testEcho2() throws Exception {
        String response = stub.echo2("foo");
        assertEquals("foobar", response);
    }

    public void testEcho3() throws Exception {
        Holder<String> req = new Holder<String>("foo");
        stub.echo3(req);
        assertEquals("foobar", req.value);
    }


    public void testEcho4() throws Exception {
        Echo4Type reqBody = new Echo4Type();
        reqBody.setExtra("foo1");
        reqBody.setArgument("bar1");

        Echo4Type reqHeader = new Echo4Type();
        reqHeader.setExtra("foo2");
        reqHeader.setArgument("bar2");

        String req2HeaderType = "foobar3";
        Holder<String> req2Header = new Holder<String>(req2HeaderType);
        Holder<String> respBody = new Holder<String>();
        Holder<String> respHeader = new Holder<String>();

        stub.echo4(reqBody, reqHeader, req2HeaderType, respBody, respHeader);
        assertEquals("foo1bar1foo2bar2foobar3", respBody.value);
    }

    public void testEcho5() throws Exception {
        EchoType body = new EchoType();
        body.setReqInfo("Hello World!");
        String resp = stub.echo5(body);
        assertEquals(resp, body.getReqInfo());
    }
    */
    /**
     * TODO: this test has header as return type, it wont work till we have annotation
     * on @WebResult or similar solution. Commenting out till we have it.
     */
//    public void testEcho6() throws Exception {
//        EchoType body = new EchoType();
//        String reqName = "Vivek";
//        String address = "4140 Network Cirlce";
//        body.setReqInfo(reqName);
//        Holder<String> name = new Holder<String>();
//        EchoType header = new EchoType();
//        header.setReqInfo(address);
//        Holder<EchoType> req = new Holder<EchoType>(body);
//        String resp = stub.echo6(name, header, req);
//        assertEquals(req.value.getReqInfo(), reqName + "'s Response");
//        assertEquals(resp, name.value +"'s Address: "+address);
//    }

    /**
     * TODO: this test has header as return type, it wont work till we have annotation
     * on @WebResult or similar solution. Commenting out till we have it.
     */
//    public void testEcho7() throws Exception {
//        String firstName = "Vivek";
//        String lastName = "Pandey";
//        Holder<String> address = new Holder<String>();
//        Holder<String> personDetails = new Holder<String>();
//        NameType nameType = stub.echo7(address, personDetails, lastName, firstName);
//        assertEquals(nameType.getName(), "Employee");
//        assertEquals(address.value, "Sun Micro Address");
//        assertEquals(personDetails.value, "Vivek Pandey");
//    }

//test echo
    String echoRequestSM = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://example.com/types\"><soapenv:Header><ns1:echo><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo><ns1:echo2><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo2></soapenv:Header><soapenv:Body><ns1:echo><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo></soapenv:Body></soapenv:Envelope>";

    String echoResponseSM = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://example.com/types\"><soapenv:Body><ns1:echoResponse><ns1:respInfo>foofoofoo</ns1:respInfo></ns1:echoResponse></soapenv:Body></soapenv:Envelope>";

    String echoRequest = "<ns1:echo xmlns:ns1=\"http://example.com/types\"><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo>";

    String echoResponse = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://example.com/types\"><soapenv:Body><ns1:echoResponse><ns1:respInfo>foofoofoo</ns1:respInfo></ns1:echoResponse></soapenv:Body></soapenv:Envelope>";

    //******************

//testecho2
    //<?xml version="1.0" ?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns1="http://example.com/types"><soapenv:Body><ns1:echo2><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo2></soapenv:Body></soapenv:Envelope>

    // <?xml version="1.0" ?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns1="http://example.com/types"><soapenv:Body><ns1:echo2Response><ns1:respInfo>foobar</ns1:respInfo></ns1:echo2Response></soapenv:Body></soapenv:Envelope>******************

    // ******************
//echo3
    //<?xml version="1.0" ?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns1="http://example.com/types"><soapenv:Body><ns1:echo3><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo3></soapenv:Body></soapenv:Envelope>

    //<?xml version="1.0" ?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns1="http://example.com/types"><soapenv:Body><ns1:echo3><ns1:reqInfo>foobar</ns1:reqInfo></ns1:echo3></soapenv:Body></soapenv:Envelope>******************

    //******************
//echo4
    String echo4RequestSM = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://example.com/types\"><soapenv:Header><ns1:echo4><ns1:argument>bar2</ns1:argument><ns1:extra>foo2</ns1:extra></ns1:echo4><ns1:echo4Header>foobar3</ns1:echo4Header></soapenv:Header><soapenv:Body><ns1:echo4><ns1:argument>bar1</ns1:argument><ns1:extra>foo1</ns1:extra></ns1:echo4></soapenv:Body></soapenv:Envelope>";

    String echo4ResponseSM = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://example.com/types\"><soapenv:Header><ns1:echo4ResponseHeader>foobar3</ns1:echo4ResponseHeader></soapenv:Header><soapenv:Body><ns1:echo4Response>foo1bar1foo2bar2foobar3</ns1:echo4Response></soapenv:Body></soapenv:Envelope>";

    // ******************
//echo5

    String echo5Request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://example.com/types\"><soapenv:Body><ns1:echo5Request><ns1:reqInfo>Hello World!</ns1:reqInfo></ns1:echo5Request></soapenv:Body></soapenv:Envelope>";

    String echo5Response = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://example.com/types\"><soapenv:Header><ns1:echo5Response>Hello World!</ns1:echo5Response></soapenv:Header><soapenv:Body></soapenv:Body></soapenv:Envelope>";
}
