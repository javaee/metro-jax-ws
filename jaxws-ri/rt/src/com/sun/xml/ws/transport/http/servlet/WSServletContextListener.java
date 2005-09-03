/*
 * $Id: WSServletContextListener.java,v 1.13 2005-09-03 02:10:34 jitu Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.servlet;
import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.WSDLPatcher;
import com.sun.xml.ws.server.DocInfo.DOC_TYPE;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sun.xml.ws.spi.runtime.WebServiceContext;

import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import com.sun.xml.ws.util.xml.XmlUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.xml.sax.EntityResolver;


/**
 * Parses sun-jaxws.xml and creates a <code>java.util.List</code> of 
 * RuntimeEndpointInfos. It also calls deploy() on each RuntimeEndpointInfo.
 *
 * @author WS Development Team
 */
public class WSServletContextListener
    implements ServletContextAttributeListener, ServletContextListener {

    /**
     * default contructor
     */
    public WSServletContextListener() {
        localizer = new Localizer();
        messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.wsservlet");
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
        if (logger.isLoggable(Level.INFO)) {
            logger.info(
                localizer.localize(
                    messageFactory.getMessage("listener.info.initialize")));
        }
        context = event.getServletContext();
        classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        try {
            // Get all the WSDL & schema documents under WEB-INF/wsdl directory
            Map<String, DocInfo> docs = new HashMap<String, DocInfo>();
            collectDocs(context, JAXWS_WSDL_DIR, docs);
            logger.fine("war metadata="+docs);

            // Parse the descriptor file and build endpoint infos
            RuntimeEndpointInfoParser parser =
                new RuntimeEndpointInfoParser(classLoader);
            InputStream is = context.getResourceAsStream(JAXWS_RI_RUNTIME);
            List<RuntimeEndpointInfo> endpoints = parser.parse(is);
            context.setAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO, endpoints);
            
            // Create WebServiceContext
            createWebServiceContext(endpoints);
            
            // Creates WSDL & schema metadata and runtime model
            createModelAndMetadata(endpoints, docs);

        } catch (Exception e) {
            logger.log(
                Level.SEVERE,
                localizer.localize(
                    messageFactory.getMessage(
                        "listener.parsingFailed",
                        e.toString())),
                e);
            context.removeAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO);
            throw new WSServletException("listener.parsingFailed", new Object[] {e});
        }
    }
    
    /*
     * Get all the WSDL & schema documents under WEB-INF/wsdl directory
     */
    private static void collectDocs(ServletContext context, String dirPath,
            Map<String, DocInfo> docs) throws MalformedURLException {
        Set paths = context.getResourcePaths(dirPath);
        if (paths != null) {
            Iterator i = paths.iterator();
            while(i.hasNext()) {
                String docPath = (String)i.next();
                if (docPath.endsWith("/")) {
                    collectDocs(context, docPath, docs);
                } else {
                    docs.put(context.getResource(docPath).toString(), new ServletDocInfo(context, docPath));
                }
            }
        }
    }

	private Map<String, DocInfo> copyDocs(Map<String, DocInfo> docs) {
		Map<String, DocInfo> newDocs = new HashMap<String, DocInfo>();
		Set<Entry<String, DocInfo>> entries = docs.entrySet();
		for(Entry<String, DocInfo> entry : entries) {
			String docPath = entry.getValue().getPath();
			newDocs.put(entry.getKey(), new ServletDocInfo(context, docPath));
		}
		return newDocs;
	}
    
    /*
     * Setting the WebServiceContext for each endpoint. WebServiceContextImpl
     * contains servlet specific code, hence the initialization is done here
     * (instead of doing in RuntimeEndpointInfoParser)
     */
    private void createWebServiceContext(List<RuntimeEndpointInfo> endpoints) {

        for(RuntimeEndpointInfo endpoint : endpoints) {
            WebServiceContext wsContext = new WebServiceContextImpl();
            endpoint.setWebServiceContext(wsContext);
        }
    }
    
    /*
     * updates metadata with query string and builds runtime model for each
     * endpoint
     */
    private void createModelAndMetadata(List<RuntimeEndpointInfo> endpoints,
            Map<String, DocInfo> docs)  throws Exception {
        URL catalogUrl = null;
        try {
            catalogUrl = context.getResource("/WEB-INF/jax-ws-catalog.xml");
        } catch(java.net.MalformedURLException e) {
            e.printStackTrace();
        }
        EntityResolver entityResolver = XmlUtil.createEntityResolver(catalogUrl);
                    
     
        for(RuntimeEndpointInfo endpoint : endpoints) {
            String wsdlFile = endpoint.getWSDLFileName();
            if (wsdlFile != null) {
                try {
                    wsdlFile = "/"+wsdlFile;
                    URL wsdlUrl = context.getResource(wsdlFile);                   
                    endpoint.setWsdlInfo(wsdlUrl, entityResolver);
                } catch(java.net.MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            
            endpoint.deploy();
            if (endpoint.needWSDLGeneration()) {
                endpoint.generateWSDL();
            } else {
                endpoint.setMetadata(copyDocs(docs));
				if (endpoint.getWsdLUrl() != null) {
					docs = endpoint.getDocMetadata();
					DocInfo wsdlDoc = docs.get(endpoint.getWsdLUrl().toString());
					if (wsdlDoc != null) {
						wsdlDoc.setQueryString("wsdl");
					}
				}
				RuntimeEndpointInfo.fillDocInfo(endpoint);
            }
			RuntimeEndpointInfo.publishWSDLDocs(endpoint);
            endpoint.updateQuery2DocInfo();
        }
    }

    private Localizer localizer;
    private LocalizableMessageFactory messageFactory;
    private ServletContext context;
    private ClassLoader classLoader;

    private static final String JAXWS_RI_RUNTIME = "/WEB-INF/sun-jaxws.xml";
    public static final String JAXWS_WSDL_DIR = "/WEB-INF/wsdl";
    public static final String JAXWS_WSDL_DD_DIR = "WEB-INF/wsdl";

    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
}
