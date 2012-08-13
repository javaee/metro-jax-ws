/*
 * $Id: MultiThreadTest.java,v 1.1 2008-07-17 00:55:49 jitu Exp $
 */

/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package server.multi.client;

import junit.framework.TestCase;

import javax.xml.ws.BindingProvider;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author Jitendra Kotamraju
 */
public class MultiThreadTest extends TestCase {
    
    private static final int NO_THREADS = 20;
    private static final int NO_REQS = 50000;
    private int noReqs = 0;
    private int noResps = 0;
    private final HelloPortType stub;

    public MultiThreadTest(String name) throws Exception {
        super(name);
        HelloService service = new HelloService();
        stub = service.getHelloPort();

        // initialize
        Map<String,Object> ctx = ((BindingProvider) stub).getRequestContext();
        ctx.put("whatever", "whatever");
        ctx.put("whatever2", "whatever2");

        // this turns RequestContext to fallback mode
        ctx.keySet();
    }

    public void testMultiThread() throws Exception {
        synchronized(this) {
            noReqs = NO_REQS; noResps = 0;
        }
        Thread[] threads = new Thread[NO_THREADS];
        for(int i=0; i < NO_THREADS; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    for(int i=0; i < noReqs/NO_THREADS; i++) {
                        try {
                            invoke();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        for(int i=0; i < NO_THREADS; i++) {
            threads[i].start();
        }
        for(int i=0; i < NO_THREADS; i++) {
            threads[i].join();
        }
        synchronized(this) {
            assertEquals(noReqs, noResps);
        }
    }


    public void invoke() throws Exception {
        //Thread.sleep(new Random(System.currentTimeMillis()).nextInt(50));
	int rand = new Random(System.currentTimeMillis()).nextInt(1000);
	String var1 = "foo"+rand;
	String var2 = "bar"+rand;
	ObjectFactory of = new ObjectFactory();
	EchoType request = of.createEchoType();
	request.setReqInfo(var1);
	Echo2Type header2 = of.createEcho2Type();
	header2.setReqInfo(var2);
	EchoResponseType response = stub.echo(request, request, header2);
	assertEquals(var1, stub.echo2(var1));
	assertEquals(var1+var1+var2, (response.getRespInfo()));
	synchronized(this) {
	    ++noResps;
	}
    }

    public void testThreadPool() throws Exception {
        ExecutorService service = new ThreadPoolExecutor(NO_THREADS/2, NO_THREADS,
            30L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()); 
        synchronized(this) {
            noReqs = NO_REQS; noResps = 0;
        }
        doTestWithThreadPool(service, noReqs);
        service.shutdown();
        while(!service.awaitTermination(7L, TimeUnit.SECONDS));
        synchronized(this) {
            assertEquals(noReqs, noResps);
        }
    }

    public void testFixedThreadPool() throws Exception {
        ExecutorService service = Executors.newFixedThreadPool(NO_THREADS);
        synchronized(this) {
            noReqs = NO_REQS; noResps = 0;
        }
        doTestWithThreadPool(service, noReqs);
        service.shutdown();
        while(!service.awaitTermination(5L, TimeUnit.SECONDS));
        synchronized(this) {
            assertEquals(noReqs, noResps);
        }
    }

    public void testCachedThreadPool() throws Exception {
        ExecutorService service = Executors.newCachedThreadPool();
        synchronized(this) {
            noReqs = 50; noResps = 0;
        }
        doTestWithThreadPool(service, noReqs);
        service.shutdown();
        while(!service.awaitTermination(5L, TimeUnit.SECONDS));
        synchronized(this) {
            assertEquals(noReqs, noResps);
        }
    }

    private void doTestWithThreadPool(ExecutorService service, int noReqs) throws Exception {
        for(int i=0; i < noReqs; i++) {
            service.execute(new Runnable() {
                public void run() {
                    try {
                        invoke();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
