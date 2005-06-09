/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package fromwsdlhandler.server;

@javax.jws.WebService(endpointInterface="fromwsdlhandler.server.AddNumbersPortType")
public class AddNumbersImpl implements AddNumbersPortType {
    public int addNumbers(int number1, int number2) {
        return number1 + number2;
    }
    
}
