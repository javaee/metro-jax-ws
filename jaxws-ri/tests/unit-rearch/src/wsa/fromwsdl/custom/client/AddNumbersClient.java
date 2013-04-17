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

package wsa.fromwsdl.custom.client;

import com.sun.xml.ws.addressing.W3CAddressingConstants;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.OneWayFeature;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.util.DOMUtil;
import com.sun.xml.ws.streaming.XMLStreamReaderException;
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Element;
import testutil.ClientServerTestUtil;
import testutil.WsaUtils;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.ws.*;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author Arun Gupta
 */
public class AddNumbersClient extends XMLTestCase {
    private static final QName SERVICE_QNAME = new QName("http://example.com/", "AddNumbersService");
    private static final QName PORT_QNAME = new QName("http://example.com/", "AddNumbersPort");
    private static final QName PORT_TYPE_QNAME = new QName("http://example.com/", "AddNumbersPortType");
    private static final String ENDPOINT_ADDRESS = "http://localhost:/jaxrpc-wsa_fromwsdl_custom/hello";
    private static final String CORRECT_ACTION = "http://example.com/AddNumbersPortType/addNumbersRequest";

    public AddNumbersClient(String name) {
        super(name);
    }

    private String getAddress() {
        if (ClientServerTestUtil.useLocal())
            return ClientServerTestUtil.getLocalAddress(PORT_QNAME);
        else
            return ENDPOINT_ADDRESS;
    }

    private String getWsdlAddress() {

        // we'll fix the test harness correctly later,
        // so that test code won't have to hard code any endpoint address nor transport,
        // but for now let's just support local and HTTP to make unit tests happier.
        // this is not a good code, but it's just a bandaid solutino that works for now.
        if (ClientServerTestUtil.useLocal())
            return "local://" + new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\', '/') + '?' + "hello_literal.wsdl";
        else
            return "http://localhost:/jaxrpc-wsa_fromwsdl_custom/hello?wsdl";
    }

    private Dispatch<SOAPMessage> createDispatchWithoutWSDL() throws Exception {
        Service service = Service.create(SERVICE_QNAME);
        service.addPort(PORT_QNAME, SOAPBinding.SOAP11HTTP_BINDING, getAddress());
        Dispatch<SOAPMessage> dispatch = service.createDispatch(PORT_QNAME,
                                                                SOAPMessage.class,
                                                                Service.Mode.MESSAGE,
                                                                new AddressingFeature(false, false));

        return dispatch;
    }

    private Dispatch<SOAPMessage> createDispatchWithWSDL() throws Exception {
        AddNumbersService service = new AddNumbersService();
        return service.createDispatch(PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE, new AddressingFeature());
    }

    private AddNumbersPortType createStub(WebServiceFeature... features) throws Exception {
        return new AddNumbersService().getAddNumbersPort(features);
    }

    /**
     * This test is added to test Addressing with only requitred headers in the message.
     * In 2.1.3, wsa:To is checked for presence although it is not requried to be present
     * as per W3C Spec.
     *  This test sends only wsa:Action and wsa:MessageId headers in the request. 
     * @throws Exception
     */
    public void testAddressingWithOnlyRequiredHeaders () throws Exception{
       WsaUtils.invoke(createDispatchWithoutWSDL(),
                            WsaUtils.SIMPLE_ADDRESSING_MESSAGE,
                            WsaUtils.S11_NS,
                            WsaUtils.W3C_WSA_NS,
                            CORRECT_ACTION);
    }

    public void testBadAction() throws Exception {
        try {
            WsaUtils.invoke(createDispatchWithoutWSDL(),
                            WsaUtils.BAD_ACTION_MESSAGE,
                            WsaUtils.S11_NS,
                            WsaUtils.W3C_WSA_NS,
                            getAddress(),
                            W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.ACTION_NOT_SUPPORTED_QNAME);
        }
    }

    public void testMissingAddressingHeaders() throws Exception {
            try {
                WsaUtils.invoke(createDispatchWithoutWSDL(),
                                WsaUtils.NO_ADDRESSING_MESSAGE,
                                WsaUtils.S11_NS);
                fail("SOAPFaultException must be thrown");
            } catch (SOAPFaultException sfe) {
                assertFault(sfe, W3CAddressingConstants.MAP_REQUIRED_QNAME);
            }
        }


    public void testMissingAction() throws Exception {
        try {
            WsaUtils.invoke(createDispatchWithoutWSDL(),
                            WsaUtils.MISSING_ACTION_MESSAGE,
                            WsaUtils.S11_NS,
                            WsaUtils.W3C_WSA_NS,
                            getAddress(),
                            W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.MAP_REQUIRED_QNAME);
        }
    }

