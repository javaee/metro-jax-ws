/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.message.stream;

import com.oracle.webservices.api.message.MessageContext;
import com.oracle.webservices.api.message.MessageContextFactory;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.encoding.MtomCodec;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import com.sun.xml.ws.util.ByteArrayBuffer;
import junit.framework.TestCase;

import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.soap.MTOMFeature;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * StreamMessage test
 *
 * @author Jitendra Kotamraju
 */
public class StreamMessageTest extends TestCase {
    String FAULT_MESSAGE  = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>"+
            "<S:Header>" +
            "<wsa:Action xmlns:wsa='http://www.w3.org/2005/08/addressing'>http://example.com/addNumbers</wsa:Action>" +
            "</S:Header>" +
            "<S:Body><S:Fault>" +
            "<faultCode>S:client</faultCode>" +
            "<faultString>Fault Test</faultString>" +
            "<detail xmlns:ns1='urn:fault'><ns1:entry></ns1:entry></detail>" +
            "</S:Fault></S:Body></S:Envelope>";

    // tests Message#getFirstDetailEntryName()
    public void testFirstDetailEntryName() throws Exception {
        Message msg = useStreamCodec(FAULT_MESSAGE);
        QName exp = new QName("urn:fault", "entry");
        assertEquals(exp, msg.getFirstDetailEntryName());
    }

    private StreamMessage useStreamCodec(String msg) throws IOException {
        Codec codec = Codecs.createSOAPEnvelopeXmlCodec(SOAPVersion.SOAP_11);
        Packet packet = new Packet();
        ByteArrayInputStream in = new ByteArrayInputStream(msg.getBytes());
        codec.decode(in, "text/xml", packet);
        return (StreamMessage) packet.getInternalMessage();
    }

    public void testCopyStreamMessage1() throws IOException {
        String soap11Msg =
"<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema'><Header xmlns='http://schemas.xmlsoap.org/soap/envelope/'> <SubscriptionInfo xmlns='http://ws.strikeiron.com'> <LicenseStatusCode>0</LicenseStatusCode> </SubscriptionInfo> </Header> <soap:Body> <GetCountryCodesResponse xmlns='http://www.strikeiron.com'> <GetCountryCodesResult/></GetCountryCodesResponse></soap:Body></soap:Envelope>";
        Message message = useStreamCodec(soap11Msg);
        message.copy();
    }

    public void testCopyWithSpaces() throws IOException {
        String soap12Msg = "<?xml version='1.0'?><S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'> <S:Header> <m:ut xmlns:m='a'> <u xmlns='' id='a'>user</u> </m:ut> <b> hello </b> </S:Header> <S:Body> <ns2:c xmlns:ns2='local'> <clientName>Test</clientName> <ns2:year>2007</ns2:year> </ns2:c> </S:Body> </S:Envelope>";
        Message message = useStreamCodec(soap12Msg);
        message.copy();
    }

    public void testCopy13() throws IOException {
        String soap13Msg = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body/></S:Envelope>";
        Message message = useStreamCodec(soap13Msg);
        message.copy();
    }

    public void testCopy14() throws IOException {
        String soap14Msg = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body> </S:Body></S:Envelope>";
        Message message = useStreamCodec(soap14Msg);
        message.copy();
    }

    public void testCopy15() throws IOException {
        String soap15Msg = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'> <S:Body> </S:Body> </S:Envelope>";
        Message message = useStreamCodec(soap15Msg);
        message.copy();
    }

    public void testCopy16() throws IOException {
        String soap16Msg = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'> <S:Body> <a> </a><b> </b> <c/> </S:Body> </S:Envelope>";
        Message message = useStreamCodec(soap16Msg);
        message.copy();
    }

    public void testCopy17() throws IOException {
        String soap17Msg = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'> <S:Header> <a> </a> <b> </b> </S:Header> <S:Body> <a> </a><b> </b> <c/> </S:Body> </S:Envelope>";
        Message message = useStreamCodec(soap17Msg);
        message.copy();
    }

