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

package server.provider.xmlbind_datasource.server;

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
import java.util.*;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.annotation.Resource;

@WebServiceProvider
@ServiceMode (value=Service.Mode.MESSAGE)
public class HelloImpl implements Provider<DataSource> {
    
    @Resource
    WebServiceContext wsContext;

    private static final JAXBContext jaxbContext = createJAXBContext ();
    private int bodyIndex;
    
    public javax.xml.bind.JAXBContext getJAXBContext (){
        return jaxbContext;
    }
    
    private static javax.xml.bind.JAXBContext createJAXBContext (){
        try{
            return JAXBContext.newInstance (ObjectFactory.class);
        }catch(javax.xml.bind.JAXBException e){
            throw new WebServiceException (e.getMessage (), e);
        }
    }
    
    private byte[] sendSource () {
        System.out.println ("**** sendSource ******");
        String begin = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>";
        String end = "</soapenv:Body></soapenv:Envelope>";
        
        String[] body  = {
            "<HelloResponse xmlns=\"urn:test:types\"><argument xmlns=\"\">foo</argument><extra xmlns=\"\">bar</extra></HelloResponse>",
                "<ans1:HelloResponse xmlns:ans1=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></ans1:HelloResponse>"
        };
        int i = (++bodyIndex)%body.length;
        String content = begin+body[i]+end;
        return content.getBytes ();
    }
    
    private void recvBean (Source source) throws Exception {
        System.out.println ("**** recvBean ******");
        Hello hello = (Hello)jaxbContext.createUnmarshaller().unmarshal(source);
        if (!"ArgSetByHandler".equals(hello.getArgument())) {
            throw new WebServiceException("hello.getArgument(): expected \"Dispatch\", got \"" + hello.getArgument() + "\"");
        }
        if (!"ExtraSetByHandler".equals(hello.getExtra())) {
            throw new WebServiceException("hello.getArgument(): expected \"Test\", got \"" + hello.getExtra() + "\"");
        }
    }
    
    public DataSource invoke (DataSource dataSource) {
        System.out.println("***** invoke(DataSource) *******");

		// Checks handler set properties and updates MessageContext
		checkAndUpdateMsgCtxt();
        
        try {
            final MimeMultipart multipart = new MimeMultipart(dataSource, null);

			// Verify no of MIME parts in the datasource
			int no = multipart.getCount();
			if (no != 2) {
				throw new WebServiceException("expected=2 MIME parts Got="+no);
			}
			
            MimeBodyPart bodyPart = (MimeBodyPart)multipart.getBodyPart (0);
            
            // Create source according to type
            String contentType = bodyPart.getContentType();
            Source source = contentType.equals("application/fastinfoset") ?
                new org.jvnet.fastinfoset.FastInfosetSource(bodyPart.getInputStream())
                : new StreamSource(bodyPart.getInputStream());
            
            SOAPMessage msg = MessageFactory.newInstance ().createMessage ();
            msg.getSOAPPart ().setContent (source);
            Node node = msg.getSOAPBody ().getFirstChild ();
            recvBean (new DOMSource(node));
            
            final MimeMultipart resp = new MimeMultipart ("related");
            resp.getContentType().setParameter("type", "text/xml");     // type is mandatory
            InternetHeaders hdrs = new InternetHeaders ();
            hdrs.setHeader ("Content-Type", "text/xml");
            byte[] buf = sendSource();
            MimeBodyPart body = new MimeBodyPart (hdrs, buf, buf.length);
            resp.addBodyPart (body);
            
            return new DataSource () {
                public InputStream getInputStream () {
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream ();
                        resp.writeTo (bos);
                        bos.close ();
                        return new ByteArrayInputStream (bos.toByteArray ());
                    } catch(Exception ioe) {
                        throw new WebServiceException ("Cannot give DataSource", ioe);
                    }
                }
                
                public OutputStream getOutputStream () {
                    return null;
                }
                
                public String getContentType () {
                    return resp.getContentType().toString();
                }
                
                public String getName () {
                    return "";
                }
            };

            
        } catch(Exception e) {
            e.printStackTrace ();
            throw new WebServiceException ("Provider endpoint failed", e);
        }
    }

	private void checkAndUpdateMsgCtxt() {
		// Get a property from context
		MessageContext ctxt = wsContext.getMessageContext();
		String gotProp = (String)ctxt.get("foo");
		if (!gotProp.equals("bar")) {
			System.out.println("foo property: expected=bar Got="+gotProp);
			throw new WebServiceException(
				"foo property: expected=bar Got="+gotProp);
		}

		// Modify the same property in the context
		ctxt.put("foo", "return-bar");

		// Set a property in the context
		ctxt.put("return-foo", "return-bar");
	}
}
