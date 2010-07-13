/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.xml.ws.transport.http.servlet;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.resources.WsservletMessages;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser;
import com.sun.xml.ws.transport.http.HttpAdapter;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import javax.xml.ws.WebServiceException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Parses {@code sun-jaxws.xml} and sets up
 * {@link HttpAdapter}s for all deployed endpoints.
 *
 * <p>
 * This code is the entry point at the server side in the servlet deployment.
 * The user application writes this in their <tt>web.xml</tt> so that we can
 * start when the container starts the webapp.
 *
 * @author WS Development Team
 */
@WebListener
public final class WSServletContextListener
    implements ServletContextAttributeListener, ServletContextListener {

    private WSServletDelegate delegate;
    private List<ServletAdapter> adapters;
    private final JAXWSRIDeploymentProbeProvider probe = new JAXWSRIDeploymentProbeProvider();

    private static final String WSSERVLET_CONTEXT_LISTENER_INVOKED="com.sun.xml.ws.transport.http.servlet.WSServletContextListener.Invoked";

    //if configured in web.xml,, then sun-jaxws.xml must be bundled.
    private final boolean explicitlyConfigured;

    public WSServletContextListener(ServletContext context) {
        this.explicitlyConfigured = false;
        parseAdaptersAndCreateDelegate(context);
    }

    public WSServletContextListener() {
        this.explicitlyConfigured = true;
    }

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

        if (adapters != null) {

            for(ServletAdapter a : adapters) {
                try {
                    a.getEndpoint().dispose();
                } catch(Throwable e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }

                // Emit undeployment probe event for each endpoint
                probe.undeploy(a);
            }
        }

        if (logger.isLoggable(Level.INFO)) {
            logger.info(WsservletMessages.LISTENER_INFO_DESTROY());
        }
    }

    void parseAdaptersAndCreateDelegate(ServletContext context){
        //The same class can be invoked via @WebListener discovery or explicit configuration in deployment descriptor
        // avoid redoing the processing of web services.
        String alreadyInvoked = (String) context.getAttribute(WSSERVLET_CONTEXT_LISTENER_INVOKED);
        if(Boolean.valueOf(alreadyInvoked)) {
            return;
        }
        context.setAttribute(WSSERVLET_CONTEXT_LISTENER_INVOKED, "true");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        try {
            URL sunJaxWsXml = context.getResource(JAXWS_RI_RUNTIME);
            if(sunJaxWsXml==null) {
                if(explicitlyConfigured)  {
                    throw new WebServiceException(WsservletMessages.NO_SUNJAXWS_XML(JAXWS_RI_RUNTIME));
                } else {
                    //TODO process @WebService without DD, utilizing servlet 3.0 capabilities
                    //for now, let 109 runtime process the classes with @WebService
                    return;
                }
            }


            // Parse the descriptor file and build endpoint infos
            DeploymentDescriptorParser<ServletAdapter> parser = new DeploymentDescriptorParser<ServletAdapter>(
                classLoader,new ServletResourceLoader(context), createContainer(context), new ServletAdapterList(context));
            adapters = parser.parse(sunJaxWsXml.toExternalForm(), sunJaxWsXml.openStream());
            registerWSServlet(adapters, context);
            delegate = createDelegate(adapters, context);

            context.setAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO,delegate);

        } catch (Throwable e) {
            logger.log(Level.SEVERE,
                WsservletMessages.LISTENER_PARSING_FAILED(e),e);
            context.removeAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO);
            throw new WSServletException("listener.parsingFailed", e);
        }

    }

    public void contextInitialized(ServletContextEvent event) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(WsservletMessages.LISTENER_INFO_INITIALIZE());
        }
        ServletContext context = event.getServletContext();

        parseAdaptersAndCreateDelegate(context);
        if(adapters != null)  {
            // Emit deployment probe event for each endpoint
            for (ServletAdapter adapter : adapters) {
                probe.deploy(adapter);
            }
        }
    }

    private void registerWSServlet(List<ServletAdapter> adapters, ServletContext context) {
        if ( !ServletUtil.isServlet30Based())
            return;
        Set<String> unregisteredUrlPatterns = new HashSet<String>();
        try {
            Collection<? extends ServletRegistration> registrations = context.getServletRegistrations().values();
            for (ServletAdapter adapter : adapters) {
                if (!existsServletForUrlPattern(adapter.urlPattern, registrations)) {
                    unregisteredUrlPatterns.add(adapter.urlPattern);
                }
            }
            if (!unregisteredUrlPatterns.isEmpty()) {
                //register WSServlet Dynamically
                ServletRegistration.Dynamic registration = context.addServlet("Dynamic JAXWS Servlet", WSServlet.class);
                registration.addMapping(unregisteredUrlPatterns.toArray(new String[]{}));
                registration.setAsyncSupported(true);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean existsServletForUrlPattern(String urlpattern, Collection<? extends ServletRegistration> registrations) {
        for (ServletRegistration r : registrations) {
            if (r.getMappings().contains(urlpattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates {@link Container} implementation that hosts the JAX-WS endpoint.
     */
    protected @NotNull Container createContainer(ServletContext context) {
        return new ServletContainer(context);
    }

    /**
     * Creates {@link WSServletDelegate} that does the real work.
     */
    protected @NotNull WSServletDelegate createDelegate(List<ServletAdapter> adapters, ServletContext context) {
        return new WSServletDelegate(adapters,context);
    }

    private static final String JAXWS_RI_RUNTIME = "/WEB-INF/sun-jaxws.xml";

    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
}
