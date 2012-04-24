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
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.api.message;

import java.io.ByteArrayInputStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.message.saaj.SAAJMessage;

import junit.framework.TestCase;

public class HeaderListTest extends TestCase {

    public static final String TEST_NS = "http://jaxws.dev.java.net/";
    private HeaderList testInstance;

    public HeaderListTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        testInstance = new HeaderList(SOAPVersion.SOAP_11);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        testInstance = null;
    }

    public void testRemoveHeader() throws Exception {

        for (int i = 0; i < 40; i++) {
            testInstance.add(Headers.create(new QName(TEST_NS, "" + i), "" + i));
        }

        testInstance.understood(1);
        testInstance.understood(testInstance.size() - 2);

        int expectedSize = testInstance.size();
        for (int i = 2; i < testInstance.size() - 2; i++) {
            testInstance.remove(new QName(TEST_NS, "" + i));


            assertEquals(--expectedSize, testInstance.size());
            assertFalse(testInstance.isUnderstood(0));
            assertTrue(testInstance.isUnderstood(1));
            for (int j = 2; j < testInstance.size() - 2; j++) {
                assertFalse(testInstance.isUnderstood(j));
            }
            assertTrue(testInstance.isUnderstood(testInstance.size() - 2));
            assertFalse(testInstance.isUnderstood(testInstance.size() - 1));
        }
    }
    
    public void testUnderstoodBehavior() throws Exception {
      //a fairly complex SOAPMessage with 2 mustUnderstand=true headers, 
        //one mustUnderstand=false and one with no mustUnderstand specified
        String soapMsgStr = "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        "<env:Header>" +
        "<wsa:Action xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" env:mustUnderstand=\"true\"></wsa:Action>" +
        "<wsa:MessageID xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">uuid:40a19d86-071d-4d3f-8b1b-8c8b5245b1de</wsa:MessageID>" +
        "<wsa:RelatesTo xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" env:mustUnderstand=\"false\">uuid:bd2cf21b-d2ad-4dc6-b0ec-2928736b5ae2</wsa:RelatesTo>" +
        "<wsa:To xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" env:mustUnderstand=\"true\">http://www.w3.org/2005/08/addressing/anonymous</wsa:To>" +
        "</env:Header>" +
        "<env:Body xmlns:wsrm11=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\">" +
        "<wsrm11:CreateSequenceResponse>" +
        "<wsrm11:Identifier>35599b13-3672-462a-a51a-31e1820ef236</wsrm11:Identifier>" +
        "<wsrm11:Expires>P1D</wsrm11:Expires>" +
        "<wsrm11:IncompleteSequenceBehavior>NoDiscard</wsrm11:IncompleteSequenceBehavior>" +
        "</wsrm11:CreateSequenceResponse>" +
        "</env:Body></env:Envelope>";
        
        SAAJMessage msg = new SAAJMessage(makeSOAPMessage(soapMsgStr));
        HeaderList hdrs = (HeaderList) msg.getMessageHeaders();
        String addrNs = "http://www.w3.org/2005/08/addressing";
        String msgId = "MessageID";
        String action = "Action";
        String relatesTo = "RelatesTo";
        String toHdrName = "To";
        QName msgIdQName = new QName(addrNs, msgId);
        QName actionQName = new QName(addrNs, action);
        QName relatesToQName = new QName(addrNs, relatesTo);
        QName toQName = new QName(addrNs, toHdrName);
        
        //check understood headers
        Set<QName> understood = hdrs.getUnderstoodHeaders();
        assertTrue(understood == null || understood.size() == 0);
        
        //check getNotUnderstoodHeaders
        Set<QName> notUnderstood = hdrs.getNotUnderstoodHeaders(null, null, null);
        assertNotNull(notUnderstood);
        assertEquals(2, notUnderstood.size());
        //notUnderstoodHeaders should not contain non-mustUnderstand headers
        assertFalse(notUnderstood.contains(msgIdQName));
        assertFalse(notUnderstood.contains(relatesToQName));
        //notUnderstoodHeaders MUST contain mustUnderstand headers
        assertTrue(notUnderstood.contains(actionQName));
        assertTrue(notUnderstood.contains(toQName));
        
        //messageid is _not_ a mustUnderstandHeader - isUnderstood should still be false
        assertFalse(hdrs.isUnderstood(addrNs, msgId));
        
        hdrs.understood(addrNs, msgId);
        //after marking it must be listed as understood
        assertTrue(hdrs.isUnderstood(addrNs, msgId));
        //notUnderstood must continue to not contain it
        notUnderstood = hdrs.getNotUnderstoodHeaders(null, null, null);
        assertNotNull(notUnderstood);
        assertEquals(2, notUnderstood.size());
        assertFalse(notUnderstood.contains(msgIdQName));
        
        //check understood headers - must contain message id
        understood = hdrs.getUnderstoodHeaders();
        assertNotNull(understood);
        assertTrue(understood.size() == 1);
        assertTrue(understood.contains(msgIdQName));
        
        //check the mustUnderstand=true header Action
        assertFalse(hdrs.isUnderstood(addrNs, action));
        hdrs.understood(addrNs, action);
        assertTrue(hdrs.isUnderstood(addrNs, action));
        notUnderstood = hdrs.getNotUnderstoodHeaders(null, null, null);
        //Action must no longer be in notUnderstood
        assertNotNull(notUnderstood);
        assertEquals(1, notUnderstood.size());
        assertFalse(notUnderstood.contains(actionQName));
        
        understood = hdrs.getUnderstoodHeaders();
        assertNotNull(understood);
        assertEquals(2, understood.size());
        assertTrue(understood.contains(actionQName));
        assertTrue(understood.contains(msgIdQName));
    }
    private SOAPMessage makeSOAPMessage(String msg) throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        Source src = new StreamSource(new ByteArrayInputStream(msg.getBytes()));
        message.getSOAPPart().setContent(src);
        return message;
//        return new SAAJMessage(message);
    }
}
