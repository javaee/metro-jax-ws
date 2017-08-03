/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package fromwsdl.catalog.client;

import junit.framework.TestCase;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.net.URL;

/**
 * @author Jitendra Kotamraju
 */
public class ClientTest extends TestCase {

    private Hello helloPort = new Hello_Service().getHelloPort();

    public ClientTest(String name) throws Exception {
        super(name);
    }

    public void test1() throws Exception {
        //import javax.xml.ws.handler.MessageContext;
        // http://localhost:8080/fromwsdl.catalog.server/HelloImpl
        String originalAddress = (String) ((BindingProvider) helloPort).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

        //now make catalog effective and get dummy address from the resolved wsdl
        Hello_Service newService = new Hello_Service(new
                URL("http://example.com/fromwsdl.catalog.server/HelloImpl?wsdl"), new QName("urn:test", "Hello"));
        Hello newPort = newService.getHelloPort();
        String newAddress = (String) ((BindingProvider) newPort).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        //Make sure the catalog was effective
        assertEquals("http://example.com/service", newAddress);

        //reset the address to original working address
        ((BindingProvider) newPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, originalAddress);
        String arg = "foo";
        String extra = "bar";
        Hello_Type req = new Hello_Type();
        req.setArgument(arg);
        req.setExtra(extra);
        HelloResponse response = newPort.hello(req);
        assertEquals(arg, response.getArgument());
        assertEquals(extra, response.getExtra());
        System.out.println("MIRAN: TEST finished.");
    }

}
