/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
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

package wsa.fromwsdl_api.binding.wsa_required_true.client;

import com.sun.xml.ws.addressing.W3CAddressingConstants;
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.binding.BindingImpl;
import testutil.ClientServerTestUtil;
import testutil.WsaUtils;
import testutil.XMLTestCase;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.soap.AddressingFeature;
import java.io.ByteArrayOutputStream;


/**
 * @author Arun Gupta
 */
public class AddNumbersClient extends XMLTestCase {
    private static final QName SERVICE_QNAME = new QName("http://example.com/", "AddNumbersService");
    private static final QName PORT_QNAME = new QName("http://example.com/", "AddNumbersPort");
    private static final String ENDPOINT_ADDRESS = "http://localhost:8080/jaxrpc-wsa_fromwsdl_api_binding_wsa_required_true/hello";
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

    private Dispatch<SOAPMessage> createDispatchWithoutWSDL() throws Exception {
        Service service = Service.create(SERVICE_QNAME);
        service.addPort(PORT_QNAME, SOAPBinding.SOAP11HTTP_BINDING, getAddress());

//        MemberSubmissionAddressingFeature msAddressingFeature = new MemberSubmissionAddressingFeature(true);
//        WebServiceFeature[] features = new WebServiceFeature[] {msAddressingFeature};
WebServiceFeature[] features = null;

        Dispatch<SOAPMessage> dispatch = service.createDispatch(PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE, features);
        return dispatch;
    }

    private Dispatch<SOAPMessage> createDispatchWithWSDL() throws Exception {

        MemberSubmissionAddressingFeature msAddressingFeature = new MemberSubmissionAddressingFeature(true);
        RespectBindingFeature respectBindingFeature = new RespectBindingFeature(true);
        WebServiceFeature[] features = new WebServiceFeature[] {msAddressingFeature, respectBindingFeature};
        AddNumbersService service = new AddNumbersService();

        return service.createDispatch(PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE );
    }

    private Dispatch<SOAPMessage> createDispatchWithWSDL(WebServiceFeature[] wsFeatures) throws Exception {
        AddNumbersService service = new AddNumbersService();
        return service.createDispatch(PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE, wsFeatures);
    }

    public void testBadAction() throws Exception {
        try {
            WsaUtils.invoke(createDispatchWithoutWSDL(), WsaUtils.BAD_ACTION_MESSAGE, WsaUtils.S11_NS, WsaUtils.W3C_WSA_NS, getAddress(), W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.ACTION_NOT_SUPPORTED_QNAME);
        }
    }

    public void testMissingAction() throws Exception {
        try {
            WsaUtils.invoke(createDispatchWithoutWSDL(), WsaUtils.MISSING_ACTION_MESSAGE, WsaUtils.S11_NS, WsaUtils.W3C_WSA_NS, getAddress(), W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.MAP_REQUIRED_QNAME);
        }
    }


    public void testFaultToRefps() throws Exception {
        try {
            WsaUtils.invoke(createDispatchWithoutWSDL(), WsaUtils.FAULT_TO_REFPS_MESSAGE, WsaUtils.S11_NS, WsaUtils.W3C_WSA_NS, getAddress(), W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS, W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS, CORRECT_ACTION);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertTrue("Got SOAPFaultException", true);
        }
    }

    public void testBadActionWithWSDL() throws Exception {
        try {
            RespectBindingFeature respectBindingFeature = new RespectBindingFeature(false);
            WebServiceFeature[] features = new WebServiceFeature[]{respectBindingFeature};
            Dispatch dispatch = createDispatchWithWSDL(features);
            BindingImpl binding = (BindingImpl)dispatch.getBinding();
            assertTrue(AddressingVersion.isEnabled(binding));

            WsaUtils.invoke(dispatch, WsaUtils.BAD_ACTION_MESSAGE, WsaUtils.S11_NS, WsaUtils.W3C_WSA_NS, getAddress(), W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.ACTION_NOT_SUPPORTED_QNAME);
        }
    }


