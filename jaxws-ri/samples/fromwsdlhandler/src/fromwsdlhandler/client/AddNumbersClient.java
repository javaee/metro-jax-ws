/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package fromwsdlhandler.client;

import javax.xml.ws.ServiceFactory;

import java.rmi.RemoteException;

public class AddNumbersClient {
    public static void main (String[] args) {
        try {
            ServiceFactory serviceFactory = ServiceFactory.newInstance ();
            AddNumbersService service = (AddNumbersService)serviceFactory.createService ((java.net.URL)null, AddNumbersService.class);
            AddNumbersPortType port = service.getAddNumbersPort ();
            
            int number1 = 10;
            int number2 = 20;
            
            System.out.printf ("Invoking addNumbers(%d, %d)\n", number1, number2);
            int result = port.addNumbers (number1, number2);
            System.out.printf ("The result of adding %d and %d is %d.\n\n", number1, number2, result);
            
            number1 = -10;
            System.out.printf ("Invoking addNumbers(%d, %d)\n", number1, number2);
            result = port.addNumbers (number1, number2);
            System.out.printf ("The result of adding %d and %d is %d.\n", number1, number2, result);
        } catch (RemoteException ex) {
            ex.printStackTrace ();
        } catch (AddNumbersFault_Exception ex) {
            System.out.printf ("Caught AddNumbersFault_Exception: %s\n", ex.getFaultInfo ().getMessage ());
        }
    }
}
