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

package fromjava.soap12.bindingtype_xsoap12.client;

import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import testutil.ClientServerTestUtil;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;


/**
 * @author JAX-RPC Development Team
 */
public class EchoClient extends TestCase {
    private QName portQName = new QName("http://echo.org/", "Echo");;
    private static Echo stub;

    public EchoClient(String name) throws Exception{
        super(name);
        if (stub == null) {
            EchoService service = new EchoService();
            stub = service.getEchoPort();      
            ClientServerTestUtil.setTransport(stub);   
            BindingProvider bp = (BindingProvider)stub;
            System.out.println("Binding for port is: "+bp.getBinding());
            assertTrue(bp.getBinding() instanceof SOAPBinding);
            SOAPBinding sb = (SOAPBinding)bp.getBinding();
	    SOAPFactory factory = sb.getSOAPFactory();
	    System.out.println("SOAPFactory for port is: "+factory);
	    if(!(factory instanceof SOAPFactory)) {
	        System.out.println("SOAPFactory for port is not an instance of SOAPFactory");
	    } else
	        System.out.println("SOAPFactory for port is an instance of SOAPFactory");

	    System.out.println("Make sure that the SOAPFactory is based on SOAP1.2 protocol");
	    SOAPFault soapfault = factory.createFault();
	    try {
		soapfault.setFaultRole("http://myfault.org");
		System.out.println("SOAPFactory is a based on SOAP1.2 protocol (Expected)");
            } catch (UnsupportedOperationException e) {
		System.out.println("SOAPFactory is a based on SOAP1.1 protocol (Unexpected)");
                assertTrue(false);
            }
        }
    }


    public void testSimple() throws Exception {
        Holder<String> strHolder = new Holder<String>();
        strHolder.value = "fred";

        assertTrue(stub.echoString("test").equals("test"));
        assertTrue(stub.echoString("Mary & Paul").equals("Mary & Paul"));
        assertTrue(stub.echoLong(33L) == 33L);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(EchoClient.class);
        return suite;
    }

    /*
     * for debugging
     */
    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            EchoClient testor = new EchoClient("TestClient");
//            testor.testHeadersDynamic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

