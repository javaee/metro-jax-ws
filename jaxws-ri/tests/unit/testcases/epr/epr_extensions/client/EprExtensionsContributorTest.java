/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package epr.epr_extensions.client;

import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import junit.framework.TestCase;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Tests pluggability of EndpointReferenceExtensionContributor and runtime access of EPR extensions inside EPR specified
 *  under wsdl:port on server and client
 *
 * @author Rama.Pulavarthi@sun.com
 */
public class EprExtensionsContributorTest extends TestCase {
    public EprExtensionsContributorTest(String name) {
        super(name);
        Hello hello = new HelloService().getHelloPort();
        endpointAddress = (String) ((BindingProvider) hello).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    }

    private String endpointAddress = "http://helloservice.org/Hello";
    private static final QName serviceName = new QName("http://helloservice.org/wsdl", "HelloService");
    private static final QName portName = new QName("http://helloservice.org/wsdl", "HelloPort");
    private static final QName portTypeName = new QName("http://helloservice.org/wsdl", "Hello");

    /**
     * Tests client-side access to EPR extensions specified in WSDL
     *
     * @throws Exception
     */
    public void testEprWithDispatchWithoutWSDL() throws Exception {
        Service service = Service.create(serviceName);
        service.addPort(portName, javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);
        Dispatch dispatch = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        WSEndpointReference wsepr = ((WSBindingProvider) dispatch).getWSEndpointReference();
        assertTrue(wsepr.getEPRExtensions().isEmpty());

    }

    /**
     * Tests client-side access to EPR extensions specified in WSDL
     *
     * @throws Exception
     */
    public void testEprWithDispatchWithWSDL() throws Exception {
        Service service = new HelloService();
        Dispatch dispatch = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        WSEndpointReference wsepr = ((WSBindingProvider) dispatch).getWSEndpointReference();
        assertTrue(wsepr.getEPRExtensions().size() == 2);
        WSEndpointReference.EPRExtension idExtn = wsepr.getEPRExtension(new QName("http://example.com/addressingidentity", "Identity"));
        assertTrue(idExtn != null && idExtn.getQName().equals(new QName("http://example.com/addressingidentity", "Identity")));


    }

    /**
     * Tests client-side access to EPR extensions specified in WSDL
     *
     * @throws Exception
     */
    public void testEprWithSEI() throws Exception {
        HelloService service = new HelloService();
        Hello hello = service.getHelloPort();
        WSEndpointReference wsepr = ((WSBindingProvider) hello).getWSEndpointReference();

        assertTrue(wsepr.getEPRExtensions().size() == 2);
        WSEndpointReference.EPRExtension idExtn = wsepr.getEPRExtension(new QName("http://example.com/addressingidentity", "Identity"));
        assertTrue(idExtn != null && idExtn.getQName().equals(new QName("http://example.com/addressingidentity", "Identity")));

    }

    /**
     * Tests server-side access to EPR extensions specified in WSDL
     *
     * @throws Exception
     */

    public void testEprOnServerSide() throws Exception {
        HelloService service = new HelloService();
        Hello hello = service.getHelloPort();
        W3CEndpointReference serverEpr = hello.getW3CEPR();
        //  printEPR(serverEpr);

        WSEndpointReference wsepr = new WSEndpointReference(serverEpr);
        assertTrue(wsepr.getEPRExtensions().size() == 2);
        WSEndpointReference.EPRExtension idExtn = wsepr.getEPRExtension(new QName("http://example.com/addressingidentity", "Identity"));
        assertTrue(idExtn != null && idExtn.getQName().equals(new QName("http://example.com/addressingidentity", "Identity")));
    }

    /**
     * Tests the published wsdl for EPR extensions
     * @throws Exception
     */

    public void testEprInPublishedWSDL() throws Exception {
        HelloService service = new HelloService();
        Hello hello = service.getHelloPort();
        WSDLPort wsdlModel =((WSBindingProvider) hello).getPortInfo().getPort();
        WSEndpointReference wsdlepr = wsdlModel.getExtension(WSEndpointReference.class);
        assertTrue(wsdlepr.getEPRExtensions().size() == 2);
        WSEndpointReference.EPRExtension idExtn = wsdlepr.getEPRExtension(new QName("http://example.com/addressingidentity", "Identity"));
        assertTrue(idExtn != null && idExtn.getQName().equals(new QName("http://example.com/addressingidentity", "Identity")));
    }

    private static void printEPR(EndpointReference epr) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult sr = new StreamResult(bos);
        epr.writeTo(sr);
        bos.flush();
        System.out.println(bos);
    }


}
