/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
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

package mtom.issue_671.server;

import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.*;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.http.HTTPBinding;
import java.io.*;

/**
 * @author Rama Pulavarthi
 */
@WebServiceProvider(targetNamespace="http://example.org/mtom", portName="HelloProviderPort", serviceName="HelloProviderService")
@ServiceMode(value= Mode.MESSAGE)
@BindingType(value=HTTPBinding.HTTP_BINDING)
public class HelloProviderImpl implements Provider<DataSource> {
    String attachment_cid = "62c44a33-72ba-4bef-9897-00586d5ab25d@example.jaxws.sun.com";
    String env = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><XDocOut xmlns=\"http://example.org/mtom\"><doc1><xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:" +
                attachment_cid +"\"/></doc1></XDocOut></S:Body></S:Envelope>";

    /**
     * Verfiy that the request is a MTOM message.
     * @param dataSource
     * @return
     */
    public DataSource invoke(DataSource dataSource) {

        System.out.println("**** Received in Provider Impl ******");

        try {
			final MimeMultipart multipart = new MimeMultipart(dataSource, null);
			MimeBodyPart bodyPart = (MimeBodyPart)multipart.getBodyPart(0);
			InputStream is = bodyPart.getInputStream();
			if(!bodyPart.getContentType().contains("application/xop+xml"))
                throw new WebServiceException("The Request ContentType is not application/xop+xml");

            Source source = bodyPart.getContentType().indexOf("xml") > 0 ?
                            new StreamSource(is) : new org.jvnet.fastinfoset.FastInfosetSource(is);
			SOAPMessage msg = MessageFactory.newInstance().createMessage();
			msg.getSOAPPart().setContent(source);

            SOAPElement elem = (SOAPElement) msg.getSOAPBody().getChildElements().next();
            assertEquals(new QName("http://example.org/mtom", "XDocIn"), elem.getElementQName());
            SOAPElement docit = (SOAPElement) elem.getChildElements().next();
            assertEquals(new QName("http://example.org/mtom", "doc1"), docit.getElementQName());

            // verify <doc1> contains xop:Include
            SOAPElement xopit = (SOAPElement) docit.getChildElements().next();
            assertEquals(new QName("http://www.w3.org/2004/08/xop/include", "Include"), xopit.getElementQName());
            String href = xopit.getAttributeValue(new QName("", "href"));
            assertEquals("cid:", href.substring(0,4));
            String cid = href.substring(4);

            MimeBodyPart attachmentPart = (MimeBodyPart)multipart.getBodyPart("<"+cid+">");
			assertNotNull(attachmentPart,"No Attachment received");
            return getDataSource(getXOPedSOAPMessage(attachmentPart.getInputStream()));

        } catch(Exception e) {
            e.printStackTrace();
            throw new WebServiceException("Provider endpoint failed", e);
        }
    }

    private void assertEquals(Object exp, Object got) {
        if(!exp.equals(got))
            throw new WebServiceException("Expecting:" + exp +",Got:"+got);

    }

    private void assertNotNull(Object o, String errmessage) {
        if(o == null)
            throw new WebServiceException(errmessage);

    }

    private DataHandler getDataHandler(final InputStream is, final String contentType) throws Exception {
        return new DataHandler(new DataSource() {
            public String getContentType() {
                return contentType;
            }

            public InputStream getInputStream() {
                return is;
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
                } catch (SOAPException e) {
                    throw new RuntimeException(e);
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

    private SOAPMessage getXOPedSOAPMessage(InputStream attachment) throws Exception {
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
        InputStream is = new ByteArrayInputStream(env.getBytes());
        SOAPMessage msg = MessageFactory.newInstance().createMessage(headers, is);

        AttachmentPart doc1 = msg.createAttachmentPart(getDataHandler(attachment,"text/xml"));
        doc1.setContentId(attachment_cid);
        msg.addAttachmentPart(doc1);

        MimeHeaders hdrs = msg.getMimeHeaders();
        String boundary = "BOUNDARY_123456789_BOUNDARY";
        String ct =
                "multipart/related;type=\"application/xop+xml\";boundary=" + boundary + ";start-info=\"text/xml\"";
        hdrs.setHeader("Content-Type", ct);

        msg.saveChanges();
        return msg;
    }
}
