/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved. 
 */
package annotations.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;
import javax.jws.WebResult;
import javax.jws.WebParam;

@WebService(targetNamespace = "http://duke.org", name="AddNumbers")
@SOAPBinding(style=SOAPBinding.Style.RPC, use=SOAPBinding.Use.LITERAL)
public interface AddNumbersIF extends Remote {
    
    @WebMethod(operationName="add", action="urn:addNumbers")
    @WebResult(name="return")
	public int addNumbers(
            @WebParam(name="num1")int number1, 
            @WebParam(name="num2")int number2) throws RemoteException, AddNumbersException;

}
