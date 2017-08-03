/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
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

package client.dispatch.wsdl_hello_lit_asynchandler.client;

import junit.framework.Assert;

import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.concurrent.ExecutionException;
import junit.framework.TestCase;

/**
 * Created by JAXRPC Development Team
 * <p/>
 * Date: Jul 15, 2004
 * Time: 3:51:11 PM
 */

public class JAXBAsyncHandlerRTEX implements javax.xml.ws.AsyncHandler {

    Hello_Type hello;
    VoidTestResponse voidTest;

    public JAXBAsyncHandlerRTEX(Hello_Type request) {
        hello = request;
    }

    public JAXBAsyncHandlerRTEX(VoidTestResponse request) {
        voidTest = request;
    }

    public void handleResponse(Response response) throws WebServiceException {
        System.out.println("Handling JAXB Response");
        try {
            Object obj = response.get();
            if (obj != null) {

                System.out.println("obj is " + obj.getClass().getName());
                HelloOutput helloResponse = null;
                VoidTestResponse voidTestResponse = null;

                if (obj instanceof HelloOutput) {
                    helloResponse = (HelloOutput) obj;
                    Assert.assertEquals(helloResponse.getExtra(), hello.getExtra());
                    Assert.assertEquals(helloResponse.getArgument(), hello.getArgument());
                    System.out.println(" testHelloResponseBadHelloSrcJAXB FAILED");
                } else if (obj instanceof VoidTestResponse){
                    voidTestResponse = (VoidTestResponse) obj;
                    Assert.assertEquals(voidTestResponse.getClass(), voidTest.getClass());
                    System.out.println(" testHelloResponseBadVoidSrcJAXB FAILED");

                } else {
					    Assert.fail("response is not valid");
					 }
            } else Assert.fail("obj is null");

        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof ExecutionException);
            //Assert.assertTrue(ex.getCause() instanceof SOAPFaultException);


            System.out.println("SOAPFaultExceptionTestPassed ");
            return;

        }

    }

}

