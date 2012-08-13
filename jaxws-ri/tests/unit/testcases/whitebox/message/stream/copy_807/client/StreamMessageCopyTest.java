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
package whitebox.message.stream.copy_807.client;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.message.stream.StreamMessage;
import junit.framework.TestCase;

import javax.xml.stream.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

/**
 * Tests the StreamMessage copy implementation
 *
 * @author Jitendra Kotamraju
 */
public class StreamMessageCopyTest extends TestCase {

    private StreamMessage createSOAP11StreamMessage(String msg) throws IOException {
        Codec codec = Codecs.createSOAPEnvelopeXmlCodec(SOAPVersion.SOAP_11);
        Packet packet = new Packet();
        ByteArrayInputStream in = new ByteArrayInputStream(msg.getBytes("utf-8"));
        codec.decode(in, "text/xml", packet);
        return (StreamMessage) packet.getMessage();
    }

    public void testMessageForestCopy() throws Throwable {
        for (int i = 0; i < 2500; i++) {
            try {
                testMessageForestCopy(i);
            } catch (Throwable t) {
                throw new Throwable("Failed for i " + i, t);
            }
        }
    }

    private void testMessageForestCopy(int i) throws Exception {
        String soapMsgStart = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body>";
        String soapMsgEnd = "</S:Body></S:Envelope>";
        StringBuilder sb = new StringBuilder(soapMsgStart);
        while (i-- > 0) {
            sb.append("<a/>");
        }
        sb.append(soapMsgEnd);
        StreamMessage msg = createSOAP11StreamMessage(sb.toString());
        Message msg1 = msg.copy();
        msg1.copy();
    }

    public void testEmptyPayload() throws Exception {
        String soapMsg = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body/></S:Envelope>";
        StringBuilder sb = new StringBuilder(soapMsg);
        StreamMessage msg = createSOAP11StreamMessage(sb.toString());
        Message msg1 = msg.copy();
        msg.consume();
        msg1.consume();
    }

    public void testMessageCopy() throws Throwable {
        for (int i = 0; i < 250; i++) {
            try {
                testMessageCopy(i);
            } catch (Throwable t) {
                throw new Throwable("Failed for i " + i, t);
            }
        }
    }

    private void testMessageCopy(int i) throws Exception {
        String str = getMessage(i);
        StreamMessage msg = createSOAP11StreamMessage(str);
        Message msg1 = msg.copy();
        Message msg2 = msg.copy();
        Message msg3 = msg1.copy();
        Message msg4 = msg2.copy();

        compareXmlStrings(str, writeMsgEnvelope(msg));
        compareXmlStrings(str, writeMsgEnvelope(msg1));
        compareXmlStrings(str, writeMsgEnvelope(msg2));
        compareXmlStrings(str, writeMsgEnvelope(msg3));
        compareXmlStrings(str, writeMsgEnvelope(msg4));
    }

    private String getMessage(int i) {
        String soapMsgStart = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body>";
        String soapMsgEnd = "</S:Body></S:Envelope>";
        StringBuilder sb = new StringBuilder(soapMsgStart);
        int j = i;
        while (j-- > 0) {
            sb.append("<a>");
        }
        j = i;
        while (j-- > 0) {
            sb.append("</a>");
        }
        sb.append(soapMsgEnd);
        return sb.toString();
    }

    private String writeMsgEnvelope(Message msg) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(baos);
        msg.writeTo(xsw);
        xsw.close();
        return new String(baos.toByteArray());
    }

    private void compareXmlStrings(String str1, String str2, String ... prefixes) throws Exception {
        XMLStreamReader rdr1 = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(str1));
        XMLStreamReader rdr2 = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(str2));
        compareReaders(rdr1, rdr2, prefixes);
        rdr1.close();
        rdr2.close();
    }

    private void compareReaders(XMLStreamReader rdr, XMLStreamReader xsbrdr, String... prefixes) throws XMLStreamException {
        while(rdr.hasNext()) {
            assertTrue(xsbrdr.hasNext());
            int expected = rdr.next();
            int actual = xsbrdr.next();
            assertEquals(expected, actual);
            if (expected == XMLStreamReader.START_ELEMENT || expected == XMLStreamReader.END_ELEMENT) {
                assertEquals(rdr.getName(), xsbrdr.getName());
                for(String prefix : prefixes) {
                    //System.out.println("|"+rdr.getNamespaceURI(prefix)+"|"+xsbrdr.getNamespaceURI(prefix)+"|");
                    assertEquals(fixNull(rdr.getNamespaceURI(prefix)), fixNull(xsbrdr.getNamespaceURI(prefix)));
                }
            }
        }
    }

    private static String fixNull(String s) {
        if (s == null) return "";
        else return s;
    }

}
