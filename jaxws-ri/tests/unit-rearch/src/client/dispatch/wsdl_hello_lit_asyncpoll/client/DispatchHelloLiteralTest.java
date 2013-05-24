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

package client.dispatch.wsdl_hello_lit_asyncpoll.client;

import junit.framework.TestCase;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import testutil.ClientServerTestUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * @author JAX-RPC RI Development Team
 */
public class DispatchHelloLiteralTest extends TestCase {

    private String helloSM = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><Hello xmlns=\"urn:test:types\"><argument>foo</argument><extra>Test </extra></Hello></soapenv:Body></soapenv:Envelope>";
    private String helloMsg = "<Hello xmlns=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></Hello>";
    private String helloResponse = "<HelloResponse xmlns=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></HelloResponse>";
    private String voidMsg = "<VoidTest xmlns=\"urn:test:types\"/>";
    private String voidResponse = "<VoidTestResponse xmlns=\"urn:test:types\"/>";

    private QName serviceQName = new QName("urn:test", "Hello");
    private QName portQName = new QName("urn:test", "HelloPort");
    ;
    private String bindingIdString = SOAPBinding.SOAP11HTTP_BINDING;
    private final String endpointAddress;
    private static final String ENDPOINT_IMPL = "client.dispatch.wsdl_hello_lit_asyncpoll.server.Hello_PortType_Impl";
    private Service service;
    private Dispatch dispatch;


    public DispatchHelloLiteralTest(String name) {
        super(name);

        // we'll fix the test harness correctly later,
        // so that test code won't have to hard code any endpoint address nor transport,
        // but for now let's just support local and HTTP to make unit tests happier.
        // this is not a good code, but it's just a bandaid solutino that works for now.

        if(ClientServerTestUtil.useLocal())
            endpointAddress = "local://"+new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\','/')+'?'+portQName.getLocalPart();
        else
            endpointAddress = "http://localhost:8080/jaxrpc-client_dispatch_wsdl_hello_lit_asyncpoll/hello";
    }

    private static javax.xml.bind.JAXBContext createJAXBContext()
        throws Exception {
        return javax.xml.bind.JAXBContext.newInstance(client.dispatch.wsdl_hello_lit_asyncpoll.client.ObjectFactory.class);
    }

    private void createService() throws Exception {
        service = service.create(serviceQName);
    }

    void setTransport(Dispatch dispatch) throws Exception {
        // create helper class
        ClientServerTestUtil util = new ClientServerTestUtil();
        // set transport
        OutputStream log = null;
        //log = System.out;
        util.setTransport(dispatch, (OutputStream) log);
    }

    private Dispatch createDispatchJAXB() throws Exception {
        String bindingId = bindingIdString;

        JAXBContext context = createJAXBContext();
        service.addPort(portQName, bindingId, endpointAddress);
        dispatch = service.createDispatch(portQName, context, Service.Mode.PAYLOAD);
        setTransport(dispatch);
        return dispatch;
    }

    private Dispatch createDispatchSource() throws Exception {
        service.addPort(portQName, bindingIdString, endpointAddress);
        dispatch = service.createDispatch(portQName, Source.class, Service.Mode.PAYLOAD);
        setTransport(dispatch);
        return dispatch;
    }

    private Dispatch createDispatchSOAPMessage() {
        try {
            service.addPort(portQName, bindingIdString, endpointAddress);
            dispatch = service.createDispatch(portQName, SOAPMessage.class,
                Service.Mode.MESSAGE);
            setTransport(dispatch);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dispatch;
    }

    private Dispatch createDispatchSOAPMessageSource() {
        try {
            service.addPort(portQName, bindingIdString, endpointAddress);
            dispatch = service.createDispatch(portQName, Source.class,
                Service.Mode.MESSAGE);
            setTransport(dispatch);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dispatch;
    }

    private Dispatch getDispatchJAXB() throws Exception {
        createService();
        return createDispatchJAXB();
    }

    private Dispatch getDispatchSource() throws Exception {
        createService();
        return createDispatchSource();
    }

     private Dispatch getDispatchSOAPMessage() throws Exception {
        createService();
        return createDispatchSOAPMessage();
    }

    private Dispatch getDispatchSOAPMessageSource() throws Exception {
        createService();
        return createDispatchSOAPMessageSource();
    }

    private Source makeStreamSource(String msg) {

        byte[] bytes = msg.getBytes();
        ByteArrayInputStream sinputStream = new ByteArrayInputStream(bytes);
        return new StreamSource(sinputStream);
    }


    private Collection<Source> makeMsgSource(String msg) throws Exception {
        Collection<Source> sourceList = new ArrayList();

        byte[] bytes = msg.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ByteArrayInputStream saxinputStream = new ByteArrayInputStream(bytes);
        InputSource inputSource = new InputSource(saxinputStream);

        ByteArrayInputStream sinputStream = new ByteArrayInputStream(bytes);

        DOMSource domSource = new DOMSource(createDOMNode(inputStream));
        sourceList.add(domSource);
        SAXSource saxSource = new SAXSource(inputSource);
        sourceList.add(saxSource);
        StreamSource streamSource = new StreamSource(sinputStream);
        sourceList.add(streamSource);

        return sourceList;
    }


    public Node createDOMNode(InputStream inputStream) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        return builder.parse(inputStream);
    }

    SOAPMessage getSOAPMessage(Source msg) throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        message.getSOAPPart().setContent((Source) msg);
        message.saveChanges();
        return message;
    }

