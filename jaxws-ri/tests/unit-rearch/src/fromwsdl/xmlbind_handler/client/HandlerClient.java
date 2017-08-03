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

package fromwsdl.xmlbind_handler.client;

import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.Service;

import fromwsdl.xmlbind_handler.common.TestHandler;
import fromwsdl.xmlbind_handler.common.TestSOAPHandler;

import junit.framework.*;
import testutil.ClientServerTestUtil;

public class HandlerClient extends TestCase {

    /*
     * main() method used during debugging
     */
    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            System.setProperty("log", "true");
            HandlerClient test = new HandlerClient("HandlerClient");
            test.testHTTPException();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HandlerClient(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(fromwsdl.xmlbind_handler.client.HandlerClient.class);
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
        Hello stub = (Hello) service.getPort(new QName("urn:test", "HelloPort"),
            fromwsdl.xmlbind_handler.client.Hello.class);
        ClientServerTestUtil.setTransport(stub, null);
        return stub;
    }

    /* tests below here */

    /*
     * Each handler adds one to the int during request and response,
     * so we should receive the original number plus 2 if the
     * server handlers are working.
     */
    public void test1() throws Exception {
        Hello stub = createStub();

        int x = 1;
        int diff = 2;
        
        int y = stub.hello(x);
        assertEquals(x+diff, y);
    }

    /*
     * This test causes the server handler to throw an http
     * exception.
     */
    public void testHTTPException() throws Exception {
        Hello stub = createStub();
        int x = TestHandler.THROW_HTTP_EXCEPTION;

        TestSOAPHandler handler = new TestSOAPHandler();
        handler.setExpectEmptyResponse(true);
        ClientServerTestUtil.addHandlerToBinding(handler,
            (javax.xml.ws.BindingProvider) stub);
        
        try {
            stub.hello(x);
            fail("did not receive an exception");
        } catch (Exception e) {
            // todo (bobby) -- check exception
            System.out.println("received " + e);
        }
        
        // check handler to make sure response was empty
//        if (handler.getException() != null) {
//            fail(handler.getException().getMessage());
//        }
    }
    
    /*
     * This test causes the server handler to throw
     * a runtime exception.
     */
    public void testRuntimeException() throws Exception {
        Hello stub = createStub();
        int x = TestHandler.THROW_RUNTIME_EXCEPTION;
        
        try {
            stub.hello(x);
            fail("did not receive an exception");
        } catch (Exception e) {
            // todo (bobby) -- check exception
            System.out.println("received " + e);
        }
    }
    
    /*
     * This test causes the server handler to throw a
     * protocol exception.
     */
    public void testProtocolException() throws Exception {
        Hello stub = createStub();
        int x = TestHandler.THROW_HTTP_EXCEPTION;
        
        try {
            stub.hello(x);
            fail("did not receive an exception");
        } catch (Exception e) {
            // todo (bobby) -- check exception
            System.out.println("received " + e);
        }
        
        // check handler to make sure response was empty
//        if (handler.getException() != null) {
//            fail(handler.getException().getMessage());
//        }
    }
    
}
