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

package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.addressing.WSEndpointReference;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.BindingType;
import javax.xml.ws.http.HTTPBinding;
import java.net.HttpURLConnection;

public interface JAXWSProperties {
    // Content negotiation property: values "none", "pessimistic" and "optimistic"
    // It is split into two strings so that package renaming for
    // Java SE 6 doesn't alter the value. So do not combine them
    @Deprecated
    public static final String CONTENT_NEGOTIATION_PROPERTY = "com.sun."+"xml.ws.client.ContentNegotiation";
    public static final String MTOM_THRESHOLOD_VALUE =  "com.sun.xml.ws.common.MtomThresholdValue";
    public static final String HTTP_EXCHANGE = "com.sun.xml.ws.http.exchange";

    /**
     * Set this property on the {@link BindingProvider#getRequestContext()} to
     * enable {@link HttpURLConnection#setConnectTimeout(int)}
     *
     *<p>
     * int timeout = ...;
     * Map<String, Object> ctxt = ((BindingProvider)proxy).getRequestContext();
     * ctxt.put(CONNECT_TIMEOUT, timeout);
     */
    public static final String CONNECT_TIMEOUT =
        "com.sun.xml.ws.connect.timeout";

    /**
     * Set this property on the {@link BindingProvider#getRequestContext()} to
     * enable {@link HttpURLConnection#setReadTimeout(int)}
     *
     *<p>
     * int timeout = ...;
     * Map<String, Object> ctxt = ((BindingProvider)proxy).getRequestContext();
     * ctxt.put(REQUEST_TIMEOUT, timeout);
     */
     public static final String REQUEST_TIMEOUT =
        "com.sun.xml.ws.request.timeout";

    /**
     * Set this property on the {@link BindingProvider#getRequestContext()} to
     * enable {@link HttpURLConnection#setChunkedStreamingMode(int)}
     *
     *<p>
     * int chunkSize = ...;
     * Map<String, Object> ctxt = ((BindingProvider)proxy).getRequestContext();
     * ctxt.put(HTTP_CLIENT_STREAMING_CHUNK_SIZE, chunkSize);
     */
    public static final String HTTP_CLIENT_STREAMING_CHUNK_SIZE = "com.sun.xml.ws.transport.http.client.streaming.chunk.size";


    /**
     * Set this property on the {@link BindingProvider#getRequestContext()} to
     * enable {@link HttpsURLConnection#setHostnameVerifier(HostnameVerifier)}}. The property
     * is set as follows:
     *
     * <p>
     * HostNameVerifier hostNameVerifier = ...;
     * Map<String, Object> ctxt = ((BindingProvider)proxy).getRequestContext();
     * ctxt.put(HOSTNAME_VERIFIER, hostNameVerifier);
     *
     * <p>
     * <b>THIS PROPERTY IS EXPERIMENTAL AND IS SUBJECT TO CHANGE WITHOUT NOTICE IN FUTURE.</b>
     */
    public static final String HOSTNAME_VERIFIER = "com.sun.xml.ws.transport.https.client.hostname.verifier";

    /**
     * Set this property on the {@link BindingProvider#getRequestContext()} to
     * enable {@link HttpsURLConnection#setSSLSocketFactory(SSLSocketFactory)}. The property is set
     * as follows:
     *
     * <p>
     * SSLSocketFactory sslFactory = ...;
     * Map<String, Object> ctxt = ((BindingProvider)proxy).getRequestContext();
     * ctxt.put(SSL_SOCKET_FACTORY, sslFactory);
     *
     * <p>
     * <b>THIS PROPERTY IS EXPERIMENTAL AND IS SUBJECT TO CHANGE WITHOUT NOTICE IN FUTURE.</b>
     */
    public static final String SSL_SOCKET_FACTORY = "com.sun.xml.ws.transport.https.client.SSLSocketFactory";