    private String sourceToXMLString(Source result) {

        String xmlResult = null;
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            OutputStream out = new ByteArrayOutputStream();
            StreamResult streamResult = new StreamResult();
            streamResult.setOutputStream(out);
            transformer.transform(result, streamResult);
            xmlResult = streamResult.getOutputStream().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return xmlResult;
    }

    public void testHelloAsyncPollJAXB() throws Exception {
        JAXBContext jc = createJAXBContext();
        Hello_Type hello = new Hello_Type();
        HelloResponse helloResult = new HelloResponse();

        hello.setExtra("Test ");
        hello.setArgument("Dispatch ");

        Dispatch dispatch = getDispatchJAXB();
        Response result = dispatch.invokeAsync(hello);

        Object obj = result.get();
        HelloResponse res = (HelloResponse) obj;

        assertEquals(result.isCancelled(), false);
        assertEquals(res.getExtra(), hello.getExtra());
        assertEquals(res.getArgument(), hello.getArgument());
    }

    //test for bug
    public void FAILSMULTITHREADISSUEtestHelloAsyncPollJAXBSource() throws Exception {
        JAXBContext jc = createJAXBContext();
        Hello_Type hello = new Hello_Type();
        HelloResponse helloResult = new HelloResponse();

        hello.setExtra("Test ");
        hello.setArgument("Dispatch ");

        Dispatch dispatch = getDispatchJAXB();
        Dispatch dispatchSrc = getDispatchSource();

        StreamSource streamSource = new StreamSource(new StringReader(helloMsg));
        Response result = dispatch.invokeAsync(hello);
        if (!result.isDone()) {
            dispatchSrc.invoke(streamSource);
        }

        Object obj = result.get();
        HelloResponse res = (HelloResponse) obj;
        assertEquals(result.isCancelled(), false);
        assertEquals(res.getExtra(), hello.getExtra());
        assertEquals(res.getArgument(), hello.getArgument());
    }

    public void testHelloAsyncPollJAXBMult() throws Exception {
        JAXBContext jc = createJAXBContext();
        Hello_Type hello = new Hello_Type();
        HelloResponse helloResult = new HelloResponse();

        hello.setExtra("Test ");
        hello.setArgument("Dispatch ");

        Dispatch dispatch = getDispatchJAXB();

        Response result = dispatch.invokeAsync(hello);
        Response result2 = dispatch.invokeAsync(hello);
        Response result3 = dispatch.invokeAsync(hello);
        Response result4 = dispatch.invokeAsync(hello);
        Response result5 = dispatch.invokeAsync(hello);
        Response result6 = dispatch.invokeAsync(hello);
        Response result7 = dispatch.invokeAsync(hello);
        Response result8 = dispatch.invokeAsync(hello);
        Response result9 = dispatch.invokeAsync(hello);
        int k = 0;
        Response resultf = null;
        for (k = 0; k < 55; k++) {
            resultf = dispatch.invokeAsync(hello);
        }
        System.out.println("last k " + k);
        while (!resultf.isDone()) {
        }
    }

    public void testHelloAsyncPollDoneJAXB() throws Exception {
        JAXBContext jc = createJAXBContext();
        Hello_Type hello = new Hello_Type();
        HelloResponse helloResult = new HelloResponse();

        hello.setExtra("Test ");
        hello.setArgument("Dispatch ");

        Dispatch dispatch = getDispatchJAXB();

        Response result = dispatch.invokeAsync(hello);

        while (!result.isDone()) {
        }
        Object obj = result.get();
        HelloResponse res = (HelloResponse) obj;

        assertEquals(res.getExtra(), hello.getExtra());
        assertEquals(res.getArgument(), hello.getArgument());
    }
    /* invalid tests - may not always cancel
    public void testHelloAsyncPollCancelJAXB() throws Exception {
        JAXBContext jc = createJAXBContext();
        Hello_Type hello = new Hello_Type();
        HelloResponse helloResult = new HelloResponse();
        
        hello.setExtra("Test ");
        hello.setArgument("Dispatch ");
        
        Dispatch dispatch = getDispatchJAXB();
        Response result = dispatch.invokeAsync(hello);
        
        while (!result.isDone()) {
            result.cancel(false); //may interrupt if running
            assertEquals(true, result.isCancelled());
        }
    }

    public void testHelloAsyncPollCancelInterruptJAXB() throws Exception {
        JAXBContext jc = createJAXBContext();
        Hello_Type hello = new Hello_Type();
        HelloResponse helloResult = new HelloResponse();
        
        hello.setExtra("Test ");
        hello.setArgument("Dispatch ");
        
        Dispatch dispatch = getDispatchJAXB();
        Response result = dispatch.invokeAsync(hello);
        
        while (!result.isDone()) {
            result.cancel(true); //may interrupt if running
            assertEquals(true, result.isCancelled());
        }
    }
    //end invalid tests
    */

