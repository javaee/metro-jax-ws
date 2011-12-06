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
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import junit.framework.TestCase;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.StringWriter;


/**
 * @author Jitendra Kotamraju
 */
public class SOAPFaultBuilderTest extends TestCase {

    private static final QName DETAIL1_QNAME =  new QName("http://www.example1.com/faults", "myFirstDetail");
    private static final QName DETAIL2_QNAME =  new QName("http://www.example2.com/faults", "mySecondDetail");
    private static final SOAPFault FAULT_11;
    private static final SOAPFault FAULT_12;
    static {
        SOAPFault fault11 = null;
        SOAPFault fault12 = null;
        try {
            fault11 = createFault(SOAPVersion.SOAP_11);
            fault12 = createFault(SOAPVersion.SOAP_12);
        } catch(Exception e) {
            // falls through
        }
        FAULT_11 = fault11;
        FAULT_12 = fault12;
    }

    public SOAPFaultBuilderTest(String testName) {
        super(testName);
    }

    private void printInfoset(Node domNode) throws Exception {
        StringWriter sw = new StringWriter(4096);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(domNode), new StreamResult(sw));
        System.out.println(sw);
    }

    private static SOAPFault createFault(SOAPVersion soapVersion) throws Exception {
        SOAPFactory fac = soapVersion.getSOAPFactory();
        SOAPFault sf = fac.createFault("This is a fault.", soapVersion.faultCodeClient);
        Detail d = sf.addDetail();
        SOAPElement de = d.addChildElement(DETAIL1_QNAME);
        de.addAttribute(new QName("", "msg1"), "This is the first detail message.");
        de = d.addChildElement(DETAIL2_QNAME);
        de.addAttribute(new QName("", "msg2"), "This is the second detail message.");
        return sf;
    }

    public void testCreate11FaultFromSFE() throws Exception {
        SOAPFaultException sfe = new SOAPFaultException(FAULT_11);
        Message msg = SOAPFaultBuilder.createSOAPFaultMessage(SOAPVersion.SOAP_11, sfe, SOAPVersion.SOAP_11.faultCodeMustUnderstand);
        verifyDetail(msg);
    }

    public void testCreate11FaultFromFault() throws Exception {
        Message msg = SOAPFaultBuilder.createSOAPFaultMessage(SOAPVersion.SOAP_11, FAULT_11);
        verifyDetail(msg);
    }

    public void testCreate12FaultFromSFE() throws Exception {
        SOAPFaultException sfe = new SOAPFaultException(FAULT_12);
        Message msg = SOAPFaultBuilder.createSOAPFaultMessage(SOAPVersion.SOAP_12, sfe, SOAPVersion.SOAP_12.faultCodeMustUnderstand);
        verifyDetail(msg);
    }

    public void testCreate12FaultFromFault() throws Exception {
        Message msg = SOAPFaultBuilder.createSOAPFaultMessage(SOAPVersion.SOAP_12, FAULT_12);
        verifyDetail(msg);
    }

    private void verifyDetail(Message message) throws Exception {
        boolean detail = false;
        XMLStreamReader rdr = message.readPayload();
        while(rdr.hasNext()) {
            int event = rdr.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                if (rdr.getName().getLocalPart().equals("detail") || rdr.getName().getLocalPart().equals("Detail")) {
                    detail = true;
                    XMLStreamReaderUtil.nextElementContent(rdr);    // <myFirstDetail>
                    assertEquals(DETAIL1_QNAME, rdr.getName());
                    XMLStreamReaderUtil.nextElementContent(rdr);    // </myFirstDetail>
                    XMLStreamReaderUtil.nextElementContent(rdr);    // <mySecondDetail>
                    assertEquals(DETAIL2_QNAME, rdr.getName());
                }
            }
        }
        if (!detail) {
            fail("There is no detail element in the fault");
        }
    }

}
