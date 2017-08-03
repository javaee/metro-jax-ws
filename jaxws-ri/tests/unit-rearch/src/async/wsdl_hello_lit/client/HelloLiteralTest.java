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

package async.wsdl_hello_lit.client;

import junit.framework.TestCase;
import testutil.ClientServerTestUtil;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;
import javax.xml.namespace.QName;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import async.wsdl_hello_lit.client.handlers.SOAPTestHandler;

/**
 *
 * @author WS Development Team
 */
public class HelloLiteralTest extends TestCase {

    private static Hello stub;

    // used for debugging
    public static void main(String [] args) throws Exception {
        System.setProperty("uselocal", "true");
        HelloLiteralTest test = new HelloLiteralTest("HelloLiteralTest");
        test.testHelloAsyncCallback0Future();
    }
    public HelloLiteralTest(String name) throws Exception{
        super(name);
//        stub = (Hello)ClientServerTestUtil.getPort(Hello_Service.class, Hello.class, new QName("urn:test", "HelloPort"));
        Hello_Service service = new Hello_Service();
        stub = service.getHelloPort();
        ClientServerTestUtil.setTransport(stub);
    }

    public void testHello() throws Exception {
        String arg = "foo";
        String extra = "bar";
        Hello_Type req = new Hello_Type();
        req.setArgument(arg);req.setExtra(extra);
        HelloOutput response = stub.hello(req);
        assertEquals(arg, response.getArgument());
        assertEquals(extra, response.getExtra());
    }

    public void testHelloAsyncPoll() throws Exception {
        System.out.println("testHelloAsyncPoll");
        System.out.println("==================");
        String arg = "foo";
        String extra = "bar";
        Hello_Type req = new Hello_Type();
        req.setArgument(arg);req.setExtra(extra);
        Response<HelloOutput> response = stub.helloAsync(req);
        System.out.print("\npolling for response ...");
        HelloOutput output = response.get(15,TimeUnit.SECONDS);
        assertEquals(arg, output.getArgument());
        assertEquals(extra, output.getExtra());
    }

    public void testHelloAsyncCallback() throws Exception {
        String arg = "foo";
        String extra = "bar";
        Hello_Type req = new Hello_Type();
        req.setArgument(arg);req.setExtra(extra);
        Future<?> response = stub.helloAsync(req, new HelloCallbackHandler());
        System.out.print("\nWaiting for CallbackHandler to complete...");
//        while(!response.isDone())
            //System.out.print(".");
    }

    public void testHello1() throws Exception {
        String arg = "foo";
        String extra = "bar";
        HelloType req = new HelloType();
        req.setArgument(arg);req.setExtra(extra);
        HelloType reqH = new HelloType();
        reqH.setArgument("header arg");
        reqH.setExtra("header extra");
        Holder<HelloOutput> resp = new Holder<HelloOutput>();
        Holder<HelloType> respH = new Holder<HelloType>();
        stub.hello1World(req, reqH, resp, respH);
        assertEquals(arg, resp.value.getArgument());
        assertEquals(extra, resp.value.getExtra());
        assertEquals(reqH.getArgument(), respH.value.getArgument());
        assertEquals(reqH.getExtra(), respH.value.getExtra());
    }

    public void testHello1AsyncPoll() throws Exception {
        System.out.println("testHello1AsyncPoll");
        System.out.println("==================");
        String arg = "foo";
        String extra = "bar";
        HelloType req = new HelloType();
        req.setArgument(arg);req.setExtra(extra);

        HelloType reqH = new HelloType();
        reqH.setArgument("header arg");
        reqH.setExtra("header extra");


        Response<Hello1WorldResponse> response = stub.hello1WorldAsync(req, reqH);
        System.out.print("\npolling for response ...");
        Hello1WorldResponse resp = response.get(15,TimeUnit.SECONDS);
        HelloOutput out = resp.getHelloOutput();
        HelloType outH = resp.getHeader();
        assertEquals(arg, out.getArgument());
        assertEquals(extra, out.getExtra());
        assertEquals(reqH.getArgument(), outH.getArgument());
        assertEquals(reqH.getExtra(), outH.getExtra());
    }

    public void testHello1AsyncCallback() throws Exception {
        String arg = "foo";
        String extra = "bar";
        HelloType req = new HelloType();
        req.setArgument(arg);req.setExtra(extra);

        HelloType reqH = new HelloType();
        reqH.setArgument("header arg");
        reqH.setExtra("header extra");

        Future<?> response = stub.hello1WorldAsync(req, reqH, new Hello1CallbackHandler());
        System.out.print("\nWaiting for CallbackHandler to complete...");
//      while(!response.isDone())
//          System.out.print(".");
    }

    //testHello0
    public void testHello0() throws Exception {
        int response = stub.hello0(54321);
        assertEquals(54321, response);
    }

    public void testHello0AsyncPoll() throws Exception {
        System.out.println("testHello0AsyncPoll");
        System.out.println("==================");

        Response<Integer> response = stub.hello0Async(54321);
        System.out.print("\npolling for response ...");
        Integer output = response.get(15,TimeUnit.SECONDS);
        assertEquals(54321, output.intValue());
    }

    public void testHelloAsyncCallback0() throws Exception {
        Future<?> response = stub.hello0Async(54321, new Hello0CallbackHandler());
        System.out.print("\nWaiting for CallbackHandler to complete...");
//      while(!response.isDone())
          //System.out.print(".");
    }

