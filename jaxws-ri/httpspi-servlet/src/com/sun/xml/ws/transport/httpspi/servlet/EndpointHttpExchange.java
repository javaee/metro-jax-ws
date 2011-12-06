/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.transport.httpspi.servlet;

import javax.xml.ws.spi.http.HttpExchange;
import javax.xml.ws.spi.http.HttpContext;
import javax.xml.ws.handler.MessageContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.net.InetSocketAddress;
import java.security.Principal;

/**
 * @author Jitendra Kotamraju
*/
final class EndpointHttpExchange extends HttpExchange {
    private final HttpServletRequest req;
    private final HttpServletResponse res;
    private final ExchangeRequestHeaders reqHeaders;
    private final ExchangeResponseHeaders resHeaders;
    private final ServletContext servletContext;
    private final HttpContext httpContext;
    private static final Set<String> attributes = new HashSet<String>();
    static {
        attributes.add(MessageContext.SERVLET_CONTEXT);
        attributes.add(MessageContext.SERVLET_REQUEST);
        attributes.add(MessageContext.SERVLET_RESPONSE);
    }

    EndpointHttpExchange(HttpServletRequest req, HttpServletResponse res, ServletContext servletContext,
                         HttpContext httpContext) {
        this.req = req;
        this.res = res;
        this.servletContext = servletContext;
        this.httpContext = httpContext;
        this.reqHeaders = new ExchangeRequestHeaders(req);
        this.resHeaders = new ExchangeResponseHeaders(res);
    }

    public Map<String, List<String>> getRequestHeaders() {
        return reqHeaders;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return resHeaders;
    }

    public String getRequestURI() {
        return req.getRequestURI();
    }

    public String getContextPath() {
        return req.getContextPath();
    }

    public String getRequestMethod() {
        return req.getMethod();
    }

    public HttpContext getHttpContext() {
        return httpContext;
    }

    public void close() throws IOException {
    }

    public String getRequestHeader(String name) {
        return reqHeaders.getFirst(name);
    }

    public void addResponseHeader(String name, String value) {
        resHeaders.add(name, value);
    }

    public InputStream getRequestBody() throws IOException {
        return req.getInputStream();
    }

    public OutputStream getResponseBody() throws IOException {
        return res.getOutputStream();
    }

    public void setStatus(int rCode) {
        res.setStatus(rCode);
    }

    public InetSocketAddress getRemoteAddress() {
        return null;
        // Only from 2.4
        // return InetSocketAddress.createUnresolved(req.getRemoteAddr(), req.getRemotePort());
    }

    public InetSocketAddress getLocalAddress() {
        return InetSocketAddress.createUnresolved(req.getServerName(), req.getServerPort());
    }

    public String getProtocol() {
        return req.getProtocol();
    }

    public Object getAttribute(String name) {
        if (name.equals(MessageContext.SERVLET_CONTEXT)) {
            return servletContext;
        } else if (name.equals(MessageContext.SERVLET_REQUEST)) {
            return req;
        } else if (name.equals(MessageContext.SERVLET_RESPONSE)) {
            return res;
        }
        return null;
    }

    public Set<String> getAttributeNames() {
        return attributes;
    }

    public Principal getUserPrincipal() {
        return req.getUserPrincipal();
    }

    public boolean isUserInRole(String role) {
        return req.isUserInRole(role);
    }

    public String getScheme() {
        return req.getScheme();
    }

    public String getPathInfo() {
        return req.getPathInfo();
    }

    public String getQueryString() {
        return req.getQueryString();
    }
}
