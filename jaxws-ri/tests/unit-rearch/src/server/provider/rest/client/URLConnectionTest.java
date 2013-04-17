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

package server.provider.rest.client;

import testutil.ClientServerTestUtil;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import junit.framework.TestCase;
import org.w3c.dom.Node;
import java.net.URL;
import java.net.HttpURLConnection;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;
import java.io.OutputStream;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.InternetHeaders;
import javax.activation.DataSource;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.transport.Headers;

public class URLConnectionTest extends TestCase {

    private String endpointAddress =
        "http://localhost:/jaxrpc-provider_tests_rest/hello/restds";
    
    // HTTP GET to get image/jpeg
    public void testGetImageFromDS() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        URL url = new URL(endpointAddress+"/java.jpg");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.connect();
        
        // processing response headers
System.out.println("HEaders="+con.getHeaderFields());
        Map<String, List<String>> hdrs =  getCaseInsensitiveHeaders(
            con.getHeaderFields());
        List<String> hdrValues = hdrs.get("content-type");
        assertTrue(hdrValues != null);
        assertEquals("image/jpeg", hdrValues.get(0));

		// TODO validate content
    }

    private Map<String, List<String>> getCaseInsensitiveHeaders(
        Map<String, List<String>> in) {

        Headers out = new Headers();
        // Doesn't work
        //headers.putAll(in);
        if (in != null) {
            for(Map.Entry<String, List<String>> e : in.entrySet()) {
                out.put(e.getKey(), e.getValue());
            }
        }
        return out;
    }


}
