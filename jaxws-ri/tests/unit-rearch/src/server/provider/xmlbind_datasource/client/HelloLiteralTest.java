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

package server.provider.xmlbind_datasource.client;

import junit.framework.TestCase;
import org.w3c.dom.Node;
import testutil.ClientServerTestUtil;

import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPBinding;
import java.awt.*;
import java.io.*;
import java.net.URI;

/**
 *
 * @author JAX-RPC RI Development Team
 */
public class HelloLiteralTest extends TestCase {

    private String helloSM= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><tns:Hello xmlns:tns=\"urn:test:types\"><argument>Dispatch</argument><extra>Test</extra></tns:Hello></soapenv:Body></soapenv:Envelope>";

    private QName serviceQName = new QName("urn:test", "Hello");
    private QName portQName = new QName("urn:test", "HelloPort");;
    private String endpointAddress;

    private Service service;
    private Dispatch<DataSource> dispatchDataSource;
    private ClientServerTestUtil util = new ClientServerTestUtil();
    
    private static final JAXBContext jaxbContext = createJAXBContext();
    
    
    public static void main(String[] args) {
        try {
            System.setProperty("uselocal", "true");
            HelloLiteralTest test = new HelloLiteralTest("HelloLiteralTest");
            test.testHelloRequestResponseDataSource();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public javax.xml.bind.JAXBContext getJAXBContext(){
        return jaxbContext;
    }
    
    private static javax.xml.bind.JAXBContext createJAXBContext(){
        try{
            return JAXBContext.newInstance(ObjectFactory.class);
        }catch(javax.xml.bind.JAXBException e){
            throw new WebServiceException(e.getMessage(), e);
        }
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

    public HelloLiteralTest(String name) {
        super(name);

        if(ClientServerTestUtil.useLocal())
            endpointAddress = "local://"+new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\','/')+'?'+portQName.getLocalPart();
        else endpointAddress =
		   "http://localhost:8080/jaxrpc-provider_tests_xmlbind_datasource/hello";

    }
    
    Service createService () {
        Service service = Service.create(serviceQName);
        
        return service;
    }

  
    private Dispatch<DataSource> createDispatchDataSource() throws Exception {
        service = createService();
        service.addPort(portQName, HTTPBinding.HTTP_BINDING, setTransport(endpointAddress));
        dispatchDataSource = service.createDispatch(portQName, DataSource.class,
            Service.Mode.MESSAGE);
        //util.setTransport(dispatchDataSource, null);
        return dispatchDataSource;
    }    

    public void testHelloRequestResponseDataSource() throws Exception {
        Dispatch<DataSource> dispatch = createDispatchDataSource();
        DataSource ds = getDataSource();
        DataSource result = null;
        try {
            result = dispatch.invoke (ds);
        } catch (Exception ex){
            throw ex;
        }
        assertTrue (result instanceof DataSource);
        // verify the results

        // Create source according to type
        String contentType = result.getContentType();
        Source source = contentType.equals("application/fastinfoset") ?
            new org.jvnet.fastinfoset.FastInfosetSource(result.getInputStream())
            : new StreamSource(result.getInputStream());

        SOAPMessage msg = MessageFactory.newInstance().createMessage();
        msg.getSOAPPart().setContent(source);
        Node node = msg.getSOAPBody().getFirstChild();
        HelloResponse response = (HelloResponse)jaxbContext.createUnmarshaller().unmarshal(node);

        assertTrue("foo".equals(response.getArgument()));
        assertTrue("bar".equals(response.getExtra()));
    }
    
    private SOAPMessage getSOAPMessage() throws Exception {
        byte[] bytes = helloSM.getBytes();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        message.getSOAPPart().setContent(new StreamSource(bis));
        AttachmentPart ap = message.createAttachmentPart(getImage("java.jpg"), "image/jpeg");
        message.addAttachmentPart(ap);

        message.saveChanges();
        return message;
    }

     private Image getImage(String imageName) throws Exception {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(imageName);
        return javax.imageio.ImageIO.read(is);
    }
    
    private DataSource getDataSource() throws Exception {
        final SOAPMessage sm = getSOAPMessage();
        
        return new DataSource() {
            public InputStream getInputStream() {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    sm.writeTo(baos);

                    ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());

                    return bis;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }
            
            public OutputStream getOutputStream() {
                return null;
            }
            
            public String getContentType() {
                return sm.getMimeHeaders().getHeader("Content-Type")[0];
            }
            
            public String getName() {
                return "";
            }
        };
    }
    

}
