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

package server.provider.wsdl_hello_lit.client;

import junit.framework.*;
import testutil.ClientServerTestUtil;
import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import java.io.PrintStream;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Random;

/*
 * @author Jitendra Kotamraju
 */
public class MultiThreadTest extends TestCase {

    private static final int NO_THREADS = 10;
    private static final int REQS_PER_THREAD = 5000;
    private static int NO_RESPS = 0;

    public void testAsyncMultiThread() throws Exception {
        Hello_Service service = new Hello_Service();
        Hello stub = service.getHelloPort();
        Hello asyncStub = service.getHelloAsyncPort();
        Thread[] threads = new Thread[NO_THREADS];
        for(int i=0; i < NO_THREADS; i++) {
            threads[i] = new Thread(new MultiRunnable(i%2==0?stub:asyncStub));
        }
        for(int i=0; i < NO_THREADS; i++) {
            threads[i].start();
        }
        for(int i=0; i < NO_THREADS; i++) {
            threads[i].join();
        }
        synchronized(HelloLiteralTest.class) {
            assertEquals(NO_THREADS*REQS_PER_THREAD, NO_RESPS);
        }
    }

    private static class MultiRunnable implements Runnable {
        Hello stub;

        MultiRunnable(Hello stub) {
            this.stub = stub;
        }

        public void run() {
            for(int i=0; i < REQS_PER_THREAD; i++) {
            	int rand = new Random(System.currentTimeMillis()).nextInt(1000);
                Hello_Type req = new Hello_Type();
		String arg = "arg"+rand;
		String extra = "extra"+rand;
                req.setArgument(arg);req.setExtra(extra);
                HelloResponse response = stub.hello(req, req);
                assertEquals(arg, response.getArgument());
                assertEquals(extra, response.getExtra());
                synchronized(HelloLiteralTest.class) {
                    ++NO_RESPS;
                }
                rand = rand/20;
                try { Thread.sleep(rand); } catch(InterruptedException e) { e.printStackTrace(); }
            }
        }
    }

}
