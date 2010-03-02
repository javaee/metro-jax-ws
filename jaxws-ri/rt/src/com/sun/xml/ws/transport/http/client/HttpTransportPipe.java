/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.transport.http.client;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.*;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.transport.Headers;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.resources.ClientMessages;
import com.sun.xml.ws.util.StreamUtils;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.handler.MessageContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.net.HttpURLConnection;

/**
 * {@link Pipe} and {@link Tube} that sends a request to a remote HTTP server.
 *
 * TODO: need to create separate HTTP transport pipes for binding. SOAP1.1, SOAP1.2,
 * TODO: XML/HTTP differ in handling status codes.
 *
 * @author Jitendra Kotamraju
 */
public class HttpTransportPipe extends AbstractTubeImpl {

    private final Codec codec;
    private final WSBinding binding;

    public HttpTransportPipe(Codec codec, WSBinding binding) {
        this.codec = codec;
        this.binding = binding;
    }

    /**
     * Copy constructor for {@link Tube#copy(TubeCloner)}.
     */
    private HttpTransportPipe(HttpTransportPipe that, TubeCloner cloner) {
        this( that.codec.copy(), that.binding);
        cloner.add(that,this);
    }

    public NextAction processException(@NotNull Throwable t) {
        throw new IllegalStateException("HttpTransportPipe's processException shouldn't be called.");
    }

    public NextAction processRequest(@NotNull Packet request) {
        return doReturnWith(process(request));
    }

    public NextAction processResponse(@NotNull Packet response) {
        throw new IllegalStateException("HttpTransportPipe's processResponse shouldn't be called.");
    }

    @Override
    public Packet process(Packet request) {
        HttpClientTransport con;
        try {
            // get transport headers from message
            Map<String, List<String>> reqHeaders = new Headers();
            Map<String, List<String>> userHeaders = (Map<String, List<String>>) request.invocationProperties.get(MessageContext.HTTP_REQUEST_HEADERS);
            if (userHeaders != null) {
                // userHeaders may not be modifiable like SingletonMap, just copy them
                reqHeaders.putAll(userHeaders);
            }

            con = new HttpClientTransport(request,reqHeaders);
            request.addSatellite(new HttpResponseProperties(con));

            ContentType ct = codec.getStaticContentType(request);
            if (ct == null) {
                ByteArrayBuffer buf = new ByteArrayBuffer();
                
                ct = codec.encode(request, buf);
                // data size is available, set it as Content-Length
                reqHeaders.put("Content-Length", Collections.singletonList(Integer.toString(buf.size())));
                reqHeaders.put("Content-Type", Collections.singletonList(ct.getContentType()));
                if (ct.getAcceptHeader() != null) {
                    reqHeaders.put("Accept", Collections.singletonList(ct.getAcceptHeader()));
                }
                if (binding instanceof SOAPBinding) {
                    writeSOAPAction(reqHeaders, ct.getSOAPActionHeader(),request);
                }
                
                if(dump)
                    dump(buf, "HTTP request", reqHeaders);
                
                buf.writeTo(con.getOutput());
            } else {
                // Set static Content-Type
                reqHeaders.put("Content-Type", Collections.singletonList(ct.getContentType()));
                if (ct.getAcceptHeader() != null) {
                    reqHeaders.put("Accept", Collections.singletonList(ct.getAcceptHeader()));
                }
                if (binding instanceof SOAPBinding) {
                    writeSOAPAction(reqHeaders, ct.getSOAPActionHeader(), request);
                }
                
                if(dump) {
                    ByteArrayBuffer buf = new ByteArrayBuffer();
                    codec.encode(request, buf);
                    dump(buf, "HTTP request - "+request.endpointAddress, reqHeaders);
                    OutputStream out = con.getOutput();
                    if (out != null) {
                        buf.writeTo(out);
                    }
                } else {
                    OutputStream os = con.getOutput();
                    if (os != null) {
                        codec.encode(request, os);
                    }
                }
            }

            con.closeOutput();

            return createResponsePacket(request, con);
        } catch(WebServiceException wex) {
            throw wex;
        } catch(Exception ex) {
            throw new WebServiceException(ex);
        }
    }

