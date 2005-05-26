/*
 * $Id: JAXRPCContextListener.java,v 1.2 2005-05-26 18:21:17 jitu Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.servlet;
import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.WSDLPatcher;
import com.sun.xml.ws.server.WSDLPatcher.DOC_TYPE;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * @author JAX-RPC Development Team
 */
public class JAXRPCContextListener
    implements ServletContextAttributeListener, ServletContextListener {

    public JAXRPCContextListener() {
        localizer = new Localizer();
        messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.jaxrpcservlet");
    }

    public void attributeAdded(ServletContextAttributeEvent event) {
    }

    public void attributeRemoved(ServletContextAttributeEvent event) {
    }

    public void attributeReplaced(ServletContextAttributeEvent event) {
    }

    public void contextDestroyed(ServletContextEvent event) {
        context = null;
        if (logger.isLoggable(Level.INFO)) {
            logger.info(
                localizer.localize(
                    messageFactory.getMessage("listener.info.destroy")));
        }
    }

    public void contextInitialized(ServletContextEvent event) {
        context = event.getServletContext();

        if (logger.isLoggable(Level.INFO)) {
            logger.info(
                localizer.localize(
                    messageFactory.getMessage("listener.info.initialize")));
        }

        classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }

        try {
            // Get all the WSDL & schema documents under WEB-INF/wsdl directory
            Map<String, DocInfo> docs = new HashMap<String, DocInfo>();
            collectDocs(context, "/WEB-INF/wsdl", docs);

            
            JAXRPCRuntimeInfoParser parser =
                new JAXRPCRuntimeInfoParser(classLoader);
            InputStream is = context.getResourceAsStream(JAXRPC_RI_RUNTIME);
            List<RuntimeEndpointInfo> endpoints = parser.parse(is);
            context.setAttribute(JAXRPCServlet.JAXRPC_RI_RUNTIME_INFO, endpoints);
            for(RuntimeEndpointInfo endpoint : endpoints) {
                
                // Set queryString for the document
                Set<Entry<String, DocInfo>> entries = docs.entrySet();
                for(Entry<String, DocInfo> entry : entries) {
                    ServletDocInfo docInfo = (ServletDocInfo)entry.getValue();
                    String path = docInfo.getPath();
                    String query = null;
                    System.out.println("*** Path ="+docInfo.getPath());
                    String queryValue = docInfo.getPath().substring(14);    // Without /WEB-INF/wsdl
                    InputStream in = docInfo.getDoc();
                    DOC_TYPE docType = WSDLPatcher.getDocType(docInfo.getDoc());
                    switch(docType) {
                        case WSDL :
                            if (path.equals(endpoint.getWSDLFileName())) {
                                query = "wsdl";
                            } else {
                                query = "wsdl=" + queryValue;
                            }
                            break;
                        case SCHEMA : 
                            query = "xsd=" + queryValue;
                            break;
                    }
                    docInfo.setQueryString(query);
                    in.close();
                }
                
                endpoint.setMetadata(docs);
                endpoint.deploy();
            }
            
        } catch (JAXRPCServletException e) {
            logger.log(
                Level.SEVERE,
                localizer.localize(
                    messageFactory.getMessage("listener.parsingFailed", e)),
                e);
        } catch (Exception e) {
            logger.log(
                Level.SEVERE,
                localizer.localize(
                    messageFactory.getMessage(
                        "listener.parsingFailed",
                        e.toString())),
                e);
        }
    }
    
    private static void collectDocs(ServletContext context, String dirPath,
            Map<String, DocInfo> docs) {
        Set paths = context.getResourcePaths(dirPath);
        if (paths != null) {
            Iterator i = paths.iterator();
            while(i.hasNext()) {
                String docPath = (String)i.next();
                if (docPath.endsWith("/")) {
                    collectDocs(context, docPath, docs);
                } else {
                    docs.put(docPath, new ServletDocInfo(context, docPath));
                }
            }
        }
    }

    private Localizer localizer;
    private LocalizableMessageFactory messageFactory;
    private ServletContext context;
    private ClassLoader classLoader;

    private static final String JAXRPC_RI_RUNTIME =
        "/WEB-INF/sun-jaxws.xml";

    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
}
