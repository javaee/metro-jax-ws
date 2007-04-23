/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.transport.http;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.PropertySet;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;

import javax.xml.ws.WebServiceContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;


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
 * expose its properties to the appliation and pipes. (This object
 * will be added to {@link Packet#addSatellite(PropertySet)}.)
 *
 * @author Jitendra Kotamraju
 */
public abstract class WSHTTPConnection extends PropertySet {

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
     * should ignore <tt>Content-Type</tt> and <tt>Content-Length</tt> headers.
     *
     * @param headers
     *      See {@link HttpURLConnection#getHeaderFields()} for the format.
     *      This parameter may not be null, but since the user application
     *      code may invoke this method, a graceful error checking with
     *      an helpful error message should be provided if it's actually null.
     * @see #setContentTypeResponseHeader(String) 
     */
    public abstract void setResponseHeaders(@NotNull Map<String,List<String>> headers);

    /**
     * Sets the <tt>"Content-Type"</tt> header.
     *
     * <p>
     * If the Content-Type header has already been set, this method will overwrite
     * the previously set value. If not, this method adds it.
     *
     * <p>
     * Note that this method and {@link #setResponseHeaders(Map<String,List<String>>)}
     * may be invoked in any arbitrary order.
     *
     * @param value
     *      strings like <tt>"application/xml; charset=UTF-8"</tt> or
     *      <tt>"image/jpeg"</tt>.
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
     * HTTP Query string, such as "foo=bar", or null if none exists.
     */
    public abstract @Nullable String getQueryString();

    /**
     * Requested path. A string like "/foo/bar/baz"
     */
    public abstract @Nullable String getPathInfo();

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

    private boolean closed;

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
     * @return
     */
    public String getProtocol() {
        return "HTTP/1.1";
    }

    /**
     * Subclasses are expected to override
     *
     * @return
     */
    public void setContentLengthResponseHeader(int value) {
    }
    
}