    /**
     * Acccess the list of SOAP headers in the SOAP message.
     *
     * <p>
     * On {@link WebServiceContext}, this property returns a {@link HeaderList} object
     * that represents SOAP headers in the request message that was received.
     * On {@link BindingProvider#getResponseContext()}, this property returns a
     * {@link HeaderList} object that represents SOAP headers in the response message from the server.
     *
     * <p>
     * The property is read-only, and please do not modify the returned {@link HeaderList}
     * as that may break the JAX-WS RI in some unexpected way.
     *
     * <p>
     * <b>THIS PROPERTY IS EXPERIMENTAL AND IS SUBJECT TO CHANGE WITHOUT NOTICE IN FUTURE.</b>
     */
    public static final String INBOUND_HEADER_LIST_PROPERTY = "com.sun.xml.ws.api.message.HeaderList";

    /**
     * Access the {@link WSEndpoint} object that delivered the request.
     *
     * <p>
     * {@link WSEndpoint} is the root of the objects that are together
     * responsible for delivering requests to the application SEI object.
     * One can look up this {@link WSEndpoint} from {@link WebServiceContext},
     * and from there access many parts of the JAX-WS RI runtime.
     *
     * <p>
     * <b>THIS PROPERTY IS EXPERIMENTAL AND IS SUBJECT TO CHANGE WITHOUT NOTICE IN FUTURE.</b>
     *
     * @since 2.1.2
     */
    public static final String WSENDPOINT = "com.sun.xml.ws.api.server.WSEndpoint";

    /**
     * Gets the {@code wsa:To} header.
     *
     * The propery value is available on incoming SOAP message. The type of the value
     * is {@link WSEndpointReference}.
     * 
     * Null if the incoming SOAP message didn't have the header.
     *
     * @since 2.1.3
     */
    public static final String ADDRESSING_TO = "com.sun.xml.ws.api.addressing.to";

    /**
     * Gets the {@code wsa:From} header.
     *
     * The propery value is available on incoming SOAP message. The type of the value
     * is {@link WSEndpointReference}.
     *
     * Null if the incoming SOAP message didn't have the header.
     *
     * @since 2.1.3
     */
    public static final String ADDRESSING_FROM = "com.sun.xml.ws.api.addressing.from";

    /**
     * Gets the {@code wsa:Action} header value.
     *
     * The propery value is available on incoming SOAP message. The type of the value
     * is {@link String}.
     *
     * Null if the incoming SOAP message didn't have the header.
     *
     * @since 2.1.3
     */
    public static final String ADDRESSING_ACTION = "com.sun.xml.ws.api.addressing.action";

    /**
     * Gets the {@code wsa:MessageID} header value.
     *
     * The propery value is available on incoming SOAP message. The type of the value
     * is {@link String}.
     *
     * Null if the incoming SOAP message didn't have the header.
     *
     * @since 2.1.3
     */
    public static final String ADDRESSING_MESSAGEID = "com.sun.xml.ws.api.addressing.messageId";

    /**
     * Reconstructs the URL the client used to make the request. The returned URL
     * contains a protocol, server name, port number, and server path, but it does
     * not include query string parameters.
     * <p>
     * The property value is available on incoming SOAP message on servlet transport.
     *
     * @since 2.1.3
     */
    public static final String HTTP_REQUEST_URL = "com.sun.xml.ws.transport.http.servlet.requestURL";

    /**
     * Binding to represent RESTful services. {@link HTTPBinding#HTTP_BINDING} works
     * only for Dispatch/Provider services, but this binding works with even SEI based
     * services. It would be XML, NOT SOAP on the wire. Hence, the SEI parameters
     * shouldn't be mapped to headers.
     *
     * <p>
     * Note that, this only solves limited RESTful usecases.
     *
     * <p>To enable restful binding on the service, specify the binding id via
     * {@link BindingType} or DD
     * <pre>
     * &#64;WebService
     * &#64;BindingType(JAXWSProperties.REST_BINDING)
     * </pre>
     *
     * <p>To enable restful binding on the client side, specify the binding id via
     * {@link BindingTypeFeature}
     * <pre>
     * proxy = echoImplService.getEchoImplPort(new BindingTypeFeature(JAXWSProperties.REST_BINDING));
     * </pre>
     *
     * @since 2.1.4
     */
    public static final String REST_BINDING = "http://jax-ws.dev.java.net/rest";
    
}
