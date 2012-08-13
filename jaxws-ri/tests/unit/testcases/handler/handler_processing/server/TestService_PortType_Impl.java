/**
 * $Id: TestService_PortType_Impl.java,v 1.1 2007-09-22 00:39:25 ramapulavarthi Exp $
 *
 * Copyright 2005 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package handler.handler_processing.server;

import handler.handler_processing.common.HandlerTracker;
import handler.handler_processing.common.TestConstants;

/**
 * @author Rama Pulvarthi
 */
@javax.jws.WebService(serviceName="TestService", portName="TestServicePort", targetNamespace="urn:test", endpointInterface="handler.handler_processing.server.TestService")
public class TestService_PortType_Impl implements TestService, TestConstants {

    /*
     * Simple echo int method used for testing. 
     */
    public int testInt(int theInt) throws MyFaultException {
        if (theInt == SERVER_THROW_RUNTIME_EXCEPTION) {
            System.err.println(
                "service throwing runtime exception as instructed");
            throw new RuntimeException("test exception");
        }
        if (theInt == SERVER_THROW_MYFAULT_EXCEPTION) {
            System.err.println(
                "service throwing service exception as instructed");
            MyFaultInfo faultInfo = new MyFaultInfo();
            faultInfo.setVarString("element string");
            throw new MyFaultException("test fault", faultInfo);
        }
        if (HandlerTracker.VERBOSE_HANDLERS) {
            System.out.println("service received (and is returning) " + theInt);
        }
        return theInt;
    }
    
    /*
     * One-way version of the testInt method. Just outputs
     * a message. This method isn't called nearly as often as
     * testInt(), so the amount of output should be small.
     */
    public void testIntOneWay(int theInt) {
        System.out.println("service received " + theInt + " in one-way method");
    }
    
}
