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
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package whitebox.message.saaj_535.client;

import junit.framework.TestCase;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.message.stream.StreamMessage;
import com.sun.xml.ws.message.saaj.SAAJMessage;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * Tests the SAAJMessage implementation for issue: 535
 *
 * @author Jitendra Kotamraju
 */
public class SaajMessageTest extends TestCase {
    String MESSAGE_535  =
        "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>"+
        "<S:Header>" +
        "<wsa:Action xmlns:wsa='http://www.w3.org/2005/08/addressing'>http://example.com/addNumbers</wsa:Action>" +
        "</S:Header>" +
        "<S:Body attr='value'>" +
        "<addNumbers xmlns='http://example.com/'/>" +
        "</S:Body></S:Envelope>";

    public void testBodyAttr() throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        Source src = new StreamSource(new ByteArrayInputStream(MESSAGE_535.getBytes()));
        message.getSOAPPart().setContent(src);

        SAAJMessage saajMsg = new SAAJMessage(message);
        saajMsg.hasHeaders();         // breaks the underlying SOAPMessage
        Source source = saajMsg.readEnvelopeAsSource();
        SOAPMessage newMsg = factory.createMessage();
        newMsg.getSOAPPart().setContent(source);
        SOAPBody body = newMsg.getSOAPBody();
        assertEquals("value", body.getAttribute("attr"));
    }
}
