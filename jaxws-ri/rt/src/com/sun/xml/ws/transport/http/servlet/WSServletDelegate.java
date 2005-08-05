/*
 * $Id: WSServletDelegate.java,v 1.5 2005-08-05 21:08:36 jitu Exp $
 *
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.servlet;
import com.sun.xml.ws.handler.MessageContextImpl;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;

import com.sun.xml.ws.spi.runtime.ServletDelegate;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
//import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.WSDLPublisher;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import com.sun.xml.ws.spi.runtime.MessageContext;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import javax.xml.ws.handler.MessageContext.Scope;

/**
 * Servlet for WS invocations
 *
 * @author WS Development Team
 */
public class WSServletDelegate implements ServletDelegate {
    
    private com.sun.xml.ws.server.Tie tie =
        new com.sun.xml.ws.server.Tie();

    public void init(ServletConfig servletConfig)
        throws ServletException {

        defaultLocalizer = new Localizer();
        localizerMap = new HashMap();
        localizerMap.put(defaultLocalizer.getLocale(), defaultLocalizer);
        messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.wsservlet");

        this.servletConfig = servletConfig;
        this.servletContext = servletConfig.getServletContext();

        if (logger.isLoggable(Level.INFO)) {
            logger.info(
                defaultLocalizer.localize(
                    messageFactory.getMessage("servlet.info.initialize")));
        }

        fixedUrlPatternEndpoints = new HashMap();
        pathUrlPatternEndpoints = new ArrayList();

        jaxwsInfo =
            (List<RuntimeEndpointInfo>) servletContext.getAttribute(
                WSServlet.JAXWS_RI_RUNTIME_INFO);
        if (jaxwsInfo == null) {
            warnMissingContextInformation();
        } else {
            Map endpointsByName = new HashMap();
            for(RuntimeEndpointInfo info : jaxwsInfo) {
                if (endpointsByName.containsKey(info.getName())) {
                    logger.warning(
                        defaultLocalizer.localize(
                            messageFactory.getMessage(
                                "servlet.warning.duplicateEndpointName",
                                info.getName())));
                } else {
                    endpointsByName.put(info.getName(), info);
                    registerEndpointUrlPattern(info);
                    
                    try {
                        info.injectContext();
                        info.beginService();
                    } catch(Exception e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                        throw new ServletException(e.getMessage());
                    }
                }
            }
        }

        String publishWSDLParam =
            servletContext.getInitParameter(
                WSServlet.JAXWS_RI_PROPERTY_PUBLISH_WSDL);
        publishWSDL =
            (publishWSDLParam == null
                ? true
                : Boolean.valueOf(publishWSDLParam).booleanValue());

        String publishStatusPageParam =
            servletContext.getInitParameter(
                WSServlet.JAXWS_RI_PROPERTY_PUBLISH_STATUS_PAGE);
        publishStatusPage =
            (publishStatusPageParam == null
                ? true
                : Boolean.valueOf(publishStatusPageParam).booleanValue());

        publisher = new WSDLPublisher(servletContext, jaxwsInfo);

        if (secondDelegate != null)
            secondDelegate.postInit(servletConfig);
    }

