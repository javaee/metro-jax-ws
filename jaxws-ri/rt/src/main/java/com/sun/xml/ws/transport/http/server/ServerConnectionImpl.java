/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.transport.http.server;

import com.sun.istack.NotNull;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.api.server.PortAddressResolver;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.resources.WsservletMessages;
import com.sun.xml.ws.util.ReadAllStream;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceException;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * {@link WSHTTPConnection} used with Java SE endpoints. It provides connection
 * implementation using {@link HttpExchange} object.
 *
 * @author Jitendra Kotamraju
 */
final class ServerConnectionImpl extends WSHTTPConnection implements WebServiceContextDelegate {

    private final HttpExchange httpExchange;
    private int status;
    private final HttpAdapter adapter;
    private LWHSInputStream in;
    private OutputStream out;


    public ServerConnectionImpl(@NotNull HttpAdapter adapter, @NotNull HttpExchange httpExchange) {
        this.adapter = adapter;
        this.httpExchange = httpExchange;
    }

    @Override
    @Property(value = {MessageContext.HTTP_REQUEST_HEADERS, Packet.INBOUND_TRANSPORT_HEADERS})
    public @NotNull Map<String,List<String>> getRequestHeaders() {
        return httpExchange.getRequestHeaders();
    }

    @Override
    public String getRequestHeader(String headerName) {
        return httpExchange.getRequestHeaders().getFirst(headerName);
    }

    @Override
    public void setResponseHeaders(Map<String,List<String>> headers) {
        Headers r = httpExchange.getResponseHeaders();
        r.clear();
        for(Map.Entry <String, List<String>> entry : headers.entrySet()) {
            String name = entry.getKey();
            List<String> values = entry.getValue();
            // ignore headers that interfere with our correct operations
            if (!"Content-Length".equalsIgnoreCase(name) && !"Content-Type".equalsIgnoreCase(name)) {
                r.put(name,new ArrayList<String>(values));
            }
        }
    }

    @Override
	public void setResponseHeader(String key, List<String> value) {
		httpExchange.getResponseHeaders().put(key, value);
	}

	@Override
	public Set<String> getRequestHeaderNames() {
        return httpExchange.getRequestHeaders().keySet();
	}

	@Override
	public List<String> getRequestHeaderValues(String headerName) {
		return httpExchange.getRequestHeaders().get(headerName);
	}

    @Override
    @Property({MessageContext.HTTP_RESPONSE_HEADERS,Packet.OUTBOUND_TRANSPORT_HEADERS})
    public Map<String,List<String>> getResponseHeaders() {
        return httpExchange.getResponseHeaders();
    }

    @Override
    public void setContentTypeResponseHeader(@NotNull String value) {
        httpExchange.getResponseHeaders().set("Content-Type",value);
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    @Property(MessageContext.HTTP_RESPONSE_CODE)
    public int getStatus() {
        return status;
    }

    public @NotNull InputStream getInput() {
        if (in == null) {
            in = new LWHSInputStream(httpExchange.getRequestBody());
        }
        return in;
    }

    // Light weight http server's InputStream.close() throws exception if
    // all the bytes are not read. Work around until it is fixed.
    private static class LWHSInputStream extends FilterInputStream {
        // Workaround for "SJSXP XMLStreamReader.next() closes stream".
        boolean closed;
        boolean readAll;
        
        LWHSInputStream(InputStream in) {
            super(in);
        }

        void readAll() throws IOException {
            if (!closed && !readAll) {
                ReadAllStream all = new ReadAllStream();
                all.readAll(in, 4000000);
                in.close();
                in = all;
                readAll = true;
            }
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                readAll();
                super.close();
                closed = true;
            }
        }

    }


    public @NotNull OutputStream getOutput() throws IOException {
        if (out == null) {
            String lenHeader = httpExchange.getResponseHeaders().getFirst("Content-Length");
            int length = (lenHeader != null) ? Integer.parseInt(lenHeader) : 0;
            httpExchange.sendResponseHeaders(getStatus(), length);

            // Light weight http server's OutputStream.close() throws exception if
            // all the bytes are not read on the client side(StreamMessage on the client
            // side doesn't read all bytes.
            out =  new FilterOutputStream(httpExchange.getResponseBody()) {
                boolean closed;
                @Override
                public void close() throws IOException {
                    if (!closed) {
                        closed = true;
                        // lwhs closes input stream, when you close the output stream
                        // This causes problems for streaming in one-way cases
                        in.readAll();
                        try {
                            super.close();
                        } catch(IOException ioe) {
                            // Ignoring purposefully.
                        }
                    }
                }

                // Otherwise, FilterOutpuStream writes byte by byte
                @Override
                public void write(byte[] buf, int start, int len) throws IOException {
                    out.write(buf, start, len);
                }
            };
        }
        return out;
    }

