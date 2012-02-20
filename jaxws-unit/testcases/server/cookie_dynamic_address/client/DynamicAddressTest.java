/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package server.cookie_dynamic_address.client;

import junit.framework.TestCase;

import javax.xml.ws.BindingProvider;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Map;

/**
 * HTTP HA test when address is set dynamically
 * Issue: GLASSFISH_15938
 *
 * @author Jitendra Kotamraju
 */
public class DynamicAddressTest extends TestCase {


    public DynamicAddressTest(String name) {
        super(name);
    }

    /*
    * With maintain property set to true, session
    * should be maintained.
    */
    public void test3() throws Exception {
        Hello proxy = new HelloService().getHelloPort();
        // So that master tube is not used for invocation
        clearTubePool(proxy);


        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);

        // Set the same address with upper case dynamically
        String address = (String)requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        URL url = new URL(address);
        String host = url.getHost();
        address = address.replace(host, host.toUpperCase());
        requestContext.put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);

        proxy.introduce();

        // So that introduce() tubeline is not used for new invocation
        clearTubePool(proxy);
        assertTrue("client session should be maintained", proxy.rememberMe());
    }

    public void test4() throws Exception {
        final Hello proxy = new HelloService().getHelloPort();

        // Set the same address
        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
        String address = (String)requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        requestContext.put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);

        // So that master tube is not used for invocation
        clearTubePool(proxy);
        proxy.introduce();

        int NO_THREADS = 4;
        Thread[] threads = new Thread[NO_THREADS];
        MyRunnable[] runs = new MyRunnable[NO_THREADS];
        for(int i=0; i < NO_THREADS; i++) {
            runs[i] = new MyRunnable(proxy);
            threads[i] = new Thread(runs[i]);
        }
        for(int i=0; i < NO_THREADS; i++) {
            threads[i].start();
        }
        for(int i=0; i < NO_THREADS; i++) {
            threads[i].join();
        }
        for(int i=0; i < NO_THREADS; i++) {
            if (runs[i].e != null) {
                throw runs[i].e;
            }
        }
    }

    static class MyRunnable implements Runnable {
        final Hello proxy;
        volatile Exception e;

        MyRunnable(Hello proxy) {
            this.proxy = proxy;
        }

        public void run() {
            try {
                assertTrue("client session should be maintained", proxy.rememberMe());
            } catch(Exception e) {
                this.e = e;
            }
        }
    }

    // Reflection code to set
    // ((com.sun.xml.ws.client.Stub)proxy).tubes.queue = null;
    private void clearTubePool(Object proxy) throws Exception {
        InvocationHandler ih = Proxy.getInvocationHandler(proxy);
        Field tubesField = com.sun.xml.ws.client.Stub.class.getDeclaredField("tubes");
        tubesField.setAccessible(true);
        Object tubes = tubesField.get(ih);
        Field queueField = com.sun.xml.ws.util.Pool.class.getDeclaredField("queue");
        queueField.setAccessible(true);
        queueField.set(tubes, null);
    }
    
}
