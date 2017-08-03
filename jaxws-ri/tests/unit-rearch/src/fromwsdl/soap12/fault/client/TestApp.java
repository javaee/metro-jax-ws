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

package fromwsdl.soap12.fault.client;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.soap.SOAPFaultException;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import testutil.ClientServerTestUtil;
import static testutil.ClientServerTestUtil.getPort;

import fromwsdl.soap12.fault.client.handlers.ExceptionThrowingHandler;
import fromwsdl.soap12.fault.client.handlers.MUHelperHandler;

public class TestApp extends TestCase {

    // main method for debugging
    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            System.setProperty("log", "true");
            TestApp test = new TestApp("TestApp");
            //test.testHandlerException1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static Fault stub;

    public TestApp(String name) throws Exception{
        super(name);
        FaultService service = new FaultService();
        stub = service.getFaultPort();
        ClientServerTestUtil.setTransport(stub);
    }

    public void testFault1()
            throws Exception {
        try {
            java.lang.String in = "Fault1";
            java.lang.String ret = stub.echo(in);
            fail("no exception was received");
        } catch (Fault1Exception e) {
            System.out.println("Expected exception received: " + e.getMessage());
            //e.printStackTrace();
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }
    }


    public void testFault2() {
        try {
            java.lang.String in = "Fault2";
            java.lang.String ret = stub.echo(in);
            fail("no exception was received");
        } catch (Fault2Exception e) {
            System.out.println("Expected exception received: " + e.getMessage());
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testFault3() {
        try {
            String in = "Fault3";
            String ret = stub.echo(in);
            fail("no exception was received");
        } catch (Fault3Exception e) {
            System.out.println("Expected exception received: " + e.getMessage());
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testFault4() {
        try {
            String in = "Fault4";
            String ret = stub.echo(in);
            fail("no exception was received");
        } catch (Fault4Exception e) {
            System.out.println("Expected exception received: " + e.getMessage());
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testNullPointerException() {
        try {
            String in = "NullPointerException";
            String ret = stub.echo(in);
            fail("no exception was received");
        } catch (ProtocolException e) {
            System.out.println("Expected exception received: " + e.getMessage());
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testSOAP12FaultException() {
        try {
            String in = "SOAPFaultException";
            String ret = stub.echo(in);
            fail("no exception was received");
        } catch (ProtocolException e) {
            if (e instanceof SOAPFaultException) {
                System.out.println("Expected exception received: " + e.getMessage());
				SOAPFaultException sfe = (SOAPFaultException)e;
				assertNotNull(sfe.getFault().getDetail());
                assertTrue(true);
            } else {
                fail("received incorrect exception: " + e);
            }
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }
    }


    public void testFault1WithCause()
            throws Exception {
        try {
            String in = "Fault1-SOAPFaultException";
            String ret = stub.echo(in);
            fail("no exception was received");
        } catch (Fault1Exception e) {
            System.out.println("Expected exception received: " + e.getMessage());
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testProtocolException()
            throws Exception {
        try {
            String in = "ProtocolException";
            String ret = stub.echo(in);
            fail("no exception was received");
        } catch (ProtocolException e) {
            // pass if ProtocolException received
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            fail("received exception other than ProtocolException");
        }
    }

    public void testProtocolExceptionWithMessage() throws Exception {
        try {
            String in = "ProtocolException2";
            String ret = stub.echo(in);
            fail("no exception was received");
        } catch (ProtocolException e) {
            assertNotNull("message in exception is null", e.getMessage());
            System.out.println("Expected exception received: " + e.getMessage());
            if (e.getMessage().indexOf("FaultImpl") != -1) {
                assertTrue(true);
            } else {
                fail();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }
    }

    /*
     * MU test here for soap 1.2. Test uses a simple handler
     * on client side to test service with no handlers.
     */
    public void testMustUnderstand1() throws Exception {
        String next_1_2 = "http://www.w3.org/2003/05/soap-envelope/role/next";
        try {
            // clear handlers (should be none) and add helper handler
            ClientServerTestUtil.clearHandlers((BindingProvider) stub);
            MUHelperHandler handler = new MUHelperHandler();
            ClientServerTestUtil.addHandlerToBinding(handler,
                (BindingProvider) stub);

            // have handler set header that is ignored
            handler.setMUHeader(new QName("urn:mutest", "someheader"),
                "notarealactor");

            // make the call
            String arg = "echo";
            assertEquals(arg, stub.echo(arg));

            // add header that should result in soap fault
            handler.setMUHeader(new QName("urn:mutest", "someheader"),
                next_1_2);

            // make the call
            try {
                stub.echo(arg);
                fail("did not receive any exception");
            } catch (ProtocolException e) {
                if (e instanceof SOAPFaultException) {
                    // pass
                } else {
                    fail("did not receive soap fault, received: " +
                        e.toString());
                }
            } catch (Exception e) {
                fail("did not receive protocol exception. received " +
                    e.toString());
            }
        } finally {
            // always clear the handlers
            ClientServerTestUtil.clearHandlers((BindingProvider) stub);
        }
    }

    /*
     * MU test here for soap 1.2. Test verifies that the not
     * understood headers are returned to the client as
     * headers in the response message.
     */
    public void testMustUnderstand2() throws Exception {
        String next_1_2 = "http://www.w3.org/2003/05/soap-envelope/role/next";
        try {
            // clear handlers (should be none) and add helper handler
            ClientServerTestUtil.clearHandlers((BindingProvider) stub);
            MUHelperHandler handler = new MUHelperHandler();
            ClientServerTestUtil.addHandlerToBinding(handler,
                (BindingProvider) stub);

            // have the handler add two headers
            QName header1 = new QName("urn:mutest", "badheader1");
            QName header2 = new QName("urn:mutest", "badheader2");
            handler.setMUHeader(header1, next_1_2);
            handler.setMUHeader(header2, next_1_2);
            List<QName> expectedHeaders = new ArrayList<QName>();
            expectedHeaders.add(header1);
            expectedHeaders.add(header2);
            handler.setExpectedHeaders(expectedHeaders);

            // make the call
            try {
                stub.echo("have a nice day");
                fail("did not receive any exception");
            } catch (ProtocolException e) {
                if (e instanceof SOAPFaultException) {
                    // pass
                } else {
                    fail("did not receive soap fault, received: " +
                        e.toString());
                }
            } catch (Exception e) {
                fail("did not receive protocol exception. received " +
                    e.toString());
            }
        } finally {
            // always clear the handlers
            ClientServerTestUtil.clearHandlers((BindingProvider) stub);
        }
    }

    /**
     * Have a handler throw a protocol exception and make sure
     * the proper values are in the soap fault. Test for 6353191.
     */
    public void testHandlerException1() throws Exception {
        String code = "Sender"; // the expected fault code local part
        try {
            // clear handlers (should be none) and add helper handler
            ClientServerTestUtil.clearHandlers((BindingProvider) stub);
            ExceptionThrowingHandler handler =
                new ExceptionThrowingHandler("ProtocolException");
            ClientServerTestUtil.addHandlerToBinding(handler,
                (BindingProvider) stub);
            
            // make the call
            try {
                stub.echo("have a nice day");
                fail("did not receive any exception");
            } catch (SOAPFaultException e) {
            SOAPFault fault = e.getFault();
            String faultCode = fault.getFaultCode();
            assertTrue("fault code should end with \"" + code +
                "\": " + faultCode,
                faultCode.endsWith(code));
            } catch (Exception e) {
                fail("did not receive soap fault exception. received " +
                    e.toString());
            }
        } finally {
            // always clear the handlers
            ClientServerTestUtil.clearHandlers((BindingProvider) stub);
        }
    }
}
