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

package mtom.encoding.client;

import junit.framework.TestCase;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.ws.*;
import javax.xml.soap.*;
import javax.xml.ws.http.*;
import javax.xml.namespace.QName;
import javax.xml.ws.Service.Mode;
import java.awt.*;
import java.io.*;
import java.util.Iterator;
import javax.imageio.ImageIO;

/**
 * This tests if MTOM attachments are inlined.
 * Creating Dispatch<DataSource> as Dispatch<SOAPMessage> seems to inline
 * response attachments. Checks if the response message's envelope is
 * encoded with utf-16 since request message's envelope is encoded with utf-16
 *
 * @author Jitendra Kotamraju
 */
public class EncodingTest extends TestCase {
    String helloPortAddress;

    public EncodingTest(String name) throws Exception {
        super(name);
    }

    protected void setUp() throws Exception {
        Hello proxy = new HelloService().getHelloPort();
        helloPortAddress = (String) ((BindingProvider)proxy).getRequestContext().get(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    }

    public void testMtomEncoding() throws Exception {
        QName serviceName = new QName("http://example.org/mtom", "Hello");
        QName portName = new QName("http://example.org/mtom", "HelloPort");
        Service service = Service.create(serviceName);
        service.addPort(portName, HTTPBinding.HTTP_BINDING, helloPortAddress);
        Dispatch<DataSource> disp = service.createDispatch(portName, DataSource.class, Mode.MESSAGE);

        // Create a request SOAPMessage
        SOAPMessage soap = getSOAPMessage();
        DataSource ds = getDataSource(soap);
        ds = disp.invoke(ds);

        // Create a SOAPMessage from response
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", ds.getContentType());
        soap = MessageFactory.newInstance().createMessage(headers, ds.getInputStream());

        // Verify the response message envelope'ss encoding
        String ct = soap.getSOAPPart().getMimeHeader("Content-Type")[0].toLowerCase();
        assertTrue(ct.contains("utf-16"));

        // verify S:Body
        SOAPBody body = soap.getSOAPBody();
        Iterator<SOAPElement> it = body.getChildElements();
        SOAPElement elem = (SOAPElement)it.next();
        assertEquals(new QName("http://example.org/mtom", "MTOMInOutResponse"), elem.getElementQName());
        Iterator<SOAPElement> docit = elem.getChildElements();

        // verify <doc1> contains xop:Include
        elem = docit.next();
        assertEquals(new QName("http://example.org/mtom", "doc1"), elem.getElementQName());
        Iterator<SOAPElement> xopit = elem.getChildElements();
        elem = xopit.next();
        assertEquals(new QName("http://www.w3.org/2004/08/xop/include", "Include"), elem.getElementQName());
        String href = elem.getAttributeValue(new QName("", "href"));
        assertEquals("cid:", href.substring(0, 4));

        // verify <doc2> contains xop:Include
        elem = docit.next();
        assertEquals(new QName("http://example.org/mtom", "doc2"), elem.getElementQName());
        xopit = elem.getChildElements();
        elem = xopit.next();
        assertEquals(new QName("http://www.w3.org/2004/08/xop/include", "Include"), elem.getElementQName());
        href = elem.getAttributeValue(new QName("", "href"));
        assertEquals("cid:", href.substring(0, 4));

        // verify <doc3> contains xop:Include
        elem = docit.next();
        assertEquals(new QName("http://example.org/mtom", "doc3"), elem.getElementQName());
        xopit = elem.getChildElements();
        elem = xopit.next();
        assertEquals(new QName("http://www.w3.org/2004/08/xop/include", "Include"), elem.getElementQName());
        href = elem.getAttributeValue(new QName("", "href"));
        assertEquals("cid:", href.substring(0, 4));

        // verify <doc4> contains xop:Include
        elem = docit.next();
        assertEquals(new QName("http://example.org/mtom", "doc4"), elem.getElementQName());
        xopit = elem.getChildElements();
        elem = xopit.next();
        assertEquals(new QName("http://www.w3.org/2004/08/xop/include", "Include"), elem.getElementQName());
        href = elem.getAttributeValue(new QName("", "href"));
        assertEquals("cid:", href.substring(0, 4));

        // verify <doc5> contains xop:Include
        elem = docit.next();
        assertEquals(new QName("http://example.org/mtom", "doc5"), elem.getElementQName());
        xopit = elem.getChildElements();
        elem = xopit.next();
        assertEquals(new QName("http://www.w3.org/2004/08/xop/include", "Include"), elem.getElementQName());
        href = elem.getAttributeValue(new QName("", "href"));
        assertEquals("cid:", href.substring(0, 4));

        // Validate the number of attachments
        assertEquals(5, soap.countAttachments());

        Iterator<AttachmentPart> ait = soap.getAttachments();

        // Validate attach.txt attachement
        AttachmentPart at = ait.next();
        validate(resource("attach.txt"), at.getRawContent());

        // Validate attach.html attachment
        at = ait.next();
        validate(resource("attach.html"), at.getRawContent());

        // Validate attach.xml attachment
        at = ait.next();
        validate(resource("attach.xml"), at.getRawContent());

        // Validate attach.jpeg attachment
        at = ait.next();
        Image img = ImageIO.read(at.getRawContent());
        assertNotNull(img);

        // Validate attach2.jpeg attachment
        at = ait.next();
        img = ImageIO.read(at.getRawContent());
        assertNotNull(img);
    }

    private InputStream resource(String file) {
        return getClass().getClassLoader().getResourceAsStream(file);
    }

    private SOAPMessage getSOAPMessage() throws Exception {
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");

        InputStream in = resource("raw.envelope");
        SOAPMessage msg = MessageFactory.newInstance().createMessage(headers, in);

        AttachmentPart doc1 = msg.createAttachmentPart(getDataHandler("attach.txt", "text/plain"));
        doc1.setContentId("6f43c576-1c18-441a-8aaa-a455dc87a656@example.jaxws.sun.com");
        msg.addAttachmentPart(doc1);

        AttachmentPart doc2 = msg.createAttachmentPart(getDataHandler("attach.html", "text/html"));
        doc2.setContentId("d04800fe-ec4b-4312-97f3-eb84172ac6d9@example.jaxws.sun.com");
        msg.addAttachmentPart(doc2);

        AttachmentPart doc3 = msg.createAttachmentPart(getDataHandler("attach.xml", "text/xml"));
        doc3.setContentId("0e558b51-7c30-4fe5-b65a-d01ac86129e5@example.jaxws.sun.com");
        msg.addAttachmentPart(doc3);

        AttachmentPart doc4 = msg.createAttachmentPart(getDataHandler("attach.jpeg", "image/jpg"));
        doc4.setContentId("72d1efa4-e45d-4cf0-90b5-56d111a4cb03@example.jaxws.sun.com");
        doc4.setContentType("image/jpeg");
        msg.addAttachmentPart(doc4);

        AttachmentPart doc5 = msg.createAttachmentPart(getDataHandler("attach2.jpeg", "image/jpg"));
        doc5.setContentType("image/jpeg");
        doc5.setContentId("e622a8c0-b994-424e-8343-f15b18bb2fa2@example.jaxws.sun.com");
        msg.addAttachmentPart(doc5);

        MimeHeaders hdrs = msg.getMimeHeaders();
        String boundary = "BOUNDARY_123456789_BOUNDARY";
        String ct = "multipart/related;type=\"application/xop+xml\";boundary=" + boundary + ";start-info=\"text/xml\"";
        hdrs.setHeader("Content-Type", ct);

        msg.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "UTF-16");
        msg.saveChanges();
        return msg;
    }

    private void validate(InputStream exp, InputStream got) throws Exception {
        int ch;
        while ((ch = exp.read()) != -1) {
            assertEquals(ch, got.read());
        }
        assertEquals(-1, got.read());
        exp.close();
        got.close();
    }

    private DataHandler getDataHandler(final String file, final String ct) throws Exception {
        return new DataHandler(new DataSource() {
            public String getContentType() {
                return ct;
            }

            public InputStream getInputStream() {
                return resource(file);
            }

            public String getName() {
                return null;
            }

            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException();
            }
        });
    }

    private DataSource getDataSource(final SOAPMessage msg) throws Exception {
        return new DataSource() {
            public String getContentType() {
                return msg.getMimeHeaders().getHeader("Content-Type")[0];
            }

            public InputStream getInputStream() throws IOException {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    msg.writeTo(baos);
                } catch (SOAPException ioe) {
                    throw new WebServiceException(ioe);
                }
                return new ByteArrayInputStream(baos.toByteArray());
            }

            public String getName() {
                return null;
            }

            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException();
            }
        };
    }

}