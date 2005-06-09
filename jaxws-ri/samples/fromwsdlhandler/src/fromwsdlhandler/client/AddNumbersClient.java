/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package fromwsdlhandler.client;

import javax.xml.ws.ServiceFactory;

import java.rmi.RemoteException;

public class AddNumbersClient {
    public static void main(String[] args) {
        try {
            ServiceFactory serviceFactory = ServiceFactory.newInstance();
            AddNumbersService service = (AddNumbersService)serviceFactory.createService((java.net.URL)null, AddNumbersService.class);
            AddNumbersPortType port = service.getAddNumbersPort();
            
            int number1 = 5;
            int number2 = 10;
            
            System.out.printf("Invoking addNumbers(%d, %d)\n", number1, number2);
            int result = port.addNumbers(number1, number2);
            System.out.printf("The result of adding %d and %d is %d.", number1, number2, result);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }
}
