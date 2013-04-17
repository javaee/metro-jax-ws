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

package fromwsdl.header.rpclit.client;

import junit.framework.TestCase;
import testutil.ClientServerTestUtil;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author JAX-RPC RI Development Team
 */
public class HeaderTest extends TestCase {

    private static HelloPortType stub;

    public HeaderTest(String name) throws Exception{
        super(name);
        HelloService service = new HelloService();
        stub = service.getHelloPort();
        ClientServerTestUtil.setTransport(stub);
    }

    public void testEcho3() throws Exception{
        assertEquals(1, stub.echo3("1"));
    }

    public void testEcho() throws Exception {
        ObjectFactory of = new ObjectFactory();
        EchoType reqBody = new EchoType();
        reqBody.setReqInfo("foobar");
        EchoType reqHeader = new EchoType();
        reqHeader.setReqInfo("foobar");
        EchoResponseType response = stub.echo(reqBody, reqHeader);
        assertEquals("foobarfoobar", response.getRespInfo());
    }

    public void testNullParameter() {
        ObjectFactory of = new ObjectFactory();
        EchoType reqHeader = new EchoType();
        reqHeader.setReqInfo("foobar");
        try{
            EchoResponseType response = stub.echo(null, reqHeader);
            assertTrue(false);
        }catch(WebServiceException e){
            assertTrue(true);
        }

    }

    public void testNullParameterOnServer() {
        ObjectFactory of = new ObjectFactory();
        EchoType reqBody = new EchoType();
        reqBody.setReqInfo("sendNull");
        EchoType reqHeader = new EchoType();
        reqHeader.setReqInfo("foobar");
        try{
            EchoResponseType response = stub.echo(reqBody, reqHeader);
            assertTrue(false);
        }catch(WebServiceException e){
            assertTrue(true);
        }

    }

    public void testEcho1() throws Exception {
        String str = "Hello";
        Holder<String> req = new Holder<String>(str);
        stub.echo1(req);
        assertEquals(str+" World!", req.value);
    }


    public void testEcho2() throws Exception {
        ObjectFactory of = new ObjectFactory();
        EchoType reqBody = of.createEchoType();
        reqBody.setReqInfo("foobar");
        EchoType req1Header = of.createEchoType();
        req1Header.setReqInfo("foobar");
        Echo2Type req2Header = of.createEcho2Type();
        req2Header.setReqInfo("foobar");
        Echo2ResponseType response = stub.echo2(reqBody, req1Header, req2Header);
        assertEquals("foobarfoobarfoobar", response.getRespInfo());
    }
}
