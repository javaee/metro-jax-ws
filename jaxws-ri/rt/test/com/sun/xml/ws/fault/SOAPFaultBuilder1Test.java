/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.fault;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.message.saaj.SAAJMessage;
import junit.framework.TestCase;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayInputStream;
import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class SOAPFaultBuilder1Test extends TestCase {
    private final String fault1 =
        "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>" +
        "<S:Body>" +
        "<ns2:Fault xmlns:ns2='http://schemas.xmlsoap.org/soap/envelope/' xmlns:ns3='http://www.w3.org/2003/05/soap-envelope'>" +
        "<faultcode>ns2:Server</faultcode>" +
        "<faultstring>com.sun.xml.ws.addressing.model.ActionNotSupportedException</faultstring>" +
        "</ns2:Fault>" +
        "</S:Body>" +
        "</S:Envelope>";
    private final String fault2 =
        "<env:Envelope xmlns:env='http://www.w3.org/2003/05/soap-envelope' xmlns:wsa='http://schemas.xmlsoap.org/ws/2004/08/addressing' xmlns:wsman='http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd'>" +
        "<env:Body>" +
        "<env:Fault xmlns:ns8='http://test.foo'>" +
        "<env:Code>" +
        "<env:Value>env:Receiver</env:Value>" +
        "<env:Subcode><env:Value>wsa:EndpointUnavailable</env:Value></env:Subcode>" +
        "</env:Code>" +
        "<env:Reason><env:Text xml:lang='en-US'>The specified endpoint is currently unavailable.</env:Text></env:Reason>" +
        "<env:Detail>" +
        "<wsman:FaultDetail>http://schemas.dmtf.org/wbem/wsman/1/wsman/faultDetail/InvalidValues</wsman:FaultDetail>" +
        "<wsman:FaultDetail2>http://schemas.dmtf.org/wbem/wsman/1/wsman/faultDetail/InvalidValues1</wsman:FaultDetail2>" +
        "</env:Detail>" +
        "</env:Fault>" +
        "</env:Body>" +
        "</env:Envelope>";

    public SOAPFaultBuilder1Test(String name) {
        super(name);
    }

    public void testFault1() throws Throwable {
        SOAPMessage msg = SOAPVersion.SOAP_11.getMessageFactory().createMessage(null, new ByteArrayInputStream(fault1.getBytes()));
        SOAPFaultBuilder sfb = SOAPFaultBuilder.create(new SAAJMessage(msg));
        Throwable t = sfb.createException(null);
        assertTrue(t instanceof SOAPFaultException);
        assertTrue(t.getMessage().contains("Client received SOAP Fault from server:"));
    }

    public void testFault2() throws Throwable {
        SOAPMessage msg = SOAPVersion.SOAP_12.getMessageFactory().createMessage(null, new ByteArrayInputStream(fault2.trim().getBytes()));
        SOAPFaultBuilder sfb = SOAPFaultBuilder.create(new SAAJMessage(msg));
        Throwable t = sfb.createException(null);
        assertTrue(t instanceof SOAPFaultException);
        SOAPFaultException sfe = (SOAPFaultException) t;
        Detail detail = sfe.getFault().getDetail();
        assertTrue(detail != null);
        Iterator iter = detail.getDetailEntries();

        //there should be two detail entries
        assertTrue(iter.hasNext());
        DetailEntry de = (DetailEntry) iter.next();
        assertTrue(de.getElementQName().equals(new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "FaultDetail")));
        Node n = de.getFirstChild();
        assertTrue(n.getNodeValue().equals("http://schemas.dmtf.org/wbem/wsman/1/wsman/faultDetail/InvalidValues"));

        assertTrue(iter.hasNext());
        de = (DetailEntry) iter.next();
        assertTrue(de.getElementQName().equals(new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "FaultDetail2")));
        n = de.getFirstChild();        
        assertTrue(n.getNodeValue().equals("http://schemas.dmtf.org/wbem/wsman/1/wsman/faultDetail/InvalidValues1"));
    }

    public void testFault3() throws Exception {
        QName sc1 = new QName("http://example.org/1", "one");
        QName sc2 = new QName("http://example.org/2", "two");
        QName sc3 = new QName("http://example.org/3", "three");
        String faultString = "INTERNAL_ERROR";
        QName detailMsg = new QName("DetailMessage");
        String detailValue = "This method is not implemented";
        SOAPFactory fac = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPFault fault = fac.createFault(faultString, SOAPConstants.SOAP_RECEIVER_FAULT);
        fault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
        fault.appendFaultSubcode(sc1);
        fault.appendFaultSubcode(sc2);
        fault.appendFaultSubcode(sc3);
        fault.addDetail().addDetailEntry(detailMsg).setTextContent(detailValue);
        Message fm = SOAPFaultBuilder.createSOAPFaultMessage(SOAPVersion.SOAP_12, fault);

        //get the SOAPFault back
        SOAPFaultBuilder sfb = SOAPFaultBuilder.create(fm);
        Throwable ex = sfb.createException(null);
        assertTrue(ex instanceof SOAPFaultException);
        SOAPFaultException sfe = (SOAPFaultException) ex;
        SOAPFault sf = sfe.getFault();
        assertTrue(sf.getFaultString().equals(faultString));

        //compare detail
        Detail detail = sfe.getFault().getDetail();
        assertTrue(detail != null);
        Iterator iter = detail.getDetailEntries();
        assertTrue(iter.hasNext());
        DetailEntry n = (DetailEntry) iter.next();
        assertTrue(n.getNamespaceURI().equals(detailMsg.getNamespaceURI())&&
        n.getLocalName().equals(detailMsg.getLocalPart()));
        assertEquals(n.getTextContent(), detailValue);

        //compare code and subcodes
        Iterator scs = sf.getFaultSubcodes();
        assertTrue(scs.hasNext());
        QName sc = (QName) scs.next();
        assertTrue(sc.equals(sc1));

        assertTrue(scs.hasNext());
        sc = (QName) scs.next();
        assertTrue(sc.equals(sc2));

        assertTrue(scs.hasNext());
        sc = (QName) scs.next();
        assertTrue(sc.equals(sc3));
    }

}
