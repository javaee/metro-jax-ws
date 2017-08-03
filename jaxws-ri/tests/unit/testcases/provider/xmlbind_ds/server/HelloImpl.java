/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
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

package provider.xmlbind_ds.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.soap.*;
import org.w3c.dom.Node;
import javax.xml.transform.dom.DOMSource;
import javax.activation.DataSource;
import javax.xml.ws.ServiceMode;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.InternetHeaders;
import java.io.*;
import junit.framework.*;
import javax.xml.ws.BindingType;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.WebServiceProvider;

/**
 * @author Jitendra Kotamraju
 */
@WebServiceProvider
@ServiceMode(value=Service.Mode.MESSAGE)
@BindingType(value=HTTPBinding.HTTP_BINDING)
public class HelloImpl implements Provider<DataSource> {

    private static final JAXBContext jaxbContext = createJAXBContext();
    private int bodyIndex;

    public javax.xml.bind.JAXBContext getJAXBContext(){
        return jaxbContext;
    }
    
    private static javax.xml.bind.JAXBContext createJAXBContext(){
        try{
            return javax.xml.bind.JAXBContext.newInstance(ObjectFactory.class);
        }catch(javax.xml.bind.JAXBException e){
            throw new WebServiceException(e.getMessage(), e);
        }
    }

    private byte[] sendSource() {
        System.out.println("**** sendSource ******");
        String begin = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>";
		String end = "</soapenv:Body></soapenv:Envelope>";

        String[] body  = {
            "<HelloResponse xmlns=\"urn:test:types\"><argument xmlns=\"\">foo</argument><extra xmlns=\"\">bar</extra></HelloResponse>",
            "<ans1:HelloResponse xmlns:ans1=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></ans1:HelloResponse>"
        };
        int i = (++bodyIndex)%body.length;
		String content = begin+body[i]+end;
		return content.getBytes();
    }

    private void recvBean(Source source) throws Exception {
        System.out.println("**** recvBean ******");
        Hello_Type hello = (Hello_Type)jaxbContext.createUnmarshaller().unmarshal(source);
        if ("Dispatch".equals(hello.getArgument())) {
            throw new WebServiceException("hello.getArgument() got ="+
				hello.getArgument());
		}
        if ("test".equals(hello.getExtra())) {
            throw new WebServiceException("hello.getExtra() got ="+
				hello.getExtra());
		}
    }

    public DataSource invoke(DataSource dataSource) {

        System.out.println("**** Received in Provider Impl ******");

        try {
			final MimeMultipart multipart = new MimeMultipart(dataSource, null);
			MimeBodyPart bodyPart = (MimeBodyPart)multipart.getBodyPart(0);
			InputStream is = bodyPart.getInputStream();
			Source source = bodyPart.getContentType().indexOf("xml") > 0 ? 
                            new StreamSource(is) : new org.jvnet.fastinfoset.FastInfosetSource(is);
			SOAPMessage msg = MessageFactory.newInstance().createMessage();
			msg.getSOAPPart().setContent(source);
			Node node = msg.getSOAPBody().getFirstChild();
			recvBean(new DOMSource(node));

			final MimeMultipart resp = new MimeMultipart("related");
                        resp.getContentType().setParameter("type", "text/xml");
			InternetHeaders hdrs = new InternetHeaders();
			hdrs.setHeader("Content-Type", "text/xml");
            byte[] bytes = sendSource();
			MimeBodyPart body = new MimeBodyPart(hdrs, bytes, bytes.length);
			resp.addBodyPart(body);

            return new DataSource() {
                public InputStream getInputStream() {
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        resp.writeTo(bos);
                        bos.close();
                        return new ByteArrayInputStream(bos.toByteArray());
                    } catch(Exception ioe) {
                        throw new WebServiceException("Cannot give DataSource", ioe);
                    }
                }

                public OutputStream getOutputStream() {
                    return null;
                }

                public String getContentType() {
                    return resp.getContentType().toString();
                }

                public String getName() {
                    return "";
                }
            };

        } catch(Exception e) {
            e.printStackTrace();
            throw new WebServiceException("Provider endpoint failed", e);
        }
    }
}
