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

import java.util.List;

import javax.xml.soap.SOAPFault;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.soap.SOAPFaultException;

import handler.handler_processing.common.HandlerTracker;
import handler.handler_processing.common.TestProtocolException;

import junit.framework.*;

/**
 * Used to test handleFault() returns false or throws an
 * exception. Added class to keep EndToEndErrorTest from
 * getting even longer.
 *
 * @author Rama Pulavarthi
 */
public class HandleFaultTest extends TestCaseBase {
    
    /*
     * main method for debugging
     */
    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            HandleFaultTest tester = new HandleFaultTest("HandleFaultTest");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public HandleFaultTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(HandleFaultTest.class);
        return suite;
    }

    /*
     * Have one of the client handlers throw a protocol exception
     * and another handler throw a new exception during the
     * handleFault method. Handler 5 throws protocol, handler 4
     * throws new exception. Should receive the new exception and
     * have proper handlers called.
     *
     * The new exception will be wrapped by the client runtime
     * in a web service exception.
     */
    public void testClientException1() throws Exception {
        TestService testStub = getTestStub(getService());
        
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        tracker.clearAll();
        
        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandleFaultAction(CLIENT_PREFIX+4,
            HF_THROW_RUNTIME_EXCEPTION);
        tracker.setHandlerAction(CLIENT_PREFIX+5,
            HA_THROW_PROTOCOL_EXCEPTION_OUTBOUND);
        
        try {
            testStub.testInt(42);
            fail("did not receive any exception");
        } catch (ProtocolException pe) {
            fail("should not have received original (protocol) exception");
        } catch (WebServiceException wse) {
            Throwable t = wse.getCause();
            assertNotNull("did not receive cause of exception", t);
            assertTrue("did not receive proper cause of exception",
                t instanceof RuntimeException);
            String msg = t.getMessage();
            assertTrue("received exception from wrong handler",
                msg.startsWith(CLIENT_PREFIX + 4));
            assertTrue("did not get proper message in exception: " + msg,
                msg.indexOf("handleFault") != -1);
        }
        
        // check called handlers
        String [] called = {"0","1","3","4","5","4_FAULT"};
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
        
        // check destroyed handlers
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("should only no handlers destroyed",
            destroyedHandlers.isEmpty());
    }

    /*
     * Same as testClientException1 except that a protocol
     * exception is thrown from handleFault instead of
     * a generic runtime exception.
     * ProtocolExceptions are not rewrapped in WebServiceException
     */
    public void testClientException2() throws Exception {
        TestService testStub = getTestStub(getService());
        
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        tracker.clearAll();
        
        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandleFaultAction(CLIENT_PREFIX+4,
            HF_THROW_PROTOCOL_EXCEPTION);
        tracker.setHandlerAction(CLIENT_PREFIX+5,
            HA_THROW_PROTOCOL_EXCEPTION_OUTBOUND);
        
        try {
            testStub.testInt(42);
            fail("did not receive any exception");
        } catch (ProtocolException pe) {
            String msg = pe.getMessage();
            assertTrue("received exception from wrong handler",
                msg.startsWith(CLIENT_PREFIX + 4));
            assertTrue("did not get proper message in exception: " + msg,
                msg.indexOf("handleFault") != -1);
        } catch (WebServiceException wse) {
           fail("did not receive ProtocolException. received: " + wse);            
        } catch (Exception oops) {
            fail("did not receive ProtocolException. received: " + oops);
        }
        
        // check called handlers
        String [] called = {"0","1","3","4","5","4_FAULT"};
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
        
        // check destroyed handlers
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("should be no handlers destroyed",
            destroyedHandlers.isEmpty());
    }
    
    /*
     * Same as testClientException21 except that a
     * test-specific protocol exception is used, and
     * the test checks to make sure the exact exception
     * is wrapped in the web service exception.
     * ProtocolExceptions are not rewrapped in WebServiceException
     */
    public void testClientException3() throws Exception {
        TestService testStub = getTestStub(getService());
        
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        tracker.clearAll();
        
        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandleFaultAction(CLIENT_PREFIX+4,
            HF_THROW_TEST_PROTOCOL_EXCEPTION);
        tracker.setHandlerAction(CLIENT_PREFIX+5,
            HA_THROW_PROTOCOL_EXCEPTION_OUTBOUND);
        
        try {
            testStub.testInt(42);
            fail("did not receive any exception");
        } catch (ProtocolException pe) {
            String msg = pe.getMessage();
            assertTrue("did not receive proper cause of exception. should " +
                "be TestProtocolException, not " + pe.getClass().toString(),
                pe instanceof TestProtocolException);
            assertTrue("received exception from wrong handler",
                msg.startsWith(CLIENT_PREFIX + 4));
            assertTrue("did not get proper message in exception: " + msg,
                msg.indexOf("handleFault") != -1);                
        } catch (WebServiceException wse) {
           fail("did not receive ProtocolException. received: " + wse);            
        } catch (Exception oops) {
            fail("did not receive ProtocolException. received: " + oops);
        }
        
        // check called handlers
        String [] called = {"0","1","3","4","5","4_FAULT"};
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
        
        // check destroyed handlers
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("should be no handlers destroyed",
            destroyedHandlers.isEmpty());
    }
    
    /*
     * Have one of the server handlers throw a protocol exception
     * and another handler throw a different exception during the
     * handleFault method. Handler 2 throws first exception, then
     * handler 4. Should receive the new exception.
     */
    public void testServerException1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        
        // tell the server handlers to register themselves
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        
        // this handler will register being called before throwing PE
        reportStub.setInstruction(SERVER_PREFIX + 2,
            HA_THROW_PROTOCOL_EXCEPTION_INBOUND);
        
        // the HF_ action does not override the HA_ action
        reportStub.setInstruction(SERVER_PREFIX + 4,
            HF_THROW_RUNTIME_EXCEPTION);
        tracker.clearAll();
        
        try {
            testStub.testInt(42);
            fail("did not receive exception");
        } catch (SOAPFaultException sfe) {
            // check which exception came back
            SOAPFault fault = sfe.getFault();
            assertNotNull("did not receive fault in exception", fault);
            String handlerMsg = fault.getFaultString();
            assertNotNull("null message in exception", handlerMsg);
            assertTrue("did not receive the expected exception, received: " +
                handlerMsg, handlerMsg.startsWith(SERVER_PREFIX+4));
        }
        
        // check called handlers on server side
        String [] called = { "4", "2", "4_FAULT" };
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
     * Have one of the server handlers throw a protocol exception
     * and another handler throw a different exception during the
     * handleFault method. Handler 2 throws first exception, then
     * handler 4. Should receive the new exception.
     *
     * Same as testServerException1 except that the handleFault
     * method throws a ProtocolException rather than a generic
     * runtime exception. This is because the SOAPMessageDispatcher
     * assumes that the message already has the proper fault information
     * in it when the exception it catches is a protocol exception.
     */
    public void testServerException2() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        reportStub.setInstruction(SERVER_PREFIX + 2,
            HA_THROW_PROTOCOL_EXCEPTION_INBOUND);
        reportStub.setInstruction(SERVER_PREFIX + 4,
            HF_THROW_PROTOCOL_EXCEPTION);
        tracker.clearAll();
        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        
        try {
            testStub.testInt(42);
            fail("did not receive exception");
        } catch (SOAPFaultException sfe) {
            // check which exception came back
            SOAPFault fault = sfe.getFault();
            assertNotNull("did not receive fault in exception", fault);
            String handlerMsg = fault.getFaultString();
            assertNotNull("null message in exception", handlerMsg);
            assertTrue("did not receive the expected exception, received: " +
                handlerMsg, handlerMsg.startsWith(SERVER_PREFIX+4));
            assertTrue("did not get proper message, got: " + handlerMsg,
                handlerMsg.indexOf("from handleFault") != -1);
        }
        
        // check called handlers on client side
        String [] called = {"0","1","3","4","5","7",
            "7_FAULT","5_FAULT","4_FAULT","3_FAULT","1_FAULT","0_FAULT"};
            
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

        // check destroyed handlers
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("should be no handler destroyed",
            destroyedHandlers.isEmpty());
    }

    /*
     * Same as testServerException2 except that a logical
     * handler throws the first protocol exception, then
     * a soap handler throws the second one from the handleFault
     * method. See testServerException2 description for more detail.
     */
    public void testServerException3() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + 1,
            HA_THROW_PROTOCOL_EXCEPTION_INBOUND);
        reportStub.setInstruction(SERVER_PREFIX + 2,
            HF_THROW_PROTOCOL_EXCEPTION);
        tracker.clearAll();
        
        try {
            testStub.testInt(42);
            fail("did not receive exception");
        } catch (SOAPFaultException sfe) {
            // check which exception came back
            SOAPFault fault = sfe.getFault();
            assertNotNull("did not receive fault in exception", fault);
            String handlerMsg = fault.getFaultString();
            assertNotNull("null message in exception", handlerMsg);
            assertTrue("did not receive the expected exception, received: " +
                handlerMsg, handlerMsg.startsWith(SERVER_PREFIX+2));
            assertTrue("did not get proper message, got: " + handlerMsg,
                handlerMsg.indexOf("from handleFault") != -1);
        }
        
        // check called handlers on server side
        String [] called = { "4", "2", "1", "2_FAULT" };
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
     * Same as testServerException3 except with different
     * handlers throwing the exceptions.
     */
    public void testServerException4() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + 0,
            HA_THROW_PROTOCOL_EXCEPTION_INBOUND);
        reportStub.setInstruction(SERVER_PREFIX + 2,
            HF_THROW_PROTOCOL_EXCEPTION);
        tracker.clearAll();
        
        try {
            testStub.testInt(42);
            fail("did not receive exception");
        } catch (SOAPFaultException sfe) {
            // check which exception came back
            SOAPFault fault = sfe.getFault();
            assertNotNull("did not receive fault in exception", fault);
            String handlerMsg = fault.getFaultString();
            assertNotNull("null message in exception", handlerMsg);
            assertTrue("did not receive the expected exception, received: " +
                handlerMsg, handlerMsg.startsWith(SERVER_PREFIX+2));
            assertTrue("did not get proper message, got: " + handlerMsg,
                handlerMsg.indexOf("from handleFault") != -1);
        }
        
        // check called handlers on server side
        String [] called = { "4", "2", "1", "0", "1_FAULT", "2_FAULT" };
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
     * Same as testServerException4 except with different
     * handlers throwing the exceptions. This one uses
     * two logical handlers.
     */
    public void testServerException5() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + 0,
            HA_THROW_PROTOCOL_EXCEPTION_INBOUND);
        reportStub.setInstruction(SERVER_PREFIX + 1,
            HF_THROW_PROTOCOL_EXCEPTION);
        tracker.clearAll();
        
        try {
            testStub.testInt(42);
            fail("did not receive exception");
        } catch (SOAPFaultException sfe) {
            // check which exception came back
            SOAPFault fault = sfe.getFault();
            assertNotNull("did not receive fault in exception", fault);
            String handlerMsg = fault.getFaultString();
            assertNotNull("null message in exception", handlerMsg);
            assertTrue("did not receive the expected exception, received: " +
                handlerMsg, handlerMsg.startsWith(SERVER_PREFIX+1));
            assertTrue("did not get proper message, got: " + handlerMsg,
                handlerMsg.indexOf("from handleFault") != -1);
        }
        
        // check called handlers on server side
        String [] called = { "4", "2", "1", "0", "1_FAULT" };
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
     * Have one of the client handlers throw a protocol exception
     * and another handler return false during the handleFault
     * method. Handler 5 throws protocol, handler 1 returns
     * false.
     */
    public void testClientReturnFalse1() throws Exception {
        TestService testStub = getTestStub(getService());
        
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        tracker.clearAll();
        
        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandleFaultAction(CLIENT_PREFIX+1, HF_RETURN_FALSE);
        tracker.setHandlerAction(CLIENT_PREFIX+5,
            HA_THROW_PROTOCOL_EXCEPTION_OUTBOUND);
        
        try {
            testStub.testInt(42);
            fail("did not receive an exception");
        } catch (ProtocolException pe) {
            // ok
        } catch (Exception oops) {
            fail("did not receive WebServiceException. received: " + oops);
        }
        
        // check called handlers
        String [] called = {"0","1","3","4","5","4_FAULT","3_FAULT","1_FAULT"};
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
        
        // check destroyed handlers
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertEquals("should be no handlers destroyed", 0,
            destroyedHandlers.size());
    }

    /*
     * Have one of the client handlers insert a fault message
     * with fault string msg1 and throw a protocol
     * exception with message msg2. Another handler should
     * get a message in the handleFault method containing
     * a fault with the msg1 fault string.
     *
     * This tests that the runtime doesn't replace a fault if
     * one is already there.
     *
     * Also checks exception received to make sure the right
     * message is there. Check for bug 6232841.
     */
    public void testClientFaultAndProtocolException1() throws Exception {
        TestService testStub = getTestStub(getService());
        
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        tracker.clearAll();
        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX+7,
            HA_INSERT_FAULT_AND_THROW_PE_OUTBOUND);
        tracker.setHandleFaultAction(CLIENT_PREFIX+4,
            HF_CHECK_FAULT_MESSAGE_STRING);
        
        try {
            testStub.testInt(42);
            fail("did not receive an exception");
        } catch (ProtocolException e) {
            assertTrue("did not get correct message",
                e.getMessage().contains(MESSAGE_IN_FAULT));
        } catch (Exception oops) {
            fail("did not receive WebServiceException. received: " + oops);
        }

        // check called handlers
        String [] called = {"0","1","3","4","5","7",
            "5_FAULT","4_FAULT","3_FAULT","1_FAULT","0_FAULT"};
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
        
        // check destroyed handlers
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertEquals("should be no handlers destroyed", 0,
                destroyedHandlers.size());
    }

    /*
     * Has the service throw an exception, which the runtime converts
     * to a fault, and has the logical and soap handlers both get the
     * body from the context. This is a test of namespace issues when
     * converting from logical to soap messages.
     */
    public void testServerFaultNamespace1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX+i, HA_REGISTER_HANDLE_XYZ);
            reportStub.setInstruction(SERVER_PREFIX+i,
                HF_GET_FAULT_IN_MESSAGE);
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
            assertEquals("did not get proper fault back",
                "test exception", msg);
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
    
}
