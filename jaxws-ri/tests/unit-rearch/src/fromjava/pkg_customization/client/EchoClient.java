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

package fromjava.pkg_customization.client;

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
    private QName portQName = new QName("http://server.seinoimpl.fromjava/jaxws", "EchoIF");;
    private Service service;
    private EchoIF stub;

    public EchoClient(String name) {
        super(name);
    }

    private EchoIF getStub() throws Exception {
        if (stub != null) {
            return stub;
        }
        EchoImplService service = new EchoImplService();
        stub = service.getEchoImplPort();      
        ClientServerTestUtil.setTransport(stub);   
        return stub;
    }

    public void testSimple() throws Exception {
        EchoIF stub = getStub();
        Bar bar = new Bar();
        bar.setAge(33);

        assertTrue(stub.echoString("test").equals("test"));
        assertTrue(stub.echoString("Mary & Paul").equals("Mary & Paul"));
        assertTrue(stub.echoBar(bar).getAge() == bar.getAge());
        Holder<Integer> age = new Holder<Integer>();
        age.value = 33;
        stub.echoIntHolder(age);
        assertTrue(age.value == 66);

    }

//    private void runArray(EchoIF stub) throws Exception {
//        Bar bar = new Bar();
//        bar.setAge(33);
//        Bar bar2 = new Bar();
//        bar2.setAge(44);
//
//        Bar[] barArray = stub.echoBarArray(new Bar[] { bar, bar2 });
//        assertTrue(barArray.length == 2);
//        assertTrue(barArray[0].getAge() == bar.getAge());
//        assertTrue(barArray[1].getAge() == bar2.getAge());
//
//        barArray = stub.echoTwoBar(bar, bar2);
//        assertTrue(barArray.length == 2);
//        assertTrue(barArray[0].getAge() == bar.getAge());
//        System.out.println(barArray[1].getAge());
//        System.out.println(bar2.getAge());
//        assertTrue(barArray[1].getAge() == bar2.getAge());
//    }

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
            System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "pessimistic");
            EchoClient testor = new EchoClient("TestClient");
            testor.testSimple();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
}

