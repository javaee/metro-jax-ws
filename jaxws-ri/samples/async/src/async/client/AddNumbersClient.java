/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package async.client;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.xml.ws.BindingProvider;
import com.sun.xml.ws.developer.JAXWSProperties;
public class AddNumbersClient {
    private AddNumbersImpl port;

    public AddNumbersClient() {
        port = new AddNumbersService().getAddNumbersImplPort();
    }

    public static void main(String[] args) {
        try {
            AddNumbersClient client = new AddNumbersClient();
            //invoke synchronous method
            client.invokeSynchronous();

            //invoke async polling method
            client.invokeAsyncPoll();

            //invoke async callback method
            client.invokeAsyncCallback();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void invokeSynchronous() throws RemoteException {
        int number1 = 10;
        int number2 = 20;

        System.out.println("\nInvoking synchronous addNumber():");
        int result = port.addNumbers(number1, number2);
        System.out.printf("The result of adding %d and %d is %d.\n", number1, number2, result);
    }

    private void invokeAsyncPoll() throws InterruptedException, ExecutionException {
        int number1 = 10;
        int number2 = 20;

        //set request timeout to 30 sec, so that the client does n't wait forever
        int timeout = 30000;
        ((BindingProvider)port).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, timeout);

        System.out.println("\nInvoking Asynchronous Polling addNumbersAsync():");
        Response<AddNumbersResponse> resp = port.addNumbersAsync(number1, number2);
        while (!resp.isDone()) {
            System.out.println("No Response yet, Sleeping for 1 sec");
            Thread.sleep(1000);
        }
        AddNumbersResponse output = resp.get();
        System.out.printf("The result of adding %d and %d is %d.\n", number1, number2, output.getReturn());
    }

    private void invokeAsyncCallback() throws InterruptedException {
        int number1 = 10;
        int number2 = 20;

        System.out.println("\nInvoking Asynchronous Callback addNumbersAsync():");
        AddNumbersCallbackHandler callbackHandler = new AddNumbersCallbackHandler();
        Future<?> response = port.addNumbersAsync(number1, number2, callbackHandler);
        Thread.sleep(8000);
        if(response.isDone()) {
            AddNumbersResponse output = callbackHandler.getResponse();
            System.out.printf("The result of adding %d and %d is %d.\n", number1, number2, output.getReturn());
        } else {
            System.out.println("Waited 8 sec, no response yet, something wrong");
        }        
    }

    /**
     * Async callback handler
     */
    private class AddNumbersCallbackHandler implements AsyncHandler<AddNumbersResponse> {
        private AddNumbersResponse output;

        /*
        * @see javax.xml.ws.AsyncHandler#handleResponse(javax.xml.ws.Response)
        */
        public void handleResponse(Response<AddNumbersResponse> response) {
            System.out.println("AddNumbersCallbackHandler: Received Response from the service");
            try {
                output = response.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        AddNumbersResponse getResponse() {
            return output;
        }
    }

}
