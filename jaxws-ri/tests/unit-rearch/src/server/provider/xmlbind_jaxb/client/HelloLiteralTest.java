/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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

package server.provider.xmlbind_jaxb.client;

import testutil.ClientServerTestUtil;
import java.io.File;
import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPBinding;
import junit.framework.TestCase;
import javax.xml.bind.JAXBContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;
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

    private ClientServerTestUtil util = new ClientServerTestUtil();

    private static final JAXBContext jaxbContext = createJAXBContext();

    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            HelloLiteralTest test = new HelloLiteralTest("HelloLiteralTest");
            test.testHelloHandlerSource();
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
            endpointAddress = "http://localhost:8080/jaxrpc-provider_tests_xmlbind_jaxb/hello";
    }

    Service createService () {
        Service service = Service.create(serviceQName);

        return service;
    }

    private Dispatch createDispatch() throws Exception {
        Service service = createService();
        service.addPort(portQName,
            HTTPBinding.HTTP_BINDING, setTransport(endpointAddress));
        Dispatch<Object> dispatch = service.createDispatch(portQName, createJAXBContext(), Service.Mode.PAYLOAD);
        return dispatch;
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
    public void testHelloRequestResponse() throws Exception {
        Dispatch<Object> dispatch = createDispatch();

        Hello_Type hello = new Hello_Type();
        hello.setArgument("Dispatch");
        hello.setExtra("Test");

        Object result = dispatch.invoke(hello);

        assertTrue(result instanceof HelloResponse);
        assertTrue("foo".equals(((HelloResponse)result).getArgument()));
        assertTrue("bar".equals(((HelloResponse)result).getExtra()));
    }

    /*
     * Version of testHelloRequestResponseSource with a handler
     * added dynamically. Handler will change "foo" to
     * "hellofromhandler." Service will respond to that argument
     * with "hellotohandler." Handler then checks incoming message
     * for that text and changes to "handlerworks" to show that handler
     * worked on incoming and outgoing message.
     */
    public void testHelloHandlerJAXB() throws Exception {
        Dispatch<Object> dispatch = createDispatch();

        TestLogicalHandler handler = new TestLogicalHandler();
        handler.setHandleMode(TestLogicalHandler.HandleMode.JAXB);
        ClientServerTestUtil.addHandlerToBinding(handler, dispatch);

        Hello_Type hello = new Hello_Type();
        hello.setArgument("Dispatch");
        hello.setExtra("Test");

        Object result = dispatch.invoke(hello);

        assertTrue(result instanceof HelloResponse);
        assertTrue("handlerworks".equals(((HelloResponse)result).getArgument()));
        assertTrue("bar".equals(((HelloResponse)result).getExtra()));
    }

    /*
     * Version of testHelloRequestResponseSource with a handler
     * added dynamically. Handler will change "foo" to
     * "hellofromhandler." Service will respond to that argument
     * with "hellotohandler." Handler then checks incoming message
     * for that text and changes to "handlerworks" to show that handler
     * worked on incoming and outgoing message.
     */
    public void testHelloHandlerSource() throws Exception {
        Dispatch<Object> dispatch = createDispatch();

        TestLogicalHandler handler = new TestLogicalHandler();
        handler.setHandleMode(TestLogicalHandler.HandleMode.SOURCE);
        ClientServerTestUtil.addHandlerToBinding(handler, dispatch);

        Hello_Type hello = new Hello_Type();
        hello.setArgument("Dispatch");
        hello.setExtra("Test");

        Object result = dispatch.invoke(hello);

        assertTrue(result instanceof HelloResponse);
        assertTrue("handlerworks".equals(((HelloResponse)result).getArgument()));
        assertTrue("bar".equals(((HelloResponse)result).getExtra()));
    }

}
