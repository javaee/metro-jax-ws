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

package provider.asyncprovider_ns_800.client;

import junit.framework.TestCase;

import javax.xml.soap.*;
import javax.xml.ws.BindingProvider;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;

/**
 * @author Jitendra Kotamraju
 */
public class NamespaceTest extends TestCase {

    public NamespaceTest(String name) throws Exception{
        super(name);
    }
    
    public void testNamespace() throws Exception {
        // Create request message
        String str = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body><ns2:Hello xmlns:ns2='urn:test:types'><argument>arg</argument><extra>extra</extra></ns2:Hello></S:Body></S:Envelope>";
        MessageFactory fact = MessageFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        SOAPMessage req = fact.createMessage(headers, new ByteArrayInputStream(str.getBytes("UTF-8")));

        // Get address
        Hello hello = new Hello_Service().getHelloAsyncPort();
        BindingProvider bp = (BindingProvider)hello;
        String address = (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

        // Get response message
        SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
        SOAPConnection con = factory.createConnection();
        SOAPMessage res = con.call(req, address);
        con.close();

        // Verify namespaces
        SOAPElement elem = (SOAPElement)res.getSOAPBody().getChildElements().next();
        elem = (SOAPElement)elem.getChildElements().next();
        QName expected = new QName("http://xml.netbeans.org/schema/dataTypes.xsd", "Element20");
        assertEquals(expected, elem.getElementQName());
        assertEquals("http://j2ee.netbeans.org/wsdl/QNameAssignment_WithStructuredPart", elem.getNamespaceURI("msgns"));
    }

}
