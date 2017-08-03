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

package client.response_context.client;

import junit.framework.TestCase;
import testutil.ClientServerTestUtil;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.util.Map;
import java.util.Set;


/**
 * @author JAX-RPC RI Development Team
 */
public class HelloLiteralTest extends TestCase {

    private QName serviceQName = new QName("urn:test", "Hello");
    private QName portQName = new QName("urn:test", "HelloPort");
    private String bindingIdString = "http://schemas.xmlsoap.org/wsdl/soap/http";
    private String endpointWSDL = "http://localhost:8080/jaxrpc-client_response_context/hello?WSDL";
    private String endpointAddress = "http://localhost:8080/jaxrpc-client_response_context/heh";  //bogus endpointAddress


    private Hello_Service service;

    public HelloLiteralTest(String name) {
        super(name);
    }


    private void createService() {

        try {

            service = new Hello_Service();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Hello createStub() {
        return (Hello) ((Hello_Service) service).getHelloPort();
    }

    private Hello getStub() throws Exception {
        createService();
        return createStub();
    }

    public void testHelloResponseContext() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            System.out.println("Need to run only in http transport");
            return;
        }
        doTestHelloErrorCode();
        doTestResponseContext();
    }

    private void doTestHelloErrorCode() {
        Hello stub = null;
        try {
            stub = getStub();
            String arg = "foo";
            String extra = "bar";
            Hello_Type req = new Hello_Type();
            req.setArgument(arg);
            req.setExtra(extra);

            ((BindingProvider) stub).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

            HelloResponse response = stub.hello(req);
            assertTrue(response == null);
        } catch (Exception e) {
            //e.printStackTrace();
            Map<String, Object> rc = ((BindingProvider) stub).getResponseContext();
            assertTrue(rc != null);
            Integer status_code = (Integer) rc.get(MessageContext.HTTP_RESPONSE_CODE);
            assertTrue(status_code != null);
            assertTrue(404 == status_code.intValue());
            /*java.util.Map<java.lang.String, java.util.List<java.lang.String>> responseHeaders =
                    (java.util.Map<java.lang.String, java.util.List<java.lang.String>>) rc.get(MessageContext.HTTP_RESPONSE_HEADERS);
            assertTrue(responseHeaders != null);
            assertFalse(responseHeaders.isEmpty());
            */
           // Set<String> keys = responseHeaders.keySet();
            //for (String key : keys) {

            //    java.util.List<java.lang.String> values = responseHeaders.get(key);
             //   System.out.print("Key : " + key + "       ");
              //  System.out.println("Values : ");
              //  for (String value : values) {
              //      System.out.println(value);
             //   }
            //}
        }
    }

    private void doTestResponseContext() throws Exception {
        Hello stub = getStub();
        String arg = "foo";
        String extra = "bar";
        Hello_Type req = new Hello_Type();
        req.setArgument(arg);
        req.setExtra(extra);

        Map<String, Object> rc = ((BindingProvider) stub).getResponseContext();

        assertTrue(rc == null);
    }

}
