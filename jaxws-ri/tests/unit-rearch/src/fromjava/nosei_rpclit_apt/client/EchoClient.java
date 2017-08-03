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

package fromjava.nosei_rpclit_apt.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import testutil.ClientServerTestUtil;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JAX-RPC Development Team
 */
public class EchoClient extends TestCase {
    private QName portQName = new QName("http://echo.org/", "EchoPort");;
    private static Echo stub;

    public EchoClient(String name) {
        super(name);
    }

    private Echo getStub() throws Exception {
        if (stub != null) {
            return stub;
        }
        EchoService service = new EchoService();
        stub = service.getEchoPort();
        ClientServerTestUtil.setTransport(stub);
        return stub;
    }


    public void testEnum() throws Exception {
        Echo stub = getStub();
        //this test no more applies as rpclit body parts cant take null parameter
        // since there are other tests that test this nullability, this test is commentedout
        //assertTrue(stub.echoBook(null) == null);
        assertTrue(stub.echoBook(Book.JWSDP).equals(Book.JWSDP));

//        assertTrue(stub.echoStatus(null) == null);
        assertTrue(stub.echoStatus(Status.YELLOW).equals(Status.YELLOW));
    }

    public void testGenerics() throws Exception {
        Echo stub = getStub();
        GenericValue var = new GenericValue();
        var.setValue("fred");
        assertTrue(stub.echoGenericString(var).getValue().equals("fred&john"));
        var.setValue(null);
        assertTrue(stub.echoGenericString(var).getValue() == null);

        var.setValue(33);
        assertTrue(stub.echoGenericInteger(var).getValue().equals(new Integer(33)));

        assertTrue(stub.echoGenericObject(new Integer(66)).equals(new Integer(66)));
        assertTrue(stub.echoGenericObject("bill").equals("bill"));
    }

    public void testSimple() throws Exception {
        Echo stub = getStub();
        Bar bar = new Bar();
        bar.setAge(33);

        assertTrue(stub.echoString("foo").equals("foo"));
        assertTrue(stub.echoString("test").equals("test"));
        assertTrue(stub.echoString("Mary & Paul").equals("Mary & Paul"));

        assertTrue(stub.echoInt(33) == 33);

        //System.out.println("stub.echoBar(): "+stub.echoBar(null));
//        assertTrue(stub.echoBar(null) == null);
        assertTrue(stub.echoBar(bar).getAge() == bar.getAge());

        InnerBar innerBar = new InnerBar();
        innerBar.setName("doug");
        assertTrue(stub.echoInnerBar(innerBar).getName().equals("dougdoug"));
    }

    public void profile() throws Exception {
        Echo stub = getStub();
        Bar bar = new Bar();
        bar.setAge(33);
        int i=0;
        while (i++ < 100) {
            stub.echoString("test");
            stub.echoString("Mary & Paul");
            stub.echoInt(33);
            stub.echoBar(bar);
        }
    }

    public void testExceptions() throws Exception {
        Echo stub = getStub();
        try {
            stub.echoString("Exception1");
            assertTrue(false);
        } catch (Exception1_Exception e){
            Exception1 ex = e.getFaultInfo();
            assertTrue(ex.getFaultString().equals("my exception1"));
            assertTrue(ex.isValid());
        }
        try {
        stub.echoString("Fault1");
          assertTrue(false);
        } catch (Fault1 e){
            FooException ex = e.getFaultInfo();
            assertTrue(e.getMessage().equals("fault1"));
            assertTrue(ex.getVarFloat() == 44F);
            assertTrue(ex.getVarInt() == 33);
            assertTrue(ex.getVarString().equals("foo"));
       }
       try {
        stub.echoString("WSDLBarException");
          assertTrue(false);
       } catch (WSDLBarException e){
            Bar ex = e.getFaultInfo();
            assertTrue(e.getMessage().equals("my barException"));
            assertTrue(ex.getAge() == 33);
       }
        try {
        stub.echoString("Fault2");
          assertTrue(false);
        } catch (Fault2_Exception e){
            assertTrue(e.getMessage().equals("my fault2"));
            Fault2 fault2 = e.getFaultInfo();
            assertTrue(fault2.getAge() == 33);
        }
    }


