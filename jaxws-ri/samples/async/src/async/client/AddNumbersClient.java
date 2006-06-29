/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package async.client;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

public class AddNumbersClient {
    private AddNumbersImpl port;
    
    public AddNumbersClient () {
        port = new AddNumbersService().getAddNumbersImplPort();
    }
    
    public static void main (String[] args) {
        try {
            AddNumbersClient client = new AddNumbersClient ();
            //invoke synchronous method
            client.invokeSynchronous ();
            
            //invoke async polling method
            client.invokeAsyncPoll ();
            
            //invoke async callback method
            client.invokeAsyncCallback ();
        } catch (RemoteException e) {
            e.printStackTrace ();
        } catch (InterruptedException e) {
            e.printStackTrace ();
        } catch (ExecutionException e) {
            e.printStackTrace ();
        }
    }
    
    private void invokeSynchronous () throws RemoteException{
        int number1 = 10;
        int number2 = 20;
        
        System.out.println ("\nInvoking synchronous addNumber():");
        int result = port.addNumbers (number1, number2);
        System.out.printf ("The result of adding %d and %d is %d.\n", number1, number2, result);
    }
    
    private void invokeAsyncPoll () throws InterruptedException, ExecutionException{
        int number1 = 10;
        int number2 = 20;
        
        System.out.println ("\nInvoking Asynchronous Polling addNumbersAsync():");
        Response<AddNumbersResponse> resp = port.addNumbersAsync (number1, number2);
        Thread.sleep (2000);
        AddNumbersResponse output = resp.get ();
        System.out.printf ("The result of adding %d and %d is %d.\n", number1, number2, output.getReturn ());
    }
    
    private void invokeAsyncCallback () throws InterruptedException{
        int number1 = 10;
        int number2 = 20;
        
        System.out.println ("\nInvoking Asynchronous Callback addNumbersAsync():");
        AddNumbersCallbackHandler callbackHandler = new AddNumbersCallbackHandler ();
        Future<?> response = port.addNumbersAsync (number1, number2, callbackHandler);
        Thread.sleep (2000);
        
        AddNumbersResponse output = callbackHandler.getResponse ();
        System.out.printf ("The result of adding %d and %d is %d.\n", number1, number2, output.getReturn ());
    }
    
    /**
     * Async callback handler
     */
    private class AddNumbersCallbackHandler implements AsyncHandler<AddNumbersResponse> {
        private AddNumbersResponse output;
        /*
         * @see javax.xml.ws.AsyncHandler#handleResponse(javax.xml.ws.Response)
         */
        public void handleResponse (Response<AddNumbersResponse> response) {
            try {
                output = response.get ();
            } catch (ExecutionException e) {
                e.printStackTrace ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
        
        AddNumbersResponse getResponse (){
            return output;
        }
    }
    
}
