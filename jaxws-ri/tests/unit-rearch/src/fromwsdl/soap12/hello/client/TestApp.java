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

package fromwsdl.soap12.hello.client;

import junit.framework.TestCase;
import testutil.ClientServerTestUtil;

import java.util.ArrayList;

/**
 * @author JAX-RPC RI Development Team
 */
public class TestApp extends TestCase {

    private Hello stub;

    public TestApp(String name) throws Exception {
        super(name);
        Hello_Service service = new Hello_Service();
        stub = service.getHelloPort();
        ClientServerTestUtil.setTransport(stub);
    }


    public void testHello() throws Exception {
        try {
            String arg = "foo";
            String extra = "bar";
            Hello_Type req = new Hello_Type();
            req.setArgument(arg);
            req.setExtra(extra);
            HelloResponse response = stub.hello(req);
            assertEquals(arg, response.getArgument());
            assertEquals(extra, response.getExtra());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }


    public void testVoid() throws Exception {
        try {
            VoidType req = new VoidType();
            VoidType response = stub.voidTest(req);
            assertNotNull(response);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testEchoArray() throws Exception {
        try {
            String[] in = {"JAXRPC 1.0", "JAXRPC 1.1", "JAXRPC 1.1.2", "JAXRPC 2.0"};
            NameType nt = new NameType();
            nt.getName().add(in[0]);
            nt.getName().add(in[1]);
            nt.getName().add(in[2]);
            nt.getName().add(in[3]);
            javax.xml.ws.Holder<NameType> req = new javax.xml.ws.Holder<NameType>(nt);
            stub.echoArray(req);
            System.out.println("Here we are");
            assertTrue(req.value == null);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testEchoArray1() throws Exception {
        try {
            String[] in = {"JAXRPC 1.0", "JAXRPC 1.1", "JAXRPC 1.1.2", "JAXRPC 2.0", "EA"};
            NameType nt = new NameType();
            nt.getName().add(in[0]);
            nt.getName().add(in[1]);
            nt.getName().add(in[2]);
            nt.getName().add(in[3]);
            javax.xml.ws.Holder<NameType> req = new javax.xml.ws.Holder<NameType>(nt);
            stub.echoArray1(req);
            assertTrue(java.util.Arrays.equals(in, req.value.getName().toArray()));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }


    public void testEchoArray2c() throws Exception {
        try {
            javax.xml.ws.Holder<NameType> req = new javax.xml.ws.Holder<NameType>();
            stub.echoArray2(req);
            assertTrue(req.value == null);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testEchoArray3() throws Exception {
        try {
            java.util.List<String> in = new ArrayList<String>();
            in.add("JAXRPC 1.0");
            in.add("JAXRPC 1.1");
            in.add("JAXRPC 1.1.2");
            in.add("JAXRPC 2.0");
            javax.xml.ws.Holder<java.util.List<String>> req = new javax.xml.ws.Holder<java.util.List<String>>(in);
            stub.echoArray3(req);
            assertTrue(in.equals(req.value));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testEchoArray4() throws Exception {
        try {
            NameType1 resp = stub.echoArray4(new NameType1());
            assertEquals(resp.getName().get(0).getArgument(), "arg1");
            assertEquals(resp.getName().get(0).getExtra(), "extra1");
            assertEquals(resp.getName().get(1).getArgument(), "arg2");
            assertEquals(resp.getName().get(1).getExtra(), "extra2");


        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
