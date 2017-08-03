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

package fromwsdl.handler.client;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.*;

import fromwsdl.handler.common.HandlerTracker;
import fromwsdl.handler.common.TestConstants;

import junit.framework.*;

/*
 * Simple class used for working on new runtime. Does not test
 * handler functionality.
 */
public class SimpleHelloTester extends TestCaseBase {

    /*
     * main method for debugging
     */
    public static void main(String [] args) {
        try {
            //System.setProperty("uselocal", "true");
            SimpleHelloTester tester =
                new SimpleHelloTester("SimpleHelloTester");
            tester.testHello();
            tester.testHelloOneWay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SimpleHelloTester(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(fromwsdl.handler.client.SimpleHelloTester.class);
        return suite;
    }

    /*
     * Simple end to end test (mostly for debug work)
     */
    public void testHello() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        tracker.clearAll();

        TestService_Service service = getService();
        TestService test = getTestStub(service);
        ReportService report = getReportStub(service);
        
        report.clearHandlerTracker();

        int foo = -1;
        int bar = test.testInt(foo);
        assertTrue(foo == bar);
        System.out.println("ok");
        
        List<String> closedHandlers =
            report.getReport(TestConstants.REPORT_CLOSED_HANDLERS);
        
        List<String> handlers =
            report.getReport(TestConstants.REPORT_CALLED_HANDLERS);
        assertNotNull("received null list back from server", handlers);
    }

    /*
     * Simple end to end test (mostly for debug work)
     */
    public void testHelloOneWay() throws Exception {
        HandlerTracker tracker = HandlerTracker.getClientInstance();
        tracker.clearAll();

        TestService_Service service = getService();
        TestService test = getTestStub(service);
        ReportService report = getReportStub(service);
        
        report.clearHandlerTracker();

        test.testIntOneWay(0);
        
        // make normal call after
        assertEquals("did not get expected response",
            4, test.testInt(4));
        
        System.out.println("ok");
    }

}
