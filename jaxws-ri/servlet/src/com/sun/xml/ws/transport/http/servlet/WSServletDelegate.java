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

package com.sun.xml.ws.transport.http.servlet;

import com.sun.xml.ws.resources.WsservletMessages;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.Localizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MimeHeaders;
import javax.xml.ws.Binding;
import javax.xml.ws.http.HTTPBinding;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Called by {@link WSServlet} to choose {@link HttpAdapter}
 * and sends a request to it.
 *
 * <p>
 * One instance of this object is created, and then shared across
 * {@link WSServlet} instances (the container might deploy many of them,
 * depending on how the user writes {@code web.xml}.)
 *
 * @author WS Development Team
 */
public class WSServletDelegate {

    /**
     * All {@link ServletAdapter}s that are deployed in the current web appliation.
     */
    public final List<ServletAdapter> adapters;

    private final Map<String, ServletAdapter> fixedUrlPatternEndpoints = new HashMap<String, ServletAdapter>();
    private final List<ServletAdapter> pathUrlPatternEndpoints = new ArrayList<ServletAdapter>();
    private final Map<Locale,Localizer> localizerMap = new HashMap<Locale,Localizer>();

    public WSServletDelegate(List<ServletAdapter> adapters, ServletContext context) {
        this.adapters = adapters;

        for(ServletAdapter info : adapters)
            registerEndpointUrlPattern(info);

        localizerMap.put(defaultLocalizer.getLocale(), defaultLocalizer);

        if (logger.isLoggable(Level.INFO)) {
            logger.info(WsservletMessages.SERVLET_INFO_INITIALIZE());
        }

        // compatibility.
        String publishStatusPageParam =
            context.getInitParameter(WSServlet.JAXWS_RI_PROPERTY_PUBLISH_STATUS_PAGE);
        if(publishStatusPageParam!=null)
            HttpAdapter.publishStatusPage = Boolean.parseBoolean(publishStatusPageParam);
    }

    public void destroy() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(WsservletMessages.SERVLET_INFO_DESTROY());
        }

        for(ServletAdapter a : adapters) {
            try {
                a.getEndpoint().dispose();
            } catch(Throwable e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response, ServletContext context)
        throws ServletException {

        try {
            ServletAdapter target = getTarget(request);
            if (target != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest(
                        WsservletMessages.SERVLET_TRACE_GOT_REQUEST_FOR_ENDPOINT(target.name));
                }
                target.handle(context, request, response);
            } else {
                Localizer localizer = getLocalizerFor(request);
                writeNotFoundErrorPage(localizer, response, "Invalid Request");
            }
        } catch (JAXWSExceptionBase e) {
            logger.log(Level.SEVERE, defaultLocalizer.localize(e), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Throwable e) {
            if (e instanceof Localizable) {
                logger.log(
                    Level.SEVERE,
                    defaultLocalizer.localize((Localizable) e),
                    e);
            } else {
                logger.log(Level.SEVERE, "caught throwable", e);
            }

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * processes web service requests by finding the {@link ServletAdapter}
     * created by the {@link WSServletContextListener} and creating a
     * {@link ServletConnectionImpl}.
     *
     * @param request the HTTP request object
     * @param response the HTTP response object
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response, ServletContext context) throws ServletException {
        doGet(request, response,context);
    }

    /**
     * Handles HTTP PUT for XML/HTTP binding based endpoints
     */
    public void doPut(HttpServletRequest request, HttpServletResponse response, ServletContext context)
        throws ServletException {
        // TODO: unify this into doGet.
        try {
            ServletAdapter target = getTarget(request);
            if (target != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest(
                        WsservletMessages.SERVLET_TRACE_GOT_REQUEST_FOR_ENDPOINT(target.name));
                }
            } else {
                Localizer localizer = getLocalizerFor(request);
                writeNotFoundErrorPage(localizer, response, "Invalid request");
                return;
            }
            Binding binding = target.getEndpoint().getBinding();
            if (binding instanceof HTTPBinding) {
                target.handle(context, request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (JAXWSExceptionBase e) {
            logger.log(Level.SEVERE, defaultLocalizer.localize(e), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Throwable e) {
            if (e instanceof Localizable) {
                logger.log(
                    Level.SEVERE,
                    defaultLocalizer.localize((Localizable) e),
                    e);
            } else {
                logger.log(Level.SEVERE, "caught throwable", e);
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    
    /**
     * Handles HTTP DELETE for XML/HTTP binding based endpoints
     */
    public void doDelete(HttpServletRequest request, HttpServletResponse response, ServletContext context)
        throws ServletException {

        // At preseent, there is no difference for between PUT and DELETE processing
        doPut(request, response, context);
    }


    private void writeNotFoundErrorPage(
        Localizer localizer,
        HttpServletResponse response,
        String message)
        throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>");
        out.println(WsservletMessages.SERVLET_HTML_TITLE());
        out.println("</title></head>");
        out.println("<body>");
        out.println(WsservletMessages.SERVLET_HTML_NOT_FOUND(message));
        out.println("</body>");
        out.println("</html>");
    }

    protected static MimeHeaders getHeaders(HttpServletRequest req) {
        Enumeration enums = req.getHeaderNames();
        MimeHeaders headers = new MimeHeaders();

        while (enums.hasMoreElements()) {
            String headerName = (String) enums.nextElement();
            String headerValue = req.getHeader(headerName);
            headers.addHeader(headerName, headerValue);
        }

        return headers;
    }

    private void registerEndpointUrlPattern(ServletAdapter a) {
        String urlPattern = a.urlPattern;
        if (urlPattern.indexOf("*.") != -1) {
            // cannot deal with implicit mapping right now
            logger.warning(
                WsservletMessages.SERVLET_WARNING_IGNORING_IMPLICIT_URL_PATTERN(a.name));
        } else if (urlPattern.endsWith("/*")) {
            pathUrlPatternEndpoints.add(a);
        } else {
            if (fixedUrlPatternEndpoints.containsKey(urlPattern)) {
                logger.warning(
                    WsservletMessages.SERVLET_WARNING_DUPLICATE_ENDPOINT_URL_PATTERN(a.name));
            } else {
                fixedUrlPatternEndpoints.put(urlPattern, a);
            }
        }
    }

    /**
     * Determines which {@link ServletAdapter} serves the given request.
     */
    protected ServletAdapter getTarget(HttpServletRequest request) {

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
        ServletAdapter result = fixedUrlPatternEndpoints.get(path);
        if (result == null) {
            for (ServletAdapter candidate : pathUrlPatternEndpoints) {
                String noSlashStar = candidate.getValidPath();
                if (path.equals(noSlashStar) || path.startsWith(noSlashStar+"/") || path.startsWith(noSlashStar+"?")) {
                    result = candidate;
                    break;
                }
            }
        }

        return result;
    }

    protected Localizer getLocalizerFor(ServletRequest request) {
        Locale locale = request.getLocale();
        if (locale.equals(defaultLocalizer.getLocale())) {
            return defaultLocalizer;
        }

        synchronized (localizerMap) {
            Localizer localizer = localizerMap.get(locale);
            if (localizer == null) {
                localizer = new Localizer(locale);
                localizerMap.put(locale, localizer);
            }
            return localizer;
        }
    }

    private static final Localizer defaultLocalizer = new Localizer();
    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".servlet.http");

}
