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

package whitebox.endpoint.client;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.net.URL;
import java.io.*;


/**
 * @author Jitendra Kotamraju
 */
public class EndpointMetadataTester extends TestCase {

    public void testMetadata() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        List<Source> metadata = new ArrayList<Source>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
                "WEB-INF/wsdl/RpcLitEndpoint.wsdl",
                "WEB-INF/wsdl/RpcLitAbstract.wsdl",
                "WEB-INF/wsdl/RpcLitEndpoint.xsd"
        };
        for (String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }
        endpoint.setMetadata(metadata);
        endpoint.publish(address);
        URL pubUrl = new URL(address + "?wsdl");
        boolean gen = isGenerated(pubUrl.openStream());
        assertFalse(gen);
        URL absUrl = new URL(address + "?wsdl=1");
        gen = isGenerated(absUrl.openStream());
        assertFalse(gen);
        URL xsdUrl = new URL(address + "?xsd=1");
        gen = isGenerated(xsdUrl.openStream());
        assertFalse(gen);
        endpoint.stop();
    }

    public void testJCKMetadata() throws IOException {
        int port = Util.getFreePort();
        String address = "http://localhost:" + port + "/jck";
        Endpoint endpoint = Endpoint.create(new JCKEndpoint());
        List<Source> metadata = new ArrayList<Source>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
                "WEB-INF/wsdl/jck.wsdl",
        };
        for (String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }

        endpoint.setMetadata(metadata);
        endpoint.publish(address);
        URL pubUrl = new URL(address + "?wsdl");
        boolean gen = isGenerated(pubUrl.openStream());
        assertFalse(gen);
        endpoint.stop();

    }

    public void testBadMetadata() throws IOException {
        int port = Util.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        List<Source> metadata = new ArrayList<Source>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
                "WEB-INF/wsdl/RpcLitBadEndpoint.wsdl",
                "WEB-INF/wsdl/RpcLitAbstract.wsdl",
                "WEB-INF/wsdl/RpcLitEndpoint.xsd"
        };
        for (String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }
        endpoint.setMetadata(metadata);

        try {
            endpoint.publish(address);
        } catch (Exception e) {
            System.out.println("Expected error: " + e.getMessage());
            assertTrue(true);
            return;
        }
    }


    public void testAbstractWsdl() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        List<Source> metadata = new ArrayList<Source>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
                "WEB-INF/wsdl/RpcLitAbstract.wsdl",
                "WEB-INF/wsdl/RpcLitEndpoint.xsd"
        };
        for (String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }
        endpoint.setMetadata(metadata);
        endpoint.publish(address);
        URL pubUrl = new URL(address + "?wsdl");
        boolean gen = isGenerated(pubUrl.openStream());
        assertTrue(gen);
        URL absUrl = new URL(address + "?wsdl=1");
        gen = isGenerated(absUrl.openStream());
        assertFalse(gen);
        URL xsdUrl = new URL(address + "?xsd=1");
        gen = isGenerated(xsdUrl.openStream());
        assertFalse(gen);
        endpoint.stop();
    }

    public void testXsd() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        List<Source> metadata = new ArrayList<Source>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
                "WEB-INF/wsdl/RpcLitEndpoint.xsd"
        };
        for (String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }
        endpoint.setMetadata(metadata);
        endpoint.publish(address);
        URL pubUrl = new URL(address + "?wsdl");
        boolean gen = isGenerated(pubUrl.openStream());
        assertTrue(gen);
        URL absUrl = new URL(address + "?wsdl=1");
        gen = isGenerated(absUrl.openStream());
        assertTrue(gen);
//        URL xsdUrl = new URL(address + "?xsd=1");
//        gen = isGenerated(xsdUrl.openStream());
//        assertFalse(gen);
        endpoint.stop();
    }

    public void testDuplicateConcreteWsdl() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        List<Source> metadata = new ArrayList<Source>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
                "WEB-INF/wsdl/RpcLitEndpoint.wsdl",
                "WEB-INF/wsdl/RpcLitAbstract.wsdl",
                "WEB-INF/wsdl/RpcLitEndpoint.xsd"
        };
        for (String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }
        URL url = cl.getResource(docs[0]);
        metadata.add(new StreamSource(url.openStream(), url.toExternalForm() + "1"));
        endpoint.setMetadata(metadata);
        try {
            endpoint.publish(address);
            assertFalse(true);
        } catch (Exception e) {
            // Duplicate concrete WSDL generates exception
            // Intentionally leaving empty
        }
    }

    public void testDuplicateAbstractWsdl() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        List<Source> metadata = new ArrayList<Source>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
                "WEB-INF/wsdl/RpcLitEndpoint.wsdl",
                "WEB-INF/wsdl/RpcLitAbstract.wsdl",
                "WEB-INF/wsdl/RpcLitEndpoint.xsd"
        };
        for (String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }
        URL url = cl.getResource(docs[1]);
        metadata.add(new StreamSource(url.openStream(), url.toExternalForm() + "1"));
        endpoint.setMetadata(metadata);
        try {
            endpoint.publish(address);
            assertFalse(true);
        } catch (Exception e) {
            // Duplicate abstract WSDL generates exception
            // Intentionally leaving empty
        }
    }

    public boolean isGenerated(InputStream in) throws IOException {
        BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
        String str;
        while ((str = rdr.readLine()) != null) {
            if (str.indexOf("NOT_GENERATED") != -1) {
                return false;
            }

        }
        return true;
    }
}

