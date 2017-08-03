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

package server.http_only.client;

import java.util.Map;
import java.util.List;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.Service;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import testutil.ClientServerTestUtil;

public class TestClient extends TestCase {

    /*
     * main() method used during debugging
     */
    public static void main(String [] args) {
        try {
            TestClient test = new TestClient("TestClient");
            test.test3();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TestClient(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(server.http_only.client.TestClient.class);
        return suite;
    }

    private HelloService createService() throws Exception {
        HelloService service = new HelloService();
        return service;
    }

    private HelloPortType createProxy(HelloService service) throws Exception {
        HelloPortType proxy = service.getHelloPort();
        ClientServerTestUtil.setTransport(proxy);
        return proxy;
    }

    /*
     * Without setting the maintain property, session
     * should not be maintained.
     */
    public void test1() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        HelloPortType proxy = createProxy(createService());
        proxy.introduce();
        assertFalse("client session should not be maintained",
            proxy.rememberMe());
    }
    
    /*
     * With maintain property set to false, session
     * should not be maintained.
     */
    public void test2() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        HelloPortType proxy = createProxy(createService());
        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.FALSE);
        proxy.introduce();
        assertFalse("client session should not be maintained",
            proxy.rememberMe());
    }
    
    /*
     * With maintain property set to true, session
     * should be maintained.
     */
    public void test3() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        HelloPortType proxy = createProxy(createService());
        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
        proxy.introduce();
        assertTrue("client session should be maintained", proxy.rememberMe());
    }

    /*
     * Tests Standard Servlet MessageContext properties
     */
    public void testServletMsgCtxt() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        HelloPortType proxy = createProxy(createService());
        proxy.testServletProperties();
    }

    /*
     * Tests Standard HTTP MessageContext properties on server side
     */
    public void testHttpMsgCtxt() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        HelloPortType proxy = createProxy(createService());
        proxy.testHttpProperties();
    }

    /*
     * Tests Standard HTTP MessageContext properties on client side
     */
    public void testClientHttpMsgCtxt() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        HelloPortType proxy = createProxy(createService());
        proxy.introduce();
        Map<String, Object> responseContext =
            ((BindingProvider) proxy).getResponseContext();
        Integer code = (Integer)responseContext.get(MessageContext.HTTP_RESPONSE_CODE);
        assertTrue(code != null);
        assertEquals((int)code, 200);
        Map<String, List<String>> headers =
			(Map<String, List<String>>)responseContext.get(MessageContext.HTTP_RESPONSE_HEADERS);
        assertTrue(headers != null);
	  System.out.println("Headers="+headers);
    }
    
}
