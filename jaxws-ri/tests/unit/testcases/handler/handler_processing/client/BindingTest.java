/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2012 Oracle and/or its affiliates. All rights reserved.
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

package handler.handler_processing.client;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.PortInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import handler.handler_processing.common.HandlerTracker;
import junit.framework.*;

import handler.handler_processing.common.HasName;
import handler.handler_processing.common.BaseSOAPHandler;

/**
 * Tests BindingProvider.getHandlerChain() anf BindingProvider.setHandlerChain()
 *
 * @Rama Pulavarthi
 */

public class BindingTest extends TestCaseBase {

    /*
     * main method for debugging
     */
    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            BindingTest tester = new BindingTest("BindingTest");
            tester.testBinding1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BindingTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(BindingTest.class);
        return suite;
    }

    /*
     * Simple end to end test (mostly for debug work)
     */

    public void testSimpleEchoInt() throws Exception {
        TestService stub = getTestStub(getService());
        int foo = -1;
        int bar = stub.testInt(foo);
        assertTrue(foo == bar);
    }

    /*
     * test the binding objects for the right number of handlers
     */
    public void testBinding1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        
        // get the bindings
        Binding testBinding = ((BindingProvider) testStub).getBinding();
        Binding reportBinding = ((BindingProvider) reportStub).getBinding();
        assertNotNull("Binding object should not be null", testBinding);
        assertNotNull("Binding object should not be null", reportBinding);
        
        // check the handlers
        List<Handler> testHandlers = testBinding.getHandlerChain();
        List<Handler> reportHandlers = reportBinding.getHandlerChain();
        assertNotNull("Handler list should not be null", testHandlers);
        assertNotNull("Handler list should not be null", reportHandlers);
        
        // check number of handlers
        assertEquals("got wrong number of handlers in test binding",
            SERVICE_HANDLERS + TEST_PORT_HANDLERS + PROTOCOL_HANDLERS,
            testHandlers.size());
        assertEquals("got wrong number of handlers in report binding",
            SERVICE_HANDLERS + REPORT_PORT_HANDLERS + PROTOCOL_HANDLERS,
            reportHandlers.size());
        
        // check handler names -- see config file for order
        int [] testNames = { 4, 0, 5, 1, 7, 3 };
        int [] reportNames = { 4, 0, 2, 6, 7, 3 };
        String foundName = null;
        for (int i=0; i<testNames.length; i++) {
            foundName = ((HasName) testHandlers.get(i)).getName();
            assertEquals("found unexpected handler in chain",
                CLIENT_PREFIX + testNames[i], foundName);
        }
        for (int i=0; i<reportNames.length; i++) {
            foundName = ((HasName) reportHandlers.get(i)).getName();
            assertEquals("found unexpected handler in chain",
                CLIENT_PREFIX + reportNames[i], foundName);
        }
    }

    /*
     * tests for SOAPBinding.
     *
     */
    public void testSoapBinding1() throws Exception {
        TestService_Service service = getService();
        TestService stub = getTestStub(service);
        Binding binding = ((BindingProvider) stub).getBinding();
        if (binding instanceof SOAPBinding) {
            SOAPBinding sb = (SOAPBinding) binding;
            assertNotNull("did not get SOAPBinding", sb);
            Set<String> roles = sb.getRoles();
            assertNotNull("roles cannot be null", roles);
            assertFalse("found zero roles in SOAPBinding", roles.isEmpty());
            assertTrue("soap 1.1 \"next\" role is not included in roles",
                roles.contains(NEXT_1_1));
            assertFalse("soap 1.2 \"none\" role cannot be included in roles",
                roles.contains(NONE));

            // try setting new roles
            Set<String> newSet = new HashSet<String>();
            String testURI = "http://java.sun.com/justanexample";
            newSet.add(testURI);
            sb.setRoles(newSet);

            try {
                newSet.add(NONE);
                sb.setRoles(newSet);
                throw new RuntimeException("did not get jaxrpc exception for setting \"none\" role");
            } catch (WebServiceException e) {
                // pass
            }
            newSet.addAll(roles);
            newSet.remove(NONE);
            sb.setRoles(newSet);

            // add empty set and check for next/ultimate
            newSet = new HashSet<String>();
            sb.setRoles(newSet);
            Set<String> newSet2 = sb.getRoles();
            assertTrue("soap 1.1 \"next\" role is not included in roles",
                newSet2.contains(NEXT_1_1));
            assertFalse("soap 1.2 \"none\" role cannot be included in roles",
                newSet2.contains(NONE));
        } else {
            throw new Exception("binding is not a SOAPBinding");
        }
    }

    /*
     * test the roles of the binding
     */
    public void testSoapBinding2() throws Exception {
        TestService_Service service = getService();
        TestService stub = getTestStub(service);
        SOAPBinding binding =
            (SOAPBinding) ((BindingProvider) stub).getBinding();
        Set<String> roles = binding.getRoles();

        if (HandlerTracker.VERBOSE_HANDLERS) {
            System.out.println("roles: " + roles);
        }

        String uri1 = "http://sun.com/client/role1";
        String uri2 =  "http://sun.com/client/role2";
        assertTrue("test \"role1\" is not included in roles",
            roles.contains(uri1));
        assertTrue("test \"role2\" is not included in roles",
            roles.contains(uri2));
    }


    /*
     * Used for testing of handlers shared between bindings.
     */
    public void testBindingInstances() throws Exception {
        TestService_Service service = getService();
        TestService stub1 = getTestStub(service);
        TestService stub2 = getTestStub(service);
        
        // make some calls
        stub1.testInt(0);
        stub2.testInt(0);
        
        Binding b1 = ((BindingProvider) stub1).getBinding();
        Binding b2 = ((BindingProvider) stub2).getBinding();
        
        List<Handler> chain = b1.getHandlerChain();
        // get a soap handler from the chain. doesn't matter which one
        BaseSOAPHandler handler = null;
        for (Handler h : chain) {
            if (h instanceof BaseSOAPHandler) {
                handler = (BaseSOAPHandler) h;
                break;
            }
        }
        assertTrue("handler should be in 'ready' state", handler.isAvailable());
        b2.setHandlerChain(new ArrayList<Handler>());
        assertTrue("handler should be in 'ready' state", handler.isAvailable());
    }
    
    /*
     * Add a handler resolver that will simply record the
     * PortInfo objects that are passed to it. Just testing that
     * the client code is requesting the correct thing.
     */
    public void testHandlerResolver() throws Exception {
        TestService_Service service = getService();
        TestHandlerResolver resolver = new TestHandlerResolver();
        ((Service) service).setHandlerResolver(resolver);
        
        // should be just one portinfo after first stub
        TestService testStub = getTestStub(service);
        assertEquals("should only be one call to resolver",
            1, resolver.getPortInfos().size());
        ReportService reportStub = getReportStub(service);
        
        // now check list of PortInfo objects
        List<PortInfo> infos = resolver.getPortInfos();
        assertEquals("should be two calls to resolver",
            2, resolver.getPortInfos().size());
        assertEquals(infos.get(0).getPortName(), testPortQName);
        assertEquals(infos.get(1).getPortName(), reportPortQName);
    }
    
}
