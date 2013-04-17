/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
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

package fromjava.nosei_bare_apt.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import testutil.ClientServerTestUtil;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author JAX-RPC Development Team
 */
public class EchoClient extends TestCase {
    private static Echo stub;
    ClientServerTestUtil util = new ClientServerTestUtil();

    public EchoClient(String name) throws Exception{
        super(name);
        EchoService service = new EchoService();
        stub = service.getEchoPort();      
        ClientServerTestUtil.setTransport(stub);   
    }


    public void testEnum() throws Exception {
        assertTrue(stub.echoBook(Book.JWSDP).equals(Book.JWSDP));
        assertTrue(stub.echoStatus(Status.YELLOW).equals(Status.YELLOW));
    }

    public void testGenerics() throws Exception {
        Holder holder = new Holder<String>("fred");
        stub.echoGenericString(holder);
	  assertTrue(holder.value.equals("fred&john"));
        holder = new Holder<Integer>(33);
        stub.echoGenericInteger(holder);
        assertTrue(holder.value.equals(new Integer(66)));
        assertTrue(stub.echoGenericObject(new Integer(66)).equals(new Integer(66)));   
        assertTrue(stub.echoGenericObject("bill").equals("bill"));   
    }

    public void testSimple() throws Exception {
        Bar bar = new Bar();
        bar.setAge(33);

        assertTrue(stub.echoString("test").equals("test"));
        assertTrue(stub.echoString("Mary & Paul").equals("Mary & Paul"));
        assertTrue(stub.echoBar(bar).getAge() == bar.getAge());
//    assertTrue(stub.echoLong(33L) == 33L);
        
        Holder<String> holder = new Holder<String>();
        holder.value = "doug";
        stub.echoInnerBar(holder);
        assertTrue(holder.value.equals("dougdoug"));
    }

    public void testExceptions() throws Exception {
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
        Holder<String> strHolder = new Holder<String>();
      strHolder.value = "fred";

      stub.inOutString(strHolder);
        assertTrue(strHolder.value.equals("fredfred"));
//    assertTrue(stub.inOutString("tmp", strHolder, 44).equals("tmp"));
//    assertTrue(strHolder.value.equals("fredfred"));
      Holder<Long> longHolder = new Holder<Long>();
      longHolder.value = 0L;
//        stub.outLong(longHolder);
//        assertTrue(longHolder.value == 345L);
      longHolder.value = 345L;
        stub.inOutLong(longHolder);
        assertTrue(longHolder.value == 690L);


//    assertTrue(stub.inOutString(strHolder).equals("tmp"));
//    assertTrue(strHolder.value.equals("fredfred"));
//    Holder<Long> longHolder = new Holder<Long>();
//        stub.outLong(33, longHolder));
//        assertTrue(longHolder.value == 345L);
    }

    public void testHeaders() throws Exception {

        assertTrue(stub.echoInHeader(33, 44L) == 77L);

        Holder<Long> longHolder = new Holder<Long>(new Long(44));
        assertTrue(stub.echoInOutHeader(33, longHolder) == 121L);
        assertTrue(longHolder.value == 88L);

//        Holder<Long> resultHolder = new Holder<Long>();
        longHolder = new Holder<Long>();
//        stub.echoOutHeader(33, resultHolder, longHolder);
//        assertTrue(resultHolder.value == 66L);
        Long result = stub.echoOutHeader(33, longHolder);
        assertTrue(result == 66L);
        assertTrue(longHolder.value == 33L);

/*
        EchoOutHeader echoOutHeader = new EchoOutHeader();
        echoOutHeader.setArg0(33);
        longHolder = new Holder<Long>();
        echoOutHeader.setArg2("fred");
        EchoOutHeaderResponse echoOutHeaderResp = stub.echoOutHeader(echoOutHeader, longHolder);
        assertTrue(echoOutHeaderResp.getReturn().equals("fred33"));
        assertTrue(longHolder.value == 33L);
*/
    }

    public void testArray1() throws Exception {
        StringArray strArray = new StringArray();
        List<String> list = strArray.getItem();
      list.add("Mary");
        list.add("Paul");
//        String[] strArray = new String[] { "Mary", "Paul" };

        StringArray returnArray = stub.echoStringArray(strArray);
        assertTrue(returnArray.getItem().size() == strArray.getItem().size());
        assertTrue(returnArray.getItem().get(0).equals(strArray.getItem().get(0)));
        assertTrue(returnArray.getItem().get(1).equals(strArray.getItem().get(1)));
//        assertTrue(returnArray[1].equals(strArray[1]));
    }

    public void testArray2() throws Exception {

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

    public void testOneway() throws Exception {
  
        stub.oneway("bogus");
        assertTrue(stub.verifyOneway(33));
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(EchoClient.class);
        return suite;
    }

}

