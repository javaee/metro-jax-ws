/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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
package async.executor_821.client;

import junit.framework.TestCase;
import testutil.AttachmentHelper;
import testutil.ClientServerTestUtil;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.Holder;
import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.*;
import javax.xml.ws.*;

/**
 * @author Jitendra Kotamraju
 */
public class ServiceExecutorTest extends TestCase {

    private Hello proxy;
    private MyExecutor executor;
    private ExecutorService delegateExecutor;

    public ServiceExecutorTest(String name) throws Exception {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        Hello_Service service = new Hello_Service();
        delegateExecutor = Executors.newFixedThreadPool(5);
        executor = new MyExecutor(delegateExecutor);
        service.setExecutor(executor);
        proxy = service.getHelloPort();
    }

    private static class MyExecutor implements Executor {
        private volatile boolean executed;
        private final ExecutorService delegateExecutor;

        MyExecutor(ExecutorService delegateExecutor) {
            this.delegateExecutor = delegateExecutor;
        }

        public boolean isExecuted() {
            return executed;
        }

        public void execute(Runnable command) {
            executed = true;
            delegateExecutor.execute(command);
        }
    }

    public void testHello0Poll() throws Exception {
        long begin = System.currentTimeMillis();
        Response<Integer> response1 = proxy.hello0Async(54321);
        long end = System.currentTimeMillis();
        if (end-begin > 5000) {
            fail("Invoking thread doesn't seem to spawn a new thread for async processing");
        }

        begin = System.currentTimeMillis();
        Response<Integer> response2 = proxy.hello0Async(43210);
        end = System.currentTimeMillis();
        if (end-begin > 5000) {
            fail("Invoking thread doesn't seem to spawn a new thread for async processing");
        }

        begin = System.currentTimeMillis();
        Response<Integer> response3 = proxy.hello0Async(32104);
        if (end-begin > 5000) {
            fail("Invoking thread doesn't seem to spawn a new thread for async processing");
        }

        Integer output1 = response1.get(15, TimeUnit.SECONDS);
        assertEquals(54321, (int)output1);
        Integer output2 = response2.get(15, TimeUnit.SECONDS);
        assertEquals(43210, (int)output2);
        Integer output3 = response3.get(15, TimeUnit.SECONDS);
        assertEquals(32104, (int)output3);

        assertTrue(executor.isExecuted());
    }

    public void testHello1Callback() throws Exception {
        long begin = System.currentTimeMillis();
        String arg = "foo";
        String extra = "bar";
        HelloType req = new HelloType();
        req.setArgument(arg);
        req.setExtra(extra);

        HelloType reqH = new HelloType();
        reqH.setArgument("header arg");
        reqH.setExtra("header extra");

        Exchanger<Hello1Response> exchanger = new Exchanger<Hello1Response>();

        proxy.hello1Async(req, reqH, new Hello1CallbackHandler(exchanger));
        long end = System.currentTimeMillis();
        if (end-begin > 5000) {
            fail("Invoking thread doesn't seem to spawn a new thread for async processing");
        }

        Hello1Response resp = exchanger.exchange(null, 15, TimeUnit.SECONDS);
        HelloOutput out = resp.getHelloOutput();
        HelloType outH = resp.getHeader();
        assertEquals("foo", out.getArgument());
        assertEquals("bar", out.getExtra());
        assertEquals("header arg", outH.getArgument());
        assertEquals("header extra", outH.getExtra());

        assertTrue(executor.isExecuted());
    }

    @Override
    protected void tearDown() {
        delegateExecutor.shutdown();
    }

}