    /*
     * Test for the following exception. Bug in StreamMessage.copy() code
     * java.lang.IllegalArgumentException: faultCode argument for createFault was passed NULL
     *   at com.sun.xml.messaging.saaj.soap.ver1_1.SOAPFactory1_1Impl.createFault(SOAPFactory1_1Impl.java:87)
     */
    public void testCopy18() throws Exception {
        String soap18Msg =
"<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>" +
  "<S:Body>" +
    "<S:Fault xmlns:ns4='http://www.w3.org/2003/05/soap-envelope'>" +
      "<faultcode>S:Server</faultcode>" +
      "<faultstring>com.sun.istack.XMLStreamException2</faultstring>" +
    "</S:Fault>" +
  "</S:Body>" +
"</S:Envelope>";
        Message message = useStreamCodec(soap18Msg);
        message.copy();
        SOAPFaultBuilder.create(message).createException(null);
    }

    /*
     * ns4 is declared on Envelope and is used in faultcode. So making sure
     * it is picked up after copy()
     */
    public void testCopy19() throws Exception {
        String soap18Msg =
"<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/' xmlns:ns4='http://schemas.xmlsoap.org/soap/envelope/'>" +
  "<S:Body>" +
    "<S:Fault>" +
      "<faultcode>ns4:Server</faultcode>" +
      "<faultstring>com.sun.istack.XMLStreamException2</faultstring>" +
    "</S:Fault>" +
  "</S:Body>" +
"</S:Envelope>";
        Message message = useStreamCodec(soap18Msg);
        message.copy();
        SOAPFaultBuilder.create(message).createException(null);
    }

    /*
     * ns4 is declared on Body and is used in faultcode. So making sure
     * it is picked up after copy()
     */
    public void testCopy20() throws Exception {
        String soap18Msg =
"<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>" +
  "<S:Body xmlns:ns4='http://schemas.xmlsoap.org/soap/envelope/'>" +
    "<S:Fault>" +
      "<faultcode>ns4:Server</faultcode>" +
      "<faultstring>com.sun.istack.XMLStreamException2</faultstring>" +
    "</S:Fault>" +
  "</S:Body>" +
"</S:Envelope>";
        Message message = useStreamCodec(soap18Msg);
        message.copy();
        SOAPFaultBuilder.create(message).createException(null);
    }

