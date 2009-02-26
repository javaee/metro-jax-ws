/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package wsa.w3c.fromwsdl.crinterop_s11.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Exchanger;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Endpoint;

import static testutil.WsaUtils.S12_NS;
import static testutil.WsaUtils.invoke12;
import static testutil.WsaUtils.invoke;
import static testutil.WsaUtils.S11_NS;
import testutil.XMLTestCase;
import static testutil.WsaW3CSOAPMessages.USER_FAULT_CODE;
import static wsa.w3c.fromwsdl.crinterop_s11.client.BindingProviderUtil.*;
import static wsa.w3c.fromwsdl.crinterop_s11.client.TestConstants.*;

/**
 * @author Rama Pulavarthi
 */
public class NonAnonymousClientTest extends XMLTestCase {
    private static Endpoint responseProcessor;
    private static Exchanger<SOAPMessage> respMsgExchanger = new Exchanger<SOAPMessage>();

    public NonAnonymousClientTest(String name) {
        super(name);
    }

    /**
     * Sets up a Endpoint for listenign to non-anonymous responses,
     * which uses the Exchanger to pass the request message.
     * This in unncessaery for anonymous tests.Ê
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();
        String address = getNonAnonymousClientAddress();
        responseProcessor = Endpoint.create(new NonAnonymousRespProcessor(respMsgExchanger));
        responseProcessor.publish(address);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        if (responseProcessor != null)
            responseProcessor.stop();
    }

    /**
     * SOAP 1.1 two-way message with a non-anonymous ReplyTo address.
     */
    public void test1150() throws Exception {
        try {
            invoke(createDispatchForNonAnonymousWithWSDLWithoutAddressing(),
                    MESSAGES.getNonAnonymousReplyToMessage(),
                    S11_NS,
                    getNonAnonymousEndpointAddress(),
                    getNonAnonymousClientAddress(),
                    ECHO_IN_ACTION,
                    "test1150");
            fail("WebServiceException must be thrown");
        } catch (WebServiceException e) {
            assertEquals("No response returned.", e.getMessage());
            //Lets see we get a response in 60 s
            SOAPMessage m = respMsgExchanger.exchange(null, TestConstants.CLIENT_MAX_TIMEOUT, TimeUnit.SECONDS);
            m.writeTo(System.out);
        }        
    }

    /**
     * SOAP 1.1 two-way message with a non-anonymous ReplyTo address and a none FaultTo.
     */
    public void xtest1152() throws Exception {
        try {
            invoke(createDispatchForNonAnonymousWithWSDLWithoutAddressing(),
                    MESSAGES.getNonAnonymousReplyToNoneFaultToMessage(),
                    S11_NS,
                    getNonAnonymousEndpointAddress(),
                    getNonAnonymousClientAddress(),
                    ECHO_IN_ACTION,
                    "fault-test1152");
            fail("WebServiceException must be thrown");
        } catch (WebServiceException e) {
            assertEquals("No response returned.", e.getMessage());
        }
    }

    /**
     * SOAP 1.1 two-way message with a non-anonymous ReplyTo and FaultTo address.
     */
    public void xtest1194() throws Exception {
        try {
            invoke(createDispatchForNonAnonymousWithWSDLWithoutAddressing(),
                    MESSAGES.getNonAnonymousReplyToMessage(),
                    S11_NS,
                    getNonAnonymousEndpointAddress(),
                    getNonAnonymousClientAddress(),
                    ECHO_IN_ACTION,
                    "fault-test1194");
            fail("1194: WebServiceException must be thrown");
        } catch (WebServiceException e) {
            assertEquals("No response returned.", e.getMessage());
            //Lets see we get a response in 60 s
            SOAPMessage m = respMsgExchanger.exchange(null, TestConstants.CLIENT_MAX_TIMEOUT, TimeUnit.SECONDS);
            //m.writeTo(System.out);
            SOAPFault f = m.getSOAPBody().getFault();
            assertNotNull(f);
            assertEquals(USER_FAULT_CODE, f.getFaultCodeAsQName());
        }
    }

}