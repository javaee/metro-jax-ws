/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.ws.transport.http.servlet;
import com.sun.xml.ws.binding.BindingImpl;
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

import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.handler.MessageContextUtil;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.WSDLPublisher;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.http.HTTPBinding;

/**
 * Servlet for WS invocations
 *
 * @author WS Development Team
 */
public class WSServletDelegate {
    
    private com.sun.xml.ws.server.Tie tie =
        new com.sun.xml.ws.server.Tie();

    public void init(ServletConfig servletConfig)
        throws ServletException {

        defaultLocalizer = new Localizer();
        localizerMap = new HashMap();
        localizerMap.put(defaultLocalizer.getLocale(), defaultLocalizer);
        messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.wsservlet");

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

        try {
            /*
            Localizer localizer = getLocalizerFor(request);

            MimeHeaders headers = getHeaders(request);
            
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
             */
            RuntimeEndpointInfo targetEndpoint = getEndpointFor(request);
            if (targetEndpoint != null) {
                String query = request.getQueryString();
                if (query != null && (query.equals("WSDL") || query.startsWith("wsdl") 
                                      || query.startsWith("xsd="))) {
                    // Sends published WSDL and schema documents
                    publisher.handle(targetEndpoint, fixedUrlPatternEndpoints,
                        request, response);
                    return;
                }
                BindingImpl binding = (BindingImpl)targetEndpoint.getBinding();
                if (binding.getBindingId().equals(HTTPBinding.HTTP_BINDING)) {
                    // The request is handled by endpoint or runtime
                    handle(request, response, targetEndpoint);
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
     * Handles HTTP PUT for XML/HTTP binding based endpoints
     */
    public void doPut(HttpServletRequest request, HttpServletResponse response)
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
            } else {
                Localizer localizer = getLocalizerFor(request);
                writeNotFoundErrorPage(localizer, response, "Invalid request");
                return;
            }
            BindingImpl binding = (BindingImpl)targetEndpoint.getBinding();
            if (binding.getBindingId().equals(HTTPBinding.HTTP_BINDING)) {
                handle(request, response, targetEndpoint);
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
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        
        // At preseent, there is no difference for between PUT and DELETE processing  
        doPut(request, response);
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
            } else {
                Localizer localizer = getLocalizerFor(request);
                writeNotFoundErrorPage(localizer, response, "Invalid request");
                return;
            }
            handle(request, response, targetEndpoint);
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
     * Invokes JAXWS runtime with the correct MessageContext
     */
    private void handle(HttpServletRequest request, HttpServletResponse response,
        RuntimeEndpointInfo targetEndpoint) throws Exception {
    
        WebServiceContext wsCtxt = targetEndpoint.getWebServiceContext();
        MessageContext msgCtxt = new MessageContextImpl();
        wsCtxt.setMessageContext(msgCtxt);
        msgCtxt.put(MessageContext.SERVLET_CONTEXT, servletContext);
        msgCtxt.setScope(MessageContext.SERVLET_CONTEXT, Scope.APPLICATION);
        msgCtxt.put(MessageContext.SERVLET_REQUEST, request);
        msgCtxt.setScope(MessageContext.SERVLET_REQUEST, Scope.APPLICATION);
        msgCtxt.put(MessageContext.SERVLET_RESPONSE, response);
        msgCtxt.setScope(MessageContext.SERVLET_RESPONSE, Scope.APPLICATION);

        MessageContextUtil.setHttpRequestMethod(msgCtxt, request.getMethod());
        if (request.getQueryString() != null) {
            MessageContextUtil.setQueryString(msgCtxt, request.getQueryString());
        }
        if (request.getPathInfo() != null) {
            MessageContextUtil.setPathInfo(msgCtxt, request.getPathInfo());
        }

        WSConnection connection =
            new ServletConnectionImpl(request, response);
        MessageContextUtil.setHttpRequestHeaders(msgCtxt, connection.getHeaders());
        tie.handle(connection, targetEndpoint);
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
                    String wsdl = (info.getPath("wsdl") == null)
                        ? "NO WSDL PUBLISHED"
                        : endpointAddress+"?wsdl";
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
                                        .getName(),
                                     wsdl})));

                    out.println("</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
            }
            out.println("</body>");
            out.println("</html>");
        }
    }

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
            com.sun.xml.ws.util.Constants.LoggingDomain + ".servlet.http");

}