    /*
     * ns4 is declared on Envelope and is used in faultcode. So making sure
     * it is picked up for payload source
     */
    public void testPayloadSource() throws Exception {
        String soap18Msg =
"<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/' xmlns:ns4='http://schemas.xmlsoap.org/soap/envelope/'>" +
  "<S:Body>" +
    "<S:Fault>" +
      "<faultcode>ns4:Server</faultcode>" +
      "<faultstring>com.sun.istack.XMLStreamException2</faultstring>" +
    "</S:Fault>" +
  "</S:Body>" +
"</S:Envelope>";
        Message message = useStreamCodec(soap18Msg);
        Source source = message.readPayloadAsSource();
        InputStream is = getInputStream(source);
        XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);
        xsr.next();
        xsr.next();
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", xsr.getNamespaceURI("ns4"));
    }

    /*
     * ns4 is declared on Envelope and Body and is used in faultcode.
     * So making sure the correct ns4 is picked up for payload source
     */
    public void testPayloadSource1() throws Exception {
        String soap18Msg =
"<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/' xmlns:ns4='A'>" +
  "<S:Body xmlns:ns4='http://schemas.xmlsoap.org/soap/envelope/'>" +
    "<S:Fault>" +
      "<faultcode>ns4:Server</faultcode>" +
      "<faultstring>com.sun.istack.XMLStreamException2</faultstring>" +
    "</S:Fault>" +
  "</S:Body>" +
"</S:Envelope>";
        Message message = useStreamCodec(soap18Msg);
        Source source = message.readPayloadAsSource();
        InputStream is = getInputStream(source);
        XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);
        xsr.next();
        xsr.next();
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", xsr.getNamespaceURI("ns4"));
    }

    public void testCData() throws Exception {
        String soap18Msg = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
"<soapenv:Body>" +
    "<echoRequest xmlns=\"http://test.oracle.com/xsd\">" +
      "<arg0>outside cdata <![CDATA[<data>inside cdata</data>]]></arg0>" +
    "</echoRequest>" +
  "</soapenv:Body>" +
"</soapenv:Envelope>";
		Message message = useStreamCodec(soap18Msg);
        Source source = message.readPayloadAsSource();
        InputStream is = getInputStream(source);
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
        reader.next();
        while (reader.getEventType() == XMLStreamReader.START_ELEMENT) reader.next();
        String text = "";
        //Depending on parser settings, multiple characters events may be reported
        //in small chunks, instead of one event for an entire text node
        while (reader.getEventType() == XMLStreamReader.CHARACTERS) {
            String textTmp = new String(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
            reader.next();
            text += textTmp;
        }
        assertEquals("outside cdata <data>inside cdata</data>", text);
    }
	
/*
    private DOMSource toDOMSource(Source source) throws Exception {
        if (source instanceof DOMSource) {
            return (DOMSource)source;
        }
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        DOMResult result = new DOMResult();
        trans.transform(source, result);
        return new DOMSource(result.getNode());
    }
    */

    private InputStream getInputStream(Source source) throws Exception {
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        ByteArrayBuffer bab = new ByteArrayBuffer();
        StreamResult result = new StreamResult(bab);
        trans.transform(source, result);
        return bab.newInputStream();
    }
    
    public void testEmptySoapHeader() throws Exception {
		String soap = 
				"<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>" +
		           "<soap:Header xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'></soap:Header>" +
		  "<soapenv:Body>"+
		  "  <tes:fooOwsmMsgProtection xmlns:tes='http://testOwsm'>"+
		  "   <!--Optional:-->"+
		  "    <arg0>string</arg0>"+
		  "  </tes:fooOwsmMsgProtection>"+
		  "</soapenv:Body>"+
		"</soapenv:Envelope>";		
		MessageContextFactory mcf = MessageContextFactory.createFactory();		
		MessageContext mc = mcf.createContext(new ByteArrayInputStream(soap.getBytes()), "text/xml");
		String string1 = mc.toString();
		
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		mc.writeTo(bo);
		String string2 = new String(bo.toByteArray());		
		assertTrue(string1.indexOf("Header") != -1);
		assertTrue(string2.indexOf("Header") != -1);    	
    }

    public void testWriteMtomToStream() throws Exception {
        // Ensure writing StreamMessage to OutputputStream preserves CID and
        // does not create duplicate attachment.  We check this by re-parsing
        // as a SOAP message and verifying the contents.

        InputStream is = null;
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        try {
            String ctype = "multipart/related;type=\"application/xop+xml\";"
                    + "boundary=\"----=_Part_0_1145105632.1353005695468\";"
                    + "start=\"<cbe648b3-2055-413e-b8ed-877cdf0f2477>\";start-info=\"text/xml\"";

            MessageContextFactory mcf = MessageContextFactory
                    .createFactory(new MTOMFeature(true));
            is = getClass().getClassLoader().getResourceAsStream(
                    "etc/testMtomMessageReload_01.msg");
            MessageContext mc = mcf.createContext(is, ctype);
            Packet packet = (Packet) mc;
            Message message = packet.getInternalMessage();
            assertTrue("StreamMessage not found, got : " + message.getClass(),
                    StreamMessage.class.isAssignableFrom(message.getClass()));
            baos = new ByteArrayOutputStream();
            mc.writeTo(baos);
            bais = new ByteArrayInputStream(baos.toByteArray());
            MessageFactory mf = MessageFactory.newInstance();
            MimeHeaders mh = new MimeHeaders();
            mh.addHeader("Content-Type", ctype);
            SOAPMessage sm = mf.createMessage(mh, bais);
            assertEquals("wrong attachment count", 1, sm.countAttachments());
            AttachmentPart ap = (AttachmentPart) sm.getAttachments().next();
            assertEquals("wrong attachemnt Content-Id",
                    "<534475ae-bdab-4594-9f97-c09908bacfbd>", ap.getContentId());
            NodeList nl = sm.getSOAPBody().getElementsByTagNameNS(
                    MtomCodec.XOP_NAMESPACEURI, MtomCodec.XOP_LOCALNAME);
            assertEquals(MtomCodec.XOP_NAMESPACEURI + ":"
                    + MtomCodec.XOP_LOCALNAME + " not found", 1, nl.getLength());
            Element elt = (Element) nl.item(0);
            assertEquals("wrong href value",
                    "cid:534475ae-bdab-4594-9f97-c09908bacfbd",
                    elt.getAttribute("href"));
        } finally {
            Exception e = null;
            try {
                if (is != null)
                    is.close();
            } catch (IOException e1) {
                e = e1;
            }
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e2) {
                e = e2;
            }
            try {
                if (bais != null)
                    bais.close();
            } catch (Exception e3) {
                e = e3;
            }
            if (e != null)
                throw e;
        }
    }
}
