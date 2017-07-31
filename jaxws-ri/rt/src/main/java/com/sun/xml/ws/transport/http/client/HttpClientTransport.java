/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.transport.http.client;

import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.client.BindingProviderProperties;
import static com.sun.xml.ws.client.BindingProviderProperties.*;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.resources.ClientMessages;
import com.sun.xml.ws.transport.Headers;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.istack.Nullable;
import com.sun.istack.NotNull;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author WS Development Team
 */
public class HttpClientTransport {

    private static final byte[] THROW_AWAY_BUFFER = new byte[8192];
    
    // Need to use JAXB first to register DatatypeConverter
    static {
        try {
            JAXBContext.newInstance().createUnmarshaller();
        } catch(JAXBException je) {
            // Nothing much can be done. Intentionally left empty
        }
    }

    /*package*/ int statusCode;
    /*package*/ String statusMessage;
    /*package*/ int contentLength;
    private final Map<String, List<String>> reqHeaders;
    private Map<String, List<String>> respHeaders = null;

    private OutputStream outputStream;
    private boolean https;
    private HttpURLConnection httpConnection = null;
    private final EndpointAddress endpoint;
    private final Packet context;
    private final Integer chunkSize;


    public HttpClientTransport(@NotNull Packet packet, @NotNull Map<String,List<String>> reqHeaders) {
        endpoint = packet.endpointAddress;
        context = packet;
        this.reqHeaders = reqHeaders;
        chunkSize = (Integer)context.invocationProperties.get(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE);
    }

    /*
     * Prepare the stream for HTTP request
     */
    OutputStream getOutput() {
        try {
            createHttpConnection();
            // for "GET" request no need to get outputStream
            if (requiresOutputStream()) {
                outputStream = httpConnection.getOutputStream();
                if (chunkSize != null) {
                    outputStream = new WSChunkedOuputStream(outputStream, chunkSize);
                }
                List<String> contentEncoding = reqHeaders.get("Content-Encoding");
                // TODO need to find out correct encoding based on q value - RFC 2616
                if (contentEncoding != null && contentEncoding.get(0).contains("gzip")) {
                    outputStream = new GZIPOutputStream(outputStream);
                }
            }
            httpConnection.connect();
        } catch (Exception ex) {
            throw new ClientTransportException(
                ClientMessages.localizableHTTP_CLIENT_FAILED(ex),ex);
        }

        return outputStream;
    }

    void closeOutput() throws IOException {
        if (outputStream != null) {
            outputStream.close();
            outputStream = null;
        }
    }

    /*
     * Get the response from HTTP connection and prepare the input stream for response
     */
    @Nullable InputStream getInput() {
        // response processing

        InputStream in;
        try {
            in = readResponse();
            if (in != null) {
                String contentEncoding = httpConnection.getContentEncoding();
                if (contentEncoding != null && contentEncoding.contains("gzip")) {
                    in = new GZIPInputStream(in);
                }
            }
        } catch (IOException e) {
            throw new ClientTransportException(ClientMessages.localizableHTTP_STATUS_CODE(statusCode, statusMessage), e);
        }
        return in;
    }

    public Map<String, List<String>> getHeaders() {
        if (respHeaders != null) {
            return respHeaders;
        }
        respHeaders = new Headers();
        respHeaders.putAll(httpConnection.getHeaderFields());
        return respHeaders;
    }

    protected @Nullable InputStream readResponse() {
        InputStream is;
        try {
            is = httpConnection.getInputStream();
        } catch(IOException ioe) {
            is = httpConnection.getErrorStream();
        }
        if (is == null) {
            return is;
        }
        // Since StreamMessage doesn't read </s:Body></s:Envelope>, there
        // are some bytes left in the InputStream. This confuses JDK and may
        // not reuse underlying sockets. Hopefully JDK fixes it in its code !
        final InputStream temp = is;
        return new FilterInputStream(temp) {
            // Workaround for "SJSXP XMLStreamReader.next() closes stream".
            // So it doesn't read from the closed stream
            boolean closed;
            @Override
            public void close() throws IOException {                
                if (!closed) {
                    closed = true;
                    while(temp.read(THROW_AWAY_BUFFER) != -1);
                    super.close();
                }
            }
        };
    }

    protected void readResponseCodeAndMessage() {
        try {
            statusCode = httpConnection.getResponseCode();
            statusMessage = httpConnection.getResponseMessage();
            contentLength = httpConnection.getContentLength();
        } catch(IOException ioe) {
            throw new WebServiceException(ioe);
        }
    }

    protected HttpURLConnection openConnection(Packet packet) {
    	// default do nothing
    	return null;
    }
    
