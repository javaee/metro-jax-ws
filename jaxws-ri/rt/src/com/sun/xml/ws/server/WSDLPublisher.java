/*
 * $Id: WSDLPublisher.java,v 1.4 2005-06-03 20:48:35 jitu Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.server;
import com.sun.xml.ws.transport.http.servlet.ServletDocContext;
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
import java.io.PrintWriter;
import java.util.List;

/**
 *
 * @author JAX-RPC Development Team
 */
public class WSDLPublisher {

    public WSDLPublisher(ServletContext context,
            List<RuntimeEndpointInfo> endpoints) {
        this.servletContext = context;
        this.endpoints = endpoints;
        //templatesByEndpointInfo = new HashMap();
        localizer = new Localizer();
        messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.jaxrpcservlet");
    }

    public void handle(
        RuntimeEndpointInfo targetEndpoint,
        Map fixedUrlPatternEndpoints,
        HttpServletRequest request,
        HttpServletResponse response)
        throws IOException, ServletException {

        Iterator urlPatterns = fixedUrlPatternEndpoints.keySet().iterator();
        String urlPattern = null;

        // need to find correct url pattern in map to create baseAddress
        /*
         * (this could still use testing. may be an issue when there
         * are >2 url patterns for the same endpoint.)
         */
        while (urlPatterns.hasNext()) { // could be empty
            String testPattern = (String) urlPatterns.next();
            if (targetEndpoint == fixedUrlPatternEndpoints.get(testPattern)) {
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


        String actualAddress =
            request.getScheme()
                + "://"
                + request.getServerName()
                + ":"
                + request.getServerPort()
                + request.getRequestURI();
        String baseAddress =
            actualAddress.substring(0, actualAddress.lastIndexOf(urlPattern));

        String inPath = targetEndpoint.getPath(request.getQueryString());
System.out.println("*** inPath ="+inPath+" *** query= "+request.getQueryString());
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
        WSDLPatcher patcher = new WSDLPatcher(inPath, baseAddress,
                targetEndpoint, endpoints, in.getDocContext());
        patcher.patchDoc(in.getDoc(), outputStream);
        return;
/*
        Templates templates;
        synchronized (this) {
            templates = (Templates) templatesByEndpointInfo.get(targetEndpoint);
            if (templates == null) {
                templates = createTemplatesFor(fixedUrlPatternEndpoints);
                templatesByEndpointInfo.put(targetEndpoint, templates);
            }
        }
        try {
            Iterator iter = fixedUrlPatternEndpoints.keySet().iterator();
            while (iter.hasNext()) {
                logger.fine(
                    localizer.localize(
                        messageFactory.getMessage(
                            "publisher.info.applyingTransformation",
                            baseAddress + iter.next())));
            }
            Source wsdlDocument =
                new StreamSource(
                    servletContext.getResourceAsStream(
                        targetEndpoint.getWSDLFileName()));
            Transformer transformer = templates.newTransformer();
            transformer.setParameter("baseAddress", baseAddress);
            transformer.transform(wsdlDocument, new StreamResult(outputStream));
        } catch (TransformerConfigurationException e) {
            throw new JAXRPCServletException("exception.cannotCreateTransformer");
        } catch (TransformerException e) {
            throw new JAXRPCServletException(
                "exception.transformationFailed",
                e.getMessageAndLocation());
        }
 */
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

    /*
    protected Templates createTemplatesFor(Map patternToPort) {
        try {
            // create the stylesheet
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(bos, "UTF-8");

            writer.write(
                "<xsl:transform version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\">\n");
            writer.write("<xsl:param name=\"baseAddress\"/>\n");

            writer.write(
                "<xsl:template match=\"/\"><xsl:apply-templates mode=\"copy\"/></xsl:template>\n");

            Iterator iter = patternToPort.keySet().iterator();
            while (iter.hasNext()) {
                String pattern = (String) iter.next();
                RuntimeEndpointInfo info =
                    (RuntimeEndpointInfo) patternToPort.get(pattern);
                writer.write(
                    "<xsl:template match=\"wsdl:definitions[@targetNamespace='");
                writer.write(info.getPortName().getNamespaceURI());
                writer.write("']/wsdl:service[@name='");
                writer.write(info.getServiceName().getLocalPart());
                writer.write("']/wsdl:port[@name='");
                writer.write(info.getPortName().getLocalPart());
                writer.write("']/soap:address\" mode=\"copy\">");
                writer.write("<soap:address><xsl:attribute name=\"location\">");
                writer.write(
                    "<xsl:value-of select=\"$baseAddress\"/>" + pattern);
                writer.write("</xsl:attribute></soap:address></xsl:template>");
            }

            writer.write(
                "<xsl:template match=\"@*|node()\" mode=\"copy\"><xsl:copy><xsl:apply-templates select=\"@*\" mode=\"copy\"/><xsl:apply-templates mode=\"copy\"/></xsl:copy></xsl:template>\n");
            writer.write("</xsl:transform>\n");
            writer.close();
            byte[] stylesheet = bos.toByteArray();
            Source stylesheetSource =
                new StreamSource(new ByteArrayInputStream(stylesheet));
            TransformerFactory transformerFactory =
                TransformerFactory.newInstance();
            Templates templates =
                transformerFactory.newTemplates(stylesheetSource);
            return templates;
        } catch (Exception e) {
            throw new JAXRPCServletException("exception.templateCreationFailed");
        }
    }

    protected static void copyStream(InputStream istream, OutputStream ostream)
        throws IOException {
        byte[] buf = new byte[1024];
        int num = 0;
        while ((num = istream.read(buf)) != -1) {
            ostream.write(buf, 0, num);
        }
        ostream.flush();
    }
     */

    private ServletContext servletContext;
    private List<RuntimeEndpointInfo> endpoints;
    private Localizer localizer;
    private LocalizableMessageFactory messageFactory;
    //private Map templatesByEndpointInfo;
    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
}
