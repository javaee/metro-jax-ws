/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
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

package client.jaxws21.epr_get_port_na.client;

import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import testutil.ClientServerTestUtil;
import testutil.XMLTestCase;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.*;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;


/**
 * @author Arun Gupta
 *         Kathy walsh
 */
public class AddNumbersClient extends XMLTestCase {
    //may be used for verification
    private static final QName SERVICE_QNAME = new QName("http://example.com/", "AddNumbersService");
    private static final QName PORT_QNAME = new QName("http://example.com/", "AddNumbersPort");
    private static final String ENDPOINT_ADDRESS = "http://localhost:8080/jaxrpc-client_jaxws21_epr_get_port_na/hello";
    //maybe used for firther tests
    // private static final String CORRECT_ACTION = "http://example.com/AddNumbersPortType/addNumbersRequest";

    public AddNumbersClient(String name) {
        super(name);
    }

    private Dispatch<SOAPMessage> createDispatchWithWSDL() throws Exception {
        AddNumbersService service = new AddNumbersService();
        return service.createDispatch(PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE);
    }

    private AddNumbersPortType createStub() throws Exception {
        AddNumbersService service = new AddNumbersService();
        return service.getAddNumbersPort();
    }

    //wsdl has no adding extension specified
    public void testEPRGetPort() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }
        AddNumbersPortType proxy = createStub();
        //EndpointReference epr = ((BindingProvider) proxy).getEndpointReference();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);
        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class);

        assertTrue(port != null);

        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");
    }


    public void testEPRGetPortII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }
        AddNumbersPortType proxy = createStub();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);

        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class);

        assertTrue(port != null);

        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");
    }


    public void testEPRGetPortIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        //EndpointReference epr = ((BindingProvider) proxy).getEndpointReference();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);
        RespectBindingFeature feature = new RespectBindingFeature(false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature};
        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);

        assertTrue(port != null);

        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");
    }


    public void testEPRGetPortIV() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);

        //force addressing off
        AddressingFeature feature = new AddressingFeature(false, false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature};


        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);


        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");

    }


    public void testEPRGetPortV() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        //EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(W3CEndpointReference.class);
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);
        //force addressing off
        AddressingFeature feature = new AddressingFeature(false, false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature};


        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);


        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");


    }


    public void testEPRGetPortVI() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);

        RespectBindingFeature feature = new RespectBindingFeature(true);
        MemberSubmissionAddressingFeature addr = new MemberSubmissionAddressingFeature(false, false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature, addr};


        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);


        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");

    }


    public void testEPRGetPortVII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        //EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(W3CEndpointReference.class);
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);
        RespectBindingFeature feature = new RespectBindingFeature(true);
        AddressingFeature addr = new AddressingFeature(false, false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature, addr};


        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);


        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");


    }


    public void testEPRGetPortVIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);

        RespectBindingFeature feature = new RespectBindingFeature(true);
        MemberSubmissionAddressingFeature addr = new MemberSubmissionAddressingFeature(true, false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature, addr};


        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);


        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");


    }


    public void testEPRGetPortVIIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        //EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(W3CEndpointReference.class);
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);
        RespectBindingFeature feature = new RespectBindingFeature(true);
        AddressingFeature addr = new AddressingFeature(true, false);
        WebServiceFeature[] features = new WebServiceFeature[]{feature, addr};

        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);

        //try {
        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assertTrue(result == 6);
        //} catch (Exception ex) {
        //   assertTrue(ex instanceof SOAPFaultException);
        //   System.out.println(((SOAPFaultException) ex).getFault().getFaultString());
        //}
    }


    public void testEPRGetPortVIIIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);

        RespectBindingFeature feature = new RespectBindingFeature(true);
        MemberSubmissionAddressingFeature addr = new MemberSubmissionAddressingFeature(true, true);
        WebServiceFeature[] features = new WebServiceFeature[]{feature, addr};

        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);

        try {
            System.out.println("Adding numbers 2 and 4");
            int result = port.addNumbers(2, 4);
            assertFalse(result == 6);
        } catch (Exception ex) {
            assertTrue(ex instanceof SOAPFaultException);
            System.out.println(((SOAPFaultException) ex).getFault().getFaultString());
        }
    }


    public void testEPRGetPortVIIIIII() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }

        AddNumbersPortType proxy = createStub();
        //EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(W3CEndpointReference.class);
        EndpointReference epr = ((BindingProvider) proxy).getEndpointReference(MemberSubmissionEndpointReference.class);
        RespectBindingFeature feature = new RespectBindingFeature(true);
        AddressingFeature addr = new AddressingFeature(true, true);
        WebServiceFeature[] features = new WebServiceFeature[]{feature, addr};

        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class, features);
        assertTrue(port != null);

        try {
            System.out.println("Adding numbers 2 and 4");
            int result = port.addNumbers(2, 4);
            assertFalse(result == 6);
        } catch (Exception ex) {
            assertTrue(ex instanceof SOAPFaultException);
            System.out.println(((SOAPFaultException) ex).getFault().getFaultString());
        }
    }


    public void testDispatchEPRGetPort() throws Exception {

        if (ClientServerTestUtil.useLocal()) {
            System.out.println("HTTP Transport Only Exiting");
            return;
        }
        Dispatch<SOAPMessage> dispatch = createDispatchWithWSDL();
        EndpointReference epr = dispatch.getEndpointReference(MemberSubmissionEndpointReference.class);

        //wsdl has addressing required
        AddNumbersPortType port = epr.getPort(AddNumbersPortType.class);

        assertTrue(port != null);

        System.out.println("Adding numbers 2 and 4");
        int result = port.addNumbers(2, 4);
        assert(result == 6);
        System.out.println("Addinion of 2 and 4 successful");
    }
}
