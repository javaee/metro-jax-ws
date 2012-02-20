/**
 * $Id: HelloService12_Impl.java,v 1.1 2007-09-21 22:43:57 ramapulavarthi Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package handler.single_handlertube.server;

import static handler.single_handlertube.common.TestConstants.*;

import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;

/**
 * @author Rama Pulavarthi
 */
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
@javax.jws.WebService(serviceName = "Hello", portName="HelloPort12", targetNamespace="urn:test", endpointInterface="handler.single_handlertube.server.Hello12")
public class HelloService12_Impl implements Hello12 {
    
    public int hello12(int x) {
        System.out.println("Hello12Service_Impl received: " + x);
        if(x == SERVER_THROW_RUNTIME_EXCEPTION) {
            throw new RuntimeException(" Throwing RuntimeException as expected");
        }
        return x;
    }
    
}
