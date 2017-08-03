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

package epr.issue564.client;

import com.sun.xml.ws.api.message.*;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.util.ByteArrayBuffer;
import junit.framework.TestCase;
import org.w3c.dom.Element;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * This testcase tests if the namespace declaration on soap:env are retained when the EPRs (replyTo and FaultTo)
 * are read and used in the response
 *
 * @author Rama Pulavarthi
 */
public class Test extends TestCase {
    public void testMember() throws Exception {
        {
            String requestStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<S:Envelope xmlns:S=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:user=\"http://foo.bar\" " +
                    "xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
                    "<S:Header>" +
                    "<wsa:Action>http://example.org/action/echoIn</wsa:Action>" +
                    "<wsa:To>http://www.w3.org/2005/08/addressing/anonymous</wsa:To>" +
                    "<wsa:MessageID>urn:uuid:1234567890</wsa:MessageID>" +
                    "<wsa:ReplyTo>" +
                    "<wsa:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:Address>" +
                    "<wsa:ReferenceParameters>" +
                    "<user:foo>bar</user:foo>" +
                    "<user:bar xmlns:user=\"http://foo.bar1\">" +
                    "<user:foobar>barfoo</user:foobar>" +
                    "</user:bar>" +
                    "</wsa:ReferenceParameters>" +
                    "</wsa:ReplyTo>" +
                    "<wsa:FaultTo>" +
                    "<wsa:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:Address>" +
                    "<wsa:ReferenceParameters>" +
                    "<user:foo>bar</user:foo>" +
                    "<user:bar xmlns:user=\"http://foo.bar1\">" +
                    "<user:foobar>barfoo</user:foobar>" +
                    "</user:bar>" +
                    "</wsa:ReferenceParameters>" +
                    "</wsa:FaultTo>" +
                    "</S:Header>" +
                    "<S:Body>" +
                    "<addNumbers xmlns=\"http://example.com/\">" +
                    "<number1>10</number1>" +
                    "<number2>10</number2>" +
                    "</addNumbers>" +
                    "</S:Body></S:Envelope>";
            Message message = useStream12Codec(requestStr);
            WSEndpointReference wsepr = AddressingUtils.getFaultTo(message.getHeaders(), AddressingVersion.MEMBER, SOAPVersion.SOAP_12);
            Message m2 = Messages.create("Test Unsupported", AddressingVersion.MEMBER, SOAPVersion.SOAP_12);
            wsepr.addReferenceParametersToList(m2.getHeaders());
            ByteArrayBuffer baos = new ByteArrayBuffer();
            XMLStreamWriter writer = XMLStreamWriterFactory.create(baos);
            m2.writeTo(writer);
            writer.flush();

            XMLInputFactory readerFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = readerFactory.createXMLStreamReader(baos.newInputStream());
            Message sm = Messages.create(reader);
            Packet sm_packet = new Packet(sm);
            MessageHeaders headers = sm_packet.getMessage().getHeaders();
            Header h1 = headers.get("http://foo.bar", "foo", true);
            assertEquals("bar", h1.getStringContent());
            Header h2 = headers.get("http://foo.bar1", "bar", true);
            assertTrue(h2 != null);


        }
    }

    public void testW3C() throws Exception {
        {
            String requestStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<S:Envelope xmlns:S=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:user=\"http://foo.bar\" " +
                    "xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
                    "<S:Header>" +
                    "<wsa:Action>http://example.org/action/echoIn</wsa:Action>" +
                    "<wsa:To>http://www.w3.org/2005/08/addressing/anonymous</wsa:To>" +
                    "<wsa:MessageID>urn:uuid:1234567890</wsa:MessageID>" +
                    "<wsa:ReplyTo>" +
                    "<wsa:Address>address1</wsa:Address>" +
                    "<wsa:ReferenceParameters>" +
                    "<user:foo>bar</user:foo>" +
                    "<user:bar xmlns:user=\"http://foo.bar\">" +
                    "<user:foobar>barfoo</user:foobar>" +
                    "</user:bar>" +
                    "</wsa:ReferenceParameters>" +
                    "</wsa:ReplyTo>" +
                    "<wsa:FaultTo>" +
                    "<wsa:Address>address2</wsa:Address>" +
                    "<wsa:ReferenceParameters>" +
                    "<user:foo>bar</user:foo>" +
                    "<user:bar xmlns:user=\"http://foo.bar\">" +
                    "<user:foobar>barfoo</user:foobar>" +
                    "</user:bar>" +
                    "</wsa:ReferenceParameters>" +
                    "</wsa:FaultTo>" +
                    "</S:Header>" +
                    "<S:Body>" +
                    "<addNumbers xmlns=\"http://example.com/\">" +
                    "<number1>10</number1>" +
                    "<number2>10</number2>" +
                    "</addNumbers>" +
                    "</S:Body></S:Envelope>";
            Message message = useStream12Codec(requestStr);
            WSEndpointReference wsepr = AddressingUtils.getFaultTo(message.getHeaders(), AddressingVersion.W3C, SOAPVersion.SOAP_12);
            Message m2 = Messages.create("Test Unsupported", AddressingVersion.W3C, SOAPVersion.SOAP_12);
            wsepr.addReferenceParametersToList(m2.getHeaders());
            ByteArrayBuffer baos = new ByteArrayBuffer();
            XMLStreamWriter writer = XMLStreamWriterFactory.create(baos);
            m2.writeTo(writer);
            writer.flush();

            XMLInputFactory readerFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = readerFactory.createXMLStreamReader(baos.newInputStream());
            Message sm = Messages.create(reader);
            Packet sm_packet = new Packet(sm);
            List refParams = sm_packet.getReferenceParameters();
            assertEquals("Did n't get expected ReferenceParameters", 2, refParams.size());
            for (Object e : refParams) {
                assertEquals("NS Decl did not match", "http://foo.bar", ((Element) e).getNamespaceURI());
            }

        }
    }

    public static Message useStream12Codec(String msg) throws IOException {
        Codec codec = Codecs.createSOAPEnvelopeXmlCodec(SOAPVersion.SOAP_12);
        Packet packet = new Packet();
        ByteArrayInputStream in = new ByteArrayInputStream(msg.getBytes());
        codec.decode(in, "application/soap+xml", packet);
        return packet.getMessage();
    }


}
