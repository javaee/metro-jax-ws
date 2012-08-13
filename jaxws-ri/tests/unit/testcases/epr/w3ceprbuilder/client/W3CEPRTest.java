/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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
package epr.w3ceprbuilder.client;

import junit.framework.TestCase;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import testutil.EprUtil;
/**
 * Tests W3CEndpointReferenceBuilder
 * @author Rama Pulavarthi
 *
 * This is somewhat similar to wsa.w3c.fromwsdl.w3cepr test, but tests JAX-WS 2.2 defined behavior
 */
public class W3CEPRTest extends TestCase {
    public W3CEPRTest(String name) throws Exception {
        super(name);
    }

    Hello getStub() throws Exception {
        return new Hello_Service().getHelloPort();
    }

    private String getEndpointAddress() throws Exception{

        BindingProvider bp = ((BindingProvider) getStub());
        return
            (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    }

    private static final String NAMESPACEURI = "urn:test";
    private static final String SERVICE_NAME = "Hello";
    private static final String PORT_NAME = "HelloPort";
    private QName SERVICE_QNAME = new QName(NAMESPACEURI, SERVICE_NAME);
    private QName PORT_QNAME = new QName(NAMESPACEURI, PORT_NAME);
    private static final QName INTERFACE_NAME =  new QName(NAMESPACEURI,"Hello");

    private static String xmlRefParam1 = "<myns1:MyParam1 wsa:IsReferenceParameter='true' xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" xmlns:myns1=\"http://example.com/myparam1\">Hello</myns1:MyParam1>";

    private static String xmlRefParam2 = "<myns2:MyParam2 xmlns:myns2=\"http://example.com/myparam2\">There</myns2:MyParam2>";

    public void testW3CEprBuilder_withWSDL_ServiceName() throws Exception {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder = builder.address(getEndpointAddress());
        builder = builder.serviceName(SERVICE_QNAME);
        builder = builder.endpointName(PORT_QNAME);
        builder = builder.wsdlDocumentLocation(getEndpointAddress()+"?wsdl");
        DOMSource domsrc = makeDOMSource(xmlRefParam1);
        Document document = (Document) domsrc.getNode();
        builder = builder.referenceParameter(document.getDocumentElement());
        domsrc = makeDOMSource(xmlRefParam2);
        document = (Document) domsrc.getNode();
        builder = builder.referenceParameter(document.getDocumentElement());
        W3CEndpointReference epr = builder.build();
         DOMResult result= new DOMResult();
        epr.writeTo(result);
        assertTrue(EprUtil.validateEPR(result.getNode(), W3CEndpointReference.class, getEndpointAddress(), SERVICE_QNAME, PORT_QNAME,null, getEndpointAddress()+"?wsdl"));
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        epr.writeTo(new StreamResult(baos));
//        baos.writeTo(System.out);
    }

    public void testW3CEprBuilder_withWSDL() throws Exception {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder = builder.address(getEndpointAddress());
        builder = builder.wsdlDocumentLocation(getEndpointAddress()+"?wsdl");
        W3CEndpointReference epr = builder.build();
        DOMResult result= new DOMResult();
        epr.writeTo(result);
        assertTrue(EprUtil.validateEPR(result.getNode(), W3CEndpointReference.class, getEndpointAddress(), null, null, null, getEndpointAddress()+"?wsdl"));
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        epr.writeTo(new StreamResult(baos));
//        baos.writeTo(System.out);
    }

    public void testW3CEprBuilder_withWSDL_InterfaceName() throws Exception {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder = builder.address(getEndpointAddress());
        builder = builder.wsdlDocumentLocation(getEndpointAddress()+"?wsdl");
        builder = builder.interfaceName(INTERFACE_NAME);
        W3CEndpointReference epr = builder.build();
        DOMResult result= new DOMResult();
        epr.writeTo(result);
        assertTrue(EprUtil.validateEPR(result.getNode(), W3CEndpointReference.class, getEndpointAddress(), null, null, INTERFACE_NAME, getEndpointAddress()+"?wsdl"));
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        epr.writeTo(new StreamResult(baos));
//        baos.writeTo(System.out);
    }

    public DOMSource makeDOMSource(String msg) {
        byte[] bytes = msg.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        return new DOMSource(createDOMNode(inputStream));
    }

    public Node createDOMNode(InputStream inputStream) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            try {
                return builder.parse(inputStream);
            } catch (SAXException e) {
                fail("Error creating Dom Document");
            } catch (IOException e) {
                fail("Error creating Dom Document");
                fail("Error creating JABDispatch");
            }
        } catch (ParserConfigurationException pce) {
            fail("Error creating Dom Document");
            //IllegalArgumentException iae = new IllegalArgumentException(pce.getMessage());
            //iae.initCause(pce);
            //throw iae;
        }
        return null;
    }
}
