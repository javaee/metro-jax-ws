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

package com.sun.xml.ws.transport.http;

import com.oracle.webservices.api.message.BasePropertySet;
import com.oracle.webservices.api.message.PropertySet;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;

import javax.xml.ws.WebServiceContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The view of an HTTP exchange from the point of view of JAX-WS.
 *
 * <p>
 * Different HTTP server layer uses different implementations of this class
 * so that JAX-WS can be shielded from individuality of such layers.
 * This is an interface implemented as an abstract class, so that
 * future versions of the JAX-WS RI can add new methods.
 *
 * <p>
 * This class extends {@link PropertySet} so that a transport can
 * expose its properties to the application and pipes. (This object
 * will be added to {@link Packet#addSatellite(PropertySet)}.)
 *
 * @author Jitendra Kotamraju
 */
public abstract class WSHTTPConnection extends BasePropertySet {

    public static final int OK=200;
    public static final int ONEWAY=202;
    public static final int UNSUPPORTED_MEDIA=415;
    public static final int MALFORMED_XML=400;
    public static final int INTERNAL_ERR=500;

    /**
     * Overwrites all the HTTP response headers written thus far.
     *
     * <p>
     * The implementation should copy the contents of the {@link Map},
     * rather than retaining a reference. The {@link Map} passed as a
     * parameter may change after this method is invoked.
     *
     * <p>
     * This method may be called repeatedly, although in normal use
     * case that's rare (so the implementation is encourage to take
     * advantage of this usage pattern to improve performance, if possible.)
     *
     * <p>
     * Initially, no header is set.
     *
     * <p>
     * This parameter is usually exposed to {@link WebServiceContext}
     * as {@link Packet#OUTBOUND_TRANSPORT_HEADERS}, and thus it
     * should ignore {@code Content-Type} and {@code Content-Length} headers.
     *
     * @param headers
     *      See {@link HttpURLConnection#getHeaderFields()} for the format.
     *      This parameter may not be null, but since the user application
     *      code may invoke this method, a graceful error checking with
     *      an helpful error message should be provided if it's actually null.
     * @see #setContentTypeResponseHeader(String)
     */
    public abstract void setResponseHeaders(@NotNull Map<String,List<String>> headers);

    public void setResponseHeader(String key, String value) {
    	setResponseHeader(key, Collections.singletonList(value));
    }
    
    public abstract void setResponseHeader(String key, List<String> value);

    /**
     * Sets the {@code "Content-Type"} header.
     *
     * <p>
     * If the Content-Type header has already been set, this method will overwrite
     * the previously set value. If not, this method adds it.
     *
     * <p>
     * Note that this method and {@link #setResponseHeaders(java.util.Map)}
     * may be invoked in any arbitrary order.
     *
     * @param value
     *      strings like {@code "application/xml; charset=UTF-8"} or
     *      {@code "image/jpeg"}.
     */
    public abstract void setContentTypeResponseHeader(@NotNull String value);

    /**
     * Sets the HTTP response code like {@link #OK}.
     *
     * <p>
     * While JAX-WS processes a {@link WSHTTPConnection}, it
     * will at least call this method once to set a valid HTTP response code.
     * Note that this method may be invoked multiple times (from user code),
     * so do not consider the value to be final until {@link #getOutput()}
     * is invoked.
     */

    public abstract void setStatus(int status);

    /**
     * Gets the last value set by {@link #setStatus(int)}.
     *
     * @return
     *      if {@link #setStatus(int)} has not been invoked yet,
     *      return 0.
     */
    // I know this is ugly method!
    public abstract int getStatus();

    /**
     * Transport's underlying input stream.
     *
     * <p>
     * This method will be invoked at most once by the JAX-WS RI to
     * read the request body. If there's no request body, this method
     * should return an empty {@link InputStream}.
     *
     * @return
     *      the stream from which the request body will be read.
     */
    public abstract @NotNull InputStream getInput() throws IOException;

    /**
     * Transport's underlying output stream
     *
     * <p>
     * This method will be invoked exactly once by the JAX-WS RI
     * to start writing the response body (unless the processing aborts abnormally.)
     * Even if there's no response body to write, this method will
     * still be invoked only to be closed immediately.
     *
     * <p>
     * Once this method is called, the status code and response
     * headers will never change (IOW {@link #setStatus(int)},
     * {@link #setResponseHeaders}, and {@link #setContentTypeResponseHeader(String)}
     * will never be invoked.
     */
    public abstract @NotNull OutputStream getOutput() throws IOException;

    /**
     * Returns the {@link WebServiceContextDelegate} for this connection.
     */
    public abstract @NotNull WebServiceContextDelegate getWebServiceContextDelegate();

    /**
     * HTTP request method, such as "GET" or "POST".
     */
    public abstract @NotNull String getRequestMethod();

    /**
     * HTTP request headers.
     *
     * @deprecated
     *      This is a potentially expensive operation.
     *      Programs that want to access HTTP headers should consider using
     *      other methods such as {@link #getRequestHeader(String)}.
     *
     * @return
     *      can be empty but never null.
     */
    public abstract @NotNull Map<String,List<String>> getRequestHeaders();

    /**
     * HTTP request header names.
     *
     * @deprecated
     *      This is a potentially expensive operation.
     *      Programs that want to access HTTP headers should consider using
     *      other methods such as {@link #getRequestHeader(String)}.
     *
     * @return
     *      can be empty but never null.
     */
    public abstract @NotNull Set<String> getRequestHeaderNames();
    
    /**
     * @return
     *      HTTP response headers.
     */
    public abstract Map<String,List<String>> getResponseHeaders();
    
    /**
     * Gets an HTTP request header.
     *
     * <p>
     * if multiple headers are present, this method returns one of them.
     * (The implementation is free to choose which one it returns.)
     *
     * @return
     *      null if no header exists.
     */
    public abstract @Nullable String getRequestHeader(@NotNull String headerName);

    /**
     * Gets an HTTP request header.
     *
     * @return
     *      null if no header exists.
     */
    public abstract @Nullable List<String> getRequestHeaderValues(@NotNull String headerName);

    /**
     * HTTP Query string, such as "foo=bar", or null if none exists.
     */
    public abstract @Nullable String getQueryString();

    /**
     * Extra portion of the request URI after the end of the expected address of the service
     * but before the query string
     */
    public abstract @Nullable String getPathInfo();

    /**
     * Requested path. A string like "/foo/bar/baz"
     */
    public abstract @NotNull String getRequestURI();
    
    /**
     * Requested scheme, e.g. "http" or "https"
     */
    public abstract @NotNull String getRequestScheme();

    /**
     * Server name
     */
    public abstract @NotNull String getServerName();

    /**
     * Server port
     */
    public abstract int getServerPort();
    
    /**
     * Portion of the request URI that groups related service addresses.  The value, if non-empty, will 
     * always begin with '/', but will never end with '/'.  Environments that do not support 
     * context paths must return an empty string.
     */
    public @NotNull String getContextPath() {
    	return "";
    }
    
    /**
     * Environment specific context , if available
     */
    public Object getContext() {
    	return null;
    }
    
    /**
     * Gets the absolute URL up to the context path.
     * @return
     *      String like "http://myhost/myapp"
     * @since 2.1.2
     */
    public @NotNull String getBaseAddress() {
        throw new UnsupportedOperationException();
    }

    /**
     * Whether connection is HTTPS or not
     *
     * @return if the received request is on HTTPS, return true
     *         else false
     */
    public abstract boolean isSecure();
    
    /**
     * User principal associated with the request
     * 
     * @return user principal
     */
    public Principal getUserPrincipal() {
    	return null;
    }
    
    /**
     * Whether user associated with the request holds the given role
     * 
     * @param role Role to check
     * @return if the caller holds the role
     */
    public boolean isUserInRole(String role) {
    	return false;
    }
    
    /**
     * Gets request metadata attribute
     * @param key Request metadata key
     * @return Value of metadata attribute or null, if no value present
     */
    public Object getRequestAttribute(String key) {
    	return null;
    }

    private volatile boolean closed;

    /**
     * Close the connection
     */
    public void close() {
        this.closed = true;
    }

    /**
     * Retuns whether connection is closed or not.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Subclasses are expected to override
     *
     * @return a {@link String} containing the protocol name and version number
     */
    public String getProtocol() {
        return "HTTP/1.1";
    }

    /**
     * Subclasses are expected to override
     *
     * @since JAX-WS RI 2.2.2
     * @return value of given cookie
     */
    public String getCookie(String name) {
        return null;
    }

    /**
     * Subclasses are expected to override
     *
     *
     * @since JAX-WS RI 2.2.2
     */
    public void setCookie(String name, String value) {
    }

    /**
     * Subclasses are expected to override
     */
    public void setContentLengthResponseHeader(int value) {
    }

}
