/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package external_customize.server;

@javax.xml.ws.WebFault (name="AddNumbersException",
    targetNamespace="http://duke.org")
    public class AddNumbersException extends Exception {
    String info;
    
    public AddNumbersException (String message, String detail) {
        super (message);
        this.info = detail;
    }
    
    public String getFaultInfo () {
        return info;
    }
}
