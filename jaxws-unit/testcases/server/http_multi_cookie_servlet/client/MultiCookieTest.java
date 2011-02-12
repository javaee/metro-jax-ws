/*
 * $Id: HaTest.java,v 1.1 2010-11-20 01:08:54 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.http_multi_cookie_servlet.client;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.Service;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * HTTP HA test
 *
 * @author Jitendra Kotamraju
 */
public class MultiCookieTest extends TestCase {

    public MultiCookieTest(String name) {
        super(name);
    }

    /*
    * With maintain property set to true, session
    * should be maintained.
    */
    public void test3() throws Exception {
        Hello proxy = new HelloService().getHelloPort();

        // Set the adress with upper case hostname
        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
        String address = (String)requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        requestContext.put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);

        proxy.introduce();
        assertTrue("client session should be maintained", proxy.rememberMe());
    }

    /*
    * With maintain property set to true, session
    * should be maintained.
    */
    public void xtest4() throws Exception {
        final Hello proxy = new HelloService().getHelloPort();

        // Set the adress with upper case hostname
        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
        String address = (String)requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        requestContext.put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);

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
    
}
