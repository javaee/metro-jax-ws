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

package whitebox.message.stream.basic.client;

import junit.framework.TestCase;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.message.stream.StreamMessage;

import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * Tests the StreamMessage implementation of Message API.
 *
 * @author Rama Pulavarthi
 */
public class StreamMessageTest extends TestCase {
    /**
     * jax-ws issue 610
     * @throws Exception
     */
    public void testMessageWriteTo() throws Exception {

    String soapMsg = "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<Header>" +
                    "</Header>" +
                    "<Body>" +
                    "<GetCountryCodesResponse xmlns='http://www.strikeiron.com'> <GetCountryCodesResult/></GetCountryCodesResponse>" +
                    "</Body>" +
                    "</Envelope>";
            Message message = createSOAP11StreamMessage(soapMsg);
            ByteArrayBuffer baos = new ByteArrayBuffer();
            XMLStreamWriter writer = XMLStreamWriterFactory.create(baos);
            message.writeTo(writer);
            writer.flush();
            baos.writeTo(System.out);
   }

   public void testMessageWriteTo1() throws Exception {

    String soapMsg = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<S:Header>" +
                    "</S:Header>" +
                    "<S:Body>" +
                    "<GetCountryCodesResponse xmlns='http://www.strikeiron.com'> <GetCountryCodesResult/></GetCountryCodesResponse>" +
                    "</S:Body>" +
                    "</S:Envelope>";
            Message message = createSOAP11StreamMessage(soapMsg);
            ByteArrayBuffer baos = new ByteArrayBuffer();
            XMLStreamWriter writer = XMLStreamWriterFactory.create(baos);
            message.writeTo(writer);
            writer.flush();
            baos.writeTo(System.out);
   }

    private StreamMessage createSOAP11StreamMessage(String msg) throws IOException {
        Codec codec = Codecs.createSOAPEnvelopeXmlCodec(SOAPVersion.SOAP_11);
        Packet packet = new Packet();
        ByteArrayInputStream in = new ByteArrayInputStream(msg.getBytes());
        codec.decode(in, "text/xml", packet);
        return (StreamMessage) packet.getMessage();
    }
}
