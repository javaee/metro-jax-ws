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

package whitebox.mtom_codec.client;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.binding.BindingImpl;
import junit.framework.TestCase;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.jvnet.staxex.Base64Data;

/**
 * @author Jitendra Kotamraju
 */
public class CharactersTest extends TestCase {
    
    public void testHandleCharacters() throws Throwable {
        //handleCharacters(12193);

        for(int i=0; i < 15000; i++) {
            try {
                handleCharacters(i);
            } catch(Throwable t) {
                System.out.println("Failed for the size="+i);
                throw t;
            }
        }
    }

    private void handleCharacters(int size) throws Exception {
        BindingID bid = BindingID.parse(SOAPBinding.SOAP11HTTP_MTOM_BINDING);
        BindingImpl binding = (BindingImpl)bid.createBinding();
        Codec codec = binding.createCodec();
        TestMessage msg = new TestMessage(size);

        Packet packet = new Packet();
        codec.decode(msg.getInputStream(), msg.getContentType(), packet);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XMLStreamWriter sw = XMLStreamWriterFactory.create(bout);
        packet.getMessage().writeTo(sw);
        sw.close();

        InputStream in = new ByteArrayInputStream(bout.toByteArray());
        XMLStreamReader sr = XMLStreamReaderFactory.create(null, in, true);
        while(sr.hasNext()) {
            sr.next();
            if(sr.getEventType() == XMLStreamReader.START_ELEMENT && sr.getLocalName().equals("doc1")) {
                assertEquals(msg.getEncodedText(), sr.getElementText().trim());
            }
        }
    }

    private static final class TestMessage {

        // keep whitespaces around <xop:Inclue> elements
        private static final String ENV =
    "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xop='http://www.w3.org/2004/08/xop/include'>"+
        "<S:Body>" +
            "<MTOMInOut xmlns='http://example.org/mtom'>"+
                "<doc1>  <xop:Include href='attachment-content-id'> </xop:Include> </doc1>"+
            "</MTOMInOut>" +
         "</S:Body>" +
    "</S:Envelope>";

        private static final String CT =
            "multipart/related;start=\"envelope-content-id\";type=\"application/xop+xml\";boundary=\"boundary\";start-info=\"text/xml\"";

        private final String attData;
        private final String attEncodedData;

        TestMessage(int size) {
            this.attData = getAttachmentData(size);
            this.attEncodedData = getEncodedAttachmentData(size);
        }

        private static String getAttachmentData(int size) {
            StringBuilder att = new StringBuilder();
            for(int i=0; i < size; i++) {
                att.append((char)('A'+i%26));
            }
            return att.toString();
        }

        private static String getEncodedAttachmentData(int size) {
            String str = getAttachmentData(size);
            Base64Data encoded = new Base64Data();
            encoded.set(str.getBytes(), null);
            return encoded.toString();
        }

        String getEncodedText() {
            return attEncodedData;
        }

        String getContentType() {
            return CT;
        }

        InputStream getInputStream() {
            String msg =
                "--boundary\r\n"+
                "Content-Type: application/xop+xml;charset=utf-8;type=\"text/xml\"\r\n"+
                "Content-Id: envelope-content-id\r\n\r\n"+
                ENV+"\r\n"+
                "--boundary\r\n"+
                "Content-Type: text/plain\r\n"+
                "Content-ID: attachment-content-id\r\n\r\n"+
                attData+"\r\n"+
                "--boundary--";

            //System.out.println(msg);
            return new ByteArrayInputStream(msg.getBytes());
        }
    }

}
