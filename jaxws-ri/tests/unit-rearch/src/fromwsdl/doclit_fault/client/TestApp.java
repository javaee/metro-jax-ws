/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2013 Oracle and/or its affiliates. All rights reserved.
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

package fromwsdl.doclit_fault.client;

import junit.framework.TestCase;

import javax.xml.namespace.QName;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import java.util.Iterator;


import static testutil.ClientServerTestUtil.getPort;
import testutil.ClientServerTestUtil;
import org.w3c.dom.Element;

public class TestApp extends TestCase {

    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            TestApp test = new TestApp("TestApp");
            //test.testProtocolException();
        } catch (Exception e) {
            System.err.println("exception: " + e);
        }
    }

    private static Fault stub;

    public TestApp(String name) throws Exception{
        super(name);
        FaultService service = new FaultService();
        stub = service.getFaultPort();
        ClientServerTestUtil.setTransport(stub, null);
    }

    public void testNullFaultBean() throws Exception{
        try{
            java.lang.String in = "nullBean";
            stub.echo(in);
            assertTrue(false);
        }catch(Fault2Exception e){
            assertTrue(e.getFaultInfo() == null);
            assertTrue(true);
        }
    }

    public void testFault1()
            throws Exception {
        try {
            java.lang.String in = "Fault1";
            java.lang.String ret = stub.echo(in);
            fail("did not receive an exception");
        } catch (Fault1Exception e) {
            System.out.println("Expected exception received: " + e.getMessage());
            //e.printStackTrace();
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testFault2() {
        try {
            java.lang.String in = "Fault2";
            java.lang.String ret = stub.echo(in);
            fail("did not receive an exception");
        } catch (Fault2Exception e) {
            System.out.println("Expected exception received: " + e.getMessage());
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testFault3() {
        try {
            String in = "Fault3";
            String ret = stub.echo(in);
            fail("did not receive an exception");
        } catch (Fault3Exception e) {
            System.out.println("Expected exception received: " + e.getMessage());
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testFault4() {
        try {
            String in = "Fault4";
            String ret = stub.echo(in);
            fail("did not receive an exception");
        } catch (Fault4Exception e) {
            System.out.println("Expected exception received: " + e.getMessage());
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testNullPointerException() {
        try {
            String in = "NullPointerException";
            String ret = stub.echo(in);
            fail("did not receive an exception");
        } catch (ProtocolException e) {
            System.out.println("Expected exception received: " + e.getMessage());
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testSOAPFaultException() {
        try {
            String in = "SOAPFaultException";
            String ret = stub.echo(in);
            fail("did not receive an exception");
        } catch (ProtocolException e) {
            if (e instanceof SOAPFaultException) {
                System.out.println("Expected exception received: " + e.getMessage());
                Detail detail = ((SOAPFaultException)e).getFault().getDetail();
                assertNotNull(detail);
                Iterator i = detail.getDetailEntries();
                assertTrue(i.hasNext());
                assertEquals(((DetailEntry)i.next()).getElementQName(),
                   new QName("http://faultservice.org/wsdl", "BasicFault"));
            } else
                assertTrue(false);
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }


    public void testFault1WithCause()
            throws Exception {
        try {
            String in = "Fault1-SOAPFaultException";
            String ret = stub.echo(in);
            fail("did not receive an exception");
        } catch (Fault1Exception e) {
            System.out.println("Expected exception received: " + e.getMessage());
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testSaajBug() throws Fault2Exception, Fault4Exception, Fault3Exception, Fault1Exception {
        try {
            stub.echo("multipleDetails");
        } catch (SOAPFaultException e) {
            System.out.println("Expected exception received: " + e.getMessage());
            Detail detail = e.getFault().getDetail();
            assertNotNull(detail);
            Iterator i = detail.getDetailEntries();
            assertTrue(i.hasNext());
            DetailEntry detail1 = (DetailEntry) i.next();
            assertEquals(detail1.getElementQName(),
                    new QName(
                            "http://www.example.com/faults", "myFirstDetail"));

            String val1 = detail1.getAttribute("msg");
            assertNotNull(val1);

            assertTrue(val1.equals("This is the first detail message."));

            assertTrue(i.hasNext());
            DetailEntry detail2 = (DetailEntry) i.next();
            assertEquals(detail2.getElementQName(),
                    new QName(
                            "http://www.example.com/faults", "mySecondDetail"));
            String val2 = detail2.getAttribute("msg");
            assertNotNull(val2);
            assertTrue(val2.equals("This is the second detail message."));
        }
    }

    public void testProtocolException() throws Exception {
        try {
            String in = "ProtocolException";
            String ret = stub.echo(in);
            fail("did not receive an exception");
        } catch (ProtocolException e) {
            System.out.println("Expected exception received: " + e.getMessage());
            if (e.getMessage().indexOf("javax.xml.ws.ProtocolException") != -1)
                assertTrue(true);
            else
                assertTrue(false);
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testProtocolExceptionWithCause() throws Exception {
        try {
            String in = "ProtocolException2";
            String ret = stub.echo(in);
            fail("did not receive an exception");
        } catch (ProtocolException e) {
            System.out.println("Expected exception received: " + e.getMessage());
            if (e.getMessage().indexOf("FaultImpl") != -1)
                assertTrue(true);
            else
                assertTrue(false);
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void COMMENTEDtestRemoteExceptionWithSFECause() throws Exception {
        try {
            String in = "RemoteExceptionWithSFECause";
            String ret = stub.echo(in);
            fail("did not receive an exception");
        } catch (ProtocolException e) {
            if (e.getCause() instanceof SOAPFaultException) {
                System.out.println("Expected exception received: " + e.getMessage());
                assertTrue(true);
            } else {
                System.out.println("Expected cause: SOAPFaultException, Got: " + e.getCause());
                assertFalse(false);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void COMMENTEDtestRemoteExceptionWithSFECause2() throws Exception {
        try {
            String in = "RemoteExceptionWithSFECause2";
            String ret = stub.echo(in);
            fail("did not receive an exception");
        } catch (ProtocolException e) {
            if (e.getCause() instanceof SOAPFaultException) {
                System.out.println("Expected exception received: " + e.getMessage());
                assertTrue(true);
            } else {
                System.out.println("Expected cause: SOAPFaultException, Got: " + e.getCause());
                assertFalse(false);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }


}
