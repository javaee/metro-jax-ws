/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package fromjava.encoding.serversf.client;

import com.sun.xml.ws.developer.SerializationFeature;
import junit.framework.TestCase;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;

/**
 * @author Jitendra Kotamraju
 */
public class EchoTest extends TestCase {

    public void testSerializationFeature() throws Exception {
        Echo echoPort = new EchoService().getEchoPort(new SerializationFeature());
        echoPort.echoString("utf-8");
        // Server sends responses encoded with UTF-16
        testCharset(echoPort, "utf-16");
    }

    public void testSerializationFeatureUtf8() throws Exception {
        String encoding = "UTF-8";
        Echo echoPort = new EchoService().getEchoPort(new SerializationFeature(encoding));
        echoPort.echoString(encoding);
        // Server sends responses encoded with UTF-16
        testCharset(echoPort, "utf-16");
    }

    public void testSerializationFeatureUtf16() throws Exception {
        String encoding = "UTF-16";
        Echo echoPort = new EchoService().getEchoPort(new SerializationFeature(encoding));
        echoPort.echoString(encoding);
        // Server sends responses encoded with UTF-16
        testCharset(echoPort, "utf-16");
    }

    public void testExceptionUtf8() throws Exception {
        String encoding = "UTF-8";
        Echo echoPort = new EchoService().getEchoPort(new SerializationFeature(encoding));
        try {
            echoPort.echoString("Exception1");
            fail("Exception1");
        } catch (Exception1_Exception e) {
            Exception1 ex = e.getFaultInfo();
            assertEquals("my exception1", ex.getFaultString());
            assertTrue(ex.isValid());
        }
        // Server sends responses encoded with UTF-16
        testCharset(echoPort, "utf-16");
    }

    public void testExceptionUtf16() throws Exception {
        String encoding = "UTF-16";
        Echo echoPort = new EchoService().getEchoPort(new SerializationFeature(encoding));
        try {
            echoPort.echoString("Fault1");
            fail("Fault1");
        } catch (Fault1 e) {
            FooException ex = e.getFaultInfo();
            assertEquals("fault1", e.getMessage());
            assertEquals(44F, ex.getVarFloat());
            assertEquals((int)33, (int)ex.getVarInt());
            assertEquals("foo", ex.getVarString());
        }
        // Server sends responses encoded with UTF-16
        testCharset(echoPort, "utf-16");
    }

    public void testExceptionJIS() throws Exception {
        String encoding = "Shift_JIS";
        Echo echoPort = new EchoService().getEchoPort(new SerializationFeature(encoding));
        try {
            echoPort.echoString("WSDLBarException");
            fail("WSDLBarException");
        } catch (WSDLBarException e) {
            Bar ex = e.getFaultInfo();
            assertEquals("my barException", e.getMessage());
            assertEquals(33, ex.getAge());
        }
        // Server sends responses encoded with UTF-16
        testCharset(echoPort, "utf-16");
    }

    private void testCharset(Echo proxy, String encoding) {
        Map<String, List<String>> headers =
            (Map<String, List<String>>)((BindingProvider)proxy).getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS);
        String s = headers.get("Content-Type").get(0).toLowerCase();
        assertTrue(s.contains(encoding.toLowerCase()));
    }

}
