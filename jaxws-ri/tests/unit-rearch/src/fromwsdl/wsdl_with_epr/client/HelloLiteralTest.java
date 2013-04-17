/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package fromwsdl.wsdl_with_epr.client;

import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.AddressingFeature;

import junit.framework.TestCase;
import testutil.ClientServerTestUtil;

import javax.xml.namespace.QName;
import javax.xml.ws.*;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import javax.xml.bind.JAXBContext;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.sun.xml.ws.util.DOMUtil;


/**
 * @author JAX-RPC RI Development Team
 */
public class HelloLiteralTest extends TestCase {

    // main method added for debugging
    public static void main(String [] args) {
        try {
            System.setProperty("uselocal", "true");
            HelloLiteralTest test = new HelloLiteralTest("HelloLiteralTest");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Hello stub;

    public HelloLiteralTest(String name) throws Exception {
        super(name);
        Hello_Service service = new Hello_Service();

        stub = service.getHelloPort();
        ClientServerTestUtil.setTransport(stub);
    }

    //tests EPR in wsdl
    // wsdl epr has two reference parameters
    public void testHello() throws Exception {
        try {
            String arg = "foo";
            String extra = "bar";
            Hello_Type req = new Hello_Type();
            req.setArgument(arg);
            req.setExtra(extra);
            HelloResponse response = stub.hello(req);
            assertEquals(arg, response.getArgument());
            assertEquals(extra, response.getExtra());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    //tests EPR in wsdl
    // wsdl epr has two reference parameters
    public void testHelloDispatch() throws Exception {
        try {
            String arg = "foo";
            String extra = "bar";
            Hello_Type req = new Hello_Type();
            req.setArgument(arg);
            req.setExtra(extra);
            Hello_Service service = new Hello_Service();
            JAXBContext jc = javax.xml.bind.JAXBContext.newInstance(fromwsdl.wsdl_with_epr.client.ObjectFactory.class);
            QName port = new QName("urn:test","HelloPort");
            Dispatch<Object> dispatch = service.createDispatch(port, jc, Service.Mode.PAYLOAD, new AddressingFeature());
            HelloResponse response = (HelloResponse) dispatch.invoke(req);
            assertEquals(arg, response.getArgument());
            assertEquals(extra, response.getExtra());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    //Tests epr overriding
    //epr(1 reference parameter) passed as argument overrides epr in wsdl(2 reference parameters)
    public void testEchoArray() throws Exception {
        try {
            String xmlParam1 = "<myns:MyParam1 xmlns:myns=\"http://cptestservice.org/wsdl\">Hello</myns:MyParam1>";
            Node n1 = DOMUtil.createDOMNode(new ByteArrayInputStream(xmlParam1.getBytes()));
            String endpointAddress = (String) ((BindingProvider) stub).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            W3CEndpointReferenceBuilder eprBuilder = new W3CEndpointReferenceBuilder();
            eprBuilder.address(endpointAddress);
            eprBuilder.referenceParameter((Element) n1.getFirstChild());
            W3CEndpointReference epr = eprBuilder.build();
            Hello_Service service = new Hello_Service();
            Hello newStub = service.getPort(epr, Hello.class);

            String[] in = {"JAXRPC 1.0", "JAXRPC 1.1", "JAXRPC 1.1.2", "JAXRPC 2.0"};
            NameType nt = new NameType();
            nt.getName().add(in[0]);
            nt.getName().add(in[1]);
            nt.getName().add(in[2]);
            nt.getName().add(in[3]);
            javax.xml.ws.Holder<NameType> req = new javax.xml.ws.Holder<NameType>(nt);
            newStub.echoArray(req);
            assertTrue(req.value == null);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }


}
