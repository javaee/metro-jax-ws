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

    @Override
    public Map<String, List<String>> getRequestHeaders() {
        return reqHeaders;
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return resHeaders;
    }

    @Override
    public String getRequestURI() {
        return req.getRequestURI();
    }

    @Override
    public String getContextPath() {
        return req.getContextPath();
    }

    @Override
    public String getRequestMethod() {
        return req.getMethod();
    }

    @Override
    public HttpContext getHttpContext() {
        return httpContext;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public String getRequestHeader(String name) {
        return reqHeaders.getFirst(name);
    }

    @Override
    public void addResponseHeader(String name, String value) {
        resHeaders.add(name, value);
    }

    @Override
    public InputStream getRequestBody() throws IOException {
        return req.getInputStream();
    }

    @Override
    public OutputStream getResponseBody() throws IOException {
        return res.getOutputStream();
    }

    @Override
    public void setStatus(int rCode) {
        res.setStatus(rCode);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
        // Only from 2.4
        // return InetSocketAddress.createUnresolved(req.getRemoteAddr(), req.getRemotePort());
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return InetSocketAddress.createUnresolved(req.getServerName(), req.getServerPort());
    }

    @Override
    public String getProtocol() {
        return req.getProtocol();
    }

    @Override
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

    @Override
    public Set<String> getAttributeNames() {
        return attributes;
    }

    @Override
    public Principal getUserPrincipal() {
        return req.getUserPrincipal();
    }

    @Override
    public boolean isUserInRole(String role) {
        return req.isUserInRole(role);
    }

    @Override
    public String getScheme() {
        return req.getScheme();
    }

    @Override
    public String getPathInfo() {
        return req.getPathInfo();
    }

    @Override
    public String getQueryString() {
        return req.getQueryString();
    }
}
