/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package fromjavahandler.server;

import javax.jws.HandlerChain;
import javax.jws.WebService;

@WebService
    @HandlerChain ( file="handlers.xml", name="Chain1")
    public class AddNumbersImpl {
    
    /**
     * @param number1
     * @param number2
     * @return The sum
     * @throws AddNumbersException
     *             if any of the numbers to be added is negative.
     */
    public int addNumbers (int number1, int number2) throws AddNumbersException {
        if (number1 < 0 || number2 < 0) {
            throw new AddNumbersException ("Negative number cant be added!",
                "Numbers: " + number1 + ", " + number2);
        }
        return number1 + number2;
    }
    
}
