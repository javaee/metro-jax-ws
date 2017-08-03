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

package async.wsdl_hello_lit_fault.client;

import junit.framework.TestCase;
import testutil.ClientServerTestUtil;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.namespace.QName;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.rmi.RemoteException;


/**
 * @author JAX-RPC RI Development Team
 */
public class HelloLiteralTest extends TestCase {

    private static Hello stub;

    public HelloLiteralTest(String name) throws Exception{
        super(name);
//        stub = (Hello)ClientServerTestUtil.getPort(Hello_Service.class, Hello.class, new QName("urn:test", "HelloPort"));
        Hello_Service service = new Hello_Service();
        stub = service.getHelloPort();
        ClientServerTestUtil.setTransport(stub);
    }

    public void testHello() throws Exception {
          try{
              String arg = "foo";
              String extra = "bar";
              Hello_Type req = new Hello_Type();
              req.setArgument(arg);req.setExtra(extra);
              HelloOutput response = stub.hello(req);
          } catch(Exception e){
              System.out.println("e is " + e.getClass().getName());
              assertTrue(e instanceof HelloFault);
          }
      }

     public void testHelloAsyncPoll() throws Exception {
          try{
              System.out.println("testHelloAsyncPoll");
              System.out.println("==================");
              String arg = "foo";
              String extra = "bar";
              Hello_Type req = new Hello_Type();
              req.setArgument(arg);req.setExtra(extra);
              Response<HelloOutput> response = stub.helloAsync(req);
              System.out.print("\npolling for response ...");
              while (!response.isDone()) {
                  //System.out.print(".");
              }
              HelloOutput output = response.get();
              assertTrue(output == null);
          } catch(Exception e){
//              e.printStackTrace();
               assertTrue(e instanceof ExecutionException);
              assertTrue(e.getCause() instanceof HelloFault);
          }
      }

    public void testHelloAsyncCallback() throws Exception {
        try {
            String arg = "foo";
            String extra = "bar";
            Hello_Type req = new Hello_Type();
            req.setArgument(arg);
            req.setExtra(extra);
            Future<?> response = stub.helloAsync(req, new HelloCallbackHandler());
            System.out.print("\nWaiting for CallbackHandler to complete...");
            while (!response.isDone()) {
            }
            //System.out.print(".");
        } catch (Exception e) {
            //e.printStackTrace();
            assertFalse(true);
        }
    }


class HelloCallbackHandler extends TestCase implements AsyncHandler<HelloOutput> {

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.rpc.AsyncHandler#handleResponse(javax.xml.rpc.Response)
     */
    public void handleResponse(Response<HelloOutput> response) {
        System.out.println("In asyncHandler");
        try {
            HelloOutput output = response.get();
            //assertEquals("foo", output.getArgument());
            //assertEquals("bar", output.getExtra());
        } catch (ExecutionException e) {
            System.out.println("ExecutionException thrown");
            assertTrue(e.getCause() instanceof HelloFault);
            assertTrue(true);
            //e.printStackTrace();
        } catch (InterruptedException e) {
            assertTrue(false);
            // e.printStackTrace();
        } catch (Exception ex) {
            System.out.println("e is " + ex.getClass().getName());
            ex.printStackTrace();
        }
    }
}
}
