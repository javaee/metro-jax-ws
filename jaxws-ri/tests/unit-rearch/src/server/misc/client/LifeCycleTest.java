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

package server.misc.client;

import java.io.*;
import java.util.concurrent.*;
import java.util.Random;
import junit.framework.*;
import testutil.ClientServerTestUtil;
import testutil.HTTPResponseInfo;
import javax.xml.soap.*;
import javax.xml.namespace.QName;


/**
 *
 * @author JAX-RPC RI Development Team
 */
public class LifeCycleTest extends TestCase {
    
    private static final int NO_THREADS = 10;
    private static final int NO_REQS = 300;
    private static int NO_RESPS = 0;
	private HelloPortType stub;

    public LifeCycleTest(String name) throws Exception {
        super(name);
		getStub();
    }

    HelloPortType getStub() throws Exception {
        if (stub == null) {
            HelloService service = new HelloService();
            stub = service.getHelloPort();
            ClientServerTestUtil.setTransport(stub);
        }
        return stub;
    }

    public void testEcho2() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            System.out.println("skipping http only test");
            return;
        }
        ExecutorService pool = Executors.newFixedThreadPool(NO_THREADS);
        try {
           for (int i=0; i < NO_REQS; i++) {
              pool.execute(new Runnable() {
                  public void run() {
                      try {
                          invoke();
                          Thread.sleep(10);
                      } catch(Exception e) {
                          assertTrue(false);
                      }
                  }
                  public void invoke() throws Exception {
                      int rand = new Random(System.currentTimeMillis()).nextInt(1000);
                      String var1 = "bar"+rand;
                      String response = stub.echo2(var1);
                      assertEquals(var1, response);
                      synchronized(MultiThreadTest.class) {
                          ++NO_RESPS;
                      }
                 }
             });
           }
           // Give sufficient time to gather all responses
           pool.awaitTermination(20000L, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            assertTrue(false);
        } finally {
            pool.shutdown();
        }
        synchronized(MultiThreadTest.class) {
        	assertEquals(NO_REQS, NO_RESPS);
        }
    }
}