    public @NotNull WebServiceContextDelegate getWebServiceContextDelegate() {
        return this;
    }

    public Principal getUserPrincipal(Packet request) {
        return httpExchange.getPrincipal();
    }

    public boolean isUserInRole(Packet request, String role) {
        return false;
    }

    public @NotNull String getEPRAddress(Packet request, WSEndpoint endpoint) {
        //return WSHttpHandler.getRequestAddress(httpExchange);
        
        PortAddressResolver resolver = adapter.owner.createPortAddressResolver(getBaseAddress());
        String address = resolver.getAddressFor(endpoint.getServiceName(), endpoint.getPortName().getLocalPart());
        if(address==null)
            throw new WebServiceException(WsservletMessages.SERVLET_NO_ADDRESS_AVAILABLE(endpoint.getPortName()));
        return address;

    }

    public String getWSDLAddress(@NotNull Packet request, @NotNull WSEndpoint endpoint) {
        String eprAddress = getEPRAddress(request,endpoint);
        if(adapter.getEndpoint().getPort() != null)
            return eprAddress+"?wsdl";
        else
            return null;
    }

    @Override
    public boolean isSecure() {
        return (httpExchange instanceof HttpsExchange);
    }

    @Override
    @Property(MessageContext.HTTP_REQUEST_METHOD)
    public @NotNull String getRequestMethod() {
        return httpExchange.getRequestMethod();
    }

    @Override
    @Property(MessageContext.QUERY_STRING)
    public String getQueryString() {
        URI requestUri = httpExchange.getRequestURI();
        String query = requestUri.getQuery();
        if (query != null)
            return query;
        return null;
    }

    @Override
    @Property(MessageContext.PATH_INFO)
    public String getPathInfo() {
        URI requestUri = httpExchange.getRequestURI();
        String reqPath = requestUri.getPath();
        String ctxtPath = httpExchange.getHttpContext().getPath();
        if (reqPath.length() > ctxtPath.length()) {
            return reqPath.substring(ctxtPath.length());
        }
        return null;
    }

    @Property(JAXWSProperties.HTTP_EXCHANGE)
    public HttpExchange getExchange() {
        return httpExchange;
    }

    @Override @NotNull
    public String getBaseAddress() {
        /*
         * Computes the Endpoint's address from the request. Use "Host" header
         * so that it has correct address(IP address or someother hostname)
         * through which the application reached the endpoint.
         *
         */
        StringBuilder strBuf = new StringBuilder();
        strBuf.append((httpExchange instanceof HttpsExchange) ? "https" : "http");
        strBuf.append("://");

        String hostHeader = httpExchange.getRequestHeaders().getFirst("Host");
        if (hostHeader != null) {
            strBuf.append(hostHeader);   // Uses Host header
        } else {
            strBuf.append(httpExchange.getLocalAddress().getHostName());
            strBuf.append(":");
            strBuf.append(httpExchange.getLocalAddress().getPort());
        }
        //Do not include URL pattern here
        //strBuf.append(httpExchange.getRequestURI().getPath());

        return strBuf.toString();
    }

    @Override
    public String getProtocol() {
        return httpExchange.getProtocol();
    }

    @Override
    public void setContentLengthResponseHeader(int value) {
        httpExchange.getResponseHeaders().set("Content-Length", ""+value);
    }

	@Override
	public String getRequestURI() {
		return httpExchange.getRequestURI().toString();
	}

	@Override
	public String getRequestScheme() {
		return (httpExchange instanceof HttpsExchange) ? "https" : "http";
	}

	@Override
	public String getServerName() {
		return httpExchange.getLocalAddress().getHostName();
	}

	@Override
	public int getServerPort() {
		return httpExchange.getLocalAddress().getPort();
	}
	
    protected PropertyMap getPropertyMap() {
        return model;
    }

    private static final PropertyMap model;

    static {
        model = parse(ServerConnectionImpl.class);
    }
}