    protected boolean checkHTTPS(HttpURLConnection connection) {
        if (connection instanceof HttpsURLConnection) {

            // TODO The above property needs to be removed in future version as the semantics of this property are not preoperly defined.
            // One should use JAXWSProperties.HOSTNAME_VERIFIER to control the behavior

            // does the client want client hostname verification by the service
            String verificationProperty =
                (String) context.invocationProperties.get(HOSTNAME_VERIFICATION_PROPERTY);
            if (verificationProperty != null) {
                if (verificationProperty.equalsIgnoreCase("true")) {
                    ((HttpsURLConnection) connection).setHostnameVerifier(new HttpClientVerifier());
                }
            }

            // Set application's HostNameVerifier for this connection
            HostnameVerifier verifier =
                (HostnameVerifier) context.invocationProperties.get(JAXWSProperties.HOSTNAME_VERIFIER);
            if (verifier != null) {
                ((HttpsURLConnection) connection).setHostnameVerifier(verifier);
            }

            // Set application's SocketFactory for this connection
            SSLSocketFactory sslSocketFactory =
                (SSLSocketFactory) context.invocationProperties.get(JAXWSProperties.SSL_SOCKET_FACTORY);
            if (sslSocketFactory != null) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
            }
            
            return true;
        }
        return false;
    }
    
    private void createHttpConnection() throws IOException {
    	httpConnection = openConnection(context);

    	if (httpConnection == null)
    		httpConnection = (HttpURLConnection) endpoint.openConnection();
    	
        String scheme = endpoint.getURI().getScheme();
        if (scheme.equals("https")) {
            https = true;
        }
        if (checkHTTPS(httpConnection))
        	https = true;

        // allow interaction with the web page - user may have to supply
        // username, password id web page is accessed from web browser
        httpConnection.setAllowUserInteraction(true);

        // enable input, output streams
        httpConnection.setDoOutput(true);
        httpConnection.setDoInput(true);

        String requestMethod = (String) context.invocationProperties.get(MessageContext.HTTP_REQUEST_METHOD);
        String method = (requestMethod != null) ? requestMethod : "POST";
        httpConnection.setRequestMethod(method);

        //this code or something similiar needs t be moved elsewhere for error checking
        /*if (context.invocationProperties.get(BindingProviderProperties.BINDING_ID_PROPERTY).equals(HTTPBinding.HTTP_BINDING)){
            method = (requestMethod != null)?requestMethod:method;            
        } else if
            (context.invocationProperties.get(BindingProviderProperties.BINDING_ID_PROPERTY).equals(SOAPBinding.SOAP12HTTP_BINDING) &&
            "GET".equalsIgnoreCase(requestMethod)) {
        }
       */     

        Integer reqTimeout = (Integer)context.invocationProperties.get(BindingProviderProperties.REQUEST_TIMEOUT);
        if (reqTimeout != null) {
            httpConnection.setReadTimeout(reqTimeout);
        }

        Integer connectTimeout = (Integer)context.invocationProperties.get(JAXWSProperties.CONNECT_TIMEOUT);
        if (connectTimeout != null) {
            httpConnection.setConnectTimeout(connectTimeout);
        }

        Integer chunkSize = (Integer)context.invocationProperties.get(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE);
        if (chunkSize != null) {
            httpConnection.setChunkedStreamingMode(chunkSize);
        }

        // set the properties on HttpURLConnection
        for (Map.Entry<String, List<String>> entry : reqHeaders.entrySet()) {
            if ("Content-Length".equals(entry.getKey())) continue;
        	for(String value : entry.getValue()) {
	            httpConnection.addRequestProperty(entry.getKey(), value);
        	}
        }
    }

    boolean isSecure() {
        return https;
    }
    
    protected void setStatusCode(int statusCode) {
    	this.statusCode = statusCode;
    }

    private boolean requiresOutputStream() {
        return !(httpConnection.getRequestMethod().equalsIgnoreCase("GET") ||
                httpConnection.getRequestMethod().equalsIgnoreCase("HEAD") ||
                httpConnection.getRequestMethod().equalsIgnoreCase("DELETE"));
    }

    @Nullable String getContentType() {
        return httpConnection.getContentType();
    }
    
    public int getContentLength() {
    	return httpConnection.getContentLength();
    }

    // overide default SSL HttpClientVerifier to always return true
    // effectively overiding Hostname client verification when using SSL
    private static class HttpClientVerifier implements HostnameVerifier {
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }

    private static class LocalhostHttpClientVerifier implements HostnameVerifier {
        public boolean verify(String s, SSLSession sslSession) {
            return "localhost".equalsIgnoreCase(s) || "127.0.0.1".equals(s);
        }
    }

    /**
     * HttpURLConnection.getOuputStream() returns sun.net.www.http.ChunkedOuputStream in chunked
     * streaming mode. If you call ChunkedOuputStream.write(byte[20MB], int, int), then the whole data
     * is kept in memory. This wraps the ChunkedOuputStream so that it writes only small
     * chunks.
     */
    private static final class WSChunkedOuputStream extends FilterOutputStream {
        final int chunkSize;

        WSChunkedOuputStream(OutputStream actual, int chunkSize) {
            super(actual);
            this.chunkSize = chunkSize;
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            while(len > 0) {
                int sent = (len > chunkSize) ? chunkSize : len;
                out.write(b, off, sent);        // don't use super.write() as it writes byte-by-byte
                len -= sent;
                off += sent;
            }
        }

    }

}

