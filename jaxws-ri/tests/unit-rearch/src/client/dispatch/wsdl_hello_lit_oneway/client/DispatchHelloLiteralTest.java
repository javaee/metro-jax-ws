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

package client.dispatch.wsdl_hello_lit_oneway.client;


import junit.framework.TestCase;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import testutil.ClientServerTestUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.MessageFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import client.common.client.DispatchTestCase;

/**
 * @author JAX-RPC RI Development Team
 */
public class DispatchHelloLiteralTest extends DispatchTestCase {
    private String helloSM = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><Hello xmlns=\"urn:test:types\"><argument>foo</argument><extra>Test </extra></Hello></soapenv:Body></soapenv:Envelope>";
    //private String helloSM = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><Hello xmlns=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></Hello></soapenv:Body></soapenv:Envelope>";
    private String helloMsg =
            "<Hello xmlns=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></Hello>";
    private String voidMsg =
            "<VoidTest xmlns=\"urn:test:types\"/>";
    private String voidSM = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><VoidTest xmlns=\"urn:test:types\"/></soapenv:Body></soapenv:Envelope>";
    private QName serviceQName = new QName("urn:test", "Hello");
    private QName portQName = new QName("urn:test", "HelloPort");
    ;
    private String bindingIdString = SOAPBinding.SOAP11HTTP_BINDING;
    private String endpointAddress;
    private static final String ENDPOINT_IMPL = "client.dispatch.wsdl_hello_lit_oneway.server.Hello_PortType_Impl";
    private Service service;
    private Dispatch dispatch;


    public DispatchHelloLiteralTest(String name) {
        super(name);

        // we'll fix the test harness correctly later,
        // so that test code won't have to hard code any endpoint address nor transport,
        // but for now let's just support local and HTTP to make unit tests happier.
        // this is not a good code, but it's just a bandaid solutino that works for now.
        if(ClientServerTestUtil.useLocal())
            endpointAddress = "local://"+new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\','/')+'?'+portQName.getLocalPart();
        else
            endpointAddress = "http://localhost:8080/jaxrpc-client_dispatch_wsdl_hello_lit_oneway/hello";
    }

    private void createService() {

        service = Service.create(serviceQName);
        //does service.addPort(portQName, bindingIdString, endpointAddress
        addPort(service, portQName, bindingIdString, endpointAddress);
    }

    private static javax.xml.bind.JAXBContext createJAXBContext() {
        try {
            return javax.xml.bind.JAXBContext.newInstance(client.dispatch.wsdl_hello_lit_oneway.client.ObjectFactory.class);
        } catch (javax.xml.bind.JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }


    private Dispatch createDispatchJAXB() {

        JAXBContext context = createJAXBContext();
        return service.createDispatch(portQName, context, Service.Mode.PAYLOAD);
    }

    private Dispatch createDispatchSource() {
        return service.createDispatch(portQName, Source.class, Service.Mode.PAYLOAD);
    }

    private Dispatch createDispatchSOAPMessage() {

        return service.createDispatch(portQName, SOAPMessage.class,
                Service.Mode.MESSAGE);
    }

    private Dispatch createDispatchSOAPMessageSource() {

        return service.createDispatch(portQName, Source.class,
                Service.Mode.MESSAGE);
    }


    private Dispatch getDispatchSOAPMessage() {
        createService();
        return createDispatchSOAPMessage();
    }

    private Dispatch getDispatchSOAPMessageSource() {
        createService();
        return createDispatchSOAPMessageSource();
    }

    private Dispatch getDispatchJAXB() {
        createService();
        return createDispatchJAXB();
    }

    private Dispatch getDispatchSource() {
        createService();
        return createDispatchSource();
    }

    //TODo: add assertion for oneway check to all tests
    public void testHelloOnewayRequestJAXB() {

        JAXBContext jc = null;
        Hello_Type hello = null;

        try {
            jc = createJAXBContext();
            hello = new Hello_Type();
        } catch (Exception jbe) {
            jbe.printStackTrace();
        }

        try {
            hello.setExtra("Test ");
            hello.setArgument("Dispatch ");

            Dispatch dispatch = getDispatchJAXB();
            dispatch.invokeOneWay(hello);

        } catch (Exception e) {
            fail("testHelloOnewayRequestJAXB with exception " + e.getMessage());
        }
    }


    public void testHelloOnewayRequestXML() {

        Dispatch dispatch = getDispatchSource();
        Collection<Source> sourceList = makeMsgSource(helloMsg);

        for (Iterator iter = sourceList.iterator(); iter.hasNext();) {
            try {
                Object sourceObject = iter.next();
                dispatch.invokeOneWay(sourceObject);
            } catch (Exception e) {
                fail("testHelloOnewayRequestXML with exception " + e.getMessage());
            }
        }
    }

    public void testVoidOnewayRequestJAXB() {

        JAXBContext jc = null;
        VoidTest voidTest = null;

        try {
            jc = createJAXBContext();
            voidTest = new VoidTest();
        } catch (Exception jbe) {
            jbe.printStackTrace();
        }

        Dispatch dispatch = getDispatchJAXB();

        try {
            dispatch.invokeOneWay(voidTest);
        } catch (Exception e) {
            fail("testVoidOnewayRequestJAXB with exception " + e.getMessage());
        }
    }

    public void testVoidOnewayRequestXML() {

        Dispatch dispatch = getDispatchSource();
        Collection<Source> sourceList = makeMsgSource(voidMsg);

        for (Iterator iter = sourceList.iterator(); iter.hasNext();) {
            try {
                Object sourceObject = iter.next();
                dispatch.invokeOneWay(sourceObject);
            } catch (Exception e) {
                fail("testVoidOnewayRequestXML with exception " + e.getMessage());
            }
        }
    }

    public void testHelloRequestOnewaySOAPMessage() throws Exception {

        try {
            Dispatch dispatch = getDispatchSOAPMessage();
            assertTrue(dispatch != null);
            assertTrue(dispatch instanceof com.sun.xml.ws.client.dispatch.SOAPMessageDispatch);

            Source source = makeStreamSource(helloSM);
            SOAPMessage message = getSOAPMessage(source);
            dispatch.invokeOneWay(message);
        } catch (Exception e) {
            fail("testHelloRequestOnewaySOAPMessage with exception " + e.getMessage());
        }
    }

    public void testHelloRequestOnewaySOAPMessageSource() throws Exception {

        Dispatch dispatch = getDispatchSOAPMessageSource();
        Source source = makeStreamSource(helloSM);
        dispatch.invokeOneWay(source);
    }

}
