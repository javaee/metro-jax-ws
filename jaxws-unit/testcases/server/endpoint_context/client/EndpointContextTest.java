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
package server.endpoint_context.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import testutil.ClientServerTestUtil;

import javax.xml.namespace.QName;
import javax.xml.ws.*;
import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.Endpoint;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;
import java.util.*;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPBinding;


/**
 * Tests Endpoint.setEndpointContext()
 *
 * @author Jitendra Kotamraju
 */
public class EndpointContextTest extends TestCase {

    // endpoint has wsdlLocation="...". It publishes the same wsdl, metadata
    // docs
    public void testEndpointContext() throws Exception {
        int port1 = Util.getFreePort();
        String address1 = "http://localhost:"+port1+"/foo";
        Endpoint endpoint1 = getEndpoint(new FooService());

        int port2 = Util.getFreePort();
        String address2 = "http://localhost:"+port2+"/bar";
        Endpoint endpoint2 = getEndpoint(new BarService());

        EndpointContext ctxt = new MyEndpointContext(endpoint1, endpoint2);
        endpoint1.setEndpointContext(ctxt);
        endpoint2.setEndpointContext(ctxt);

        endpoint1.publish(address1);
        endpoint2.publish(address2);

        URL pubUrl = new URL(address1+"?wsdl");
        boolean patched = isPatched(pubUrl.openStream(), address1, address2);
        assertTrue(patched);

        pubUrl = new URL(address2+"?wsdl");
        patched = isPatched(pubUrl.openStream(), address1, address2);
        assertTrue(patched);

        endpoint1.stop();
        endpoint2.stop();
    }

    private Endpoint getEndpoint(Object impl) throws IOException {
        Endpoint endpoint = Endpoint.create(impl);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] docs = {
            "EchoService.wsdl",
        };
        List<Source> metadata = new ArrayList<Source>();
        for(String doc : docs) {
            URL url = cl.getResource(doc);
            metadata.add(new StreamSource(url.openStream(), url.toExternalForm()));
        }
        endpoint.setMetadata(metadata);
        return endpoint;
    }


/*
    public void testHtmlPage() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:"+port+"/echo";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpointWsdlLocation());
        endpoint.publish(address);
        URL pubUrl = new URL(address);
        URLConnection con = pubUrl.openConnection();
        InputStream is = con.getInputStream();
        int ch;
        while((ch=is.read()) != -1);
        assertTrue(con.getContentType().contains("text/html"));
        endpoint.stop();
    }
*/

    public boolean isPatched(InputStream in, String address1, String address2)
		throws IOException {
        boolean address1Patched = false;
        boolean address2Patched = false;

        BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
        String str;
        while ((str=rdr.readLine()) != null) {
            if (str.indexOf(address1) != -1) {
                address1Patched = true;
            }
            if (str.indexOf(address2) != -1) {
                address2Patched = true;
            }
        }
        return address1Patched && address2Patched;
    }


    @WebServiceProvider(serviceName="EchoService", portName="fooPort",
        targetNamespace="http://echo.org/")
    public class FooService implements Provider<Source> {
        public Source invoke(Source source) {
            throw new WebServiceException("Not testing the invocation");
        }
    }

    @WebServiceProvider(serviceName="EchoService", portName="barPort",
        targetNamespace="http://echo.org/")
    public class BarService implements Provider<Source> {
        public Source invoke(Source source) {
            throw new WebServiceException("Not testing the invocation");
        }
    }

    private static class MyEndpointContext extends EndpointContext {
        final Set<Endpoint> set = new HashSet<Endpoint>();

        public MyEndpointContext(Endpoint endpoint1, Endpoint endpoint2) {
            set.add(endpoint1);
            set.add(endpoint2);
        }

        public Set<Endpoint> getEndpoints() {
            return set;
        }
    }

}

