/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;


/**
 * @author Rama Pulavarthi
 */
public class AllResponsesTest extends TestCase {
    private static Endpoint responseProcessor;
    private static Exchanger<SOAPMessage> respMsgExchanger = new Exchanger<SOAPMessage>();

    private static QName PORT = new QName("http://server.responses.wsa.fromjava/", "AllResponsesEndpoint");
    private static final String action = "http://server.responses.wsa.fromjava/AllResponsesEndpoint/addNumbersRequest";
    private static final String endpointAddress = System.getProperty("allResponsesEndpointAddress");
    private static String nonAnonAddress;
    public static final Dispatch<SOAPMessage> createDispatchWithoutAddressing() {
        return new AllResponsesEndpointService().createDispatch(PORT, SOAPMessage.class, Service.Mode.MESSAGE, new AddressingFeature(false));
    }

    public static String getNonAnonymousClientAddress() {
        return "http://localhost:"+ PortAllocator.getFreePort()+ "/AllResponsesTest/nonanoymous";
    }

    /**
     * Sets up a Endpoint for listenign to non-anonymous responses,
     * which uses the Exchanger to pass the request message.
     * This in unnecessary for anonymous tests.
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();
        nonAnonAddress = getNonAnonymousClientAddress();
        responseProcessor = Endpoint.create(new NonAnonymousRespProcessor(respMsgExchanger));
        responseProcessor.publish(nonAnonAddress);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        // Sleep to give endpoint some time to process the non-anonymous request
        // this is needed in case the object is exchanged between client and non-anonymous response endpoint,
        // which leads to test tearDown(), yet the ack to non-anonymous response is not sent back to server yet.
        Thread.sleep(5000);
        if (responseProcessor != null)
            responseProcessor.stop();
    }

    /**
     * SEI based invocation
     * @throws Exception
     */
    public void testAnonymousReplyTo1() throws Exception {
        AllResponsesEndpoint client =new AllResponsesEndpointService().getAllResponsesEndpoint();
        assertEquals(20, client.addNumbers(10,10));
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
        invokeAsync(createDispatchWithoutAddressing(),
                TestMessages.NON_ANONYMOUS_REPLY_TO_COMPLETE_MESSAGE,
                S11_NS,
                nonAnonAddress,
                action,
                endpointAddress,
                "testNonAnonymousReplyTo");

        //Lets see we get a response in 60 s
        SOAPMessage m = respMsgExchanger.exchange(null, TestMessages.CLIENT_MAX_TIMEOUT, TimeUnit.SECONDS);
        // System.out.println("****************************");
        // m.writeTo(System.out);
        // System.out.println("\n****************************");
        SOAPBody sb = m.getSOAPBody();
        Iterator itr = sb.getChildElements(new QName("http://server.responses.wsa.fromjava/", "addNumbersResponse"));
        assertTrue(itr.hasNext());

    }

    /**
     * Normal response case,just tests if the endpoint accepts non-anon FaultTo
     * @throws Exception
     */
    public void testNonAnonymousFaultTo1() throws Exception {
        SOAPMessage response = invoke(createDispatchWithoutAddressing(),
                TestMessages.NON_ANONYMOUS_FAULT_TO_COMPLETE_MESSAGE,
                S11_NS,
                nonAnonAddress,
                action,
                endpointAddress,
                "testNonAnonymousReplyTo");
        SOAPBody sb = response.getSOAPBody();
        Iterator itr = sb.getChildElements(new QName("http://server.responses.wsa.fromjava/", "addNumbersResponse"));
        assertTrue(itr.hasNext());
    }

    /**
     * Fault response case
     * @throws Exception
     */
    public void testNonAnonymousFaultTo2() throws Exception {
        invokeAsync(createDispatchWithoutAddressing(),
                TestMessages.NON_ANONYMOUS_FAULT_TO_COMPLETE_FAULTY_MESSAGE,
                S11_NS,
                nonAnonAddress,
                action,
                endpointAddress,
                "testNonAnonymousReplyTo");
        //Lets see we get a response in 60 s
        SOAPMessage m = respMsgExchanger.exchange(null, TestMessages.CLIENT_MAX_TIMEOUT, TimeUnit.SECONDS);
        // System.out.println("****************************");
        // m.writeTo(System.out);
        // System.out.println("\n****************************");
        SOAPBody sb = m.getSOAPBody();
        assertTrue(sb.hasFault());
        SOAPFault fault = sb.getFault();
        assertEquals(fault.getFaultString(),"Negative numbers can't be added!");

    }

}
