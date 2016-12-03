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

package com.sun.xml.ws.message.saaj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.saaj.SAAJFactory;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.encoding.SOAPBindingCodec;
import com.sun.xml.ws.message.stream.StreamMessage;

import com.sun.xml.ws.util.ByteArrayBuffer;
import junit.framework.TestCase;
import org.w3c.dom.Node;

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
    
    /**
     * Test whether SAAJFactory.readAsSOAPMessage can handle null namespace prefixes if the 
     * appropriate flag is set on Woodstox
     * @throws Exception
     */
    public void testNullNamespacePrefix() throws Exception {
    	XMLInputFactory infact = XMLInputFactory.newFactory();
    	try {
    		//for Woodstox, set property that ensures it will return null prefixes for default namespace
    		infact.setProperty("com.ctc.wstx.returnNullForDefaultNamespace", Boolean.TRUE);
    	} catch(Throwable t) {
    		//ignore - it is not Woodstox or it is an old version of Woodstox, so this
    		//test is irrelevant. Note this try/catch is needed because Woodstox isPropertySupported
    		//is unreliable
    		return;
    	}
    	
		String soap = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
			    "<soap:Body>" + 
			        "<sendMessage xmlns=\"http://www.foo.bar/schema/\" xmlns:ns2=\"http://www.foo.bar/types/\">;" +
			        "    <message xsi:type=\"ns2:someType\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
			            "</message>" + 
			        "</sendMessage></soap:Body></soap:Envelope>";
		XMLStreamReader envelope = infact.createXMLStreamReader(new StringReader(soap));
		StreamMessage smsg = new StreamMessage(SOAPVersion.SOAP_11, 
				envelope, null);
		SAAJFactory saajFac = new SAAJFactory();
		try {
			//Previously this line failed with NPE - should be fixed now. 
			SOAPMessage msg = saajFac.readAsSOAPMessage(SOAPVersion.SOAP_11, smsg);
		} catch (NullPointerException npe) {
			fail("NPE for null namespace prefix is not fixed!");
			npe.printStackTrace();
		}
    }

    /**
     * Test whether SAAJFactory.readAsSOAPMessage can handle default namespace reset correctly.
     *
     * <p>
     * This test emulates JDK-8159058 issue. The issue is that the default namespace reset was not respected
     * with built-in JDK XML input factory (it worked well with woodstax).
     * </p>
     *
     * <p>
     * This test operates against JDK XMLInputFactory.
     * </p>
     *
     * @throws Exception
     */
    public void testResetDefaultNamespaceToGlobalWithJDK() throws Exception {
        XMLInputFactory inputFactory = getBuiltInJdkXmlInputFactory();
        XMLStreamReader envelope = inputFactory.createXMLStreamReader(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<s:Body>" +
                "<SampleServiceRequest xmlns=\"http://sample.ex.org/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<RequestParams xmlns=\"\">" +
                "<Param1>hogehoge</Param1>" +
                "<Param2>fugafuga</Param2>" +
                "</RequestParams>" +
                "</SampleServiceRequest>" +
                "</s:Body>" +
                "</s:Envelope>"));
        StreamMessage streamMessage = new StreamMessage(SOAPVersion.SOAP_11,
                envelope, null);
        SAAJFactory saajFac = new SAAJFactory();
        SOAPMessage soapMessage = saajFac.readAsSOAPMessage(SOAPVersion.SOAP_11, streamMessage);
        // check object model
        SOAPElement request = (SOAPElement)soapMessage.getSOAPBody().getFirstChild();
        assertEquals("SampleServiceRequest", request.getLocalName());
        assertEquals("http://sample.ex.org/", request.getNamespaceURI());
        SOAPElement params = (SOAPElement)request.getFirstChild();
        assertEquals("RequestParams", params.getLocalName());
        assertNull(params.getNamespaceURI());
        SOAPElement param1 = (SOAPElement)params.getFirstChild();
        assertEquals("Param1", param1.getLocalName());
        assertNull(param1.getNamespaceURI());
        SOAPElement param2 = (SOAPElement)params.getChildNodes().item(1);
        assertEquals("Param2", param2.getLocalName());
        assertNull(param2.getNamespaceURI());
        // check the message as string
        assertEquals("<SampleServiceRequest xmlns=\"http://sample.ex.org/\">" +
                        "<RequestParams xmlns=\"\">" +
                        "<Param1>hogehoge</Param1>" +
                        "<Param2>fugafuga</Param2>" +
                        "</RequestParams>" +
                        "</SampleServiceRequest>",
                nodeToText(request));
    }

    /**
     * Test whether SAAJFactory.readAsSOAPMessage can handle default namespace reset correctly.
     *
     * <p>
     * This test emulates JDK-8159058 issue. The issue is that the default namespace reset was not respected
     * with built-in JDK XML input factory (it worked well with woodstax).
     * </p>
     *
     * <p>
     * This test operates against woodstax.
     * </p>
     *
     * @throws Exception
     */
    public void testResetDefaultNamespaceToGlobalWithWoodstax() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        XMLStreamReader envelope = inputFactory.createXMLStreamReader(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<s:Body>" +
                "<SampleServiceRequest xmlns=\"http://sample.ex.org/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<RequestParams xmlns=\"\">" +
                "<Param1>hogehoge</Param1>" +
                "<Param2>fugafuga</Param2>" +
                "</RequestParams>" +
                "</SampleServiceRequest>" +
                "</s:Body>" +
                "</s:Envelope>"));
        StreamMessage streamMessage = new StreamMessage(SOAPVersion.SOAP_11,
                envelope, null);
        SAAJFactory saajFac = new SAAJFactory();
        SOAPMessage soapMessage = saajFac.readAsSOAPMessage(SOAPVersion.SOAP_11, streamMessage);
        // check object model
        SOAPElement request = (SOAPElement)soapMessage.getSOAPBody().getFirstChild();
        assertEquals("SampleServiceRequest", request.getLocalName());
        assertEquals("http://sample.ex.org/", request.getNamespaceURI());
        SOAPElement params = (SOAPElement)request.getFirstChild();
        assertEquals("RequestParams", params.getLocalName());
        assertNull(params.getNamespaceURI());
        SOAPElement param1 = (SOAPElement)params.getFirstChild();
        assertEquals("Param1", param1.getLocalName());
        assertNull(param1.getNamespaceURI());
        SOAPElement param2 = (SOAPElement)params.getChildNodes().item(1);
        assertEquals("Param2", param2.getLocalName());
        assertNull(param2.getNamespaceURI());
        // check the message as string
        assertEquals("<SampleServiceRequest xmlns=\"http://sample.ex.org/\">" +
                        "<RequestParams xmlns=\"\">" +
                        "<Param1>hogehoge</Param1>" +
                        "<Param2>fugafuga</Param2>" +
                        "</RequestParams>" +
                        "</SampleServiceRequest>",
                nodeToText(request));
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
        Codec codec = new SOAPBindingCodec(binding.getFeatures());
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

    private XMLInputFactory getBuiltInJdkXmlInputFactory() {
        final String factoryId = "test.only.xml.input.factory.class.name";
        final String className = "com.sun.xml.internal.stream.XMLInputFactoryImpl";
        System.setProperty(factoryId, className);
        XMLInputFactory infact = XMLInputFactory.newFactory(factoryId, null);
        if (!className.equals(infact.getClass().getName())) {
            throw new IllegalStateException("Can not obtain requested XMLInputFactory. Desired: " + className + ", actual: " + infact.getClass().getName());
        }
        return infact;
    }

    private String nodeToText(Node node) throws TransformerException {
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        trans.transform(new DOMSource(node), result);
        return writer.toString();
    }
}
