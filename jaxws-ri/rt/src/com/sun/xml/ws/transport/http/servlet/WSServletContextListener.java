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
import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.ServerRtException;
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


            // Parse the descriptor file and build endpoint infos
            RuntimeEndpointInfoParser parser =
                new RuntimeEndpointInfoParser(classLoader);
            InputStream is = context.getResourceAsStream(JAXWS_RI_RUNTIME);
            List<RuntimeEndpointInfo> endpoints = parser.parse(is);
            context.setAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO, endpoints);
            
            // Create WebServiceContext
            createWebServiceContext(endpoints);
            
            // Creates WSDL & schema metadata and runtime model
            createModelAndMetadata(endpoints);

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
    private void createModelAndMetadata(List<RuntimeEndpointInfo> endpoints)  throws Exception {
        URL catalogUrl = null;
        try {
            catalogUrl = context.getResource("/WEB-INF/jax-ws-catalog.xml");
        } catch(java.net.MalformedURLException e) {
            e.printStackTrace();
        }
        EntityResolver entityResolver = XmlUtil.createEntityResolver(catalogUrl);
                    
        for(RuntimeEndpointInfo endpoint : endpoints) {
            // Get all the WSDL & schema documents under WEB-INF/wsdl directory
            Map<String, DocInfo> docs = new HashMap<String, DocInfo>();
            collectDocs(context, JAXWS_WSDL_DIR, docs);
            logger.fine("Endpoint metadata="+docs);
            
            String wsdlFile = endpoint.getWSDLFileName();
            if (wsdlFile != null) {
                try {
                    wsdlFile = "/"+wsdlFile;
                    URL wsdlUrl = context.getResource(wsdlFile);
                    if (wsdlUrl == null) {
                        throw new ServerRtException("cannot.load.wsdl", wsdlFile);
                    }
                    endpoint.setWsdlInfo(wsdlUrl, entityResolver);
                } catch(java.net.MalformedURLException e) {
                    throw new ServerRtException("cannot.load.wsdl", wsdlFile);
                }
            }
            
            endpoint.init();
            if (endpoint.needWSDLGeneration()) {
                endpoint.generateWSDL();
            } else {
                endpoint.setMetadata(docs);
				if (endpoint.getWsdlUrl() != null) {
					docs = endpoint.getDocMetadata();
					DocInfo wsdlDoc = docs.get(endpoint.getWsdlUrl().toString());
					if (wsdlDoc != null) {
						wsdlDoc.setQueryString("wsdl");
					}
				}
				RuntimeEndpointInfo.fillDocInfo(endpoint);
            }
			RuntimeEndpointInfo.publishWSDLDocs(endpoint);
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
