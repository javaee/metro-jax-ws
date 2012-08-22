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

package com.sun.xml.ws.transport.httpspi.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPBinding;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Called by {@link WSSPIServlet} to choose {@link EndpointAdapter}
 * and sends a request to it.
 *
 * <p>
 * One instance of this object is created, and then shared across
 * {@link WSSPIServlet} instances (the container might deploy many of them,
 * depending on how the user writes {@code web.xml}.)
 *
 * @author Jitendra Kotamraju
 */
public class WSServletDelegate {

    /**
     * All {@link EndpointAdapter}s that are deployed in the current web appliation.
     */
    public final List<EndpointAdapter> adapters;

    private final Map<String, EndpointAdapter> fixedUrlPatternEndpoints = new HashMap<String, EndpointAdapter>();
    private final List<EndpointAdapter> pathUrlPatternEndpoints = new ArrayList<EndpointAdapter>();

    public WSServletDelegate(List<EndpointAdapter> adapters, ServletContext context) {
        this.adapters = adapters;

        for(EndpointAdapter info : adapters)
            registerEndpointUrlPattern(info);

        if (logger.isLoggable(Level.INFO)) {
            logger.info("Initializing Servlet for "+fixedUrlPatternEndpoints);
        }

    }

    public void destroy() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Destroying Servlet for "+fixedUrlPatternEndpoints);
        }

        for(EndpointAdapter a : adapters) {
            try {
                a.dispose();
            } catch(Throwable e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response, ServletContext context) {

        try {
            EndpointAdapter target = getTarget(request);
            if (target != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Got request for endpoint "+target.getUrlPattern());
                }
                target.handle(context, request, response);
            } else {
                writeNotFoundErrorPage(response, "Invalid Request");
            }
        } catch (WebServiceException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "caught throwable", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * processes web service requests by finding the {@link EndpointAdapter}
     * created by the {@link WSSPIContextListener}
     *
     * @param request the HTTP request object
     * @param response the HTTP response object
     * @param context servlet context
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        doGet(request, response,context);
    }

    /**
     * Handles HTTP PUT for XML/HTTP binding based endpoints
     *
     * @param request the HTTP request object
     * @param response the HTTP response object
     * @param context servlet context
     */
    public void doPut(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        // TODO: unify this into doGet.
        try {
            EndpointAdapter target = getTarget(request);
            if (target != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Got request for endpoint "+target.getUrlPattern());
                }
            } else {
                writeNotFoundErrorPage(response, "Invalid request");
                return;
            }
            Binding binding = target.getEndpoint().getBinding();
            if (binding instanceof HTTPBinding) {
                target.handle(context, request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (WebServiceException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "caught throwable", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Handles HTTP DELETE for XML/HTTP binding based endpoints
     *
     * @param request the HTTP request object
     * @param response the HTTP response object
     * @param context servlet context
     */
    public void doDelete(HttpServletRequest request, HttpServletResponse response, ServletContext context) {

        // At preseent, there is no difference for between PUT and DELETE processing
        doPut(request, response, context);
    }


    private void writeNotFoundErrorPage(
        HttpServletResponse response,
        String message)
        throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>");
        out.println("Web Services");
        out.println("</title></head>");
        out.println("<body>");
        out.println("Not found "+message);
        out.println("</body>");
        out.println("</html>");
    }

    private void registerEndpointUrlPattern(EndpointAdapter a) {
        String urlPattern = a.getUrlPattern();
        if (urlPattern.indexOf("*.") != -1) {
            // cannot deal with implicit mapping right now
            logger.warning("Ignoring implicit url-pattern "+urlPattern);
        } else if (urlPattern.endsWith("/*")) {
            pathUrlPatternEndpoints.add(a);
        } else {
            if (fixedUrlPatternEndpoints.containsKey(urlPattern)) {
                logger.warning("Ignoring duplicate url-pattern "+urlPattern);
            } else {
                fixedUrlPatternEndpoints.put(urlPattern, a);
            }
        }
    }

    /**
     * Determines which {@link EndpointAdapter} serves the given request.
     *
     * @param request the HTTP request object
     */
    protected EndpointAdapter getTarget(HttpServletRequest request) {

        /*System.err.println("----");
        System.err.println("CONTEXT PATH   : " + request.getContextPath());
        System.err.println("PATH INFO      : " + request.getPathInfo());
        System.err.println("PATH TRANSLATED: " + request.getPathTranslated());
        System.err.println("QUERY STRING   : " + request.getQueryString());
        System.err.println("REQUEST URI    : " + request.getRequestURI());
        System.err.println();
         */

        String path =
            request.getRequestURI().substring(
                request.getContextPath().length());
        EndpointAdapter result = fixedUrlPatternEndpoints.get(path);
        if (result == null) {
            for (EndpointAdapter candidate : pathUrlPatternEndpoints) {
                String noSlashStar = candidate.getValidPath();
                if (path.equals(noSlashStar) || path.startsWith(noSlashStar+"/") || path.startsWith(noSlashStar+"?")) {
                    result = candidate;
                    break;
                }
            }
        }

        return result;
    }

    private static final Logger logger =
        Logger.getLogger(WSServletDelegate.class.getName());

}