    public void testMissingActionWithWSDL() throws Exception {
        try {

            WebServiceFeature[] features = new WebServiceFeature[]{new AddressingFeature(false)};
            Dispatch dispatch = createDispatchWithWSDL(features);
            BindingImpl binding = (BindingImpl)dispatch.getBinding();
            AddressingVersion addressingVersion = AddressingVersion.fromBinding(binding);
            System.out.println("Addressing version is " + addressingVersion);
            assertFalse(AddressingVersion.isEnabled(binding));

            WsaUtils.invoke(dispatch, WsaUtils.MISSING_ACTION_MESSAGE, WsaUtils.S11_NS, WsaUtils.W3C_WSA_NS, getAddress(), W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.MAP_REQUIRED_QNAME);
        }
    }

    public void testReplyToRefpsWithWSDL() throws Exception {
        MemberSubmissionAddressingFeature addressingFeature = new MemberSubmissionAddressingFeature(false);

        WebServiceFeature[] features = new WebServiceFeature[]{addressingFeature};
        Dispatch dispatch = createDispatchWithWSDL(features);
        BindingImpl binding = (BindingImpl)dispatch.getBinding();
        boolean enabled = AddressingVersion.isEnabled(binding);
        assertTrue(enabled);

        SOAPMessage response = WsaUtils.invoke(dispatch, WsaUtils.REPLY_TO_REFPS_MESSAGE, WsaUtils.S11_NS, WsaUtils.W3C_WSA_NS, getAddress(), W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS, CORRECT_ACTION);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeTo(baos);
        assertXpathExists(REPLY_TO_REFPS, baos.toString());
        assertXpathEvaluatesTo(REPLY_TO_REFPS_VALUE, baos.toString(),"Key#123456789");
        assertXpathExists(REPLY_TO_REFPS_ISREFP, baos.toString());
    }

    //bug RespectBindingFeature will turn off enabled features in the wsdl
    //but not features that have been explicitly set by the client
    public void testFaultToRefpsWithWSDL() throws Exception {
        try {
            MemberSubmissionAddressingFeature addressingFeature = new MemberSubmissionAddressingFeature(true);
            RespectBindingFeature bindingFeature = new RespectBindingFeature(false);

            WebServiceFeature[] features = new WebServiceFeature[]{addressingFeature, bindingFeature};

            Dispatch dispatch = createDispatchWithWSDL(features);
            BindingImpl binding = (BindingImpl)dispatch.getBinding();
            boolean enabled = AddressingVersion.isEnabled(binding);
            if (enabled){
                System.out.println("Addressing is Enabled");
            } else {
                System.out.println("Addressing is disabled");
            }

            assertTrue (enabled == true);

            WsaUtils.invoke(dispatch, WsaUtils.FAULT_TO_REFPS_MESSAGE, WsaUtils.S11_NS, WsaUtils.W3C_WSA_NS, getAddress(), W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS, W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS, CORRECT_ACTION);
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException sfe) {
            assertTrue("Got SOAPFaultException", true);
        }
    }



    private void assertFault(SOAPFaultException sfe, QName expected) {
        assertNotNull("SOAPFaultException is null", sfe);
        assertNotNull("SOAPFault is null", sfe.getFault());
        assertEquals(expected, sfe.getFault().getFaultCodeAsQName());
    }

    private static final String REPLY_TO_REFPS =
            "//*[namespace-uri()='http://schemas.xmlsoap.org/soap/envelope/' and local-name()='Envelope']/*" +
                    "[namespace-uri()='http://schemas.xmlsoap.org/soap/envelope/' and local-name()='Header']/*" +
                    "[namespace-uri()='http://example.org/customer' and local-name()='CustomerKey']";
    private static final String REPLY_TO_REFPS_VALUE =
            "string(//*[namespace-uri()='http://schemas.xmlsoap.org/soap/envelope/' and local-name()='Envelope']/*" +
                                "[namespace-uri()='http://schemas.xmlsoap.org/soap/envelope/' and local-name()='Header']/*" +
                                "[namespace-uri()='http://example.org/customer' and local-name()='CustomerKey']/text())";
    private static final String REPLY_TO_REFPS_ISREFP =
            "//*[namespace-uri()='http://schemas.xmlsoap.org/soap/envelope/' and local-name()='Envelope']/*" +
                    "[namespace-uri()='http://schemas.xmlsoap.org/soap/envelope/' and local-name()='Header']/*" +
                    "[namespace-uri()='http://example.org/customer' and local-name()='CustomerKey']" +
                    "[@*[namespace-uri()='" + W3CAddressingConstants.WSA_NAMESPACE_NAME + "' and local-name()='IsReferenceParameter']]";
}                    
