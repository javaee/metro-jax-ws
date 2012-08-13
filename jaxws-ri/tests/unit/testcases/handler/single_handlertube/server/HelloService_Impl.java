/**
 * $Id: HelloService_Impl.java,v 1.1 2007-09-21 22:43:57 ramapulavarthi Exp $
 *
 * Copyright 2005 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package handler.single_handlertube.server;

import static handler.single_handlertube.common.TestConstants.*;


/**
 * @author Rama Pulavarthi
 */
@javax.jws.WebService(serviceName = "Hello", portName="HelloPort", targetNamespace="urn:test", endpointInterface="handler.single_handlertube.server.Hello")
public class HelloService_Impl implements Hello {
    
    public int hello(int x) {
        System.out.println("HelloService_Impl received: " + x);
        if(x == SERVER_THROW_RUNTIME_EXCEPTION) {
            throw new RuntimeException(" Throwing RuntimeException as expected");
        }
        return x;
    }
    
}
