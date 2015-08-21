/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2015 Oracle and/or its affiliates. All rights reserved.
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

package client.response_context.client;

import junit.framework.TestCase;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import java.util.Map;

/**
 * @author Jitendra Kotamraju
 */
public class ClientTest extends TestCase {

    private Hello helloPort = new Hello_Service().getHelloPort();
    private String helloPortAddress = (String) ((BindingProvider) helloPort).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

    Dispatch createDispatchJAXB() {
        Service service = Service.create(new QName("urn:test", "Hello"));
        JAXBContext context = createJAXBContext(ObjectFactory.class);
        QName portQName = new QName("urn:test", "HelloPort");
        service.addPort(portQName,
                "http://schemas.xmlsoap.org/wsdl/soap/http",
                helloPortAddress);
        Dispatch dispatch = service.createDispatch(portQName, context, Service.Mode.PAYLOAD);
        return dispatch;
    }

    JAXBContext createJAXBContext(Class<?> objectFactory) {
        try {
            return JAXBContext.newInstance(new Class[]{objectFactory});
        } catch (JAXBException e) {
            e.printStackTrace();
            fail("Error creating JAXBContext");
            return null;
        }
    }

    public ClientTest(String name) throws Exception {
        super(name);
    }

    public void testResponseContext1() throws Exception {
        try {
            Hello_Type req = new Hello_Type();
            req.setArgument("foo");
            req.setExtra("bar");

            ((BindingProvider) helloPort).getRequestContext().put(
                    BindingProvider.ENDPOINT_ADDRESS_PROPERTY, helloPortAddress);

            HelloResponse response = helloPort.hello(req);
        } catch (Exception e) {
            Map rc = ((BindingProvider) helloPort).getResponseContext();
            assertNotNull(rc);
            assertNotNull(rc.get(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE));
            assertEquals(200,rc.get(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE));
        }
    }

    public void testResponseContext2() throws Exception {
        Object ctx = ((BindingProvider)helloPort).getResponseContext();
        System.out.println("ctx = " + ctx);
        assertNull(ctx);
    }

    public void testResponseContext3() throws Exception {
        Dispatch dispatch = createDispatchJAXB();
        try {
            Hello_Type req = new Hello_Type();
            req.setArgument("foo");
            req.setExtra("bar");

            dispatch.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, helloPortAddress.toString());
            dispatch.invoke(req);
        } catch (Exception e) {
            Map rc = dispatch.getResponseContext();
            assertEquals(200,rc.get(javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE));
        }
    }

    public void testResponseContext4() throws Exception {
        Dispatch dispatch = createDispatchJAXB();
        assertNull(dispatch.getResponseContext());
    }

}
