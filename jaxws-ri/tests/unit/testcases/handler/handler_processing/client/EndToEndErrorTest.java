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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPFaultException;

import handler.handler_processing.common.HandlerTracker;

import junit.framework.*;

/**
 * Tests End to End Handler Fault Processing, checks handler invocation order and closing order,
 * when exceptions occur.
 *
 * See EndToEndTest. Moved tests for exceptions and faults
 * to this class.
 *
 * @author Rama Pulavarthi 
 */
public class EndToEndErrorTest extends TestCaseBase {

    /*
     * main method for debugging
     */
    public static void main(String [] args) throws Exception {
        System.setProperty("uselocal", "true");
        EndToEndErrorTest tester =
            new EndToEndErrorTest("EndToEndErrorTest");
    }

    public EndToEndErrorTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(EndToEndErrorTest.class);
        return suite;
    }

    /*
     * Have one of the client handlers throw a runtime exception
     * and check that the proper methods are called. This test
     * is on outbound message with 2.0 handler (so handleFault
     * should not be called on the handler that throws the
     * exception).
     */
    public void testClientOutboundRuntimeException1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX+5,
            HA_THROW_RUNTIME_EXCEPTION_OUTBOUND);

        try {
            testStub.testInt(42);
            fail("did not receive any exception.");
        } catch (WebServiceException e) {
            Throwable cause = e.getCause();
            assertNotNull("cause of exception is null", cause);
            assertTrue("cause should be runtime exception, instead is " +
                cause.getClass().toString(), cause instanceof RuntimeException);
        }

        // check called handlers
        int [] called = {0,1,3,4,5}; // one direction only
        int [] closed = {5,4,3,1,0};
        List<String> calledHandlers = tracker.getCalledHandlers();
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + called[i], calledHandlers.get(i));
        }

        // check closed handlers
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("Did not get proper number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], closedHandlers.get(i));
        }

        // check destroyed handlers -- none in jaxws 2.0
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("destroyed handler list should be empty",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have one of the client handlers throw a runtime exception
     * and check that the proper methods are called. This test
     * is on outbound message with 2.0 handler (so handleFault
     * should not be called on the handler that throws the
     * exception). Testing with logical handler.
     */
    public void testClientOutboundRuntimeException2() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX+1,
            HA_THROW_RUNTIME_EXCEPTION_OUTBOUND);

        try {
            testStub.testInt(42);
            fail("test did not throw any exception");
        } catch (WebServiceException e) {
            Throwable cause = e.getCause();
            assertNotNull("cause of exception is null", cause);
            assertTrue("cause should be runtime exception, instead is " +
                cause.getClass().toString(), cause instanceof RuntimeException);
        }

        // check result
        int [] called = {0,1}; // one direction only
        int [] closed = {1,0};
        List<String> calledHandlers = tracker.getCalledHandlers();
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + called[i], calledHandlers.get(i));
        }
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("Did not get proper number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], closedHandlers.get(i));
        }

        // check destroyed handlers -- none in jaxws 2.0
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("destroyed handler list should be empty",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have one of the client handlers throw a runtime exception
     * and check that the proper methods are called. This test
     * is on inbound message. Testing with soap handler.
     */
    public void testClientInboundRuntimeException1() throws Exception {
        int badHandler = 5;
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX + badHandler,
            HA_THROW_RUNTIME_EXCEPTION_INBOUND);

        try {
            testStub.testInt(42);
            fail("should have received a runtime exception");
        } catch (WebServiceException e) {
            Throwable cause = e.getCause();
            assertNotNull("cause of exception is null", cause);
            assertTrue("cause should be runtime exception, instead is " +
                cause.getClass().toString(), cause instanceof RuntimeException);
        }

        // check called handlers
        int [] called = {0,1,3,4,5,7,7,5};
        int [] closed = {7,5,4,3,1,0};
        List<String> calledHandlers = tracker.getCalledHandlers();
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + called[i], calledHandlers.get(i));
        }

        // check closed handlers
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("Did not get proper number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], closedHandlers.get(i));
        }

        // check destroyed handlers -- none in jaxws 2.0
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("destroyed handler list should be empty",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have one of the client handlers throw a runtime exception
     * and check that the proper methods are called. This test
     * is on inbound message. Testing with logical handler.
     */
    public void testClientInboundRuntimeException2() throws Exception {
        int badHandler = 3;
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX + badHandler,
            HA_THROW_RUNTIME_EXCEPTION_INBOUND);

        try {
            testStub.testInt(42);
            fail("should have received a runtime exception");
        } catch (WebServiceException e) {
            Throwable cause = e.getCause();
            assertNotNull("cause of exception is null", cause);
            assertTrue("cause should be runtime exception, instead is " +
                cause.getClass().toString(), cause instanceof RuntimeException);
        }

        // check called handlers
        int [] called = {0,1,3,4,5,7,7,5,4,3};
        int [] closed = {7,5,4,3,1,0};
        List<String> calledHandlers = tracker.getCalledHandlers();
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + called[i], calledHandlers.get(i));
        }

        // check closed handlers
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("Did not get proper number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], closedHandlers.get(i));
        }

        // check destroyed handlers -- none in jaxws 2.0
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("destroyed handler list should be empty",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have one of the client handlers throw a proper exception
     * and check that the proper methods are called. This test
     * is on inbound message. Testing with soap handler.
     */
    public void testClientInboundProtocolException1() throws Exception {
        int badHandler = 5;
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX + badHandler,
            HA_THROW_PROTOCOL_EXCEPTION_INBOUND);

        try {
            testStub.testInt(42);
            fail("should have received a web service exception");
        } catch (ProtocolException e) {
            assertTrue("ProtocolException message not as expected, but got:" +
                    e.getMessage(), 
                    e.getMessage().contains(CLIENT_PREFIX + badHandler));
        }

        // check called handlers
        int [] called = {0,1,3,4,5,7,7,5};
        int [] closed = {7,5,4,3,1,0};
        List<String> calledHandlers = tracker.getCalledHandlers();
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + called[i], calledHandlers.get(i));
        }

        // check closed handlers
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("Did not get proper number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], closedHandlers.get(i));
        }

        // check destroyed handlers -- none in jaxws 2.0
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("destroyed handler list should be empty",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have one of the client handlers throw a proper exception
     * and check that the proper methods are called. This test
     * is on inbound message. Testing with logical handler.
     *
     * Is this a valid test case?
     */
    public void testClientInboundProtocolException2() throws Exception {
        int badHandler = 1;
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX + badHandler,
            HA_THROW_PROTOCOL_EXCEPTION_INBOUND);

        try {
            testStub.testInt(42);
            fail("should have received a web service exception");
        } catch (ProtocolException e) {
            assertTrue("ProtocolException message not as expected, but got:" +
                    e.getMessage(), 
                    e.getMessage().contains(CLIENT_PREFIX + badHandler));
        }

        // check called handlers
        int [] called = {0,1,3,4,5,7,7,5,4,3,1};
        int [] closed = {7,5,4,3,1,0};
        List<String> calledHandlers = tracker.getCalledHandlers();
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + called[i], calledHandlers.get(i));
        }

        // check closed handlers
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("Did not get proper number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], closedHandlers.get(i));
        }

        // check destroyed handlers -- none in jaxws 2.0
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("destroyed handler list should be empty",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have one of the client handlers throw a protocol exception
     * and check that the proper methods are called. This test
     * is on outbound message with 2.0 handler (so handleFault
     * should not be called on the handler that throws the
     * exception). Testing with soap handler.
     *
     * Exception should result in a fault in the message, which
     * is dispatched to and wrapped by the client.
     *
     * This test checks the fault code as well -- test for
     * bug 6350633.
     */
    public void testClientOutboundProtocolException1() throws Exception {
        String client = "Client"; // the expected fault code local part
        
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX + 5,
            HA_THROW_PROTOCOL_EXCEPTION_OUTBOUND);

        try {
            testStub.testInt(42);
            fail("did not receive an exception");
        } catch (SOAPFaultException e) {
            SOAPFault fault = e.getFault();
            String faultCode = fault.getFaultCode();
            assertTrue("fault code should end with \"" + client +
                "\": " + faultCode,
                faultCode.endsWith(client));
        }

        // check result
        String [] called = {"0", "1", "3", "4", "5",
            "4_FAULT", "3_FAULT", "1_FAULT", "0_FAULT"};
        int [] closed = {5,4,3,1,0};
        List<String> calledHandlers = tracker.getCalledHandlers();
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + called[i], calledHandlers.get(i));
        }
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("Did not get proper number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], closedHandlers.get(i));
        }

        // should be no destroyed handlers
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("Should be 0 destroyed handlers",
            destroyedHandlers.isEmpty());
    }

    /*
     * Same as testClientOutboundProtocolException1 except that
     * it uses the first soap handler in the chain to make sure
     * it skips to the logical handlers without an array index error.
     */
    public void testClientOutboundProtocolException2() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX + 4,
            HA_THROW_PROTOCOL_EXCEPTION_OUTBOUND);

        try {
            testStub.testInt(42);
            fail("did not receive an exception");
        } catch (SOAPFaultException e) {
            assertTrue(true);
        }

        // check result
        String [] called = {"0", "1", "3", "4",
            "3_FAULT", "1_FAULT", "0_FAULT"};
        int [] closed = {4,3,1,0};
        List<String> calledHandlers = tracker.getCalledHandlers();
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + called[i], calledHandlers.get(i));
        }
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("Did not get proper number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], closedHandlers.get(i));
        }

        // should be no destroyed handlers
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("Should be 0 destroyed handlers",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have one of the client handlers throw a SOAPFault exception
     * and check that the proper methods are called.
     */
    public void testClientOutboundProtocolException3() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX+5,
            HA_THROW_SOAP_FAULT_EXCEPTION_OUTBOUND);

        try {
            testStub.testInt(42);
        } catch (SOAPFaultException e) {
            // ok
        } catch (Exception oops) {
            fail("did not receive WebServiceException. received: " + oops);
        }

        // check result
        String [] called = {"0", "1", "3", "4", "5",
            "4_FAULT", "3_FAULT", "1_FAULT", "0_FAULT"};
        int [] closed = {5,4,3,1,0};
        List<String> calledHandlers = tracker.getCalledHandlers();
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + called[i], calledHandlers.get(i));
        }
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("Did not get proper number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], closedHandlers.get(i));
        }

        // should be no destroyed handlers
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("Should be 0 destroyed handlers",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have the first handler throw a protocol exception.
     * Regression test for bug 6225892. Same test as
     * testClientOutboundProtocolException2 except with
     * first logical handler in chain.
     */
    public void testClientOutboundProtocolException4() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX+0,
            HA_THROW_PROTOCOL_EXCEPTION_OUTBOUND);

        try {
            testStub.testInt(42);
            fail("did not receive an exception");
        } catch (SOAPFaultException e) {
            // ok
            int breakPointDummyLine = 0;
        }

        // check result
        String [] called = {"0"};
        int [] closed = {0};
        List<String> calledHandlers = tracker.getCalledHandlers();
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + called[i], calledHandlers.get(i));
        }
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("Did not get proper number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], closedHandlers.get(i));
        }

        // should be no destroyed handlers
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("Should be 0 destroyed handlers",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have one of the server handlers throw a simple protocol
     * exception and check that the proper methods are called.
     *
     * This test checks the fault code as well -- related to
     * bug 6350633.
     */
    public void testServerInboundProtocolException1() throws Exception {
        String server = "Server"; // the expected fault code local part
        
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + 2,
            HA_THROW_PROTOCOL_EXCEPTION_INBOUND);

        // so we clear out the client handlers afterwards
        tracker.clearAll();

        try {
            testStub.testInt(3);
            fail("did not receive exception");
        } catch (SOAPFaultException e) {
            SOAPFault fault = e.getFault();
            String faultCode = fault.getFaultCode();
            assertTrue("fault code should end with \"" + server +
                "\": " + faultCode,
                faultCode.endsWith(server));
        }

        // check result
        String [] called = {"4", "2", "4_FAULT"};
        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                SERVER_PREFIX + called[i], calledHandlers.get(i));
        }

        // too many closes to check them all

        // should be no destroyed handlers
        List<String> destroyedHandlers =
            reportStub.getReport(REPORT_DESTROYED_HANDLERS);
        assertEquals("Should be 0 destroyed handlers",
            0, destroyedHandlers.size());
    }

    /*
     * Have one of the server handlers throw a soap fault
     * exception and check that the proper methods are called.
     */
    public void testServerInboundSoapFaultException1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + 2,
            HA_THROW_SOAP_FAULT_EXCEPTION_INBOUND);

        // so we clear out the client handlers afterwards
        tracker.clearAll();

        try {
            testStub.testInt(3);
            fail("did not receive any exception");
        } catch (SOAPFaultException e) {
            // check some details
            SOAPFault fault = e.getFault();
            assertEquals("did not get proper fault actor",
                "faultActor", fault.getFaultActor());
            assertEquals("did not get proper fault string",
                "fault", fault.getFaultString());
            assertEquals("did not get proper fault code",
                new QName("uri", "local", "prefix"),
                fault.getFaultCodeAsQName());
        }

        // check result
        String [] called = {"4", "2", "4_FAULT"};
        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                SERVER_PREFIX + called[i], calledHandlers.get(i));
        }

        // should be no destroyed handlers
        List<String> destroyedHandlers =
            reportStub.getReport(REPORT_DESTROYED_HANDLERS);
        assertTrue("Should be 0 destroyed handlers",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have one of the server handlers throw a soap protocol
     * exception and check that the proper methods are called.
     * This version removes all the client-side handlers to
     * test a differnt (simpler) path through the client code.
     */
    public void testServerInboundSOAPExceptionNoClientHandlers()
        throws Exception {
        
        TestService_Service service = getService();

        // remove the client handlers before creating stubs
        clearHandlersInService(service);

        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX+2,
            HA_THROW_SOAP_FAULT_EXCEPTION_INBOUND);

        try {
            testStub.testInt(3);
            fail("did not receive an exception");
        } catch (SOAPFaultException e) {
            SOAPFault fault = e.getFault();
            assertEquals("did not get proper fault actor",
                "faultActor", fault.getFaultActor());
            assertEquals("did not get proper fault string",
                "fault", fault.getFaultString());
            assertEquals("did not get proper fault code",
                new QName("uri", "local", "prefix"),
                fault.getFaultCodeAsQName());
        }

        // check result
        String [] called = {"4", "2", "4_FAULT"};
        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                SERVER_PREFIX + called[i], calledHandlers.get(i));
        }

        // should be no destroyed handlers
        List<String> destroyedHandlers =
            reportStub.getReport(REPORT_DESTROYED_HANDLERS);
        assertEquals("Should be 0 destroyed handlers",
            0, destroyedHandlers.size());
    }

    /*
     * Have one of the server handlers throw a runtime
     * exception and check that the proper methods are called.
     * This test uses a logical handler.
     */
    public void testServerInboundRuntimeException1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + 1,
            HA_THROW_RUNTIME_EXCEPTION_INBOUND);

        // so we clear out the client handlers afterwards
        tracker.clearAll();

        try {
            testStub.testInt(3);
            fail("did not receive exception");
        } catch (ProtocolException e) {
            // pass
        }

        // check result
        String [] called = {"4", "2", "1"};
        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                SERVER_PREFIX + called[i], calledHandlers.get(i));
        }

        // should be no destroyed handlers
        List<String> destroyedHandlers =
            reportStub.getReport(REPORT_DESTROYED_HANDLERS);
        assertTrue("Should be no destroyed handlers",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have one of the server handlers throw a runtime
     * exception and check that the proper methods are called.
     * This test uses a protocol handler.
     */
    public void testServerInboundRuntimeException2() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + 2,
            HA_THROW_RUNTIME_EXCEPTION_INBOUND);

        // so we clear out the client handlers afterwards
        tracker.clearAll();

        try {
            testStub.testInt(3);
            fail("did not receive exception");
        } catch (ProtocolException e) {
            // ok
        }

        // check result
        String [] called = {"4", "2"};
        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                SERVER_PREFIX + called[i], calledHandlers.get(i));
        }

        // should be no destroyed handlers
        List<String> destroyedHandlers =
            reportStub.getReport(REPORT_DESTROYED_HANDLERS);
        assertTrue("Should be no destroyed handlers",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have one of the server handlers throw a simple protocol
     * exception and check that the proper methods are called.
     */
    public void testServerOutboundProtocolException1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + 2,
            HA_THROW_PROTOCOL_EXCEPTION_OUTBOUND);

        // so we clear out the client handlers afterwards
        tracker.clearAll();

        try {
            testStub.testInt(3);
            fail("did not receive exception");
        } catch (ProtocolException e) {
            // ok
        }

        // check result
        String [] called = {"4", "2", "1", "0", "0", "1", "2"};
        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                SERVER_PREFIX + called[i], calledHandlers.get(i));
        }

        // too many closes to check them all

        // should be no destroyed handlers
        List<String> destroyedHandlers =
            reportStub.getReport(REPORT_DESTROYED_HANDLERS);
        assertTrue("Should be no destroyed handlers",
            destroyedHandlers.isEmpty());
    }

    /*
     * Have one of the server handlers throw a soap protocol
     * exception and check that the proper methods are called.
     */
    public void testServerOutboundSOAPFault1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + 2,
            HA_THROW_SOAP_FAULT_EXCEPTION_OUTBOUND);

        // clear out the client handlers afterwards and set instructions
        tracker.clearAll();
        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }

        try {
            testStub.testInt(3);
            fail("did not receive exception");
        } catch (ProtocolException e) {
            // ok
        }

        // check result
        List<String> clientCalledHandlers = tracker.getCalledHandlers();
        List<String> clientClosedHandlers = tracker.getClosedHandlers();

        // check client handlers
        String [] clientCalled = {"0", "1", "3", "4", "5", "7",
            "7_FAULT", "5_FAULT", "4_FAULT", "3_FAULT", "1_FAULT", "0_FAULT"};
        int [] clientClosed = {7,5,4,3,1,0};
        assertEquals("Did not get proper number of called handlers",
            clientCalled.length, clientCalledHandlers.size());
        for (int i=0; i<clientCalled.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + clientCalled[i], clientCalledHandlers.get(i));
        }
        assertEquals("Did not get proper number of closed handlers",
            clientClosed.length, clientClosedHandlers.size());
        for (int i=0; i<clientClosed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + clientClosed[i], clientClosedHandlers.get(i));
        }

        // check server after client because it makes calls through the handlers
        String [] serverCalled = {"4", "2", "1", "0", "0", "1", "2"};
        List<String> serverCalledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        assertEquals("Did not get proper number of called handlers",
            serverCalled.length, serverCalledHandlers.size());
        for (int i=0; i<serverCalled.length; i++) {
            assertEquals("did not find expected handler",
                SERVER_PREFIX + serverCalled[i], serverCalledHandlers.get(i));
        }

        // too many closes on server to check them all

        // should be no destroyed handlers
        List<String> destroyedHandlers =
            reportStub.getReport(REPORT_DESTROYED_HANDLERS);
        assertEquals("Should be 0 destroyed handlers",
            0, destroyedHandlers.size());
    }

    /*
     * Test to make sure mustUnderstand headers of the right type get
     * through with no fault.
     */
    public void testValidMustUnderstand() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX+i, HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX+2,
            HA_ADD_GOOD_MU_HEADER_OUTBOUND);

        // so we clear out the client handlers afterwards
        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX+5,
            HA_ADD_GOOD_MU_HEADER_OUTBOUND);

        // should get no fault
        int result = testStub.testInt(5);
        assertEquals("did not get int echoed back from server", 5, result);
    }

    /*
     * Test to make sure mustUnderstand headers that are not understood
     * cause a fault.
     */
    public void testServerMustUnderstand1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX+i, HA_REGISTER_HANDLE_XYZ);
        }

        // so we clear out the client handlers afterwards
        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX+5,
            HA_ADD_BAD_MU_HEADER_OUTBOUND);

        // should get fault
        try {
            int result = testStub.testInt(5);
            fail("did not receive remote exception");
        } catch (WebServiceException e) {
            // ok
        }
    }

    /*
     * Test to make sure mustUnderstand headers that are not understood
     * cause a fault. This test uses the "next" role.
     */
    public void testClientMustUnderstand1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        reportStub.setInstruction(SERVER_PREFIX+4,
            HA_ADD_BAD_MU_HEADER_OUTBOUND);

        // so we clear out the client handlers afterwards
        tracker.clearAll();

        // should get fault
        try {
            int result = testStub.testInt(5);
            fail("did not receive soap exception");
        } catch (WebServiceException e) {
            // ok
        }

        // check called and closed handlers
        List<String> closedHandlers = tracker.getClosedHandlers();

        // check client handlers
        int [] closed = {7,5,4,3,1,0};
        assertEquals("Did not get proper number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], closedHandlers.get(i));
        }
    }

    /*
     * Test to make sure mustUnderstand headers that are not understood
     * cause a fault. This test uses the "client2" role.
     */
    public void testClientMustUnderstand2() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        reportStub.setInstruction(SERVER_PREFIX+4,
            HA_ADD_BAD_MU_HEADER_CLIENT2_OUTBOUND);

        // so we clear out the client handlers afterwards
        tracker.clearAll();

        // should get fault
        try {
            int result = testStub.testInt(5);
            fail("did not receive soap exception");
        } catch (WebServiceException e) {
            // ok
        }

        // check called and closed handlers
        List<String> closedHandlers = tracker.getClosedHandlers();

        // check client handlers
        int [] closed = {7,5,4,3,1,0};
        assertEquals("Did not get proper number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], closedHandlers.get(i));
        }
    }

    /*
     * Have the service endpoint throw a runtime exception and check
     * that handleFault is called properly on server handlers.
     */
    public void testServiceRuntimeException1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX+i, HA_REGISTER_HANDLE_XYZ);
        }
        HandlerTracker.getClientInstance().clearAll();

        try {
            testStub.testInt(SERVER_THROW_RUNTIME_EXCEPTION);
            fail("did not receive remote exception");
        } catch (SOAPFaultException sfe) {
            SOAPFault fault = sfe.getFault();
            assertNotNull("SOAPFault should not be null", fault);
            String msg = fault.getFaultString();
            assertNotNull("fault string should not be null");
        }
        
        // check that the proper methods were called on server handlers
        String [] called = {"4", "2", "1", "0",
            "0_FAULT", "1_FAULT", "2_FAULT", "4_FAULT"};
        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                SERVER_PREFIX + called[i], calledHandlers.get(i));
        }
        
    }

    /*
     * Test that when a handler changes a message to a fault *and*
     * throws a protocol exception, the information in the fault
     * gets back to the client rather than the exception. Server-
     * side test for bug 6232841.
     */
    public void testServiceFaultAndProtocolException1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        reportStub.clearHandlerTracker();
        reportStub.setInstruction(SERVER_PREFIX+2,
            HA_INSERT_FAULT_AND_THROW_PE_INBOUND);
        HandlerTracker.getClientInstance().clearAll();

        try {
            testStub.testInt(0);
            fail("did not receive expected exception");
        } catch (SOAPFaultException sfe) {
            SOAPFault fault = sfe.getFault();
            assertNotNull("SOAPFault should not be null", fault);
            String msg = fault.getFaultString();
            assertNotNull("fault string should not be null");
            if (msg.equals(MESSAGE_IN_FAULT)) {
                // passed
            } else if (msg.equals(MESSAGE_IN_EXCEPTION)) {
                fail("Received exception info instead of fault info");
            } else {
                fail("Did not receive expected exception");
            }
        }
    }

    /*
     * Have the endpoint throw a service specific exception and make
     * sure that the client gets it back. Test case for bug 6232002.
     */
    public void testServiceSpecificException1() throws Exception {
        TestService_Service service = getService();
        HandlerTracker tracker = HandlerTracker.getClientInstance();

        // get stubs and clear the trackers
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        reportStub.clearHandlerTracker();
        tracker.clearAll();

        try {
            testStub.testInt(SERVER_THROW_MYFAULT_EXCEPTION);
            fail("did not receive exception (1)");
        } catch (MyFaultException mfe) {
            // passed
        } catch (Exception e) {
            fail("did not receive MyFaultException (1), received " + e);
        }

        // check closed handlers to be sure
        List<String> actualClosed = tracker.getClosedHandlers();
        int [] closed = {7,5,4,3,1,0};
        assertEquals("Did not get proper number of closed handlers",
            closed.length, actualClosed.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not find expected handler",
                CLIENT_PREFIX + closed[i], actualClosed.get(i));
        }

        // remove all client handlers and try again
        Binding binding = ((BindingProvider) testStub).getBinding();
        binding.setHandlerChain(new ArrayList<Handler>());
        tracker.clearAll();
        try {
            testStub.testInt(SERVER_THROW_MYFAULT_EXCEPTION);
            fail("did not receive exception (2)");
        } catch (MyFaultException mfe) {
            // passed
        } catch (Exception e) {
            fail("did not receive MyFaultException (2), received " + e);
        }

        // this just makes sure there really were no handlers
        actualClosed = tracker.getClosedHandlers();
        assertTrue("should not have been closed handlers",
            actualClosed.isEmpty());
    }

}
