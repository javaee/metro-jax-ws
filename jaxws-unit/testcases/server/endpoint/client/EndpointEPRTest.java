/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package server.endpoint.client;

import junit.framework.TestCase;

import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import testutil.EprUtil;

/**
 * @author WS Development Team
 */
public class EndpointEPRTest extends TestCase {

    private static final QName serviceName = new QName("http://echo.org/", "RpcLitEndpoint");
    private static final QName portName = new QName("http://echo.org/", "RpcLitPort");
    private static final QName portTypeName = new QName("http://echo.abstract.org/", "RpcLitIF");

    public void testDefaultEPRWithWSDL() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        List<Source> metadata = new ArrayList<Source>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
                "RpcLitEndpoint.wsdl",
                "RpcLitAbstract.wsdl",
                "RpcLitEndpoint.xsd"
        };
        for (String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }
        endpoint.setMetadata(metadata);
        endpoint.publish(address);
        EndpointReference epr = endpoint.getEndpointReference();
        endpoint.stop();
        EprUtil.validateEPR(epr, address, null /*serviceName*/, null /*portName*/, null /* portTypeName */, Boolean.FALSE );
        printEPR(epr);
    }

    private void printEPR(EndpointReference epr) {
        System.out.println("---------------------------------------");
        epr.writeTo(new StreamResult(System.out));
        System.out.println("---------------------------------------");
    }

    public void testDefaultEPR() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        endpoint.publish(address);
        EndpointReference epr = endpoint.getEndpointReference();
        endpoint.stop();
        EprUtil.validateEPR(epr, address, null /*serviceName*/, null /*portName*/, null/*portTypeName*/, Boolean.FALSE );
        printEPR(epr);
    }

    public void testW3CEPR() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        List<Source> metadata = new ArrayList<Source>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
                "RpcLitEndpoint.wsdl",
                "RpcLitAbstract.wsdl",
                "RpcLitEndpoint.xsd"
        };
        for (String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }
        endpoint.setMetadata(metadata);
        endpoint.publish(address);
        W3CEndpointReference epr = endpoint.getEndpointReference(W3CEndpointReference.class);
        endpoint.stop();
        EprUtil.validateEPR(epr, address, null/*serviceName*/, null/*portName*/, null/*portTypeName*/, Boolean.FALSE );
        printEPR(epr);
    }

    public void testMSEPR() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        List<Source> metadata = new ArrayList<Source>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
                "RpcLitEndpoint.wsdl",
                "RpcLitAbstract.wsdl",
                "RpcLitEndpoint.xsd"
        };
        for (String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }
        endpoint.setMetadata(metadata);
        endpoint.publish(address);
        MemberSubmissionEndpointReference epr = endpoint.getEndpointReference(MemberSubmissionEndpointReference.class);
        endpoint.stop();
        EprUtil.validateEPR(epr, address, serviceName, portName, portTypeName, Boolean.TRUE );
        printEPR(epr);
    }

    public void testProviderEndpointW3CEPR() {
        int port = Util.getFreePort();
        String address = "http://127.0.0.1:" + port + "/";
        Endpoint e = Endpoint.create(HTTPBinding.HTTP_BINDING, new MyProvider());
        e.publish(address);
        W3CEndpointReference epr = e.getEndpointReference(W3CEndpointReference.class);
        e.stop();
        printEPR(epr);
        EprUtil.validateEPR(epr, address, null, null, null, Boolean.FALSE );

    }

    public void testProviderEndpointW3CEPR_WSDL() throws Exception {
        int port = Util.getFreePort();
        String address = "http://127.0.0.1:" + port + "/";
        Endpoint e = Endpoint.create(HTTPBinding.HTTP_BINDING, new MyProvider());
        List<Source> metadata = new ArrayList<Source>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
                "RpcLitEndpoint.wsdl",
                "RpcLitAbstract.wsdl",
                "RpcLitEndpoint.xsd"
        };
        for (String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }
        e.setMetadata(metadata);
        Map<String, Object> endpointProps = new HashMap<String, Object>();
        endpointProps.put(Endpoint.WSDL_SERVICE, new QName("http://echo.org/", "RpcLitEndpoint"));
        endpointProps.put(Endpoint.WSDL_PORT, new QName("http://echo.org/", "RpcLitPort"));
        e.setProperties(endpointProps);
        e.publish(address);
        W3CEndpointReference epr = e.getEndpointReference(W3CEndpointReference.class);
        e.stop();
        //EprUtil.validateEPR(epr, address, serviceName, portName, portTypeName, Boolean.TRUE );
        EprUtil.validateEPR(epr, address, null,null,null,false );
        printEPR(epr);
    }

    public void testProviderEndpointMSCEPR_WSDL() throws Exception {
        int port = Util.getFreePort();
        String address = "http://127.0.0.1:" + port + "/";
        Endpoint e = Endpoint.create(HTTPBinding.HTTP_BINDING, new MyProvider());
        List<Source> metadata = new ArrayList<Source>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
                "RpcLitEndpoint.wsdl",
                "RpcLitAbstract.wsdl",
                "RpcLitEndpoint.xsd"
        };
        for (String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }
        e.setMetadata(metadata);
        Map<String, Object> endpointProps = new HashMap<String, Object>();
        endpointProps.put(Endpoint.WSDL_SERVICE, new QName("http://echo.org/", "RpcLitEndpoint"));
        endpointProps.put(Endpoint.WSDL_PORT, new QName("http://echo.org/", "RpcLitPort"));
        e.setProperties(endpointProps);
        e.publish(address);
        MemberSubmissionEndpointReference epr = e.getEndpointReference(MemberSubmissionEndpointReference.class);
        e.stop();
        EprUtil.validateEPR(epr, address, serviceName, portName, portTypeName, Boolean.TRUE );
        printEPR(epr);
    }

    public void testProviderEndpointMSCEPR() {
        int port = Util.getFreePort();
        String address = "http://127.0.0.1:" + port + "/";
        Endpoint e = Endpoint.create(HTTPBinding.HTTP_BINDING, new MyProvider());
        e.publish(address);
        MemberSubmissionEndpointReference epr = e.getEndpointReference(MemberSubmissionEndpointReference.class);
        e.stop();
        EprUtil.validateEPR(epr, address, null, null, null, Boolean.FALSE );
        printEPR(epr);
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