    public void testMissingAddressInReplyTo() throws Exception {
        try {
            WsaUtils.invoke(createDispatchWithoutWSDL(),
                            WsaUtils.MISSING_ADDRESS_MESSAGE,
                            WsaUtils.S11_NS,
                            WsaUtils.W3C_WSA_NS,
                            getAddress(),
                            CORRECT_ACTION);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.MISSING_ADDRESS_IN_EPR);
        }
    }

    public void testReplyToRefps() throws Exception {
        SOAPMessage response = WsaUtils.invoke(createDispatchWithoutWSDL(),
                                               WsaUtils.REPLY_TO_REFPS_MESSAGE,
                                               WsaUtils.S11_NS,
                                               WsaUtils.W3C_WSA_NS,
                                               getAddress(),
                                               W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS,
                                               CORRECT_ACTION);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        response.writeTo(baos);
//        assertXpathExists(REPLY_TO_REFPS, baos.toString());
//        assertXpathEvaluatesTo("Key#123456789", REPLY_TO_REFPS_VALUE, baos.toString());
//        assertXpathExists(REPLY_TO_REFPS_ISREFP, baos.toString());
    }

    public void testInvalidReplyTo() throws Exception {
        try {
            SOAPMessage response = WsaUtils.invoke(createDispatchWithoutWSDL(),
                                               WsaUtils.INVALID_REPLY_TO_MESSAGE,
                                               WsaUtils.S11_NS,
                                               WsaUtils.W3C_WSA_NS,
                                               getAddress(),
                                               "WRONG",
                                               W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS,
                                               CORRECT_ACTION);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertTrue("Got SOAPFaultException", true);
        }
    }

    public void testFaultToRefps() throws Exception {
        try {
            WsaUtils.invoke(createDispatchWithoutWSDL(),
                            WsaUtils.FAULT_TO_REFPS_MESSAGE,
                            WsaUtils.S11_NS,
                            WsaUtils.W3C_WSA_NS,
                            getAddress(),
                            W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS,
                            W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS,
                            CORRECT_ACTION);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertTrue("Got SOAPFaultException", true);
        }
    }

    public void testDuplicateToHeader() throws Exception {
        try {
            WsaUtils.invoke(createDispatchWithWSDL(),
                            WsaUtils.DUPLICATE_TO_MESSAGE,
                            WsaUtils.S11_NS,
                            WsaUtils.W3C_WSA_NS,
                            getAddress(),
                            getAddress());
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.INVALID_CARDINALITY);
        }
    }

    public void testDuplicateReplyToHeader() throws Exception {
        try {
            WsaUtils.invoke(createDispatchWithWSDL(),
                            WsaUtils.DUPLICATE_REPLY_TO_MESSAGE,
                            WsaUtils.S11_NS,
                            WsaUtils.W3C_WSA_NS,
                            W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS,
                            W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.INVALID_CARDINALITY);
        }
    }


    public void testDuplicateFaultToHeader() throws Exception {
        try {
            WsaUtils.invoke(createDispatchWithWSDL(),
                            WsaUtils.DUPLICATE_FAULT_TO_MESSAGE,
                            WsaUtils.S11_NS,
                            WsaUtils.W3C_WSA_NS,
                W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS,
                W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.INVALID_CARDINALITY);
        }
    }

    public void testDuplicateActionHeader() throws Exception {
        try {
            WsaUtils.invoke(createDispatchWithWSDL(),
                            WsaUtils.DUPLICATE_ACTION_MESSAGE,
                            WsaUtils.S11_NS,
                            WsaUtils.W3C_WSA_NS,getAddress(),CORRECT_ACTION,
                            CORRECT_ACTION);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.INVALID_CARDINALITY);
        }
    }

    public void testDuplicateMessageIDHeader() throws Exception {
        try {
            WsaUtils.invoke(createDispatchWithWSDL(),
                            WsaUtils.DUPLICATE_MESSAGE_ID_MESSAGE,
                            WsaUtils.S11_NS,
                            WsaUtils.W3C_WSA_NS,
                            getAddress());
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.INVALID_CARDINALITY);
        }
    }

    public void testDuplicateMessageIDHeaderOneway() throws Exception {
        WsaUtils.invokeOneWay(createDispatchWithWSDL(),
                              WsaUtils.DUPLICATE_MESSAGE_ID_MESSAGE_ONEWAY,
                              WsaUtils.S11_NS,
                              WsaUtils.W3C_WSA_NS,
                              getAddress());
    }

    public void testOnewayWithReplyTo() throws Exception {
        AddNumbersPortType stub = createStub(new OneWayFeature(true, new WSEndpointReference(getAddress(), AddressingVersion.W3C)));
        stub.addNumbers5(10, 10);
    }

    /**
     * This test tests the functionality of OnewayFeature and AddressingFeature the way it being used in WS-AT implementation
     * In WS-AT impl, Server-side has to send fault messages to predefined coordinator and this test replicates that usage.
     *
     * @throws Exception
     */
    public void testCustomFault() throws Exception {
        if(ClientServerTestUtil.useLocal()){
            System.out.println("Only Testable in HTTP transport!");
            return;
        }
        SOAPFault fault = SOAPFactory.newInstance().createFault("custom fault from client", SOAPConstants.SOAP_SENDER_FAULT);

        InputStream is = getClass().getClassLoader().getResourceAsStream("wsa/fromwsdl/custom/config/AddNumbers.wsdl");
        assertNotNull("WSDL cannot be read", is);
        ArrayList<Element> metadata = new ArrayList<Element>();
        metadata.add((Element)DOMUtil.createDOMNode(is).getFirstChild());

//        WSEndpointReference to = new WSEndpointReference(AddressingVersion.W3C,
//                getAddress(),
//                SERVICE_QNAME,
//                PORT_QNAME,
//                PORT_TYPE_QNAME,
//                metadata,
//                getWsdlAddress(),
//                null);
        OneWayFeature owf = new OneWayFeature();
        owf.setRelatesToID("uuid:foobar");


        Service service = Service.create(SERVICE_QNAME);
        service.addPort(PORT_QNAME, SOAPBinding.SOAP11HTTP_BINDING, getAddress());
//        Dispatch<Source> dispatch = service.createDispatch(to.toSpec(), Source.class,
//                                                                Service.Mode.PAYLOAD,
//                                                                new AddressingFeature(true, true),
//                                                                owf);

                Dispatch<Source> dispatch = service.createDispatch(PORT_QNAME,Source.class,
                                                                Service.Mode.PAYLOAD,
                                                                new MemberSubmissionAddressingFeature(true),
                                                                owf);
        //Since this fault is not a wsdl operation, we need to set SOAPAction for correct wsa:Action
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY,true);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,"http://example.com/myfault");

        try {
            dispatch.invokeOneWay(new DOMSource(fault));
        } catch (WebServiceException e) {
             // since the server-side is not provider based for this test.
            // it does n't know from the fault message request that it is oneway and throws 500 code.
            // so, expect a WebServcieException here.
        }