    public void testHolders() throws Exception {
        Echo stub = getStub();
        Holder<String> strHolder = new Holder<String>();
        strHolder.value = "fred";

        assertTrue(stub.outString("tmp", strHolder, 44).equals("tmp"));
        assertTrue(strHolder.value.equals("tmp44"));
        strHolder.value = "fred";

        assertTrue(stub.inOutString("tmp", strHolder, 44).equals("tmp"));
        assertTrue(strHolder.value.equals("fredfred"));

        Holder<Long> longHolder = new Holder<Long>();
        assertTrue(stub.outLong(33, longHolder, "tmp") == 33);

        assertTrue(longHolder.value == 345L);
        assertTrue(stub.inOutLong(44, longHolder, "tmp") == 44);
        assertTrue(longHolder.value == 690L);
    }


    public void testHeaders() throws Exception {
        Echo stub = getStub();

        assertTrue(stub.echoInHeader(33, 34L, "fred") == 34L);

        Holder<Long> longHolder = new Holder<Long>(new Long(44));
        assertTrue(stub.echoInOutHeader(33, longHolder, "fred").equals("fred88"));
        assertTrue(longHolder.value == 88L);


        longHolder = new Holder<Long>();
        assertTrue(stub.echoOutHeader(33, longHolder, "fred").equals("fred33"));
        assertTrue(longHolder.value == 33L);
    }

    public void testArray1() throws Exception {
        Echo stub = getStub();
        StringArray strArray = new StringArray();
//        List<String> list = strArray.getItem();
        strArray.getItem().add("Mary");
        strArray.getItem().add("Paul");
//        String[] strArray = new String[] { "Mary", "Paul" };

        StringArray returnArray = stub.echoStringArray(strArray);
        assertTrue(returnArray.getItem().size() == strArray.getItem().size());
        assertTrue(returnArray.getItem().get(0).equals(strArray.getItem().get(0)));
        assertTrue(returnArray.getItem().get(1).equals(strArray.getItem().get(1)));
//        assertTrue(returnArray[1].equals(strArray[1]));
    }

    public void testArray2() throws Exception {
        Echo stub = getStub();

        Bar bar = new Bar();
        bar.setAge(33);
        Bar bar2 = new Bar();
        bar2.setAge(44);
      BarArray array = new BarArray();
      array.getItem().add(bar);
      array.getItem().add(bar2);

        BarArray barArray = stub.echoBarArray(array);
        assertTrue(barArray.getItem().size() == 2);
        assertTrue(barArray.getItem().get(0).getAge() == bar.getAge());
        assertTrue(barArray.getItem().get(1).getAge() == bar2.getAge());
    }

    public void testArray3() throws Exception {
        Echo stub = getStub();

        Bar bar = new Bar();
        bar.setAge(33);
        Bar bar2 = new Bar();
        bar2.setAge(44);
//    BarArray array = new BarArray();
//    array.getItem().add(bar);
//    array.getItem().add(bar2);

        BarArray barArray = stub.echoBarAndBar(bar, bar2);
        assertTrue(barArray.getItem().size() == 2);
        assertTrue(barArray.getItem().get(0).getAge() == bar.getAge());
        assertTrue(barArray.getItem().get(1).getAge() == bar2.getAge());
//        assertTrue(barArray[0].getAge() == bar.getAge());
//        assertTrue(barArray[1].getAge() == bar2.getAge());
    }

    public void testOneway() throws Exception {
        Echo stub = getStub();

        stub.oneway();
        assertTrue(stub.verifyOneway());
    }

    public void testTCKTests() throws Exception {
        Echo stub = getStub();

        assertTrue(stub.helloWorld().equals("hello world"));
        stub.oneWayOperation();
        assertTrue(stub.overloadedOperation("fred").equals("fred"));
        assertTrue(stub.overloadedOperation2("ernie", " and bert").equals("ernie and bert"));
        StringArray strArray = stub.arrayOperation();
        assertTrue(strArray.getItem().get(0).equals("one"));
        assertTrue(strArray.getItem().get(1).equals("two"));
        assertTrue(strArray.getItem().get(2).equals("three"));
        assertTrue(stub.arrayOperationFromClient(strArray).equals("success"));
        Holder<String> h1 = new Holder<String>();
        Holder<String> h2 = new Holder<String>();
        h1.value = "Hello1";
        h2.value = "Hello2";
        assertTrue(stub.holderOperation(h1, h2).equals("success"));
        assertTrue(h1.value.equals("Hello11"));
        assertTrue(h2.value.equals("Hello22"));
    }

    public void testResultHeaders() throws Exception {
          Echo stub = getStub();

        assertTrue(stub.echoIntHeaderResult(33) == 66);
    }


    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(EchoClient.class);
        return suite;
    }

    /*
     * for debugging
     */
    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            EchoClient tester = new EchoClient("TestClient");
//              tester.testGenerics();
            tester.profile();
//            tester.testHeadersDynamic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
