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

package com.sun.xml.ws.message.saaj;

import junit.framework.TestCase;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.message.StringHeader;
import java.io.ByteArrayInputStream;

/**
 * @author Rama Pulavarthi
 */
public class SAAJMessageTest extends TestCase {
    String MESSAGE  = 	"<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header>" +
            "<wsa:Action xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">http://example.com/addNumbers</wsa:Action>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\">" +
            "<number1>10</number1>" +
            "<number2>10</number2>" +
            "</addNumbers>" +
            "</S:Body></S:Envelope>";

    public void test1() throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        Source src = new StreamSource(new ByteArrayInputStream(MESSAGE.getBytes()));
        message.getSOAPPart().setContent(src);

        SAAJMessage saajMsg = new SAAJMessage(message);
        assertEquals("addNumbers",saajMsg.getPayloadLocalPart());
        assertEquals("http://example.com/addNumbers",saajMsg.getHeaders().getAction(AddressingVersion.W3C, SOAPVersion.SOAP_11));
        Header header = new StringHeader(new QName("urn:foo","header1"),"test header  ");
        saajMsg.getHeaders().add(header);
        
        SOAPMessage newMsg = saajMsg.readAsSOAPMessage();
        newMsg.writeTo(System.out);
        SAAJMessage saajMsg2 = new SAAJMessage(newMsg);
        assertEquals(2,saajMsg2.getHeaders().size());

        Message saajMsg3 = saajMsg2.copy();
        assertEquals("addNumbers",saajMsg3.getPayloadLocalPart());
        assertEquals("http://example.com/addNumbers",saajMsg3.getHeaders().getAction(AddressingVersion.W3C, SOAPVersion.SOAP_11));
        assertEquals(2,saajMsg2.getHeaders().size());
        XMLStreamWriter writer = XMLStreamWriterFactory.create(System.out);
        saajMsg3.writeTo(writer);
        writer.close();

    }
}
