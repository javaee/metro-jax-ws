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
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
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
public final class WSServletDelegate {

    /**
     * All {@link ServletAdapter}s that are deployed in the current web appliation.
     */
    public final List<ServletAdapter> adapters;

    private final Map<String, ServletAdapter> fixedUrlPatternEndpoints = new HashMap<String, ServletAdapter>();
    private final List<ServletAdapter> pathUrlPatternEndpoints = new ArrayList<ServletAdapter>();
    private final Map<Locale,Localizer> localizerMap = new HashMap<Locale,Localizer>();
    private boolean publishStatusPage;

    public WSServletDelegate(List<ServletAdapter> adapters, ServletContext context) {
        this.adapters = adapters;

        for(ServletAdapter info : adapters)
            registerEndpointUrlPattern(info);

        localizerMap.put(defaultLocalizer.getLocale(), defaultLocalizer);

        if (logger.isLoggable(Level.INFO)) {
            logger.info(WsservletMessages.SERVLET_INFO_INITIALIZE());
        }

        String publishStatusPageParam =
            context.getInitParameter(
                WSServlet.JAXWS_RI_PROPERTY_PUBLISH_STATUS_PAGE);
        publishStatusPage =
            (publishStatusPageParam == null || Boolean.parseBoolean(publishStatusPageParam));
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
                String query = request.getQueryString();
                if (target.isMetadataQuery(query)) {
                    // Sends published WSDL and schema documents
                    target.publishWSDL(context, request, response);
                    return;
                }
                Binding binding = target.getEndpoint().getBinding();
                if (binding instanceof HTTPBinding) {
                    // The request is handled by endpoint or runtime
                    target.handle(context, request, response);
                } else {
                    // Writes HTML page with all the endpoint descriptions
                    writeWebServicesHtmlPage(request, response);
                }
            } else {
                Localizer localizer = getLocalizerFor(request);
                writeNotFoundErrorPage(localizer, response, "Invalid Request");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException(e.getMessage());
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
    public void doPost(HttpServletRequest request, HttpServletResponse response, ServletContext context) {

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
            target.handle(context, request, response);
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
     * Handles HTTP PUT for XML/HTTP binding based endpoints
     */
    public void doPut(HttpServletRequest request, HttpServletResponse response, ServletContext context)
        throws ServletException {

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


    protected void writeNotFoundErrorPage(
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



    protected void warnMissingContextInformation() {
        logger.warning(WsservletMessages.SERVLET_WARNING_MISSING_CONTEXT_INFORMATION());
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

    protected boolean checkContentType(MimeHeaders headers) {

        String[] contentTypes = headers.getHeader("Content-Type");
        if ((contentTypes != null) && (contentTypes.length >= 1)) {
            if (contentTypes[0].indexOf("text/xml") != -1) {
                return true;
            }
        }
        return false;
    }

    protected boolean checkContentLength(MimeHeaders headers) {
        String[] contentLength = headers.getHeader("Content-Length");
        if ((contentLength != null) && (contentLength.length > 0)) {
            int length = Integer.parseInt(contentLength[0]);
            if (length > 0) {
                return true;
            }
        }
        return false;
    }

    boolean checkForContent(MimeHeaders headers) {
        if (checkContentType(headers)) {
            if (checkContentLength(headers))
                return true;
        }
        return false;
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

    private void writeWebServicesHtmlPage(HttpServletRequest request,
                                          HttpServletResponse response) throws IOException {

        if (publishStatusPage) {
            Localizer localizer = getLocalizerFor(request);

            // standard browsable page
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<html>");
            out.println("<head><title>");
            // out.println("Web Services");
            out.println(WsservletMessages.SERVLET_HTML_TITLE());
            out.println("</title></head>");
            out.println("<body>");
            // out.println("<h1>Web Services</h1>");
            out.println(WsservletMessages.SERVLET_HTML_TITLE_2());

            if (adapters.isEmpty()) {
                // out.println("<p>No JAX-WS context information available.</p>");
                out.println(WsservletMessages.SERVLET_HTML_NO_INFO_AVAILABLE());
            } else {
                out.println("<table width='100%' border='1'>");
                out.println("<tr>");
                out.println("<td>");
                // out.println("WSDLPort Name");
                out.println(
                    localizer.localize(
                        messageFactory.getMessage(
                            "servlet.html.columnHeader.portName")));
                out.println("</td>");
                out.println("<td>");
                // out.println("Status");
                out.println(
                    localizer.localize(
                        messageFactory.getMessage(
                            "servlet.html.columnHeader.status")));
                out.println("</td>");
                out.println("<td>");
                // out.println("Information");
                out.println(
                    localizer.localize(
                        messageFactory.getMessage(
                            "servlet.html.columnHeader.information")));
                out.println("</td>");
                out.println("</tr>");
                String baseAddress =
                    request.getScheme()
                        + "://"
                        + request.getServerName()
                        + ":"
                        + request.getServerPort()
                        + request.getContextPath();

                for (ServletAdapter a : adapters) {
                    String endpointAddress =
                        baseAddress + a.getValidPath();
                    out.println("<tr>");
                    out.println("<td>" + a.name + "</td>");
                    out.println("<td>");
                    out.println(
                        localizer.localize(
                            messageFactory.getMessage(
                                "servlet.html.status.active")));
                    out.println("</td>");
                    out.println("<td>");
                    out.println(
                        localizer.localize(
                            messageFactory.getMessage(
                                "servlet.html.information.table",
                                endpointAddress,
                                a.getPortName(),
                                a.getEndpoint().getImplementationClass().getName()
                            )));
                    out.println("</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
            }
            out.println("</body>");
            out.println("</html>");
        }
    }

    private static final Localizer defaultLocalizer = new Localizer();
    private static final LocalizableMessageFactory messageFactory =
        new LocalizableMessageFactory("com.sun.xml.ws.resources.wsservlet");
    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".servlet.http");

}