    public void destroy() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(
                defaultLocalizer.localize(
                    messageFactory.getMessage("servlet.info.destroy")));
        }
        if (jaxwsInfo != null) {
            for(RuntimeEndpointInfo info : jaxwsInfo) {
                try {
                    info.endService();
                } catch(Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException {

        if (secondDelegate != null)
            secondDelegate.doGet(request, response);
        else
            doGetDefault(request, response);
    }

    private void doGetDefault(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException {
        try {

            MimeHeaders headers = getHeaders(request);
            Localizer localizer = getLocalizerFor(request);

            if (checkForContent(headers)) {
                writeInvalidMethodType(
                    localizer,
                    response,
                    "Invalid Method Type");
                if (logger.isLoggable(Level.INFO)) {
                    logger.severe(
                        defaultLocalizer.localize(
                            messageFactory.getMessage("servlet.html.method")));
                    logger.severe("Must use Http POST for the service request");
                }
                return;
            }
            RuntimeEndpointInfo targetEndpoint = getEndpointFor(request);
            if (jaxwsInfo == null && request.getQueryString() != null) {
                writeNotFoundErrorPage(localizer, response, "Invalid Context");
            } else if (targetEndpoint != null && request.getQueryString() != null) {
                if (request.getQueryString().equals("WSDL") ||
                    request.getQueryString().startsWith("wsdl") ||
                    request.getQueryString().startsWith("xsd")) {
                    if (publishWSDL
                        && targetEndpoint.getWSDLFileName() != null) {
                        // return a WSDL document
                        publisher.handle(
                            targetEndpoint,
                            fixedUrlPatternEndpoints,
                            request,
                            response);
                    } else {
                        writeNotFoundErrorPage(
                            localizer,
                            response,
                            "Invalid request");
                    }
                } else {
                    writeNotFoundErrorPage(
                        localizer,
                        response,
                        "Invalid request");
                }
            } else if (request.getPathInfo() == null) {
                if (publishStatusPage) {
                    // standard browsable page
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html>");
                    out.println("<head><title>");
                    // out.println("Web Services");
                    out.println(
                        localizer.localize(
                            messageFactory.getMessage("servlet.html.title")));
                    out.println("</title></head>");
                    out.println("<body>");
                    // out.println("<h1>Web Services</h1>");
                    out.println(
                        localizer.localize(
                            messageFactory.getMessage("servlet.html.title2")));
                    if (jaxwsInfo == null) {
                        // out.println("<p>No JAX-WS context information available.</p>");
                        out.println(
                            localizer.localize(
                                messageFactory.getMessage(
                                    "servlet.html.noInfoAvailable")));
                    } else {
                        out.println("<table width='100%' border='1'>");
                        out.println("<tr>");
                        out.println("<td>");
                        // out.println("Port Name");
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

                        for (RuntimeEndpointInfo info : jaxwsInfo) {
                            String endpointAddress =
                                baseAddress + getValidPathForEndpoint(info);
                            out.println("<tr>");
                            out.println("<td>" + info.getName() + "</td>");
                            out.println("<td>");
                            if (info.isDeployed()) {
                                // out.println("ACTIVE");
                                out.println(
                                    localizer.localize(
                                        messageFactory.getMessage(
                                            "servlet.html.status.active")));
                            } else {
                                // out.println("ERROR");
                                out.println(
                                    localizer.localize(
                                        messageFactory.getMessage(
                                            "servlet.html.status.error")));
                            }
                            out.println("</td>");
                            out.println("<td>");
                            out.println(
                                localizer.localize(
                                    messageFactory.getMessage(
                                        "servlet.html.information.table",
                                        new Object[] {
                                            endpointAddress,
                                            info.getPortName(),
                                            info
                                                .getImplementor()
                                                .getClass()
                                                .getName()})));

                            out.println("</td>");
                            out.println("</tr>");
                        }
                        out.println("</table>");
                    }
                    out.println("</body>");
                    out.println("</html>");
                } else {
                    writeNotFoundErrorPage(
                        localizer,
                        response,
                        "Invalid request");
                }
            } else {
                if (publishStatusPage) {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html>");
                    out.println("<head><title>");
                    // out.println("Web Services");
                    out.println(
                        localizer.localize(
                            messageFactory.getMessage("servlet.html.title")));
                    out.println("</title></head>");
                    out.println("<body>");
                    out.println("</body>");
                    out.println("</html>");
                } else {
                    writeNotFoundErrorPage(
                        localizer,
                        response,
                        "Invalid request");
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException(e.getMessage());
        }
    }

    /**
     * processes web service requests by finding the <code>RuntimeEndpointInfo</code>
     * created by the <code>WSContextListener</code> and creating a 
     * <code>ServletConnectionImpl</code> and passing it to <code>Tie.handle</code>
     * @param request the HTTP request object
     * @param response the HTTP response object
     * @throws javax.servlet.ServletException 
     */
    public void doPost(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException {
        
        try {
            RuntimeEndpointInfo targetEndpoint = getEndpointFor(request);
            if (targetEndpoint != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest(defaultLocalizer.localize(
                            messageFactory.getMessage(
                                "servlet.trace.gotRequestForEndpoint",
                                targetEndpoint.getName())));
                }
            }
            WebServiceContext wsCtxt = targetEndpoint.getWebServiceContext();
            MessageContext msgCtxt = new MessageContextImpl();
            wsCtxt.setMessageContext(msgCtxt);
            msgCtxt.put(MessageContext.SERVLET_CONTEXT, servletContext);
            msgCtxt.setScope(MessageContext.SERVLET_CONTEXT, Scope.APPLICATION);
            msgCtxt.put(MessageContext.SERVLET_SESSION, request.getSession());
            msgCtxt.setScope(MessageContext.SERVLET_SESSION, Scope.APPLICATION);
            msgCtxt.put(MessageContext.SERVLET_REQUEST, request);
            msgCtxt.setScope(MessageContext.SERVLET_REQUEST, Scope.APPLICATION);
            msgCtxt.put(MessageContext.SERVLET_RESPONSE, response);
            msgCtxt.setScope(MessageContext.SERVLET_RESPONSE, Scope.APPLICATION);
            WSConnection connection =
                new ServletConnectionImpl(request, response);
            tie.handle(connection, targetEndpoint);
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
        out.println(
            localizer.localize(
                messageFactory.getMessage("servlet.html.title")));
        out.println("</title></head>");
        out.println("<body>");
        out.println(
            localizer.localize(
                messageFactory.getMessage("servlet.html.notFound", message)));
        out.println("</body>");
        out.println("</html>");
    }

    protected void writeInvalidMethodType(
        Localizer localizer,
        HttpServletResponse response,
        String message)
        throws IOException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>");
        out.println(
            localizer.localize(
                messageFactory.getMessage("servlet.html.title")));
        out.println("</title></head>");
        out.println("<body>");
        out.println(
            localizer.localize(
                messageFactory.getMessage("servlet.html.method", message)));
        out.println("</body>");
        out.println("</html>");

    }

    protected void warnMissingContextInformation() {
        if (secondDelegate != null)
            secondDelegate.warnMissingContextInformation();
        else
            logger.warning(
                defaultLocalizer.localize(
                    messageFactory.getMessage(
                        "servlet.warning.missingContextInformation")));
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

    public void registerEndpointUrlPattern(
        com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo info) {
        String urlPattern = ((RuntimeEndpointInfo) info).getUrlPattern();
        if (urlPattern.indexOf("*.") != -1) {
            // cannot deal with implicit mapping right now
            logger.warning(
                defaultLocalizer.localize(
                    messageFactory.getMessage(
                        "servlet.warning.ignoringImplicitUrlPattern",
                        ((RuntimeEndpointInfo) info).getName())));
        } else if (urlPattern.endsWith("/*")) {
            pathUrlPatternEndpoints.add(info);
        } else {
            if (fixedUrlPatternEndpoints.containsKey(urlPattern)) {
                logger.warning(
                    defaultLocalizer.localize(
                        messageFactory.getMessage(
                            "servlet.warning.duplicateEndpointUrlPattern",
                            ((RuntimeEndpointInfo) info).getName())));
            } else {
                fixedUrlPatternEndpoints.put(urlPattern, info);
            }
        }
    }

    protected String getValidPathForEndpoint(RuntimeEndpointInfo info) {
        String s = info.getUrlPattern();
        if (s.endsWith("/*")) {
            return s.substring(0, s.length() - 2);
        } else {
            return s;
        }
    }

    protected RuntimeEndpointInfo getEndpointFor(HttpServletRequest request) {

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
        RuntimeEndpointInfo result =
            (RuntimeEndpointInfo) fixedUrlPatternEndpoints.get(path);
        if (result == null) {
            for (Iterator iter = pathUrlPatternEndpoints.iterator();
                iter.hasNext();
                ) {
                RuntimeEndpointInfo candidate =
                    (RuntimeEndpointInfo) iter.next();
                if (path.startsWith(getValidPathForEndpoint(candidate))) {
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
            int length = new Integer(contentLength[0]).intValue();
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
            Localizer localizer = (Localizer) localizerMap.get(locale);
            if (localizer == null) {
                localizer = new Localizer(locale);
                localizerMap.put(locale, localizer);
            }
            return localizer;
        }
    }

    protected QName getFaultServerQName(){
        return SOAPConstants.FAULT_CODE_SERVER;
    }

    public void setSecondDelegate(
        com.sun.xml.ws.spi.runtime.ServletSecondDelegate secondDelegate) {
        this.secondDelegate = secondDelegate;
    }

    private ServletConfig servletConfig;
    private ServletContext servletContext;
    private List<RuntimeEndpointInfo> jaxwsInfo;
    private Localizer defaultLocalizer;
    private LocalizableMessageFactory messageFactory;
    private Map fixedUrlPatternEndpoints;
    private List pathUrlPatternEndpoints;
    private Map localizerMap;
    private WSDLPublisher publisher;
    private boolean publishWSDL;
    private boolean publishStatusPage;

    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");

    private com.sun.xml.ws.spi.runtime.ServletSecondDelegate secondDelegate =
        null;

}