    /*
     * Adds handler to increment value of request and response.
     */
    public void testHello0AsyncPollHandler() throws Exception {
        try{
            // should be no handlers, but to be safe
            ClientServerTestUtil.clearHandlers((BindingProvider) stub);

            // add handler
            ClientServerTestUtil.addHandlerToBinding(new SOAPTestHandler(),
                (BindingProvider) stub);
            
            int orig = 1;
            int diff = 2; // 1 per handler invocation
            
            Response<Integer> response = stub.hello0Async(orig);
            Integer output = response.get(15,TimeUnit.SECONDS);
            assertEquals("Handlers were not invoked properly",
                orig + diff, output.intValue());
        } finally {
            ClientServerTestUtil.clearHandlers((BindingProvider) stub);
        }
    }


    /*
     * Adds handler to increment value of request and response.
     */
    public void testHelloAsyncCallback0Handler() throws Exception {
        try{
            // should be no handlers, but to be safe
            ClientServerTestUtil.clearHandlers((BindingProvider) stub);

            // test with no handler first
            int orig = 1;
            final IntHolder intHolder = new IntHolder();
            Future<?> response = stub.hello0Async(orig,
                new AsyncHandler<Integer>() {
                public void handleResponse(Response<Integer> resp) {
                    try {
                        // add 10 to make sure this was called
                        intHolder.setValue(resp.get().intValue() + 10);
                    } catch (Exception e) {
                        e.printStackTrace();
                        intHolder.setValue(-100); // will cause failure
                    }
                }
            });
            
            // wait, but only up to 15 seconds. if it takes more,
            // it's considered a hangup
            response.get(15,TimeUnit.SECONDS);

            assertEquals("did not get expected value back in response",
                orig + 10, intHolder.getValue());
            
            // add handler
            ClientServerTestUtil.addHandlerToBinding(new SOAPTestHandler(),
                (BindingProvider) stub);
            int diff = 2; // 1 per handler invocation
            response = stub.hello0Async(orig,
                new AsyncHandler<Integer>() {
                public void handleResponse(Response<Integer> resp) {
                    try {
                        intHolder.setValue(resp.get().intValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                        intHolder.setValue(-100); // will cause failure
                    }
                }
            });
            
            response.get(15,TimeUnit.SECONDS);

            assertEquals("handlers did not execute successfully",
                orig + diff, intHolder.getValue());
        } finally {
            ClientServerTestUtil.clearHandlers((BindingProvider) stub);
        }
    }

    /*
     * test for bug 6311690
     */
    public void testHelloAsyncCallback0Future() throws Exception {
        int orig = 1;
        final IntHolder intHolder = new IntHolder();
        Future<?> response = stub.hello0Async(orig,
            new AsyncHandler<Integer>() {
            public void handleResponse(Response<Integer> resp) {
                try {
                    // add 10 to make sure this was called
                    intHolder.setValue(resp.get().intValue() + 10);
                } catch (Exception e) {
                    e.printStackTrace();
                    intHolder.setValue(-100); // will cause failure
                }
            }
        });
        response.get(15,TimeUnit.SECONDS);
        int intResponseA = intHolder.getValue();        
        Thread.sleep(7000);
        int intResponseB = intHolder.getValue();
        if (intResponseB != intResponseA) {
            fail("Future.isDone() did not wait for response");
        }
    }

    static class IntHolder {
        private int value;
        
        public void setValue(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }

}

class HelloCallbackHandler extends TestCase implements AsyncHandler<HelloOutput> {

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.rpc.AsyncHandler#handleResponse(javax.xml.rpc.Response)
     */
    public void handleResponse(Response<HelloOutput> response) {
        try {
            HelloOutput output = response.get();
            assertEquals("foo", output.getArgument());
            assertEquals("bar", output.getExtra());
            System.out.println("Callback Handler Completed-Test pass");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }                    
}

class Hello0CallbackHandler extends TestCase implements AsyncHandler<Integer> {

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.rpc.AsyncHandler#handleResponse(javax.xml.rpc.Response)
     */
    public void handleResponse(Response<Integer> response) {
        try {
            Integer output = response.get();
            assertEquals(54321, output.intValue());
            System.out.println("Callback Handler Completed-Test pass");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Hello1CallbackHandler extends TestCase implements AsyncHandler<Hello1WorldResponse> {

    /*
    * (non-Javadoc)
    * 
    * @see javax.xml.rpc.AsyncHandler#handleResponse(javax.xml.rpc.Response)
    */
    public void handleResponse(Response<Hello1WorldResponse> response) {
    try {    
        Hello1WorldResponse resp = response.get();
        HelloOutput out = resp.getHelloOutput();
        HelloType outH = resp.getHeader();            
        assertEquals("foo", out.getArgument());
        assertEquals("bar", out.getExtra());
        assertEquals("header arg", outH.getArgument());
        assertEquals("header extra", outH.getExtra());
        System.out.println("should fail next line");
        assertTrue(false); // this should cause failure, but doesn't
//        assertEquals("Hello World!", resp.getExtraHeader());
        System.out.println("Callback Handler Completed-Test pass");
    } catch (ExecutionException e) {
        e.printStackTrace();
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}                    
}
