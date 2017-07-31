/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.transport.httpspi.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.ws.WebServiceException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Parses {@code sun-jaxws.xml} and sets up
 * {@link com.sun.xml.ws.transport.httpspi.servlet.EndpointAdapter}s for all deployed endpoints.
 *
 * <p>
 * This code is the entry point at the server side in the servlet deployment.
 * The user application writes this in their <tt>web.xml</tt> so that we can
 * start when the container starts the webapp.
 *
 * @author Jitendra Kotamraju
 */
public final class WSSPIContextListener
    implements ServletContextAttributeListener, ServletContextListener {

    private WSServletDelegate delegate;

    public void attributeAdded(ServletContextAttributeEvent event) {
    }

    public void attributeRemoved(ServletContextAttributeEvent event) {
    }

    public void attributeReplaced(ServletContextAttributeEvent event) {
    }

    public void contextDestroyed(ServletContextEvent event) {
        if (delegate != null) { // the deployment might have failed.
            delegate.destroy();
        }

        if (logger.isLoggable(Level.INFO)) {
            logger.info("JAX-WS context listener destroyed");
        }
    }

    public void contextInitialized(ServletContextEvent event) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("JAX-WS context listener initializing");
        }
        ServletContext context = event.getServletContext();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        try {
            // Parse the descriptor file and build endpoint infos
            DeploymentDescriptorParser<EndpointAdapter> parser = new DeploymentDescriptorParser<EndpointAdapter>(
                classLoader, (ResourceLoader) new ServletResourceLoader(context), new EndpointAdapterFactory() );
            URL sunJaxWsXml = context.getResource(JAXWS_RI_RUNTIME);
            if(sunJaxWsXml==null)
                throw new WebServiceException("Runtime descriptor "+JAXWS_RI_RUNTIME+" is mising");
            List<EndpointAdapter> adapters = parser.parse(sunJaxWsXml.toExternalForm(), sunJaxWsXml.openStream());
            for(EndpointAdapter adapter : adapters) {
                adapter.publish();
            }

            delegate = createDelegate(adapters, context);

            context.setAttribute(WSSPIServlet.JAXWS_RI_RUNTIME_INFO,delegate);

        } catch (Throwable e) {
            logger.log(Level.SEVERE, "failed to parse runtime descriptor", e);
            context.removeAttribute(WSSPIServlet.JAXWS_RI_RUNTIME_INFO);
            throw new WebServiceException("failed to parse runtime descriptor", e);
        }
    }

    /**
     * Creates {@link com.sun.xml.ws.transport.httpspi.servlet.WSServletDelegate} that does the real work.
     */
    protected WSServletDelegate createDelegate(List<EndpointAdapter> adapters, ServletContext context) {
        return new WSServletDelegate(adapters,context);
    }

    private static final String JAXWS_RI_RUNTIME = "/WEB-INF/sun-jaxws.xml";

    private static final Logger logger =
        Logger.getLogger(WSSPIContextListener.class.getName());

}
