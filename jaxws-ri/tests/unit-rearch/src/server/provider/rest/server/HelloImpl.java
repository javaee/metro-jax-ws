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

package server.provider.rest.server;

import java.io.StringReader;
import javax.xml.ws.WebServiceContext;
import java.io.ByteArrayInputStream;
import javax.xml.ws.Provider;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.handler.MessageContext;
import javax.annotation.Resource;
import javax.xml.ws.http.HTTPException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.xml.ws.WebServiceException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.activation.DataHandler;
import javax.jws.HandlerChain;

@HandlerChain(file="handler.xml")
@WebServiceProvider
@ServiceMode(value=Service.Mode.MESSAGE)
public class HelloImpl implements Provider<Source> {
    private int bodyIndex = 0;
    private String[] body = {
        "<HelloResponse xmlns=\"urn:test:types\"><argument xmlns=\"\">foo</argument><extra xmlns=\"\">bar</extra></HelloResponse>",
        "<ans1:HelloResponse xmlns:ans1=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></ans1:HelloResponse>"
    };

    @Resource(type=Object.class)
    protected WebServiceContext wsContext;

    private byte[] getSource() {
        int i = (++bodyIndex)%body.length;
        return body[i].getBytes();
    }

    public Source invoke(Source source) {
        MessageContext mc = wsContext.getMessageContext();
        String method = (String)mc.get(MessageContext.HTTP_REQUEST_METHOD);
        if (method.equals("HEAD")) {
            return head(source, mc);
        } else  if (method.equals("GET")) {
            return get(source, mc);
        } else if (method.equals("POST")) {
            return post(source, mc);
        } else if (method.equals("PUT")) {
            return put(source, mc);
        } else if (method.equals("DELETE")) {
            return delete(source, mc);
        }
        HTTPException ex = new HTTPException(404);
        throw ex;
    }

    private Source head(Source source, MessageContext mc) {
        Map <String, List<String>> hdrs = new HashMap<String, List<String>>();
        hdrs.put("custom-header",
            Collections.singletonList("custom-head-value"));
        mc.put(MessageContext.HTTP_RESPONSE_HEADERS, hdrs);
        mc.put(MessageContext.HTTP_RESPONSE_CODE, 201);
        // for HEAD, no content goes on the wire. Looks like servlet
        // is sending content on the wire !
        // But send some source so that it's not oneway
        //return new StreamSource(new ByteArrayInputStream(getSource()));
        return new StreamSource();
    }

    private Source get(Source source, MessageContext mc) {
        Map <String, List<String>> hdrs = new HashMap<String, List<String>>();
        hdrs.put("custom-header",
            Collections.singletonList("custom-get-value"));
        mc.put(MessageContext.HTTP_RESPONSE_HEADERS, hdrs);
        String query = (String)mc.get(MessageContext.QUERY_STRING);
        String path = (String)mc.get(MessageContext.PATH_INFO);
        System.out.println("Query String = "+query);
        System.out.println("PathInfo = "+path);
        if (query != null && query.equals("a=%3C%3Fxml+version%3D%221.0%22%3E&b=c")) {
            return new StreamSource(new ByteArrayInputStream(getSource()));
        } else if (path != null && path.equals("/a/b")) {
            return new StreamSource(new ByteArrayInputStream(getSource()));
        } else {
            HTTPException ex = new HTTPException(404);
            throw ex;
        }
    }

    private Source post(Source source, MessageContext mc) {
        // check request Source content
        String str;
        try {
            str = getStringFromSource(source);
            //System.out.println(str);
        } catch(Exception e) {
            throw new WebServiceException(
                "POST doesn't have expected content", e);
        }
        if (str.indexOf("HelloRequest") == -1) {
            throw new WebServiceException("POST doesn't have expected content");
        }

        // configure response
        Map <String, List<String>> hdrs = new HashMap<String, List<String>>();
        hdrs.put("custom-header",
            Collections.singletonList("custom-post-value"));
        mc.put(MessageContext.HTTP_RESPONSE_HEADERS, hdrs);
        mc.put(MessageContext.HTTP_RESPONSE_CODE, 201);
        if(str.indexOf("NO_ATTACHMENTS") == -1) {
            // Add an attachment to the response
            Map<String, DataHandler> att = new HashMap<String, DataHandler>();
            Source xmlAtt = new StreamSource(new StringReader("<ok/>"));
            att.put("<abcd@example.org>", new DataHandler(xmlAtt, "text/xml"));
            mc.put(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS, att);
        }
        return new StreamSource(new ByteArrayInputStream(getSource()));
    }

    private Source put(Source source, MessageContext mc) {
        // check request Source content
        String str;
        try {
            str = getStringFromSource(source);
        } catch(Exception e) {
            throw new WebServiceException(
                "PUT doesn't have expected content", e);
        }
        if (str.indexOf("HelloRequest") == -1) {
            throw new WebServiceException("PUT doesn't have expected content");
        }

        // configure response
        Map <String, List<String>> hdrs = new HashMap<String, List<String>>();
        hdrs.put("custom-header",
            Collections.singletonList("custom-put-value"));
        mc.put(MessageContext.HTTP_RESPONSE_HEADERS, hdrs);
        mc.put(MessageContext.HTTP_RESPONSE_CODE, 201);
        return new StreamSource(new ByteArrayInputStream(getSource()));
    }

    public Source delete(Source source, MessageContext mc) {
        Map <String, List<String>> hdrs = new HashMap<String, List<String>>();
        hdrs.put("custom-header",
            Collections.singletonList("custom-delete-value"));
        mc.put(MessageContext.HTTP_RESPONSE_HEADERS, hdrs);
        mc.put(MessageContext.HTTP_RESPONSE_CODE, 201);
        //return new StreamSource(new ByteArrayInputStream(getSource()));
        return new StreamSource();
    }

    private String getStringFromSource(Source source) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult sr = new StreamResult(bos );
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        Properties oprops = new Properties();
        oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperties(oprops);
        trans.transform(source, sr);
        bos.flush();
        return bos.toString();
    }

}