    public void testHelloAsyncPollSOAPMessageSource() throws Exception {
        Dispatch dispatch = getDispatchSOAPMessageSource();
        Source src = makeStreamSource(helloSM);
        try {
            Response<Source> result = dispatch.invokeAsync(src);
            assertTrue(result != null);
            Source resultmsg = result.get();
            assertTrue(resultmsg != null);
            System.out.println("-----------------------------------------");
            System.out.println("Source SOAPMessage Result = ");
            System.out.println(sourceToXMLString(resultmsg));
            System.out.println("-----------------------------------------");
        } catch(Exception e){
            e.printStackTrace();
            fail("Test fails");
        }
    }

    public void testHelloAsyncPollSOAPMessage() throws Exception {
        Dispatch dispatch = getDispatchSOAPMessage();
        SOAPMessage msg = getSOAPMessage(makeStreamSource(helloSM));
        try {
            Response<SOAPMessage> result = dispatch.invokeAsync(msg);
            assertTrue(result != null);
            SOAPMessage resultmsg = result.get();
            assertTrue(resultmsg != null);
            System.out.println("-----------------------------------------");
            System.out.println("SOAPMessage Result = ");
            resultmsg.writeTo(System.out);
            System.out.println("-----------------------------------------");
        } catch(Exception e){
            e.printStackTrace();
            fail("Test fails");
        }
    }

    public void testHelloAsyncPollXMLMulti() throws Exception {
        Dispatch dispatch = getDispatchSource();
        int i = 0;
        while (i < 20) {
            Collection<Source> sourceList = makeMsgSource(helloMsg);
            Collection<Source> responseList = makeMsgSource(helloResponse);
            for (Iterator iter = sourceList.iterator(); iter.hasNext();) {
                Object sourceObject = iter.next();
                Response result = null;
                result = dispatch.invokeAsync(sourceObject);
                result.get();
            }
            i++;
        }
    }

    //todo: object comes back as jaxb object investigate
    public void testHelloAsyncPollDoneXML() throws Exception {
        Dispatch dispatch = getDispatchSource();
        Collection<Source> sourceList = makeMsgSource(helloMsg);
        Collection<Source> responseList = makeMsgSource(helloResponse);

        for (Iterator iter = sourceList.iterator(); iter.hasNext();) {
            Object sourceObject = iter.next();
            Response result = dispatch.invokeAsync(sourceObject);

            while (!result.isDone()) {
            }
            Object realResult = ((Response) result).get();
            assertTrue(realResult instanceof Source);
        }
    }

    /*Invalid test may not cancel
    public void testHelloAsyncPollCancelXML() throws Exception {
        Dispatch dispatch = getDispatchSource();
        Collection<Source> sourceList = makeMsgSource(helloMsg);
        Collection<Source> responseList = makeMsgSource(helloResponse);

        for (Iterator iter = sourceList.iterator(); iter.hasNext();) {
            Object sourceObject = iter.next();
            Response result = dispatch.invokeAsync(sourceObject);

            while (!result.isDone()) {
                result.cancel(false);
                assertEquals(true, result.isCancelled());
            }
        }
    }


    public void testHelloAsyncPollCancelInterruptXML() throws Exception {

        Dispatch dispatch = getDispatchSource();
        Collection<Source> sourceList = makeMsgSource(helloMsg);
        Collection<Source> responseList = makeMsgSource(helloResponse);

        for (Iterator iter = sourceList.iterator(); iter.hasNext();) {
            Object sourceObject = iter.next();
            Response result = dispatch.invokeAsync(sourceObject);

            while (!result.isDone()) {
                result.cancel(true);
                assertEquals(true, result.isCancelled());
            }
        }
    }
     --end invalid test*/
    public void testVoidAsyncPollJAXB() throws Exception {
        JAXBContext jc = createJAXBContext();
        VoidTest voidTest = new VoidTest();
        VoidTestResponse voidTestResult = new VoidTestResponse();

        Dispatch dispatch = getDispatchJAXB();
        Response result = dispatch.invokeAsync(voidTest);
        Object obj = result.get();
        assertTrue(obj != null);
        assertTrue(obj instanceof VoidTestResponse);
        VoidTestResponse res = (VoidTestResponse) obj;
    }

    //todo: this does not come back as source investigate
    public void testVoidAsyncPollXML() throws Exception {

        Dispatch dispatch = getDispatchSource();
        Collection<Source> sourceList = makeMsgSource(voidMsg);
        Collection<Source> responseList = makeMsgSource(voidResponse);

        for (Iterator iter = sourceList.iterator(); iter.hasNext();) {
            Object sourceObject = iter.next();
            Response<Source> result = dispatch.invokeAsync(sourceObject);
            Source realResult = result.get();
            assertTrue(realResult != null);
            System.out.println("-----------------------------------------");
            System.out.println("Source Result = ");
            System.out.println(sourceToXMLString(realResult));
            System.out.println("-----------------------------------------");
        }
    }
}
