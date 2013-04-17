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

import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.ws.transport.Headers;
import junit.framework.TestCase;
import org.w3c.dom.Node;
import server.provider.rest.common.MyHandler;
import testutil.ClientServerTestUtil;

import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests HTTP GET, POST, HEAD, PUT, DELETE methods using HttpURLConnection
 */
public class DispatchClient extends TestCase {
    private String endpointAddress =
            "http://localhost:/jaxrpc-provider_tests_rest/hello/rest";
    private int bodyIndex = 0;
    private static final String[] body = {
        "<HelloRequest xmlns=\"urn:test:types\"><argument xmlns=\"\">foo</argument><extra xmlns=\"\">bar</extra></HelloRequest>",
        "<ans1:HelloRequest xmlns:ans1=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></ans1:HelloRequest>"
    };

    private static final String body1 = "<HelloRequest xmlns=\"urn:test:types\"><argument xmlns=\"\">NO_ATTACHMENTS</argument><extra xmlns=\"\">bar</extra></HelloRequest>";
    
    private void setHandlerChain(BindingProvider bp) {
        List<Handler> handlerChain= new ArrayList<Handler>();
        handlerChain.add(new MyHandler());
        bp.getBinding().setHandlerChain(handlerChain);
        
    }
    Dispatch<Source> createDispatchSource() {
        QName serviceName = new QName("test", "test");
        QName portName = new QName("test", "test");
        
        Service s = Service.create(serviceName);
        s.addPort(portName, HTTPBinding.HTTP_BINDING, endpointAddress);
        Dispatch<Source> d = s.createDispatch(portName, Source.class,
                Service.Mode.PAYLOAD);
        setHandlerChain(d);
        return d;
        
    }
    
    Dispatch<DataSource> createDispatchDataSource() {
        QName serviceName = new QName("test", "test");
        QName portName = new QName("test", "test");
        
        Service s = Service.create(serviceName);
        s.addPort(portName, HTTPBinding.HTTP_BINDING, endpointAddress);
        Dispatch<DataSource> d = s.createDispatch(portName, DataSource.class,
                Service.Mode.MESSAGE);
        setHandlerChain(d);
        return d;
        
    }
    
    // HTTP POST
    public void testDispatchPost1() throws Exception {
        
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        Dispatch<DataSource> d = createDispatchDataSource();
        Map<String, Object> requestContext = d.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, "POST");
        DataSource result = d.invoke(getDataSource(body[0]));


        // check if we got the correct response code
        Map<String, Object> responseContext = d.getResponseContext();
        Integer status = (Integer)responseContext.get(MessageContext.HTTP_RESPONSE_CODE);
        assertTrue(status != null);
        assertEquals(201, (int)status);
 
        // check if we got response headers
        Map<String, List<String>> hdrs = (Map<String, List<String>>)responseContext.get(MessageContext.HTTP_RESPONSE_HEADERS);
        assertTrue(hdrs != null);
 
        hdrs =  getCaseInsensitiveHeaders(hdrs);
        List<String> hdrValues = hdrs.get("custom-header");
        assertTrue(hdrValues != null);
        assertEquals(hdrValues.get(0), "custom-post-value");
 
        // Check if we got the correct Content-Type
        List<String> ctValues = hdrs.get("Content-Type");
        assertTrue(ctValues != null);

        // Check if we got the correct response
        processMime(result);

    }

    private void processMime(DataSource ds)
        throws Exception {
        System.out.println("ct="+ds.getContentType());
        final MimeMultipart multipart = new MimeMultipart(ds, null);

        int no = multipart.getCount();
        assertEquals(2, no);
        MimeBodyPart bodyPart = multipart.getBodyPart(0);
        process(new StreamSource(bodyPart.getInputStream()));
    }

    
    // HTTP POST
    public void testDispatchPost2() throws Exception {
        
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        Dispatch<DataSource> d = createDispatchDataSource();
        Map<String, Object> requestContext = d.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, "POST");
        DataSource result = d.invoke(getDataSource(body1));
        System.out.println("****\n"+result.getContentType()+"----\n");
        Source resultSrc = new StreamSource(result.getInputStream());        
        // Check if we got the correct response
        process(resultSrc);
    }
    
    // HTTP GET
    public void testDispatchGet1() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        Dispatch<DataSource> d = createDispatchDataSource();
        Map<String, Object> requestContext = d.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, "GET");
        requestContext.put(MessageContext.QUERY_STRING,"a=%3C%3Fxml+version%3D%221.0%22%3E&b=c");
        DataSource result = d.invoke(null);        
        System.out.println("****\n"+result.getContentType()+"----\n");
        Source resultSrc = new StreamSource(result.getInputStream());        
        // Check if we got the correct response
        process(resultSrc);
    }
    
    private Source getSource() {
        int i = (++bodyIndex)%body.length;
        return new StreamSource(new ByteArrayInputStream(body[i].getBytes()));
    }
    
    private DataSource getDataSource(final String ds) throws Exception {
        return new DataSource() {
            public InputStream getInputStream() {
                try {
                    
                    ByteArrayInputStream bis = new ByteArrayInputStream(ds.getBytes());

                    return bis;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }
            
            public OutputStream getOutputStream() {
                return null;
            }
            
            public String getContentType() {
                return "text/xml";
            }
            
            public String getName() {
                return "";
            }
        };
    }
    
    private void process(Source source) throws Exception {
        Node node = getDOM(source);
        node = node.getFirstChild();
        assertEquals("HelloResponse", node.getLocalName());
        System.out.println("Node Localname="+node.getLocalName());
        assertEquals("urn:test:types", node.getNamespaceURI());
        System.out.println("Node NS URI="+node.getNamespaceURI());
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
            for (Map.Entry<String, List<String>> e : in.entrySet()) {
                out.put(e.getKey(), e.getValue());
            }
        }
        return out;
    }

}
