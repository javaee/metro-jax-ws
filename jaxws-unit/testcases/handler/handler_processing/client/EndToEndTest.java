/**
 * $Id: EndToEndTest.java,v 1.2 2008-03-06 02:38:15 ramapulavarthi Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package handler.handler_processing.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.WebServiceException;

import handler.handler_processing.common.BaseSOAPHandler;
import handler.handler_processing.common.HandlerTracker;

import junit.framework.*;

/**
 *
 * Tests End to End Handler Processing, checks handler invocation order and closing order.
 * Tests of handler execution, flow, etc.
 */
public class EndToEndTest extends TestCaseBase {

    /*
     * main method for debugging
     */
    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            EndToEndTest tester = new EndToEndTest("EndToEndTest");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public EndToEndTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(EndToEndTest.class);
        return suite;
    }

    /*
     * Test to make sure handlers have close() called properly.
     */
    public void testHandlerCloseOrder() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService testStub = getTestStub(getService());
        int x = 1;
        tracker.clearAll();
        int y = testStub.testInt(x);
        assertEquals("something wrong with testInt service", x, y);
        List<String> closedHandlers = tracker.getClosedHandlers();
        String [] expectedNames = new String[] { CLIENT_PREFIX + 7,
            CLIENT_PREFIX + 5,
            CLIENT_PREFIX + 4,
            CLIENT_PREFIX + 3,
            CLIENT_PREFIX + 1,
            CLIENT_PREFIX + 0 };
        for (int i=0; i<expectedNames.length; i++) {
            assertEquals("closed handler names not matching",
                expectedNames[i], closedHandlers.get(i));
        }
    }

    /*
     * Check properties in MessageContext
     */
    public void testMessageContextProperties() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService testStub = getTestStub(getService());
        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX + "0",
            HA_CHECK_MC_PROPS);
        tracker.setHandlerAction(CLIENT_PREFIX + "1",
            HA_CHECK_MC_PROPS);
        tracker.setHandlerAction(CLIENT_PREFIX + "3",
            HA_CHECK_MC_PROPS);
        tracker.setHandlerAction(CLIENT_PREFIX + "4",
            HA_CHECK_MC_PROPS);
        tracker.setHandlerAction(CLIENT_PREFIX + "5",
            HA_CHECK_MC_PROPS);
        tracker.setHandlerAction(CLIENT_PREFIX + "7",
            HA_CHECK_MC_PROPS);
        testStub.testInt(2);
    }

    /*
     * Check SOAPMessageContext in handlers on client side.
     */
    public void testSOAPMessageContextInformationClient() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService testStub = getTestStub(getService());
        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX + "4",
            HA_CHECK_SMC);
        tracker.setHandlerAction(CLIENT_PREFIX + "5",
            HA_CHECK_SMC);
        tracker.setHandlerAction(CLIENT_PREFIX + "7",
            HA_CHECK_SMC);
        testStub.testInt(3);
    }

    /*
     * Check SOAPMessageContext in handlers on server side.
     */
    public void testSOAPMessageContextInformationServer() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService testStub = getTestStub(getService());
        ReportService reportStub = getReportStub(getService());

        reportStub.clearHandlerTracker();
        reportStub.setInstruction(SERVER_PREFIX + "2", HA_CHECK_SMC);
        reportStub.setInstruction(SERVER_PREFIX + "4", HA_CHECK_SMC);
        tracker.clearAll();
        testStub.testInt(4);
    }

    /**
     * Testcase for https://jax-ws.dev.java.net/issues/show_bug.cgi?id=457
     *
     */
    public void testAddMimeHeadersInSOApMessage() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService testStub = getTestStub(getService());
        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX + "4",HA_ADD_MIMEHEADER_OUTBOUND);
        ReportService reportStub = getReportStub(getService());
        reportStub.clearHandlerTracker();
        reportStub.setInstruction(SERVER_PREFIX + "4", HA_CHECK_MIMEHEADER_INBOUND);

        testStub.testInt(2);
    }

    /*
     * Check LogicalMessageContext in handlers
     */
    public void testLogicalMessageContextInformation() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService testStub = getTestStub(getService());
        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX + "0",
            HA_CHECK_LMC);
        tracker.setHandlerAction(CLIENT_PREFIX + "1",
            HA_CHECK_LMC);
        tracker.setHandlerAction(CLIENT_PREFIX + "3",
            HA_CHECK_LMC);
        testStub.testInt(4);
    }

    /*
     * Make sure the right number of client side handlers are in place
     */
    public void testClientHandlers1() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService_Service service = getService();
        TestService stub = getTestStub(service);

        // check for the correct number
        int x = 0;
        for (int i=0; i<numTotalHandlers; i++) { // just set them all
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_ADD_ONE);
        }
        int y = stub.testInt(x);
        int diff = 2 * numTestHandlers; // handlers times 2 messages (in/out)
        assertEquals("error in number of handlers working", x + diff, y);

        // check for the correct order
        tracker.clearCalledHandlers();
        for (int i=0; i<numTotalHandlers; i++) { // just set them all
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        stub.testInt(-1);
        List<String> calledHandlers = tracker.getCalledHandlers();
        assertEquals("did not get the right number of called handlers",
            2 * numTestHandlers, calledHandlers.size());
        int [] calledNames = {0,1,3,4,5,7,7,5,4,3,1,0};
        for (int i=0; i<calledNames.length; i++) {
            assertEquals("did not get expected handler name",
                CLIENT_PREFIX + calledNames[i],
                calledHandlers.get(i));
        }

        // check destroyed handlers (not really needed in jaxws 2.0)
        List<String> destroyedHandlers = tracker.getDestroyedHandlers();
        assertTrue("should be 0 handlers destroyed",
            destroyedHandlers.isEmpty());
    }

    /*
     * Make sure the right number of server side handlers are in place
     */
    public void testServerHandlers1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        // check for the correct number
        for (int i=0; i<numTotalServerHandlers; i++) { // just set them all
            reportStub.setInstruction(SERVER_PREFIX + i, HA_ADD_ONE);
        }
        int x = 0;
        int y = testStub.testInt(x);
        int diff = 2 * numTestServerHandlers; // handlers times 2 messages
        assertEquals("error in number of server handlers working", x + diff, y);

        // check for the correct order
        for (int i=0; i<5; i++) { // just set them all
            reportStub.setInstruction(SERVER_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.clearCalledHandlers();
        testStub.testInt(-1);
        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        if (HandlerTracker.VERBOSE_HANDLERS) {
            System.out.println("\ncalled handlers:");
            for (int i=0; i<calledHandlers.size(); i++) {
                System.out.println("\t" + calledHandlers.get(i));
            }
        }
        assertEquals("did not get the right number of called handlers",
            2 * numTestServerHandlers, calledHandlers.size());
        int [] calledNames = {4,2,1,0,0,1,2,4};
        for (int i=0; i<calledNames.length; i++) {
            assertEquals("did not get expected handler name",
                SERVER_PREFIX + calledNames[i],
                calledHandlers.get(i));
        }
    }

    /*
     * Make sure the right number of client side handlers are
     * in place for a one-way call.
     */
    public void testClientHandlers2() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService_Service service = getService();
        TestService stub = getTestStub(service);

        tracker.clearAll();
        for (int i=0; i<numTotalHandlers; i++) { // just set them all
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        stub.testIntOneWay(0);

        List<String> calledHandlers = tracker.getCalledHandlers();
        assertEquals("did not get the right number of called handlers",
            numTestHandlers, calledHandlers.size());
        int [] calledNames = {0,1,3,4,5,7};
        for (int i=0; i<calledNames.length; i++) {
            assertEquals("did not get expected handler name",
                CLIENT_PREFIX + calledNames[i],
                calledHandlers.get(i));
        }
    }

    /*
     * Make sure the right number of server side handlers are in
     * place for a one-way call.
     */
    public void testServerHandlers2() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.clearCalledHandlers();
        testStub.testIntOneWay(0);

        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        assertEquals("did not get the right number of called handlers",
            numTestServerHandlers, calledHandlers.size());
        int [] calledNames = {4,2,1,0};
        for (int i=0; i<calledNames.length; i++) {
            assertEquals("did not get expected handler name",
                SERVER_PREFIX + calledNames[i],
                calledHandlers.get(i));
        }
    }

    /*
     * Have one of the client handlers return false.
     */
    public void testClientOutboundReturnFalse1() throws Exception {
        TestService testStub = getTestStub(getService());

        HandlerTracker tracker = HandlerTracker.getClientInstance();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX+5, HA_RETURN_FALSE);

        try {
            // This should throw some client-side exception since
            // the handler does not replace the message with a response.
            testStub.testInt(42);
            fail("did not get exception as expected");
        } catch (WebServiceException e) {
            System.out.println(e.getMessage());
            // make sure the message says what the problem is
            // In this case should be, unexpected XML tag. expected: {urn:test:types}TestIntResponse
            // but found: {urn:test:types}TestInt
            assertTrue(e.getMessage().contains("TestIntResponse"));
        }

        // check called handlers
        int [] called = {0,1,3,4,4,3,1,0}; // client 5 will not register
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
    }

    /*
     * Have one of the client handlers return false and change
     * the message contents to look like a reply. Should get 0
     * back as result and proper handlers were called and closed.
     */
    public void testClientOutboundReturnFalse2() throws Exception {
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
            HA_RETURN_FALSE_CHANGE_MESSAGE);

        int result = testStub.testInt(0);
        assertEquals("did not get expected value back", 0, result);

        // check called handlers
        int [] called = {0,1,3,4,5,4,3,1,0}; // 5 only called once
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
    }

    /*
     * Have one of the client handlers return false and change
     * the message contents to look like a reply. Should get 0
     * back as result and proper handlers were called and closed.
     * Have first handler return false. Test for bug 6232834.
     */
    public void testClientOutboundReturnFalse3() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        HandlerTracker tracker = HandlerTracker.getClientInstance();

        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX + 0,
            HA_RETURN_FALSE_CHANGE_MESSAGE);

        int result = testStub.testInt(0);
        assertEquals("did not get expected value back", 0, result);

        // check called handlers
        int [] called = {0};
        int [] closed = {0};
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
    }

    /*
     * Have one of the client handlers return false and change
     * the message contents to look like a reply. Then have
     * another handler return false during the now incoming
     * message. Further handler processing should stop.
     *
     * First handler 5 returns false. Message should then be
     * inbound. Handler 3 should see that the message is
     * inbound and will return false. Then handlers 1 and 0
     * should be skipped.
     *
     * Test for bug 6381858.
     */
    public void testClientOutInReturnFalse1() throws Exception {
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
            HA_RETURN_FALSE_CHANGE_MESSAGE);
        tracker.setHandlerAction(CLIENT_PREFIX + 3,
            HA_RETURN_FALSE_INBOUND);

        int result = testStub.testInt(0);
        assertEquals("did not get expected value back", 0, result);

        // check called handlers
        int [] called = {0,1,3,4,5,4,3}; // 5 only called once
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
    }

    /*
     * Have one of the client handlers return false during a
     * one-way request. The handler chain caller should stop
     * calling handlers and dispatch the message to the endpoint.
     *
     * Also check the server handlers to make sure the message
     * went through to the endpoint.
     */
    public void testClientOneWayReturnFalse1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        HandlerTracker tracker = HandlerTracker.getClientInstance();

        reportStub.clearHandlerTracker();
        tracker.clearAll();

        // tell server handlers to register
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        tracker.clearAll();
        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX + 0, HA_RETURN_FALSE);

        // make one-way call
        testStub.testIntOneWay(0);

        // handler 0 won't register
        int [] closed = { 0 };
        List<String> calledHandlers = tracker.getCalledHandlers();
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("did not get the right number of called handlers",
            0, calledHandlers.size());
        assertEquals("did not get the right number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not get expected handler name",
                CLIENT_PREFIX + closed[i],
                closedHandlers.get(i));
        }

        // check server handlers
        tracker.clearAll();
        int [] serverExpected = { 4, 2, 1, 0 };
        List<String> serverCalled =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        assertEquals("did not get the right number of called handlers",
            serverExpected.length, serverCalled.size());
        for (int i=0; i<serverExpected.length; i++) {
            assertEquals("did not get expected handler name",
                SERVER_PREFIX + serverExpected[i],
                serverCalled.get(i));
        }
    }

    /*
     * Have one of the client handlers return false during a
     * one-way request. The handler chain caller should stop
     * calling handlers and dispatch the message to the endpoint.
     *
     * Also check the server handlers to make sure the message
     * went through to the endpoint.
     */
    public void testClientOneWayReturnFalse2() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        HandlerTracker tracker = HandlerTracker.getClientInstance();

        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX + 3, HA_RETURN_FALSE);

        testStub.testIntOneWay(0);

        int [] calledNames = { 0, 1 }; // handler 3 won't register
        int [] closed = { 3, 1, 0 };
        List<String> calledHandlers = tracker.getCalledHandlers();
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("did not get the right number of called handlers",
            calledNames.length, calledHandlers.size());
        for (int i=0; i<calledNames.length; i++) {
            assertEquals("did not get expected handler name",
                CLIENT_PREFIX + calledNames[i],
                calledHandlers.get(i));
        }
        assertEquals("did not get the right number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not get expected handler name",
                CLIENT_PREFIX + closed[i],
                closedHandlers.get(i));
        }
    }

    /*
     * Have one of the client handlers return false during a
     * one-way request. The handler chain caller should stop
     * calling handlers and dispatch the message to the endpoint.
     */
    public void testClientOneWayReturnFalse3() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        HandlerTracker tracker = HandlerTracker.getClientInstance();

        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX + 4, HA_RETURN_FALSE);

        testStub.testIntOneWay(0);

        int [] calledNames = { 0, 1, 3 }; // handler 4 won't register
        int [] closed = { 4, 3, 1, 0 };
        List<String> calledHandlers = tracker.getCalledHandlers();
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("did not get the right number of called handlers",
            calledNames.length, calledHandlers.size());
        for (int i=0; i<calledNames.length; i++) {
            assertEquals("did not get expected handler name",
                CLIENT_PREFIX + calledNames[i],
                calledHandlers.get(i));
        }
        assertEquals("did not get the right number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not get expected handler name",
                CLIENT_PREFIX + closed[i],
                closedHandlers.get(i));
        }
    }

    /*
     * Have one of the client handlers return false during a
     * one-way request. The handler chain caller should stop
     * calling handlers and dispatch the message to the endpoint.
     */
    public void testClientOneWayReturnFalse4() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        HandlerTracker tracker = HandlerTracker.getClientInstance();

        reportStub.clearHandlerTracker();
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }
        tracker.setHandlerAction(CLIENT_PREFIX + 7, HA_RETURN_FALSE);

        testStub.testIntOneWay(0);

        int [] calledNames = { 0, 1, 3, 4, 5 }; // handler 7 won't register
        int [] closed = { 7, 5, 4, 3, 1, 0 };
        List<String> calledHandlers = tracker.getCalledHandlers();
        List<String> closedHandlers = tracker.getClosedHandlers();
        assertEquals("did not get the right number of called handlers",
            calledNames.length, calledHandlers.size());
        for (int i=0; i<calledNames.length; i++) {
            assertEquals("did not get expected handler name",
                CLIENT_PREFIX + calledNames[i],
                calledHandlers.get(i));
        }
        assertEquals("did not get the right number of closed handlers",
            closed.length, closedHandlers.size());
        for (int i=0; i<closed.length; i++) {
            assertEquals("did not get expected handler name",
                CLIENT_PREFIX + closed[i],
                closedHandlers.get(i));
        }
    }

    /*
     * Have one of the server handlers return false and change
     * the message contents to look like a reply.
     *
     * This is really a test of the handler util
     * changeRequestToResponse() method used in client side tests.
     */
    public void testServerInboundReturnFalse1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        HandlerTracker tracker = HandlerTracker.getClientInstance();

        reportStub.clearHandlerTracker();
        reportStub.setInstruction(SERVER_PREFIX+2,
            HA_RETURN_FALSE_CHANGE_MESSAGE);
        tracker.clearAll();

        for (int i=0; i<numTotalHandlers; i++) {
            tracker.setHandlerAction(CLIENT_PREFIX + i, HA_REGISTER_HANDLE_XYZ);
        }

        int result = testStub.testInt(0);
        assertEquals("did not get expected value back", 0, result);
    }

    /*
     * Have one of the server handlers return false during a
     * one-way request. The handler chain caller should stop
     * calling handlers and dispatch the message to the endpoint.
     */

    public void testServerOneWayReturnFalse1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        HandlerTracker tracker = HandlerTracker.getClientInstance();

        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + 2, HA_RETURN_FALSE);
        tracker.clearAll();

        testStub.testIntOneWay(0);

        int [] calledNames = { 4 }; // handler 2 will not register

        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        assertEquals("did not get the right number of called handlers",
            calledNames.length, calledHandlers.size());
        for (int i=0; i<calledNames.length; i++) {
            assertEquals("did not get expected handler name",
                SERVER_PREFIX + calledNames[i],
                calledHandlers.get(i));
        }
    }

    /*
     *
     * Have one of the server handlers return false during a
     * one-way request. The handler chain caller should stop
     * calling handlers and dispatch the message to the endpoint.
     *
     * Same as testServerInboundReturnFalse2 but with a different
     * handler returning false.
     */

    public void testServerOneWayReturnFalse2() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        HandlerTracker tracker = HandlerTracker.getClientInstance();

        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + 1, HA_RETURN_FALSE);
        tracker.clearAll();

        testStub.testIntOneWay(0);

        int [] calledNames = { 4, 2 }; // handler 1 will not register
        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);
        assertEquals("did not get the right number of called handlers",
            calledNames.length, calledHandlers.size());
        for (int i=0; i<calledNames.length; i++) {
            assertEquals("did not get expected handler name",
                SERVER_PREFIX + calledNames[i],
                calledHandlers.get(i));
        }
    }

    /*
     * Have one of the server handlers return false and check
     * that the proper handlers were called.
     */
    public void testServerOutboundReturnFalse1() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        HandlerTracker tracker = HandlerTracker.getClientInstance();

        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + "0",
            HA_RETURN_FALSE_OUTBOUND);
        tracker.clearAll();

        int result = testStub.testInt(0);
        assertEquals("did not get expected value back", 0, result);

        // check called handlers on server side
        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);

        // server0 called twice, the rest are skipped
        int [] called = { 4, 2, 1, 0, 0 };
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                SERVER_PREFIX + called[i], calledHandlers.get(i));
        }
    }

    /*
     * Have one of the server handlers return false and check
     * that the proper handlers were called.
     */
    public void testServerOutboundReturnFalse2() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        HandlerTracker tracker = HandlerTracker.getClientInstance();

        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + "1",
            HA_RETURN_FALSE_OUTBOUND);
        tracker.clearAll();

        int result = testStub.testInt(0);
        assertEquals("did not get expected value back", 0, result);

        // check called handlers on server side
        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);

        // server0 called twice, the rest are skipped
        int [] called = { 4, 2, 1, 0, 0, 1 };
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                SERVER_PREFIX + called[i], calledHandlers.get(i));
        }
    }

    /*
     * Have one of the server handlers return false and check
     * that the proper handlers were called.
     */
    public void testServerOutboundReturnFalse3() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);
        HandlerTracker tracker = HandlerTracker.getClientInstance();

        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX + "4",
            HA_RETURN_FALSE_OUTBOUND);
        tracker.clearAll();

        int result = testStub.testInt(0);
        assertEquals("did not get expected value back", 0, result);

        // check called handlers on server side
        List<String> calledHandlers =
            reportStub.getReport(REPORT_CALLED_HANDLERS);

        // server0 called twice, the rest are skipped
        int [] called = { 4, 2, 1, 0, 0, 1, 2, 4 };
        assertEquals("Did not get proper number of called handlers",
            called.length, calledHandlers.size());
        for (int i=0; i<called.length; i++) {
            assertEquals("did not find expected handler",
                SERVER_PREFIX + called[i], calledHandlers.get(i));
        }
    }

    /*
     * Sets a property on the request context with a static stub
     * and verifies that the property exists in the handler.
     */
    public void testRequestProperty() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService stub = getTestStub(getService());

        ((BindingProvider) stub).getRequestContext().put(
            USER_CLIENT_PROPERTY_NAME, USER_PROPERTY_CLIENT_SET);

        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX + 5,
            HA_CHECK_FOR_USER_PROPERTY_OUTBOUND);
        stub.testInt(1);
    }

    
    /*
     * Sets a property on the (client side) response handler context
     * and verifies that the client sees it in the response context.
     */
    public void testResponseProperty() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService stub = getTestStub(getService());

        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX + 5,
            HA_ADD_USER_PROPERTY_INBOUND);
        stub.testInt(1);

        Map context = ((BindingProvider) stub).getResponseContext();
        Object testValue = context.get(USER_HANDLER_PROPERTY_NAME);
        assertNotNull("did not receive property in response context",
            testValue);
        String testValueString = (String) testValue;
        assertTrue("property value incorrect. expected ",
            testValueString.equals(USER_PROPERTY_HANDLER_SET));
    }

   
    /*
     * Sets a property on the (client side) response handler context
     * and verifies that the client sees it in the response context.
     * This test uses an async client
     */
    public void testResponsePropertyAsync() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService stub = getTestStub(getService());

        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX + 5,
            HA_ADD_USER_PROPERTY_INBOUND);

        int x = 1;
        Response<TestIntResponse> response = stub.testIntAsync(x);
        System.out.print("waiting for async response");
        while (!response.isDone()) {
            System.out.print(".");
            Thread.sleep(100);
        }
        System.out.println("");

        int y = response.get().getIntout();
        assertEquals("did not get expected response", x, y);

        Map context = response.getContext();
        assertNotNull("response context in Response<?> object is null",
            context);
        Object testValue = context.get(USER_HANDLER_PROPERTY_NAME);
        assertNotNull("did not receive property in response context",
            testValue);
        String testValueString = (String) testValue;
        assertTrue("property value incorrect. expected ",
            testValueString.equals(USER_PROPERTY_HANDLER_SET));
    }

    /*
     * Sets a property on the (client side) response handler context
     * and verifies that the client sees it in the response context.
     * This test uses an async client with an async handler.
     */
    public void testResponsePropertyAsyncHandler() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService stub = getTestStub(getService());

        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX + 5,
            HA_ADD_USER_PROPERTY_INBOUND);

        int x = 1;
        final IntHolder intHolder = new IntHolder();
        intHolder.setValue(x);

        Future<?> response = stub.testIntAsync(x,
            new AsyncHandler<TestIntResponse>() {
            public void handleResponse(Response<TestIntResponse> resp) {
                try {
                    Map context = resp.getContext();
                    if (context == null) {
                        intHolder.setValue(-10);
                        return;
                    }
                    Object testValue = context.get(USER_HANDLER_PROPERTY_NAME);
                    if (testValue == null) {
                        intHolder.setValue(-20);
                        return;
                    }
                    String testValueString = (String) testValue;
                    if (!testValueString.equals(USER_PROPERTY_HANDLER_SET)) {
                        intHolder.setValue(-30);
                        return;
                    }
                    //add 10 to make sure this was called
                    intHolder.setValue(resp.get().getIntout() + 10);
                } catch (Exception e) {
                    e.printStackTrace();
                    intHolder.setValue(-40); // will cause failure
                }
            }
        });
        while (!response.isDone()) { /* wait */ }

        int y = intHolder.getValue();
        assertFalse("response context in Response<?> object is null", y == -10);
        assertFalse("did not receive property in response context", y == -20);
        assertFalse("property value incorrect. expected", y == -30);
        assertFalse("some error occurred in AsyncHandler. see output",
            y == -40);
        assertEquals("did not get expected value back in response", x + 10, y);
    }

    /*
     * Verifies that properties exist in request and response. Also
     * combines some of the above request and response tests.
     */
    public void testClientRequestAndResponseProperties() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService stub = getTestStub(getService());

        ((BindingProvider) stub).getRequestContext().put(
            USER_CLIENT_PROPERTY_NAME, USER_PROPERTY_CLIENT_SET);

        tracker.clearAll();

        // handler looks for client prop in request
        tracker.setHandlerAction(CLIENT_PREFIX + 5,
            HA_CHECK_FOR_USER_PROPERTY_OUTBOUND);

        // handler adds another prop during request
        tracker.setHandlerAction(CLIENT_PREFIX + 7,
            HA_ADD_USER_PROPERTY_OUTBOUND);

        // handler checks for both props and adds third
        tracker.setHandlerAction(CLIENT_PREFIX + 3,
            HA_ADD_AND_CHECK_PROPS_INBOUND);

        stub.testInt(1);

        Map context = ((BindingProvider) stub).getResponseContext();

        Object testValue = context.get(USER_HANDLER_PROPERTY_NAME);
        assertNotNull(
            "did not receive first handler property in response context",
            testValue);
        assertEquals("property value incorrect",
            USER_PROPERTY_HANDLER_SET, (String) testValue);

        // this is the last property added
        testValue = context.get(USER_HANDLER_PROPERTY_NAME + INBOUND);
        assertNotNull(
            "did not receive second handler property in response context",
            testValue);
        assertEquals("property value incorrect",
            USER_PROPERTY_HANDLER_SET + INBOUND, (String) testValue);

    }

    /*
     * Test soap header. This test has a handler add header elements
     * and another handler check for them. Test1 is two client side
     * handlers.
     */
    public void testSOAPHeader1() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService testStub = getTestStub(getService());
        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX + 5, HA_ADD_HEADER_OUTBOUND);
        tracker.setHandlerAction(CLIENT_PREFIX + 7,
            HA_CHECK_FOR_ADDED_HEADER_OUTBOUND);
        testStub.testInt(123);
    }

    /*
     * Test soap header. This test has a handler add header elements
     * and another handler check for them. Test2 starts on the server
     * side.
     */
    public void testSOAPHeader2() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        for (int i=0; i<numTotalServerHandlers; i++) {
            reportStub.setInstruction(SERVER_PREFIX + i,
                HA_REGISTER_HANDLE_XYZ);
        }
        reportStub.setInstruction(SERVER_PREFIX+2, HA_ADD_HEADER_OUTBOUND);

        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX + 7,
            HA_CHECK_FOR_ADDED_HEADER_INBOUND);
        testStub.testInt(123);
    }

    /*
     * Test the allRoles boolean argument of getHeaders()
     * method in SOAPMessageContext.
     */
    public void testAllRoles() throws Exception {
        TestService_Service service = getService();
        TestService testStub = getTestStub(service);
        ReportService reportStub = getReportStub(service);

        HandlerTracker tracker = HandlerTracker.getClientInstance();

        // these lines make calls to the server
        reportStub.clearHandlerTracker();
        reportStub.setInstruction(SERVER_PREFIX+4,
            HA_ADD_HEADER_OUTBOUND_CLIENT_ROLE1);

        // so we clear out the client handlers afterwards
        tracker.clearAll();
        tracker.setHandlerAction(CLIENT_PREFIX+7, HA_CHECK_SMC_ALL_ROLES);

        // first check with the client1 role
        int result = testStub.testInt(5);
        
        // now check without the known role (should get no headers in handler)
        SOAPBinding sBinding = (SOAPBinding)
            ((BindingProvider) testStub).getBinding();
        sBinding.setRoles(new HashSet<String>());
        result = testStub.testInt(5);
    }
    
    static class IntHolder {
        private int value;
        
        public void setValue(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
}
