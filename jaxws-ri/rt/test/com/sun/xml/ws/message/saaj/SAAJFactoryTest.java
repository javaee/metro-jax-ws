/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.message.saaj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.saaj.SAAJFactory;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.encoding.SOAPBindingCodec;

import junit.framework.TestCase;

public class SAAJFactoryTest extends TestCase {
    private static final String CUSTOM_MIME_HEADER_NAME = "custom-header";
    private static final String CUSTOM_MIME_HEADER_NAME2 = "Content-custom-header";
    private static final String CUSTOM_MIME_HEADER_VALUE = "custom-value";
    private static final String CUSTOM_MIME_HEADER_VALUE2 = "content-custom-value";

    // Test that SAAJ message converted to JAX-WS logical message and then
    // back to SAAJ message keeps all MIME headers in the attachment part.
    public void testMimeHeadersPreserved() throws Exception {
        // Create a test SAAJ message.
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage msg = mf.createMessage();
        msg.getSOAPBody()
                .addBodyElement(new QName("http://test/", "myelement"));

        // Add an attachment with extra MIME headers.
        addAttachmentPart(msg, "hello1");

        // Convert the SAAJ message to a logical message.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        msg.writeTo(bos);
        String contentType = msg.getMimeHeaders().getHeader("Content-Type")[0];
        Message message = getLogicalMessage(bos.toByteArray(), contentType);

        // Convert the logical message back to a SAAJ message and ensure
        // the extra headers are present.
        SOAPMessage msg2 = SAAJFactory.read(SOAPVersion.SOAP_11, message);
        assertCustomMimeHeadersOnAttachments(msg2);
        msg2.writeTo(System.out);
    }

    private AttachmentPart addAttachmentPart(SOAPMessage msg, String value) {
        AttachmentPart att = msg.createAttachmentPart(value, "text/html");
        att.addMimeHeader(CUSTOM_MIME_HEADER_NAME, CUSTOM_MIME_HEADER_VALUE);
        att.addMimeHeader(CUSTOM_MIME_HEADER_NAME2, CUSTOM_MIME_HEADER_VALUE2);
        msg.addAttachmentPart(att);
        return att;
    }

    private Message getLogicalMessage(byte[] bytes, String contentType)
            throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        WSBinding binding = BindingImpl.create(BindingID.SOAP11_HTTP);
        Codec codec = new SOAPBindingCodec(binding);
        Packet packet = new Packet();
        codec.decode(in, contentType, packet);
        return packet.getMessage();
    }

    private void assertCustomMimeHeadersOnAttachments(SOAPMessage msg) {
        @SuppressWarnings("unchecked")
        Iterator<AttachmentPart> attachments = msg.getAttachments();
        assertTrue(attachments.hasNext());
        while (attachments.hasNext()) {
            AttachmentPart part = attachments.next();
            String[] hdr = part.getMimeHeader(CUSTOM_MIME_HEADER_NAME);
            assertNotNull("Missing first custom MIME header", hdr);
            assertEquals("Expected one header value", hdr.length, 1);
            assertEquals("Wrong value for first header", hdr[0],
                    CUSTOM_MIME_HEADER_VALUE);
            hdr = part.getMimeHeader(CUSTOM_MIME_HEADER_NAME2);
            assertNotNull("Missing second custom MIME header", hdr);
            assertEquals("Expected one header value", hdr.length, 1);
            assertEquals("Wrong value for second header", hdr[0],
                    CUSTOM_MIME_HEADER_VALUE2);
            assertNull("Unexpected header found",
                    part.getMimeHeader("not found header"));
        }
    }
}
