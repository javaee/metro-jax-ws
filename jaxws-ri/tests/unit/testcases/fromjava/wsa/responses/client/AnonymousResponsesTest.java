/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package fromjava.wsa.responses.client;

import testutil.WsaUtils;
import testutil.PortAllocator;
import static testutil.W3CWsaUtils.invokeAsync;
import static testutil.W3CWsaUtils.S11_NS;
import static testutil.W3CWsaUtils.invoke;

import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.*;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;

import com.sun.xml.ws.addressing.W3CAddressingConstants;


/**
 * @author Rama Pulavarthi
 */
public class AnonymousResponsesTest extends TestCase {

    private static QName PORT = new QName("http://server.responses.wsa.fromjava/", "AnonymousResponsesEndpoint");
    private static final String action = "http://server.responses.wsa.fromjava/AnonymousResponsesEndpoint/addNumbersRequest";
    private static final String endpointAddress = System.getProperty("anonymousResponsesEndpointAddress");
    private static String nonAnonAddress;

    public static final Dispatch<SOAPMessage> createDispatchWithoutAddressing() {
        return new AnonymousResponsesEndpointService().createDispatch(PORT, SOAPMessage.class, Service.Mode.MESSAGE, new AddressingFeature(false));
    }

    public static String getNonAnonymousClientAddress() {
        return "http://localhost:" + PortAllocator.getFreePort() + "/AnonymousResponsesTest/nonanoymous";
    }


    /**
     * SEI based invocation
     *
     * @throws Exception
     */
    public void testAnonymousReplyTo1() throws Exception {
        AnonymousResponsesEndpoint client = new AnonymousResponsesEndpointService().getAnonymousResponsesEndpoint();
        assertEquals(20, client.addNumbers(10, 10));
    }

    /**
     * Using Dispatch
     */
    public void testAnonymousReplyTo2() throws Exception {
        WsaUtils.invoke(createDispatchWithoutAddressing(),
                TestMessages.ANONYMOUS_REPLY_TO_COMPLETE_MESSAGE,
                WsaUtils.S11_NS,
                action,
                endpointAddress,
                "testAnonymousReplyTo");

    }

    public void testNonAnonymousReplyTo() throws Exception {
        try {
            invoke(createDispatchWithoutAddressing(),
                    TestMessages.NON_ANONYMOUS_REPLY_TO_COMPLETE_MESSAGE,
                    S11_NS,
                    nonAnonAddress,
                    action,
                    endpointAddress,
                    "testNonAnonymousReplyTo");
            fail("MUST throw SOAPFaultException with ONLY_ANONYMOUS_ADDRESS_SUPPORTED fault code");
        } catch (SOAPFaultException e) {
            assertNotNull(e.getFault());
            assertEquals(W3CAddressingConstants.ONLY_ANONYMOUS_ADDRESS_SUPPORTED, e.getFault().getFaultCodeAsQName());
        }
    }

    /**
     * Normal response case,just tests if the endpoint accepts non-anon FaultTo
     *
     * @throws Exception
     */
    public void testNonAnonymousFaultTo1() throws Exception {
        try {
            SOAPMessage response = invoke(createDispatchWithoutAddressing(),
                    TestMessages.NON_ANONYMOUS_FAULT_TO_COMPLETE_MESSAGE,
                    S11_NS,
                    nonAnonAddress,
                    action,
                    endpointAddress,
                    "testNonAnonymousReplyTo");
            fail("MUST throw SOAPFaultException with ONLY_ANONYMOUS_ADDRESS_SUPPORTED fault code");
        } catch (SOAPFaultException e) {
            assertNotNull(e.getFault());
            assertEquals(W3CAddressingConstants.ONLY_ANONYMOUS_ADDRESS_SUPPORTED, e.getFault().getFaultCodeAsQName());
        }


    }

    /**
     * Fault response case
     *
     * @throws Exception
     */
    public void testNonAnonymousFaultTo2() throws Exception {
        try {
            invoke(createDispatchWithoutAddressing(),
                    TestMessages.NON_ANONYMOUS_FAULT_TO_COMPLETE_FAULTY_MESSAGE,
                    S11_NS,
                    nonAnonAddress,
                    action,
                    endpointAddress,
                    "testNonAnonymousReplyTo");
            fail("MUST throw SOAPFaultException with ONLY_ANONYMOUS_ADDRESS_SUPPORTED fault code");
        } catch (SOAPFaultException e) {
            assertNotNull(e.getFault());
            assertEquals(W3CAddressingConstants.ONLY_ANONYMOUS_ADDRESS_SUPPORTED, e.getFault().getFaultCodeAsQName());
        }

    }

}
