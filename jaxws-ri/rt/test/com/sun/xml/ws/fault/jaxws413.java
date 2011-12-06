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
import junit.framework.TestCase;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class jaxws413 extends TestCase {

    public void test1() throws SOAPException, IOException {
        SOAPFault fault = SOAPVersion.SOAP_11.getSOAPFactory().createFault();

        fault.setFaultCode(new QName("http://foo/bar", "mycode", "myprefix"));
        fault.setFaultString("Some exception");
        Detail detail = fault.addDetail();
        detail.addDetailEntry(new QName("http://foo/bar", "soap11detail"));

        Message faultMsg = SOAPFaultBuilder.createSOAPFaultMessage(SOAPVersion.SOAP_11, fault);

        SOAPMessage sm = faultMsg.readAsSOAPMessage();

        Detail det = sm.getSOAPBody().getFault().getDetail();
        Iterator iter = det.getDetailEntries();
        assertTrue(iter.hasNext());
        Element item = (Element) iter.next();
        assertEquals(item.getNamespaceURI(), "http://foo/bar");
        assertEquals(item.getLocalName(), "soap11detail");

    }

    public void test2() throws SOAPException, IOException {
        SOAPFault fault = SOAPVersion.SOAP_12.getSOAPFactory().createFault();

        fault.setFaultCode(new QName("http://www.w3.org/2003/05/soap-envelope", "Sender", "myprefix"));
        fault.setFaultString("Some exception");
        Detail detail = fault.addDetail();
        detail.addDetailEntry(new QName("http://foo/bar", "soap12detail"));

        Message faultMsg = SOAPFaultBuilder.createSOAPFaultMessage(SOAPVersion.SOAP_12, fault);

        SOAPMessage sm = faultMsg.readAsSOAPMessage();
        Detail det = sm.getSOAPBody().getFault().getDetail();
        Iterator iter = det.getDetailEntries();
        assertTrue(iter.hasNext());
        Element item = (Element) iter.next();
        assertEquals(item.getNamespaceURI(), "http://foo/bar");
        assertEquals(item.getLocalName(), "soap12detail");

    }
}
