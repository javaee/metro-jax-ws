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
package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.server.WSEndpoint;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
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
}
