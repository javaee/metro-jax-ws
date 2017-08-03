/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package async.async_service.client;

import junit.framework.TestCase;
import java.lang.Exception;
import java.util.concurrent.*;
import java.util.Date;
import java.util.Random;



/**
 * @author Rama Pulavarthi
 */
public class AsyncServiceTest extends TestCase {
    private static final int NO_THREADS = 20;
    private static final int NO_OF_REQUESTS = 1000;
    private final Hello proxy;
    private int noReqs = 0;
    private int noResps = 0;

    public AsyncServiceTest(String name) throws Exception {
        super(name);
        Hello_Service service = new Hello_Service();
		proxy = service.getHelloPort();
    }

    public void testAsync() throws Exception {
      invoke("Hello","Duke");  
    }
    
    public void testAsyncWithCachedThreadPool() throws Exception {
        ExecutorService service = new ThreadPoolExecutor(NO_THREADS/2, NO_THREADS,
            30L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        synchronized(this) {
            noReqs = NO_OF_REQUESTS; noResps = 0;
        }
        doTestWithThreadPool(service, noReqs);
        service.shutdown();
        while(!service.awaitTermination(5L, TimeUnit.SECONDS));
        synchronized(this) {
            if(noReqs!= noResps) {
                throw new Exception("No. of requests and responses did not match");
            }
        }
    }

    private void doTestWithThreadPool(ExecutorService service, int noReqs) throws Exception {
        for(int i=0; i < noReqs; i++) {
            final int j = i;
            service.execute(new Runnable() {
                public void run() {
                    try {
                        invoke("Hello", "Duke"+j);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

	private void invoke(String arg, String extra) throws Exception {
    	Hello_Type req = new Hello_Type();
        req.setArgument(arg);
        req.setExtra(extra);
        System.out.println("Invoking Web Service with = " + arg + "," + extra);
        HelloResponse response = proxy.hello(req, req);
        System.out.println("arg=" + response.getArgument() + " extra=" + response.getExtra());

        if(!arg.equals(response.getArgument()) || !extra.equals(response.getExtra())) {
            throw new Exception("Mismatch in comparison");
        }

        synchronized (this) {
            ++noResps;
        }
	}
}
