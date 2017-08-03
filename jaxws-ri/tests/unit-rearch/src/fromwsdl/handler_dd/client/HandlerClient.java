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

package fromwsdl.handler_dd.client;

import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.Service;

import fromwsdl.handler_dd.common.TestHandler;

import junit.framework.*;
import testutil.ClientServerTestUtil;

public class HandlerClient extends TestCase {

    /*
     * main() method used during debugging
     */
    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            HandlerClient test = new HandlerClient("HandlerClient");
            test.test1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HandlerClient(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(fromwsdl.handler_dd.client.HandlerClient.class);
        return suite;
    }

    private Hello_Service createService() throws Exception {
        Hello_Service service = new Hello_Service();

        return service;
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

    /* tests below here */

    /*
     * Each handler adds one to the int during request and response.
     */
    public void test1() throws Exception {
        Hello stub = createStub();

        int x = 1;
        int diff = 6; // 2 per handler invoked

        int y = stub.hello(x);

        System.out.println("sent: " + x + ", expect " + (x+diff) +
            " back. received: " + y);
        assertEquals("handlers not invoked correctly", x+diff, y);
    }

//    public void testDispatch1() throws Exception {
//        int x = 1; // what we send
//        int diff = 2; // 4 handler invocations
//
//        JAXBContext jxbContext = JAXBContext.newInstance(ObjectFactory.class);
//        Dispatch dispatch = createDispatchForJAXB(
//            new QName("urn:test", "HelloPort"), jxbContext);
//
//        Hello_Type request = objectFactory.createHello_Type();
//        request.setValue(1);
//
//        // make first call with no client side handlers
//        HelloResponse response = (HelloResponse) dispatch.invoke(request);
//        assertNotNull("response cannot be null", response);
//
//        int y = response.getValue();
//        System.out.println("sent: " + x + ", expect " + (x+diff) +
//            " back. received: " + y);
//        assertTrue(y == x+diff);
//
//        System.out.println("now adding handler to dispatch");
//        Binding binding = dispatch.getBinding();
//        assertNotNull("binding cannot be null", binding);
//        int handlerChainLength = binding.getHandlerChain().size();
//        assertEquals("the handler list is not empty", 0, handlerChainLength);
//
//        // add handler
//        HandlerInfo hInfo = new HandlerInfo(
//            handler_tests.simple_handler_test.client.handlers.ClientHandler.class,
//            null, null);
//        List<HandlerInfo> newHandlers = new ArrayList<HandlerInfo>();
//        newHandlers.add(hInfo);
//        binding.setHandlerChain(newHandlers);
//
//        // now try again
//        diff = 4;
//        response = (HelloResponse) dispatch.invoke(request);
//        assertNotNull("response cannot be null", response);
//
//        y = response.getValue();
//        System.out.println("sent: " + x + ", expect " + (x+diff) +
//            " back. received: " + y);
//        assertTrue(y == x+diff);
//    }

    /*
     * Test tries to add a handler programmatically after clearing
     * handlers out of registry in the service.
     */
    public void test2() throws Exception {
        Hello_Service service = createService();
        service.setHandlerResolver(new HandlerResolver() {
            public List<Handler> getHandlerChain(PortInfo info) {
                return new ArrayList<Handler>();
            }
        });

        Hello stub = createStub(service);

        int x = 1;
        int diff = 6; // 2 per handler invoked

        int y = stub.hello(x);

        System.out.println("sent: " + x + ", expect " + (x+diff) +
            " back. received: " + y);
        assertEquals("handlers not invoked correctly", x+diff, y);

        // now add client handler
        service.setHandlerResolver(new HandlerResolver() {
            public List<Handler> getHandlerChain(PortInfo info) {
                List handlers = new ArrayList<Handler>();
                handlers.add(new TestHandler());
                return handlers;
            }
        });
        stub = createStub(service);

        // test again
        diff = 8;
        y = stub.hello(x);
        System.out.println("sent: " + x + ", expect " + (x+diff) +
            " back. received: " + y);
        assertEquals("handlers not invoked correctly", x+diff, y);

    }

}
