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

package wsa.submission.fromwsdl.crinterop.s12.client;

import testutil.MemberSubmissionAddressingConstants;
import static testutil.WsaSOAPMessages.USER_FAULT_CODE;
import static testutil.WsaUtils.*;
import testutil.XMLTestCase;
import static wsa.submission.fromwsdl.crinterop.s12.client.BindingProviderUtil.*;
import static wsa.submission.fromwsdl.crinterop.s12.common.TestConstants.*;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayOutputStream;

/**
 * @author Arun Gupta
 */
public class EchoClient extends XMLTestCase {

    public EchoClient(String name) {
        super(name);
    }

    /**
     * SOAP 1.2 two-way message.
     */
    public void test1230() throws Exception {
        String result = createStub().echo("test1230");
        assertEquals("test1230", result);
    }

    /**
     * SOAP 1.2 two-way message with ReplyTo address of anonymous.
     */
    public void test1231() throws Exception {
        String result = createStub().echo("test1231");
        assertEquals("test1231", result);
    }

    /**
     * SOAP 1.2 two-way message with ReplyTo address containing Reference Parameters.
     */
    public void test1232() throws Exception {
        SOAPMessage response = invoke12(createDispatchWithWSDLWithoutAddressing(),
                                        MESSAGES.getReplyToRefpsEchoMessage(),
                                        S12_NS,
                                        getAddress(),
                                        ECHO_IN_ACTION,
                                        "test1232");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeTo(baos);
        assertXpathExists(ENVELOPE_HEADER, baos.toString());
        assertXpathExists(ACTION_HEADER, baos.toString());
        assertXpathEvaluatesTo(ACTION_HEADER, baos.toString(), ECHO_OUT_ACTION);
    }

    /**
     * SOAP 1.2 two-way message with fault. ReplyTo and FaultTo addresses containing Reference Parameters.
     */
    public void test1233() throws Exception {
        try {
            invoke12(createDispatchWithWSDLWithoutAddressing(),
                     MESSAGES.getReplyToFaultToRefpsEchoMessage(),
                     S12_NS,
                     getAddress(),
                     ECHO_IN_ACTION,
                     "fault-test1233");
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException e) {
            checkUserFaultCode(e);
        }
    }

    /**
     * SOAP 1.2 two-way message with fault. FaultTo is defaulted, ReplyTo address contains Reference Parameters.
     */
    public void test1234() throws Exception {
        try {
            invoke12(createDispatchWithWSDLWithoutAddressing(),
                     MESSAGES.getReplyToRefpsEchoMessage(),
                     S12_NS,
                     getAddress(),
                     ECHO_IN_ACTION,
                     "fault-test1234");
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException e) {
            checkUserFaultCode(e);
        }
    }

    /**
     * SOAP 1.2 two-way message with fault. FaultTo is anonymous, ReplyTo is non-anonymous.
     */
    public void test1235() throws Exception {
        try {
            invoke12(createDispatchWithWSDLWithoutAddressing(),
                     MESSAGES.getNonAnonymousReplyToAnonymousFaultToMessage(),
                     S12_NS,
                     getAddress(),
                     getNonAnonymousAddress(),
                     ECHO_IN_ACTION,
                     "fault-test1235");
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException e) {
            checkUserFaultCode(e);
        }
    }

    /**
     * SOAP 1.2 two-way message with a duplicate To header.
     */
    public void test1240() throws Exception {
        try {
            invoke12(createDispatchWithWSDLWithAddressing(),
                     MESSAGES.getDuplicateToMessage(),
                     S12_NS,
                     getAddress(),
                     getAddress(),
                     "test1240");
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException e) {
            assertNotNull(e.getFault());
            assertInvalidCardinalityCode12(e.getFault(), MemberSubmissionAddressingConstants.WSA_NAMESPACE_NAME);
        }
    }


    /**
     * SOAP 1.2 two-way message with a duplicate Reply-To header.
     */
    public void test1241() throws Exception {
        try {
            invoke12(createDispatchWithWSDLWithAddressing(),
                     MESSAGES.getDuplicateReplyToMessage(),
                     S12_NS,
                     "test1241");
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException e) {
            assertNotNull(e.getFault());
            assertInvalidCardinalityCode12(e.getFault(), MemberSubmissionAddressingConstants.WSA_NAMESPACE_NAME);
        }
    }

    /**
     * SOAP 1.2 two-way message with a duplicate Fault-To header.
     */
    public void test1242() throws Exception {
        try {
            invoke12(createDispatchWithWSDLWithAddressing(),
                     MESSAGES.getDuplicateFaultToMessage(),
                     S12_NS,
                     "test1242");
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException e) {
            assertNotNull(e.getFault());
            assertInvalidCardinalityCode12(e.getFault(), MemberSubmissionAddressingConstants.WSA_NAMESPACE_NAME);
        }
    }

