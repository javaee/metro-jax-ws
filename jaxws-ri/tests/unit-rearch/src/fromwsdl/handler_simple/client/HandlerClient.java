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

package fromwsdl.handler_simple.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;

import javax.xml.namespace.QName;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.Service;
import static javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;

import fromwsdl.handler_simple.common.SOAPTestHandler;

import junit.framework.*;
import testutil.ClientServerTestUtil;

import org.w3c.dom.Node;

/*
 * These tests are for basic handler cases in many different
 * settings. They test the runtime around the handler mostly,
 * instead of testing the behavior of the handlers themselves.
 *
 * The detailed tests of handler execution are in fromwsdl/handler.
 */
public class HandlerClient extends TestCase {

    /*
     * main() method used during debugging
     */
    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            HandlerClient test = new HandlerClient("HandlerClient");
//            test.testSOAP12Binding1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HandlerClient(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(fromwsdl.handler_simple.client.HandlerClient.class);
        return suite;
    }

    private Hello_Service createService() throws Exception {
        return new Hello_Service();
    }

    // util method when the service isn't needed
    private Hello createStub() throws Exception {
        return createStub(createService());
    }

    private Hello createStub(Hello_Service service) throws Exception {
        Hello stub = service.getHelloPort();
        ClientServerTestUtil.setTransport(stub);
	    return stub;
    }

    private Hello12 create12Stub(Hello_Service service) throws Exception {
        Hello12 stub = service.getHelloPort12();
        ClientServerTestUtil.setTransport(stub);
	    return stub;
    }
    
    private String getEndpointAddress(String defaultAddress) {
        if(ClientServerTestUtil.useLocal())
            return "local://"+new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\','/');
        else
            return defaultAddress;
    }

    /* tests below here */

    /*
     * Tests that handler specified in config file is used.
     *
     * Each handler adds one to the int during request and response,
     * so we should receive the original number plus 4 if the
     * handlers are working.
     */
    public void testSimple() throws Exception {
        Hello stub = createStub();

        int x = 1;
        int diff = 4; // 2 per handler invoked

        int y = stub.hello(x);
        assertEquals(x+diff, y);
    }

    /*
     * Test tries to add a handler programmatically after clearing
     * handlers out of the service. Adds handler to binding. Uses
     * an empty handler resolver for clearing the service.
     */
    public void testDynamic1() throws Exception {
        Hello_Service service = createService();
        service.setHandlerResolver(new HandlerResolver() {
            public List<Handler> getHandlerChain(PortInfo info) {
                return new ArrayList<Handler>();
            }
        });

        Hello stub = createStub(service);

        int x = 1;
        int diff = 2; // 2 per handler invoked

        int y = stub.hello(x);
        assertTrue(y == x+diff);

        // now add client handler
        List<Handler> handlerList = new ArrayList<Handler>();
        handlerList.add(new SOAPTestHandler());
        Binding binding = ((BindingProvider) stub).getBinding();
        binding.setHandlerChain(handlerList);
        
        // test again
        diff = 4;
        y = stub.hello(x);
        assertTrue(y == x+diff);

    }

    /*
     * Test tries to add a handler programmatically after clearing
     * handlers out of the service. Adds handler using HandlerResolver.
     * Uses a null HandlerResolver to clear the service.
     */
    public void testDynamic2() throws Exception {
        Hello_Service service = createService();
        service.setHandlerResolver(null);

        Hello stub = createStub(service);

        int x = 1;
        int diff = 2; // 2 per handler invoked

        int y = stub.hello(x);
        assertEquals(x+diff, y);

        // now add client handler
        service.setHandlerResolver(new HandlerResolver() {
           public List<Handler> getHandlerChain(PortInfo info) {
               List list = new ArrayList<Handler>();
               list.add(new SOAPTestHandler());
               return list;
           } 
        });
        stub = createStub(service);
        
        // test again
        diff = 4;
        y = stub.hello(x);
        assertTrue(y == x+diff);

    }

    /*
     * Test removes the static handler and adds a logical
     * handler that uses a Source to change the message.
     */
    public void testLogicalSource() throws Exception {
        Hello stub = createStub();
        Binding binding = ((BindingProvider) stub).getBinding();
        
        LogicalTestHandler handler = new LogicalTestHandler();
        handler.setHandleMode(LogicalTestHandler.HandleMode.SOURCE);
        List<Handler> handlerChain = new ArrayList<Handler>();
        handlerChain.add(handler);
        binding.setHandlerChain(handlerChain);
        

        int x = 1;
        int diff = 4; // 2 per handler invoked

        int y = stub.hello(x);
        assertEquals(x+diff, y); // x+4 with all handlers
    }
    
    /*
     * Test removes the static handler and adds a logical
     * handler that uses JAXB to change the message.
     */
    public void testLogicalJAXB() throws Exception {
        Hello stub = createStub();
        Binding binding = ((BindingProvider) stub).getBinding();
        
        LogicalTestHandler handler = new LogicalTestHandler();
        handler.setHandleMode(LogicalTestHandler.HandleMode.JAXB);
        List<Handler> handlerChain = new ArrayList<Handler>();
        handlerChain.add(handler);
        binding.setHandlerChain(handlerChain);
        

        int x = 1;
        int diff = 4; // 2 per handler invoked

        int y = stub.hello(x);
        assertEquals(x+diff, y); // x+4 with all handlers
    }
    
    /*
     * Test removes the static handler and adds a logical
     * handler that gets the source but does not change it.
     */
    public void testLogicalGetSourceOnly() throws Exception {
        Hello stub = createStub();
        Binding binding = ((BindingProvider) stub).getBinding();
        
        LogicalTestHandler handler = new LogicalTestHandler();
        handler.setHandleMode(LogicalTestHandler.HandleMode.SOURCE_NO_CHANGE);
        List<Handler> handlerChain = new ArrayList<Handler>();
        handlerChain.add(handler);
        binding.setHandlerChain(handlerChain);
        

        int x = 1;
        int diff = 2; // 2 per handler invoked

        int y = stub.hello(x);
        assertEquals(x+diff, y);
    }
    
    /*
     * Creates a Dispatch object with jaxb and tests that the
     * handler is called.
     */
    public void testDispatchJAXB() throws Exception {
        QName portQName = new QName("urn:test", "HelloPort");
        String endpointAddress = getEndpointAddress(
            "http://localhost:8080/jaxrpc-fromwsdl_handler_simple/hello");
        
        // create service with just qname -- no handlers in that case
        //Hello_Service service = createService();
        QName serviceQName = new QName("urn:test", "Hello");
        Service service = Service.create(serviceQName);

        service.addPort(portQName, SOAP11HTTP_BINDING, setTransport(endpointAddress));
        
        JAXBContext jaxbContext =
            JAXBContext.newInstance(ObjectFactory.class);
        Dispatch<Object> dispatch = service.createDispatch(portQName,
            jaxbContext, Service.Mode.PAYLOAD);
        //ClientServerTestUtil.setTransport(dispatch, null);
        int numHandlers = 0;
        assertEquals("Should be " + numHandlers +
            " handler(s) on dispatch object", numHandlers,
            dispatch.getBinding().getHandlerChain().size());
        
        int x = 1;
        int diff = 2; // 2 per handler
        
        Hello_Type hello = new Hello_Type();
        hello.setIntin(x);
        HelloResponse response = (HelloResponse) dispatch.invoke(hello);
        assertEquals(x+diff, response.getIntout());
        
        // add handler programatically
        ClientServerTestUtil.addHandlerToBinding(
            new SOAPTestHandler(), dispatch);
        diff = 4;
        response = (HelloResponse) dispatch.invoke(hello);
        assertEquals(x+diff, response.getIntout());
    }

    /*
     * Creates a Dispatch object with source and tests that
     * the handler is called. Test uses a SOAP handler.
     */
    public void testDispatchSourceSOAPHandler() throws Exception {
        String req = "<?xml version=\"1.0\" ?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><Hello xmlns=\"urn:test:types\"><intin>1</intin></Hello></soapenv:Body></soapenv:Envelope>";
        QName portQName = new QName("urn:test", "HelloPort");
        String endpointAddress =
            getEndpointAddress("http://localhost:8080/jaxrpc-fromwsdl_handler_simple/hello");
        
        // create service with just qname -- no handlers in that case
        QName serviceQName = new QName("urn:test", "Hello");
        Service service = Service.create(serviceQName);
        service.addPort(portQName, SOAP11HTTP_BINDING, setTransport(endpointAddress));
        
        Dispatch<Source> dispatch = service.createDispatch(portQName,
            Source.class, Service.Mode.MESSAGE);
        //ClientServerTestUtil.setTransport(dispatch, null);
        int numHandlers = 0;
        assertEquals("Should be " + numHandlers +
            " handler(s) on dispatch object", numHandlers,
            dispatch.getBinding().getHandlerChain().size());
        
        int x = 1;
        int diff = 2; // 2 per handler
        
        ByteArrayInputStream iStream = new ByteArrayInputStream(req.getBytes());
        Source requestSource = new StreamSource(iStream);
        Source response = dispatch.invoke(requestSource);
        int responseInt = getIntFromResponse(response);
        assertEquals(x+diff, responseInt);
        
        // add handler programatically
        ClientServerTestUtil.addHandlerToBinding(
            new SOAPTestHandler(), dispatch);
        diff = 4;

        // make new call
        iStream = new ByteArrayInputStream(req.getBytes());
        requestSource = new StreamSource(iStream);
        response = dispatch.invoke(requestSource);
        responseInt = getIntFromResponse(response);
        assertEquals(x+diff, responseInt);
    }
    public void testReferenceParametersProperty() throws Exception {
        String xmlRefParam1 = "<myns:MyParam1 wsa:IsReferenceParameter='true' xmlns:myns=\"http://cptestservice.org/wsdl\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">Hello</myns:MyParam1>";
        String xmlRefParam2 = "<myns:MyParam2 wsa:IsReferenceParameter='true' xmlns:myns=\"http://cptestservice.org/wsdl\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">There</myns:MyParam2>";
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Header>" +
                xmlRefParam1 + xmlRefParam2 +
                "</S:Header><S:Body><Hello xmlns=\"urn:test:types\"><intin>1</intin></Hello></S:Body></S:Envelope>";

        QName portQName = new QName("urn:test", "HelloPort");
        String endpointAddress =
            getEndpointAddress("http://localhost:8080/jaxrpc-fromwsdl_handler_simple/hello");

        // create service with just qname -- no handlers in that case
        QName serviceQName = new QName("urn:test", "Hello");
        Service service = Service.create(serviceQName);
        service.addPort(portQName, javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);
        Dispatch dispatch = service.createDispatch(portQName, SOAPMessage.class , Service.Mode.MESSAGE);
        MessageFactory messageFactory = MessageFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
        SOAPMessage soapMsg = messageFactory.createMessage(headers,
                new ByteArrayInputStream(request.getBytes()));
        List<Handler> handlers = new ArrayList<Handler>();
        handlers.add(new ReferenceParameterHandler());
        dispatch.getBinding().setHandlerChain(handlers);
        SOAPMessage msg = (SOAPMessage) dispatch.invoke(soapMsg);
        msg.writeTo(System.out);
    }
    /*
     * Creates a Dispatch object with source and tests that
     * the handler is called. Test uses a logical handler.
     */
    public void testDispatchSourceLogicalHandler() throws Exception {
        String req = "<?xml version=\"1.0\" ?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><Hello xmlns=\"urn:test:types\"><intin>1</intin></Hello></soapenv:Body></soapenv:Envelope>";
        QName portQName = new QName("urn:test", "HelloPort");
        String endpointAddress = getEndpointAddress(
            "http://localhost:8080/jaxrpc-fromwsdl_handler_simple/hello");
        
        // create service with just qname -- no handlers in that case
        QName serviceQName = new QName("urn:test", "Hello");
        Service service = Service.create(serviceQName);
        service.addPort(portQName, SOAP11HTTP_BINDING, setTransport(endpointAddress));
        
        Dispatch<Source> dispatch = service.createDispatch(portQName,
            Source.class, Service.Mode.MESSAGE);
        //ClientServerTestUtil.setTransport(dispatch, null);
        int numHandlers = 0;
        assertEquals("Should be " + numHandlers +
            " handler(s) on dispatch object", numHandlers,
            dispatch.getBinding().getHandlerChain().size());
        
        int x = 1;
        int diff = 2; // 2 per handler
        
        ByteArrayInputStream iStream = new ByteArrayInputStream(req.getBytes());
        Source requestSource = new StreamSource(iStream);
        Source response = dispatch.invoke(requestSource);
        int responseInt = getIntFromResponse(response);
        assertEquals(x+diff, responseInt);
        
        // add handler programatically
        ClientServerTestUtil.addHandlerToBinding(
            new LogicalTestHandler(), dispatch);
        diff = 4;

        // make new call
        iStream = new ByteArrayInputStream(req.getBytes());
        requestSource = new StreamSource(iStream);
        response = dispatch.invoke(requestSource);
        responseInt = getIntFromResponse(response);
        assertEquals(x+diff, responseInt);
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
    /*
     * Full exception tests are in the fromwsdl/handler
     * package. This one can be used for debugging simpler
     * cases.
     *
     * The test clears the client handlers and the exception
     * is thrown on the server side.
     */
    public void testException() throws Exception {
        Hello stub = createStub();
        
        try {
            stub.hello(SOAPTestHandler.THROW_RUNTIME_EXCEPTION);
            fail("did not receive an exception");
        } catch (Exception e) {
            // pass
        }
        
    }

    private int getIntFromResponse(Source source) throws Exception {
        Transformer xFormer =
            TransformerFactory.newInstance().newTransformer();
        xFormer.setOutputProperty("omit-xml-declaration", "yes");
        DOMResult dResult = new DOMResult();
        xFormer.transform(source, dResult);
        Node documentNode = dResult.getNode();
        Node envelopeNode = documentNode.getFirstChild();
        Node bodyNode = envelopeNode.getLastChild();
        Node requestResponseNode = bodyNode.getFirstChild();
        Node textNode = requestResponseNode.getFirstChild().getFirstChild();
        int responseInt = Integer.parseInt(textNode.getNodeValue());
        return responseInt;
    }
    
    /*
     * The normal tests in this file are for soap 1.1. This is a soap 1.2
     * test to make sure that the port is created with the proper binding
     * so that the proper handlers are called. See bug 6353179.
     *
     * Not working right now -- can't get endpoint to work.
     */
    public void testSOAP12Binding1() throws Exception {
        Hello_Service service = createService();
        Hello12 stub = create12Stub(service);

        // make sure port is working
        int x = 1;
        int diff = 2; // server handler only

        int y = stub.hello12(x);
        assertEquals(x+diff, y);
        
        Binding binding = ((BindingProvider) stub).getBinding();
        List<Handler> handlers = binding.getHandlerChain();
        assertEquals("should be 1 handler in chain", 1,
            handlers.size());
        
        Handler handler = handlers.get(0);
        assertTrue("handler should be type Port12Handler, not " +
            handler.getClass().toString(),
            handler instanceof Port12Handler);
        
        Port12Handler p12h = (Port12Handler) handler;
        p12h.resetCalled();
        
        stub.hello12(2);
        assertEquals("handler should have been called two times",
            2, p12h.getCalled());
    }
    
}
