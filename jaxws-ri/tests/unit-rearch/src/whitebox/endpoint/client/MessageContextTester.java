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

package whitebox.endpoint.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.xml.ws.Endpoint;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.net.URL;
import java.io.*;
import javax.xml.ws.Provider;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceProvider;
import java.io.StringReader;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.net.httpserver.HttpExchange;


/**
 * @author Jitendra Kotamraju
 */
public class MessageContextTester extends TestCase {

    @WebServiceProvider
    static class MyProvider implements Provider<Source> {
        @Resource
        WebServiceContext ctxt;

        public Source invoke(Source source) {
            MessageContext msgCtxt = ctxt.getMessageContext();

            // toString()
            System.out.println("MessageContext="+msgCtxt);

            // Test Map.get()
            String method = (String)msgCtxt.get(MessageContext.HTTP_REQUEST_METHOD);
            if (method == null || !method.equals("GET")) {
                throw new WebServiceException("Expected method=GET but got="+method);
            }

            // Test iterator
            Iterator<Map.Entry<String, Object>> i = msgCtxt.entrySet().iterator();
            while(i.hasNext()) {
                Map.Entry<String, Object> e = i.next();
            }

            // Test keySet() iterator
            Iterator<String> i1 = msgCtxt.keySet().iterator();
            while(i1.hasNext()) {
                i1.next();
            }

            // Test Map.size()
            int no = msgCtxt.size();

            // Test Map.put()
            msgCtxt.put("key", "value");

            // Test Map.get() for the new addition
            String value = (String)msgCtxt.get("key");
            if (value == null || !value.equals("value")) {
                throw new WebServiceException("Expected=value but got="+value);
            }

            // Test Map.size() for the new addition
            int num = msgCtxt.size();
            if (num != no+1) {
                throw new WebServiceException("Expected no="+(no+1)+" but got="+num);
            }

            // Test Map.remove() for the new addition
            value = (String)msgCtxt.remove("key");
            if (value == null || !value.equals("value")) {
                throw new WebServiceException("Expected=value but got="+value);
            }

            // Test Map.size() for the removal
            num = msgCtxt.size();
            if (num != no) {
                throw new WebServiceException("Expected="+no+" but got="+num);
            }

            // Test iterator.remove()
            msgCtxt.put("key", "value");
            i = msgCtxt.entrySet().iterator();
            while(i.hasNext()) {
                Map.Entry<String, Object> e = i.next();
                if (e.getKey().equals("key")) {
                    i.remove();
                }
            }
            num = msgCtxt.size();
            if (num != no) {
                throw new WebServiceException("Expected="+no+" but got="+num);
            }
            value = (String)msgCtxt.get("key");
            if (value != null) {
                throw new WebServiceException("Expected=null"+" but got="+value);
            }

            // Test Map.putAll()
            Map<String, Object> two = new HashMap<String, Object>();
            two.put("key1", "value1");
            two.put("key2", "value2");
            msgCtxt.putAll(two);
            num = msgCtxt.size();
            if (num != no+2) {
                throw new WebServiceException("Expected="+(no+2)+" but got="+num);
            }
            value = (String)msgCtxt.get("key1");
            if (value == null || !value.equals("value1")) {
                throw new WebServiceException("Expected=value1"+" but got="+value);
            }
            value = (String)msgCtxt.get("key2");
            if (value == null || !value.equals("value2")) {
                throw new WebServiceException("Expected=value2"+" but got="+value);
            }

            String replyElement = new String("<p>hello world</p>");
            StreamSource reply = new StreamSource(new StringReader (replyElement));
            return reply;
        }
    }

    public void testMessageContext() throws Exception {
        int port = Util.getFreePort();
        String address = "http://127.0.0.1:"+port+"/";
        Endpoint e = Endpoint.create(HTTPBinding.HTTP_BINDING, new MyProvider());
        e.publish(address);

        URL url = new URL(address);
        InputStream in = url.openStream();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
        String str;
        while ((str=rdr.readLine()) != null);

        e.stop();
    }

    @WebServiceProvider
    static class MessageContextProvider implements Provider<Source> {
        @Resource
        WebServiceContext ctxt;

        public Source invoke(Source source) {
            MessageContext msgCtxt = ctxt.getMessageContext();
            
            String qs = (String)msgCtxt.get(MessageContext.QUERY_STRING);
            if (qs == null || !qs.equals("a=b")) {
                throw new WebServiceException("Unexpected QUERY_STRING. Expected: "+"a=b"+" Got: "+qs);
            }

            String pathInfo = (String)msgCtxt.get(MessageContext.PATH_INFO);
            if (pathInfo == null || !pathInfo.equals("/a/b")) {
                throw new WebServiceException("Unexpected PATH_INFO. Expected: "+"/a/b"+" Got: "+pathInfo);
            }

            HttpExchange exchange = (HttpExchange)msgCtxt.get(JAXWSProperties.HTTP_EXCHANGE);
            if (exchange == null ) {
                throw new WebServiceException("HttpExchange object is not populated");
            }

            String replyElement = new String("<p>hello world</p>");
            StreamSource reply = new StreamSource(new StringReader (replyElement));
            return reply;
        }
    }

    public void testHttpProperties() throws Exception {
        int port = Util.getFreePort();
        String address = "http://127.0.0.1:"+port+"/hello";
        Endpoint e = Endpoint.create(HTTPBinding.HTTP_BINDING, new MessageContextProvider());
        e.publish(address);

        URL url = new URL(address+"/a/b?a=b");
        InputStream in = url.openStream();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
        String str;
        while ((str=rdr.readLine()) != null);

        e.stop();
    }

}

