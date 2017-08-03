/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
