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

/**
 * Tests HTTP GET, POST, HEAD, PUT, DELETE methods using HttpURLConnection
 */
public class HelloLiteralTest extends TestCase {

    private String endpointAddress =
        "http://localhost:/jaxrpc-provider_tests_rest/hello/rest";
    private int bodyIndex = 0;
    private String[] body = {
        "<HelloRequest xmlns=\"urn:test:types\"><argument xmlns=\"\">foo</argument><extra xmlns=\"\">bar</extra></HelloRequest>",
        "<ans1:HelloRequest xmlns:ans1=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></ans1:HelloRequest>"
    };

    private byte[] getSource() {
        int i = (++bodyIndex)%body.length;
        return body[i].getBytes();
    }


    public HelloLiteralTest(String name) {
        super(name);
    }

    // HTTP DELETE
    public void testDeleteMethod() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        URL url = new URL(endpointAddress+"?a=%3C%3Fxml+version%3D%221.0%22%3E&b=c");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("DELETE");
        con.connect();

        // Got correct HTTP status code ?
        int code = con.getResponseCode();
        assertEquals(201, code);

        // Got correct HTTP response headers ?
        Map<String, List<String>> hdrs =  getCaseInsensitiveHeaders(
            con.getHeaderFields());
        List<String> hdrValues = hdrs.get("custom-header");
        assertTrue(hdrValues != null);
        assertEquals(hdrValues.get(0), "custom-delete-value");
    }
    
    // HTTP GET
    public void testQuery() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        URL url = new URL(endpointAddress+"?a=%3C%3Fxml+version%3D%221.0%22%3E&b=c");
        process(url);
    }

    // HTTP GET
    public void testPath() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        URL url = new URL(endpointAddress+"/a/b");
        process(url);
    }

    // HTTP HEAD
    public void testHeadMethod() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        URL url = new URL(endpointAddress+"/a/b");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("HEAD");
        con.connect();

        // processing response
        int code = con.getResponseCode();
        assertEquals(201, code);
        
        // processing response headers
        Map<String, List<String>> hdrs =  getCaseInsensitiveHeaders(
            con.getHeaderFields());
        List<String> hdrValues = hdrs.get("custom-header");
        assertTrue(hdrValues != null);
        assertEquals(hdrValues.get(0), "custom-head-value");
    }

    // HTTP PUT
    public void testPutMethod() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        URL url = new URL(endpointAddress);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("PUT");
        con.addRequestProperty("Content-Type", "text/xml");
        con.setDoInput(true);
        con.setDoOutput(true);
        // Write XML content
        OutputStream out = con.getOutputStream();
        out.write(getSource());
        out.close();

        // Got correct HTTP status code ?
        int code = con.getResponseCode();
        assertEquals(201, code);

        // Got correct HTTP response headers ?
        Map<String, List<String>> hdrs =  getCaseInsensitiveHeaders(
            con.getHeaderFields());
        List<String> hdrValues = hdrs.get("custom-header");
        assertTrue(hdrValues != null);
        assertEquals(hdrValues.get(0), "custom-put-value");

        // processing response xml
        process(con.getInputStream());
    }

    // HTTP POST
    public void testPostMethod() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        URL url = new URL(endpointAddress);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.addRequestProperty("Content-Type", "text/xml");
        con.setDoInput(true);
        con.setDoOutput(true);
        // Write XML content
        OutputStream out = con.getOutputStream();
        out.write(getSource());
        out.close();

        // Check if we got the correct HTTP response code
        int code = con.getResponseCode();
        assertEquals(201, code);

        // Check if we got the correct HTTP response headers
        Map<String, List<String>> hdrs =  getCaseInsensitiveHeaders(
            con.getHeaderFields());
        List<String> hdrValues = hdrs.get("custom-header");
        assertTrue(hdrValues != null);
        assertEquals(hdrValues.get(0), "custom-post-value");

        // Check if we got the correct Content-Type
        List<String> ctValues = hdrs.get("Content-Type");
        assertTrue(ctValues != null);

        // Check if we got the correct response
        processMime(con.getInputStream(), ctValues.get(0));
    }

    private void process(URL url) throws Exception {
        process(url.openStream());
    }

    private void process(InputStream in) throws Exception {
        StreamSource source = new StreamSource(in);
        Node node = getDOM(source);
        node = node.getFirstChild();
        assertEquals("HelloResponse", node.getLocalName());
        assertEquals("urn:test:types", node.getNamespaceURI());
    }
    
    private void printSource(Source source) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StreamResult sr = new StreamResult(bos );
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperties(oprops);
            trans.transform(source, sr);
            System.out.println("**** bos ******"+bos.toString());
            bos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private Node getDOM(Source source) {
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            DOMResult dr = new DOMResult();
            trans.transform(source, dr);
            return dr.getNode();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
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

	private void processMime(final InputStream in, final String ct)
		throws Exception {
		System.out.println("ct="+ct);
		DataSource ds = new DataSource() {
			public InputStream getInputStream() {
				return in;
			}
			public OutputStream getOutputStream() {
				return null;
			}
			public String getContentType() {
				return ct;
			}
			public String getName() {
				return "";
			}
		};
		final MimeMultipart multipart = new MimeMultipart(ds, null);

		int no = multipart.getCount();
		assertEquals(2, no);
		MimeBodyPart bodyPart = (MimeBodyPart)multipart.getBodyPart(0);
		process(bodyPart.getInputStream());
	}

}
