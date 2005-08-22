/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package external_customize.client;

import java.rmi.RemoteException;

import javax.xml.ws.ServiceFactory;

public class AddNumbersClient {
    private MathUtil port;
    
    public AddNumbersClient () {
        ServiceFactory serviceFactory = ServiceFactory.newInstance ();
        MathUtilService service = (MathUtilService)serviceFactory.createService ((java.net.URL)null, MathUtilService.class);
        port = service.getMathUtil ();
    }
    
    public static void main (String[] args) {
        try {
            AddNumbersClient client = new AddNumbersClient ();
            
            //invoke synchronous method
            client.invoke ();
        } catch(MathUtilException e){
            System.out.println ("\tException detail: "+ e.getMessage ()+", "+e.getFaultInfo ());
        }
    }
    
    private void invoke () throws MathUtilException{
        int number1 = 10;
        int number2 = 20;
        
        System.out.printf ("Invoking addNumbers(%d, %d)\n", number1, number2);
        int result = port.add (number1, number2);
        System.out.printf ("The result of adding %d and %d is %d.\n\n", number1, number2, result);
        
        //lets make endpoint throw exception
        number1 = -10;
        System.out.printf ("Invoking addNumbers(%d, %d) and expect exception.\n", number1, number2);
        result = port.add (number1, number2);
    }
}