    /**
     * SOAP 1.2 two-way message with a duplicate action header.
     */
    public void test1243() throws Exception {
        try {
            invoke12(createDispatchWithWSDLWithAddressing(),
                     MESSAGES.getDuplicateActionMessage(),
                     S12_NS,getAddress(),ECHO_IN_ACTION,
                     ECHO_IN_ACTION,
                     "test1243");
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException e) {
            assertNotNull(e.getFault());
            assertInvalidCardinalityCode12(e.getFault(), MemberSubmissionAddressingConstants.WSA_NAMESPACE_NAME);
        }
    }

    /**
     * SOAP 1.2 two-way message with a duplicate MessageID header.
     */
    public void test1244() throws Exception {
        try {
            invoke12(createDispatchWithWSDLWithAddressing(),
                     MESSAGES.getDuplicateMessageIDMessage(),
                     S12_NS,
                     "test1244");
            fail("SOAPFaultException must be thrown");
        } catch (SOAPFaultException e) {
            assertNotNull(e.getFault());
            assertInvalidCardinalityCode12(e.getFault(), MemberSubmissionAddressingConstants.WSA_NAMESPACE_NAME);
        }
    }

    /**
     * SOAP 1.2 two-way message with no Action header.
     */
//    public void test1248() throws Exception {
//        try {
//            invoke12(createDispatchWithWSDLWithoutAddressing(),
//                   MESSAGES.getNoActionEchoMessage(),
//                   S12_NS,
//                   getAddress(),
//                   "test1248");
//            fail("SOAPFaultException must be thrown");
//        } catch (SOAPFaultException e) {
//            assertNotNull(e.getFault());
//            SOAPFault f = e.getFault();
//            assertHeaderRequiredFaultCode(f.getFaultCodeAsQName());
//        }
//    }

    /**
     * SOAP 1.2 two-way message with a non-anonymous ReplyTo address.
     */
    public void test1250() throws Exception {
        try {
            invoke12(createDispatchWithWSDLWithoutAddressing(),
                     MESSAGES.getNonAnonymousReplyToMessage(),
                     S12_NS,
                     getAddress(),
                     getNonAnonymousAddress(),
                     ECHO_IN_ACTION,
                     "test1250");
            fail("WebServiceException must be thrown");
        } catch (WebServiceException e) {
            assertEquals("No response returned.", e.getMessage());
        }
    }

    /**
     * SOAP 1.2 two-way message with a non-anonymous ReplyTo address and a Reply targeted to none.
     */
    public void test1251() throws Exception {
        try {
            invoke12(createDispatchWithWSDLWithoutAddressing(),
                     MESSAGES.getNoneTargetedNonAnonymousReplyToMessage(),
                     S12_NS,
                     getAddress(),
                     getNonAnonymousAddress(),
                     getNonAnonymousAddress(),
                     ECHO_IN_ACTION,
                     "test1251");
            fail("WebServiceException must be thrown");
        } catch (WebServiceException e) {
            assertEquals("No response returned.", e.getMessage());
        }
    }

    /**
     * SOAP 1.2 two-way message with a non-anonymous ReplyTo address and a none FaultTo.
     */
    public void test1252() throws Exception {
        try {
            invoke12(createDispatchWithWSDLWithoutAddressing(),
                     MESSAGES.getNonAnonymousReplyToNoneFaultToMessage(),
                     S12_NS,
                     getAddress(),
                     getNonAnonymousAddress(),
                     ECHO_IN_ACTION,
                     "fault-test1252");
            fail("WebServiceException must be thrown");
        } catch (WebServiceException e) {
            assertEquals("No response returned.", e.getMessage());
        }
    }

    /**
     * SOAP 1.2 two-way message with wsa:From.
     */
    public void test1270() throws Exception {
        SOAPMessage response = invoke12(createDispatchWithWSDLWithoutAddressing(),
                                        MESSAGES.getFromMustUnderstandEchoMessage(),
                                        S12_NS,
                                        getAddress(),
                                        ECHO_IN_ACTION,
                                        "test1270");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeTo(baos);
        assertXpathExists(ENVELOPE_HEADER, baos.toString());
        assertXpathExists(ACTION_HEADER, baos.toString());
        assertXpathEvaluatesTo(ACTION_HEADER, baos.toString(), ECHO_OUT_ACTION);
    }

    private void checkUserFaultCode(SOAPFaultException e) {
        assertNotNull(e.getFault());
        assertNotNull(e.getFault().getFaultSubcodes());
        assertTrue("No fault subcodes are found", e.getFault().getFaultSubcodes().hasNext());
        assertEquals(USER_FAULT_CODE, e.getFault().getFaultSubcodes().next());
    }

    private static final String ENVELOPE_HEADER = "//S12:Envelope";
    private static final String ACTION_HEADER = "//S12:Envelope/S12:Header/wsa04:Action";
}
