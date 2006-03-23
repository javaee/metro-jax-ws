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

package com.sun.xml.ws.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 *
 * @author WS Development Team
 */
public class WSDLPublisher {

    public WSDLPublisher(ServletContext context,
            List<RuntimeEndpointInfo> endpoints) {
        this.endpoints = endpoints;
        localizer = new Localizer();
        messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.wsservlet");
    }

    public void handle(
        RuntimeEndpointInfo targetEndpoint,
        Map fixedUrlPatternEndpoints,
        HttpServletRequest request,
        HttpServletResponse response)
        throws IOException, ServletException {

        Iterator urlPatterns = fixedUrlPatternEndpoints.entrySet().iterator();
        String urlPattern = null;

        // need to find correct url pattern in map to create baseAddress
        /*
         * (this could still use testing. may be an issue when there
         * are >2 url patterns for the same endpoint.)
         */
        while (urlPatterns.hasNext()) { // could be empty
            Map.Entry entry = (Map.Entry) urlPatterns.next();
            String testPattern = (String)entry.getKey();
            if (targetEndpoint == (RuntimeEndpointInfo)entry.getValue()) {
                urlPattern = testPattern;
                break;
            }
        }

        // have to assume that only 1 path url was used for this endpoint
        if (urlPattern == null) {
            urlPattern = targetEndpoint.getUrlPattern();
            if (urlPattern.endsWith("/*")) { // should always be true at this point
                urlPattern = urlPattern.substring(0, urlPattern.length() - 2);
            }

            // add new pattern and endpoint to map for stylesheet
            fixedUrlPatternEndpoints.put(urlPattern, targetEndpoint);
        }

        StringBuffer addrBuf = new StringBuffer();
        addrBuf.append(request.getScheme());
        addrBuf.append("://");
        addrBuf.append(request.getServerName());
        addrBuf.append(":");
        addrBuf.append(request.getServerPort());
        addrBuf.append(request.getRequestURI());
        
        String actualAddress = addrBuf.toString();
        String baseAddress =
            actualAddress.substring(0, actualAddress.lastIndexOf(urlPattern));

        String inPath = targetEndpoint.getPath(request.getQueryString());
        if (inPath == null) {
            writeNotFoundErrorPage(response, "Invalid Request");
            return;
        }
        DocInfo in = targetEndpoint.getDocMetadata().get(inPath);
        if (in == null) {
            writeNotFoundErrorPage(response, "Invalid Request");
            return;
        }
        response.setContentType("text/xml");
        response.setStatus(HttpServletResponse.SC_OK);
        OutputStream outputStream = response.getOutputStream();
        WSDLPatcher patcher = new WSDLPatcher(in, baseAddress,
                targetEndpoint, endpoints);
        InputStream is = in.getDoc();
        try {
            patcher.patchDoc(is, outputStream);
        } finally {
            is.close();
        }
        return;
    }
    
    protected void writeNotFoundErrorPage(
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

    private List<RuntimeEndpointInfo> endpoints;
    private Localizer localizer;
    private LocalizableMessageFactory messageFactory;
    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
}
