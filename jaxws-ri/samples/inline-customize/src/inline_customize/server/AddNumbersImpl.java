/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package inline_customize.server;

@javax.jws.WebService (endpointInterface="inline_customize.server.MathUtil")
public class AddNumbersImpl implements MathUtil{
    
    /**
     * @param number1
     * @param number2
     * @return The sum
     * @throws MathUtilException
     *             if any of the numbers to be added is negative.
     */
    public int add (int number1, int number2) throws MathUtilException {
        if(number1 < 0 || number2 < 0){
            throw new MathUtilException ("Negative number cant be added!", "Numbers: "+number1+", "+number2);
        }
        return number1 + number2;
    }
    
}
