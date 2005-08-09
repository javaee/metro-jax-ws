/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved. 
 */
package annotations.client;

import javax.xml.ws.ServiceFactory;

public class AddNumbersClient {
    public static void main(String[] args) {
        try {
            ServiceFactory serviceFactory = ServiceFactory.newInstance();
            AddNumbersIFService service = (AddNumbersIFService)serviceFactory.createService((java.net.URL)null, AddNumbersIFService.class);
            AddNumbers port = service.getAddNumbers();

            int number1 = 10;
            int number2 = 20;

            System.out.printf("Invoking addNumbers(%d, %d)\n", number1, number2);
            int result = port.add(number1, number2);
            System.out.printf("The result of adding %d and %d is %d.\n\n", number1, number2, result);

            number1 = -10;
            System.out.printf("Invoking addNumbers(%d, %d)\n", number1, number2);
            result = port.add(number1, number2);
            System.out.printf("The result of adding %d and %d is %d.\n", number1, number2, result);

        } catch (AddNumbersException_Exception ex) {
                System.out.printf("Caught AddNumbersException_Exception: %s\n", ex.getFaultInfo().getFaultInfo());
        }
    }
}
