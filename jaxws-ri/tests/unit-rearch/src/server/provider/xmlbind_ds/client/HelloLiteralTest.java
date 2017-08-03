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

package server.provider.xmlbind_ds.client;

import junit.framework.*;
import testutil.ClientServerTestUtil;
import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import java.io.PrintStream;
import javax.xml.ws.*;
import javax.xml.ws.soap.*;
import javax.xml.ws.http.*;
import javax.xml.soap.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.awt.Toolkit;
import java.awt.Image;
import java.net.URI;
import javax.activation.DataSource;

/**
 *
 * @author JAX-RPC RI Development Team
 */
public class HelloLiteralTest extends TestCase {

    private String helloSM= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><tns:Hello xmlns:tns=\"urn:test:types\"><argument>Dispatch </argument><extra>Test </extra></tns:Hello></soapenv:Body></soapenv:Envelope>";

    private QName serviceQName = new QName("urn:test", "Hello");
    private QName portQName = new QName("urn:test", "HelloPort");;
    private String endpointAddress;

    private Service service;
    private Dispatch<SOAPMessage> dispatch;

    public HelloLiteralTest(String name) {
        super(name);

        if (ClientServerTestUtil.useLocal())
            endpointAddress = "local://" + new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\', '/') + '?' + portQName.getLocalPart();
        else
            endpointAddress = "http://localhost:8080/jaxrpc-provider_tests_xmlbind_ds/hello";
    }
    protected String setTransport(String endpoint) {
           try {

               if (ClientServerTestUtil.useLocal()) {
                  URI uri = new URI(endpoint);
                  return uri.resolve(new URI("local", uri.getPath(), uri.getFragment())).toString();
               }

           } catch (Exception ex) {
               ex.printStackTrace();
           }
        return endpoint;
       }
    private Dispatch<SOAPMessage> createDispatchSOAPMessage() throws Exception {
        Service service = Service.create(serviceQName);
        service.addPort(portQName, SOAPBinding.SOAP11HTTP_BINDING, setTransport(endpointAddress));
        dispatch = service.createDispatch(portQName, SOAPMessage.class,
            Service.Mode.MESSAGE);
        ClientServerTestUtil util = new ClientServerTestUtil();
        return dispatch;
    }

    public void testDataSource() throws Exception {
        Service service = Service.create(serviceQName);
        service.addPort(portQName, HTTPBinding.HTTP_BINDING, endpointAddress);
        Dispatch<DataSource> dispatch = service.createDispatch(portQName, DataSource.class, Service.Mode.MESSAGE);
        final SOAPMessage message = getSOAPMessage();
        DataSource ds = new DataSource() {
            public InputStream getInputStream() {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     message.writeTo(bos);
                    bos.close();
                    return new ByteArrayInputStream(bos.toByteArray());
                } catch(Exception ioe) {
                    throw new RuntimeException(ioe);
                }
            }

            public OutputStream getOutputStream() {
                return null;
            }

            public String getContentType() {
                return message.getMimeHeaders().getHeader("Content-Type")[0];
            }

            public String getName() {
                return "";
            }
        };

        DataSource result = dispatch.invoke(ds);
    }

    public void testHelloRequestResponseSOAPMessage() throws Exception {
        Dispatch<SOAPMessage> dispatch = createDispatchSOAPMessage();
        SOAPMessage message = getSOAPMessage();
        SOAPMessage result = dispatch.invoke(message);
        assertTrue(result instanceof SOAPMessage);
    }

    private SOAPMessage getSOAPMessage() throws Exception {
        byte[] bytes = helloSM.getBytes();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        message.getSOAPPart().setContent(new StreamSource(bis));

        String userDir = System.getProperty("user.dir");
        String sepChar = System.getProperty("file.separator");
        String imageFile = userDir+sepChar
            +"src/server/provider/xmlbind_ds/common_resources/WEB-INF/"
            +"java.jpg";

        // Attach Image        
        Image img = Toolkit.getDefaultToolkit().getImage(imageFile);
        AttachmentPart ap = message.createAttachmentPart(img, "image/jpeg");
        message.addAttachmentPart(ap);

        message.saveChanges();
        return message;
    }

}