    private Packet createResponsePacket(Packet request, HttpClientTransport con) throws IOException {
        con.readResponseCodeAndMessage();   // throws IOE

        InputStream responseStream = con.getInput();
        if (dump) {
            ByteArrayBuffer buf = new ByteArrayBuffer();
            if (responseStream != null) {
                buf.write(responseStream);
                responseStream.close();
            }
            dump(buf,"HTTP response - "+request.endpointAddress+" - "+con.statusCode, con.getHeaders());
            responseStream = buf.newInputStream();
        }

        // Check if stream contains any data
        int cl = con.contentLength;
        InputStream tempIn = null;
        if (cl == -1) {                     // No Content-Length header
            tempIn = StreamUtils.hasSomeData(responseStream);
            if (tempIn != null) {
                responseStream = tempIn;
            }
        }
        if (cl == 0 || (cl == -1 && tempIn == null)) {
            responseStream.close();         // No data, so close the stream
            responseStream = null;
        }

        // Allows only certain http status codes for a binding. For all
        // other status codes, throws exception
        checkStatusCode(responseStream, con); // throws ClientTransportException

        Packet reply = request.createClientResponse(null);
        reply.wasTransportSecure = con.isSecure();
        if (responseStream != null) {
            String contentType = con.getContentType();
            if (contentType != null && contentType.contains("text/html") && binding instanceof SOAPBinding) {
                throw new ClientTransportException(ClientMessages.localizableHTTP_STATUS_CODE(con.statusCode, con.statusMessage));
            }
            codec.decode(responseStream, contentType, reply);
        }
        return reply;
    }

    /*
     * Allows the following HTTP status codes.
     * SOAP 1.1/HTTP - 200, 202, 500
     * SOAP 1.2/HTTP - 200, 202, 400, 500
     * XML/HTTP - all
     *
     * For all other status codes, it throws an exception
     */
    private void checkStatusCode(InputStream in, HttpClientTransport con) throws IOException {
        int statusCode = con.statusCode;
        String statusMessage = con.statusMessage;
        // SOAP1.1 and SOAP1.2 differ here
        if (binding instanceof SOAPBinding) {
            if (binding.getSOAPVersion() == SOAPVersion.SOAP_12) {
                //In SOAP 1.2, Fault messages can be sent with 4xx and 5xx error codes
                if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_ACCEPTED || isErrorCode(statusCode)) {
                    // acceptable status codes for SOAP 1.2
                    if (isErrorCode(statusCode) && in == null) {
                        // No envelope for the error, so throw an exception with http error details
                        throw new ClientTransportException(ClientMessages.localizableHTTP_STATUS_CODE(statusCode, statusMessage));
                    }
                    return;
                }
            } else {
                // SOAP 1.1
                if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_ACCEPTED || statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    // acceptable status codes for SOAP 1.1
                    if (statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR && in == null) {
                        // No envelope for the error, so throw an exception with http error details
                        throw new ClientTransportException(ClientMessages.localizableHTTP_STATUS_CODE(statusCode, statusMessage));
                    }
                    return;
                }
            }
            if (in != null) {
                in.close();
            }
            throw new ClientTransportException(ClientMessages.localizableHTTP_STATUS_CODE(statusCode, statusMessage));
        }
        // Every status code is OK for XML/HTTP
    }

    private boolean isErrorCode(int code) {              
        //if(code/100 == 5/*Server-side error*/ || code/100 == 4 /*client error*/ ) {
        if(code == 500|| code == 400 ) {
            return true;
        }
        return false;
    }

//    private void checkStatusCodeOneway(InputStream in, int statusCode, String statusMessage) throws IOException {
//        if (statusCode != WSHTTPConnection.ONEWAY && statusCode != WSHTTPConnection.OK) {
//            if (in != null) {
//                in.close();
//            }
//            throw new ClientTransportException(ClientMessages.localizableHTTP_STATUS_CODE(statusCode,statusMessage));
//        }
//    }

    /**
     * write SOAPAction header if the soapAction parameter is non-null or BindingProvider properties set.
     * BindingProvider properties take precedence.
     */
    private void writeSOAPAction(Map<String, List<String>> reqHeaders, String soapAction, Packet packet) {
        //dont write SOAPAction HTTP header for SOAP 1.2 messages.
        if(SOAPVersion.SOAP_12.equals(binding.getSOAPVersion()))
            return;
        if (soapAction != null)
            reqHeaders.put("SOAPAction", Collections.singletonList(soapAction));
        else
            reqHeaders.put("SOAPAction", Collections.singletonList("\"\""));
    }

    public void preDestroy() {
        // nothing to do. Intentionally left empty.
    }

    public HttpTransportPipe copy(TubeCloner cloner) {
        return new HttpTransportPipe(this,cloner);
    }

    private void dump(ByteArrayBuffer buf, String caption, Map<String, List<String>> headers) throws IOException {
        System.out.println("---["+caption +"]---");
        for (Entry<String,List<String>> header : headers.entrySet()) {
            if(header.getValue().isEmpty()) {
                // I don't think this is legal, but let's just dump it,
                // as the point of the dump is to uncover problems.
                System.out.println(header.getValue());
            } else {
                for (String value : header.getValue()) {
                    System.out.println(header.getKey()+": "+value);
                }
            }
        }

        buf.writeTo(System.out);
        System.out.println("--------------------");
    }

    /**
     * Dumps what goes across HTTP transport.
     */
    public static boolean dump;

    static {
        boolean b;
        try {
            b = Boolean.getBoolean(HttpTransportPipe.class.getName()+".dump");
        } catch( Throwable t ) {
            b = false;
        }
        dump = b;
    }
}