//        System.out.println(dispatch.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
    }

    /**
     * This test tests the functionality of OnewayFeature and AddressingFeature the way it being used in WS-AT implementation
     * In WS-AT impl, Server-side has to send fault messages to predefined coordinator and this test replicates that usage.
     *
     * @throws Exception
     */
    public void testCustomFault1() throws Exception {
        SOAPFault fault = SOAPFactory.newInstance().createFault("custom fault from client", SOAPConstants.SOAP_SENDER_FAULT);
        WSEndpointReference to = new WSEndpointReference(
                getAddress(), AddressingVersion.MEMBER);
        OneWayFeature owf = new OneWayFeature();
        owf.setRelatesToID("uuid:foobar");
        WSService service = WSService.create();
        service.addPort(PORT_QNAME, SOAPBinding.SOAP11HTTP_BINDING, getAddress());

        Dispatch<Source> dispatch = service.createDispatch(PORT_QNAME, to, Source.class,
                Service.Mode.PAYLOAD,
                new MemberSubmissionAddressingFeature(true),
                owf);
        //Since this fault is not a wsdl operation, we need to set SOAPAction for correct wsa:Action
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "http://example.com/myfault");

        try {
            dispatch.invokeOneWay(new DOMSource(fault));
        } catch (WebServiceException e) {
            // since the server-side is not provider based for this test.
            // it does n't know from the fault message request that it is oneway and throws 500 code.
            // so, expect a WebServcieException here.
        }
    }


    public void testIncorrectNonAnonymousURI() throws Exception {
        WsaUtils.invokeOneWay(createDispatchWithoutWSDL(),
                        WsaUtils.INVALID_NON_ANONYMOUS_URI_MESSAGE,
                        WsaUtils.S11_NS,
                        WsaUtils.W3C_WSA_NS,
                        CORRECT_ACTION,
                        getAddress());
    }

    private void assertFault(SOAPFaultException sfe, QName expected) {
        assertNotNull("SOAPFaultException is null", sfe);
        assertNotNull("SOAPFault is null", sfe.getFault());
        assertEquals(expected, sfe.getFault().getFaultCodeAsQName());
    }

    private static final String REPLY_TO_REFPS =
            "//[namespace-uri()='http://schemas.xmlsoap.org/soap/envelope/' and local-name()='Envelope']/" +
                    "[namespace-uri()='http://schemas.xmlsoap.org/soap/envelope/' and local-name()='Header']/" +
                    "[namespace-uri()='http://example.org/customer' and local-name()='CustomerKey']";
    private static final String REPLY_TO_REFPS_VALUE =
            "string(//[namespace-uri()='http://schemas.xmlsoap.org/soap/envelope/' and local-name()='Envelope']/" +
                    "[namespace-uri()='http://schemas.xmlsoap.org/soap/envelope/' and local-name()='Header']/" +
                    "[namespace-uri()='http://example.org/customer' and local-name()='CustomerKey'])";
    private static final String REPLY_TO_REFPS_ISREFP =
            "//[namespace-uri()='http://schemas.xmlsoap.org/soap/envelope/' and local-name()='Envelope']/" +
                    "[namespace-uri()='http://schemas.xmlsoap.org/soap/envelope/' and local-name()='Header']/" +
                    "[namespace-uri()='http://example.org/customer' and local-name()='CustomerKey']" +
                    "[@[namespace-uri()='" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "' and local-name()='IsReferenceParameter']]";
}
