/**
 * $Id: WSHttpHandler.java,v 1.1 2005-09-04 23:33:06 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpInteraction;
import com.sun.xml.ws.handler.MessageContextImpl;
import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.server.WSDLPatcher;
import com.sun.xml.ws.spi.runtime.MessageContext;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.Tie;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author WS Development Team
 */
public class WSHttpHandler implements HttpHandler {
    
    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";
    private static final String HTML_CONTENT_TYPE = "text/html";
    private static final String XML_CONTENT_TYPE = "text/xml";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    
    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
    private static final Localizer localizer = new Localizer();
    private static final LocalizableMessageFactory messageFactory =
        new LocalizableMessageFactory("com.sun.xml.ws.resources.httpserver");
    
    private RuntimeEndpointInfo endpointInfo;
    private Tie tie;
    
    public WSHttpHandler(Tie tie, RuntimeEndpointInfo endpointInfo) {
        this.tie = tie;
        this.endpointInfo = endpointInfo;
    }
    
    public void handleInteraction(HttpInteraction msg) {
        logger.fine("Received HTTP request:"+msg.getRequestURI());
        String method = msg.getRequestMethod();
        if (method.equals(GET_METHOD)) {
            get(msg);
        } else if (method.equals(POST_METHOD)) {
            post(msg);
        } else {
            logger.warning(
                localizer.localize(
                    messageFactory.getMessage(
                        "unexpected.http.method", method)));
            try {
                msg.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();          // Not much can be done
            }
        }
    }
    
    /*
     * Handles POST requests
     */
    private void post(HttpInteraction msg) {
        try {
            ServerConnectionImpl con = new ServerConnectionImpl(msg);
            MessageContext msgCtxt = new MessageContextImpl();
            WebServiceContext wsContext = endpointInfo.getWebServiceContext();
            wsContext.setMessageContext(msgCtxt);
            tie.handle(con, endpointInfo);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                msg.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();          // Not much can be done
            }
        }
    }
    
    /*
     * Consumes the entire input stream
     */
    private static void readFully(InputStream is) throws IOException {
        byte[] buf = new byte[1024];
        if (is != null) {
            while (is.read(buf) != -1);
        }
    }
    
    /*
     * Handles GET requests
     */ 
    public void get(HttpInteraction msg) {
        try {
            InputStream is = msg.getRequestBody();
            try {
                readFully(is);
            } catch(IOException ioe) {
                ioe.printStackTrace();
                String message = "Couldn't read Request";
                writeErrorPage(msg, HttpURLConnection.HTTP_INTERNAL_ERROR, message);
                return;
            } finally {
                closeInputStream(is);
            }
            String queryString = msg.getRequestURI().getQuery();
            logger.fine("Query String for request ="+queryString);

            String inPath = endpointInfo.getPath(queryString);
            if (inPath == null) {
                String message =
                    localizer.localize(
                        messageFactory.getMessage("html.notFound",
                            "Invalid Request ="+msg.getRequestURI()));
                writeErrorPage(msg, HttpURLConnection.HTTP_NOT_FOUND, message);
                return;
            }
            DocInfo docInfo = endpointInfo.getDocMetadata().get(inPath);
            if (docInfo == null) {
                String message =
                    localizer.localize(
                        messageFactory.getMessage("html.notFound",
                            "Invalid Request ="+msg.getRequestURI()));
                writeErrorPage(msg, HttpURLConnection.HTTP_NOT_FOUND, message);
                return;
            }
            
            OutputStream os = null;
            InputStream docStream = null;
            try {
                msg.getResponseHeaders().addHeader(CONTENT_TYPE_HEADER, XML_CONTENT_TYPE);
                msg.sendResponseHeaders(HttpURLConnection.HTTP_OK,  0);
                os = msg.getResponseBody();

                List<RuntimeEndpointInfo> endpoints = new ArrayList<RuntimeEndpointInfo>();
                endpoints.add(endpointInfo);

                String address =
                    "http"
                        + "://"
                        + msg.getLocalAddress().getHostName()
                        + ":"
                        + msg.getLocalAddress().getPort()
                        + msg.getRequestURI().getPath();
                logger.fine("Address ="+address);
                WSDLPatcher patcher = new WSDLPatcher(docInfo, address,
                        endpointInfo, endpoints);
                docStream = docInfo.getDoc();
                patcher.patchDoc(docStream, os);
            } catch(IOException ioe) {
                ioe.printStackTrace();
            } finally {
                closeInputStream(docStream);
                //closeOutputStream(os);
            }
        } finally {
            try {
                msg.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();          // Not much can be done
            }
        }
    }

    /*
     * writes error html page
     */
    private void writeErrorPage(HttpInteraction msg, int status, String message) {
        OutputStream outputStream = null;
        try {
            msg.getResponseHeaders().addHeader(CONTENT_TYPE_HEADER, HTML_CONTENT_TYPE);
            msg.sendResponseHeaders(status, 0);
            outputStream = msg.getResponseBody();
            PrintWriter out = new PrintWriter(outputStream);
            out.println("<html><head><title>");
            out.println(
                localizer.localize(
                    messageFactory.getMessage("html.title")));
            out.println("</title></head><body>");
            out.println(message);
            out.println("</body></html>");
            out.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            //closeOutputStream(outputStream);
        }
    }
    
    private static void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    private static void closeOutputStream(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
}
