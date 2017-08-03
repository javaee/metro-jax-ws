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

package async.wsdl_rpclit.client;

import junit.framework.Assert;
import junit.framework.TestCase;
import testutil.ClientServerTestUtil;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;
import javax.xml.namespace.QName;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author JAX-RPC RI Development Team
 */
public class HelloLiteralTest extends TestCase {

    private static Hello stub;

    public HelloLiteralTest(String name) throws Exception{
        super(name);
        Hello_Service service = new Hello_Service();
        stub = service.getHelloPort();
        ClientServerTestUtil.setTransport(stub);
    }

    public void testHello() throws Exception {
        try{
            String arg = "foo";
            String extra = "bar";
            HelloType req = new HelloType();
            req.setArgument(arg);req.setExtra(extra);
            Holder<HelloType> inout = new Holder<HelloType>(req);
            stub.hello(inout);
            HelloType response = inout.value;
            assertEquals(arg, response.getArgument());
            assertEquals(extra, response.getExtra());
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testHelloAsyncPoll() throws Exception {
        try{
            String arg = "foo";
            String extra = "bar";
            HelloType req = new HelloType();
            req.setArgument(arg);req.setExtra(extra);
            Response<HelloType> response = stub.helloAsync(req);
            System.out.print("\npolling for response ...");
            while (!response.isDone()) {
                //System.out.print(".");
            }
            HelloType output = response.get();
            assertEquals(arg, output.getArgument());
            assertEquals(extra, output.getExtra());
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testHelloAsyncCallback() throws Exception {
        try{
            String arg = "foo";
            String extra = "bar";
            HelloType req = new HelloType();
            req.setArgument(arg);req.setExtra(extra);
            Future<?> response = stub.helloAsync(req, new HelloCallbackHandler());
            System.out.print("\nWaiting for CallbackHandler to complete...");
//            while(!response.isDone())
                //System.out.print(".");
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testHello2() throws Exception {
        try{
            String arg = "foo";
            String extra = "bar";
            HelloType req = new HelloType();
            req.setArgument(arg);req.setExtra(extra);
            Holder<HelloType> inout = new Holder<HelloType>(req);
            int age = stub.hello2(inout, "foo");
            HelloType response = inout.value;
            assertEquals(arg, response.getArgument());
            assertEquals(extra, response.getExtra());
            assertEquals(age, 1234);
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testHello2AsyncPoll() throws Exception {
        try{
            String arg = "foo";
            String extra = "bar";
            HelloType req = new HelloType();
            req.setArgument(arg);req.setExtra(extra);
            Response<Hello2Response> response = stub.hello2Async(req, "foo");
            System.out.print("\npolling for response ...");
            while (!response.isDone()) {
                //System.out.print(".");
            }
            Hello2Response resp = response.get();
            HelloType output = resp.getParam();
            int age = resp.getAge();
            assertEquals(arg, output.getArgument());
            assertEquals(extra, output.getExtra());
            assertEquals(age, 1234);
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testHello2AsyncCallback() throws Exception {
        try{
            String arg = "foo";
            String extra = "bar";
            HelloType req = new HelloType();
            req.setArgument(arg);req.setExtra(extra);
            Future<?> response = stub.hello2Async(req, "foo", new Hello2CallbackHandler());
            System.out.print("\nWaiting for CallbackHandler to complete...");
//            while(!response.isDone())
                //System.out.print(".");
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testHello1() throws Exception {
        try{
            String arg = "foo";
            String extra = "bar";
            HelloType req = new HelloType();
            req.setArgument(arg);req.setExtra(extra);
            HelloType reqH = new HelloType();
            reqH.setArgument("header arg");
            reqH.setExtra("header extra");
            Holder<HelloType> resp = new Holder<HelloType>();
            Holder<HelloType> respH = new Holder<HelloType>();
            Holder<String> hdr = new Holder<String>("Hello");
            stub.hello1(req, reqH, resp, respH);
            assertEquals(arg, resp.value.getArgument());
            assertEquals(extra, resp.value.getExtra());
            assertEquals(reqH.getArgument(), respH.value.getArgument());
            assertEquals(reqH.getExtra(), respH.value.getExtra());
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testHello1AsyncPoll() throws Exception {
        try{
            String arg = "foo";
            String extra = "bar";
            HelloType req = new HelloType();
            req.setArgument(arg);req.setExtra(extra);

            HelloType reqH = new HelloType();
            reqH.setArgument("header arg");
            reqH.setExtra("header extra");


            Response<Hello1Response> response = stub.hello1Async(req, reqH);
            System.out.print("\npolling for response ...");
            while (!response.isDone()) {
                //System.out.print(".");
            }
            Hello1Response resp = response.get();
            HelloType out = resp.getRes();
            HelloType outH = resp.getHeader();
            assertEquals(arg, out.getArgument());
            assertEquals(extra, out.getExtra());
            assertEquals(reqH.getArgument(), outH.getArgument());
            assertEquals(reqH.getExtra(), outH.getExtra());
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testHello1AsyncCallback() throws Exception {
        try{
            String arg = "foo";
            String extra = "bar";
            HelloType req = new HelloType();
            req.setArgument(arg);req.setExtra(extra);

            HelloType reqH = new HelloType();
            reqH.setArgument("header arg");
            reqH.setExtra("header extra");

            Future<?> response = stub.hello1Async(req, reqH,
                new Hello1CallbackHandler());
            System.out.print("\nWaiting for CallbackHandler to complete...");
//            while(!response.isDone())
//                System.out.print(".");
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

        //testHello0
    public void testHello0() throws Exception {
        try{
            int response = stub.hello0(54321);
            assertEquals(response, 54321);
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testHello0AsyncPoll() throws Exception {
        try{
            System.out.println("testHello0AsyncPoll");
            System.out.println("==================");

            Response<Integer> response = stub.hello0Async(54321);
            System.out.print("\npolling for response ...");
            while (!response.isDone()) {
                //System.out.print(".");
            }
            Integer output = response.get();
            assertEquals(output.intValue(), 54321);
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testHelloAsyncCallback0() throws Exception {
        try{
            Future<?> response = stub.hello0Async(54321, new Hello0CallbackHandler());
            System.out.print("\nWaiting for CallbackHandler to complete...");
//            while(!response.isDone())
                //System.out.print(".");
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }
    
    public void testHello4AsyncPoll() throws Exception {
        try{
            String arg = "foo";
            String extra = "bar";
            HelloType req = new HelloType();
            req.setArgument(arg);req.setExtra(extra);

            HelloType reqH = new HelloType();
            reqH.setArgument("header arg");
            reqH.setExtra("header extra");


            Response<Hello4Response> response = stub.hello4Async(req, reqH);
            System.out.print("\npolling for response ...");
            while (!response.isDone()) {
                //System.out.print(".");
            }
            Hello4Response resp = response.get();
            HelloType out = resp.getRes();
            HelloType outH = resp.getHeader();
            assertEquals(arg, out.getArgument());
            assertEquals(extra, out.getExtra());
            assertEquals(reqH.getArgument(), outH.getArgument());
            assertEquals(reqH.getExtra(), outH.getExtra());
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }



}

class HelloCallbackHandler extends TestCase implements AsyncHandler<HelloType> {

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.rpc.AsyncHandler#handleResponse(javax.xml.rpc.Response)
     */
    public void handleResponse(Response<HelloType> response) {
        try {            
            HelloType output = response.get();
            assertEquals("foo", output.getArgument());
            assertEquals("bar", output.getExtra());
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
            assertEquals(output.intValue(), 54321);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Hello1CallbackHandler extends TestCase implements AsyncHandler<Hello1Response> {

    /*
    * (non-Javadoc)
    * 
    * @see javax.xml.rpc.AsyncHandler#handleResponse(javax.xml.rpc.Response)
    */
    public void handleResponse(Response<Hello1Response> response) {
        try {    
            Hello1Response resp = response.get();
            HelloType out = resp.getRes();
            HelloType outH = resp.getHeader();            
            assertEquals("foo", out.getArgument());
            assertEquals("bar", out.getExtra());
            assertEquals("header arg", outH.getArgument());
            assertEquals("header extra", outH.getExtra());
//            assertEquals("Hello World!", resp.getExtraHeader());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
    
class Hello2CallbackHandler extends TestCase implements AsyncHandler<Hello2Response> {
    /*
    * (non-Javadoc)
    * 
    * @see javax.xml.rpc.AsyncHandler#handleResponse(javax.xml.rpc.Response)
    */
    public void handleResponse(Response<Hello2Response> response) {
        try {    
            Hello2Response resp = response.get();
            HelloType out = resp.getParam();
            int age = resp.getAge();
            assertEquals("foo", out.getArgument());
            assertEquals("bar", out.getExtra());
            assertEquals(age, 1234);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
    
