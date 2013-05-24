/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2013 Oracle and/or its affiliates. All rights reserved.
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

package server.provider.xmlbind_source.client;

import javax.xml.ws.Response;
import testutil.ClientServerTestUtil;

import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.*;
import java.awt.Toolkit;
import java.awt.Image;
import java.util.Iterator;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPBinding;
import junit.framework.TestCase;
import javax.xml.bind.JAXBContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPBinding;
import java.util.concurrent.Future;
import java.net.URI;

import org.w3c.dom.Node;

/**
 *
 * @author WS Development Team
 */
public class HelloLiteralTest extends TestCase {

    private String start = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>";
    private String end = "</soapenv:Body></soapenv:Envelope>";
    private String helloSM= "<tns:Hello xmlns:tns=\"urn:test:types\"><argument>Dispatch</argument><extra>Test</extra></tns:Hello>";

    private QName serviceQName = new QName("urn:test", "Hello");
    private QName portQName = new QName("urn:test", "HelloPort");;
    private String endpointAddress;
    private Service service;
    private Dispatch<Source> dispatchSource;
    
    private ClientServerTestUtil util = new ClientServerTestUtil();
    
    private static final JAXBContext jaxbContext = createJAXBContext();
    
    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            HelloLiteralTest test = new HelloLiteralTest("HelloLiteralTest");
            // test.testHelloRequestResponseSource();
            test.testHelloWithHandlerJAXB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public JAXBContext getJAXBContext(){
        return jaxbContext;
    }
    
    private static javax.xml.bind.JAXBContext createJAXBContext(){
        try {
            return JAXBContext.newInstance(ObjectFactory.class);
        } catch(javax.xml.bind.JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }
    
    public HelloLiteralTest(String name) {
        super(name);

        // we'll fix the test harness correctly later,
        // so that test code won't have to hard code any endpoint address nor transport,
        // but for now let's just support local and HTTP to make unit tests happier.
        // this is not a good code, but it's just a bandaid solutino that works for now.
        if(ClientServerTestUtil.useLocal())
            endpointAddress = "local://"+new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\','/')+'?'+portQName.getLocalPart();
        else
            endpointAddress = "http://localhost:8080/jaxrpc-provider_tests_xmlbind_source/hello";
    }
    
    Service createService () {
        Service service = Service.create(serviceQName);
        
        return service;
    }

    private Dispatch<Source> createDispatchSource() {
        try {
            service = createService();
            service.addPort(portQName,
                HTTPBinding.HTTP_BINDING, setTransport(endpointAddress));
            dispatchSource = service.createDispatch(portQName, Source.class, Service.Mode.PAYLOAD);            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dispatchSource;
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
    public void testHelloRequestResponseSource() throws Exception {
        Dispatch<Source> dispatch = createDispatchSource();
        Source source = getSource();
        Source result = dispatch.invoke(source);

        HelloResponse response = (HelloResponse)jaxbContext.createUnmarshaller().unmarshal(result);            
        
        assertTrue("foo".equals(response.getArgument()));
        assertTrue("bar".equals(response.getExtra()));
    }

    public void testHelloRequestResponseSourceAsync() throws Exception {
        Dispatch<Source> dispatch = createDispatchSource();
        Source source = getSource();
        SourceAsyncHandler asyncHandler = new SourceAsyncHandler();
        Future result = dispatch.invokeAsync(source, asyncHandler);
        try {
            System.out.println("Response retured to test Thread " + Thread.currentThread().getId());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        while (!result.isDone()) {
            int i = 0;
            while (i < 5000) {
                i++;
            }
        }
        assertTrue(result.isDone());
    }

    public void testHelloRequestResponseSourceAsyncPoll() throws Exception {
        Dispatch<Source> dispatch = createDispatchSource();
        Source source = getSource();
        Response result = dispatch.invokeAsync(source);
        Object obj = result.get();
        assertTrue(obj instanceof Source);
        
        HelloResponse response = (HelloResponse)jaxbContext.createUnmarshaller().unmarshal((Source) obj);
        
        assertTrue("foo".equals(response.getArgument()));
        assertTrue("bar".equals(response.getExtra()));
    }

    public void testHelloRequestResponseSourceOneway() throws Exception {
        Dispatch<Source> dispatch = createDispatchSource();
        Source source = getSource();
        dispatch.invokeOneWay(source);
    }

    
    /*
     * Version of testHelloRequestResponseSource with a handler
     * added dynamically. Handler will change "foo" to
     * "hellofromhandler." Service will respond to that argument
     * with "hellotohandler." Handler then checks incoming message
     * for that text and changes to "handlerworks" to show that handler
     * worked on incoming and outgoing message.
     */
    public void testHelloWithHandlerJAXB() throws Exception {
        Dispatch<Source> dispatch = createDispatchSource();
        
        TestLogicalHandler handler = new TestLogicalHandler();
        handler.setHandleMode(TestLogicalHandler.HandleMode.JAXB);
        ClientServerTestUtil.addHandlerToBinding(handler, dispatch);
        
        Source source = getSource();
        Source result = dispatch.invoke(source);
        assertTrue(result instanceof Source);
        
        HelloResponse response = (HelloResponse)jaxbContext.createUnmarshaller().unmarshal(result);
        
        assertTrue("handlerworks".equals(response.getArgument()));
        assertTrue("bar".equals(response.getExtra()));
    }
    
    /*
     * Version of testHelloRequestResponseSource with a handler
     * added dynamically. Handler will change "Dispatch" to
     * "hellofromhandler." Service will respond to that argument
     * with "hellotohandler." Handler then checks incoming message
     * for that text and changes to "handlerworks" to show that handler
     * worked on incoming and outgoing message.
     */
    public void testHelloWithHandlerSource() throws Exception {
        Dispatch<Source> dispatch = createDispatchSource();
        
        TestLogicalHandler handler = new TestLogicalHandler();
        handler.setHandleMode(TestLogicalHandler.HandleMode.SOURCE);
        ClientServerTestUtil.addHandlerToBinding(handler, dispatch);
        
        Source source = getSource();
        Source result = dispatch.invoke(source);
        assertTrue(result instanceof Source);
        
        HelloResponse response = null;
        if (result instanceof StreamSource || 
                result instanceof org.jvnet.fastinfoset.FastInfosetResult) {
            response = (HelloResponse) jaxbContext.createUnmarshaller().unmarshal(result);
        } else {
            response = getResponseFromSource(result);
        }
        
        assertTrue("handlerworks".equals(response.getArgument()));
        assertTrue("bar".equals(response.getExtra()));
    }
    
    private Source getSource() throws Exception {
        byte[] bytes = helloSM.getBytes();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        return new StreamSource(bis);
    }
    
    private HelloResponse getResponseFromSource(Source source)
        throws Exception {
        
        Transformer xFormer =
            TransformerFactory.newInstance().newTransformer();
        xFormer.setOutputProperty("omit-xml-declaration", "yes");
        DOMResult result = new DOMResult();
        xFormer.transform(source, result);
        Node documentNode = result.getNode();
//        Node envNode = documentNode.getFirstChild();
//        Node requestResponseNode = envNode.getFirstChild().getFirstChild();
        HelloResponse response = (HelloResponse) jaxbContext.
            createUnmarshaller().unmarshal(documentNode);
        return response;
    }
    
}
