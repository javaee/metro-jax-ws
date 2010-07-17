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
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Module;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WebModule;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link HttpAdapter} for servlets.
 *
 * <p>
 * This is a thin wrapper around {@link HttpAdapter} with some description
 * specified in the deployment (in particular those information are related
 * to how a request is routed to a {@link ServletAdapter}.
 *
 * <p>
 * This class implements {@link BoundEndpoint} and represent the
 * servlet-{@link WSEndpoint} association for {@link }
 *
 */
public final class ServletAdapter extends HttpAdapter implements BoundEndpoint {
    final String name;

    protected ServletAdapter(String name, String urlPattern, WSEndpoint endpoint, ServletAdapterList owner) {
        super(endpoint, owner, urlPattern);
        this.name = name;
        // registers itself with the container
        Module module = endpoint.getContainer().getSPI(Module.class);
        if(module==null)
            LOGGER.warning("Container "+endpoint.getContainer()+" doesn't support "+Module.class);
        else {
            module.getBoundEndpoints().add(this);
        }

    }

    public ServletContext getServletContext() {
        return ((ServletAdapterList)owner).getServletContext();
    }

    /**
     * Gets the name of the endpoint as given in the <tt>sun-jaxws.xml</tt>
     * deployment descriptor.
     */
    public String getName() {
        return name;
    }


    @NotNull
    public URI getAddress() {
        WebModule webModule = endpoint.getContainer().getSPI(WebModule.class);
        if(webModule==null)
            // this is really a bug in the container implementation
            throw new WebServiceException("Container "+endpoint.getContainer()+" doesn't support "+WebModule.class);

        return getAddress(webModule.getContextPath());
    }

    public @NotNull URI getAddress(String baseAddress) {
        String adrs = baseAddress+getValidPath();
        try {
            return new URI(adrs);
        } catch (URISyntaxException e) {
            // this is really a bug in the container implementation
            throw new WebServiceException("Unable to compute address for "+endpoint,e);
        }
    }

    /**
     * Convenient method to return a port name from {@link WSEndpoint}.
     *
     * @return
     *      null if {@link WSEndpoint} isn't tied to any paritcular port.
     */
    public QName getPortName() {
        WSDLPort port = getEndpoint().getPort();
        if(port==null)  return null;
        else            return port.getName();
    }

    /**
     * Version of {@link #handle(WSHTTPConnection)}
     * that takes convenient parameters for servlet.
     *
     * @param context Servlet Context
     * @param request Servlet Request
     * @param response Servlet Response
     * @throws IOException when there is i/o error in handling request
     */
    public void handle(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws IOException {
        WSHTTPConnection connection = new ServletConnectionImpl(this,context,request,response);
        super.handle(connection);
    }

    /**
     * Version of {@link #handle(WSHTTPConnection)}  that takes convenient parameters for servlet.
     *
     * Based on the async capabilities of the request and the application processing it, the method may run in asynchronous mode.
     * When run in async mode, this method returns immediately. The response is delayed until the application is ready with the response or
     *  the corresponding asynchronous operation times out. The CompletionCallback is guaranteed to run after response is committed..
     *
     * @param context Servlet Context
     * @param request Servlet Request
     * @param response Servlet Response
     * @param callback CompletionCallback
     * @throws IOException when there is i/o error in handling request
     */
    public void invokeAsync(ServletContext context, HttpServletRequest request, HttpServletResponse response, final CompletionCallback callback) throws IOException {
        boolean asyncStarted = false;
        try {
            WSHTTPConnection connection = new ServletConnectionImpl(this, context, request, response);
            if (handleGet(connection)) {
                return;
            }

            boolean asyncRequest = false;
            try {
                asyncRequest = isServlet30Based && request.isAsyncSupported() && !request.isAsyncStarted();
            } catch (Throwable t) {
                //this happens when the loaded Servlet API is 3.0, but the impl is not, ending up as AbstractMethodError
                LOGGER.log(Level.INFO, request.getClass().getName() + " does not support Async API, Continuing with synchronous processing", t);
                //Continue with synchronous processing and don't repeat the check for processing further requests
                isServlet30Based = false;
            }

            if (asyncRequest) {
                final javax.servlet.AsyncContext asyncContext = request.startAsync(request, response);
                new WSAsyncListener(connection, callback).addListenerTo(asyncContext);
                //asyncContext.setTimeout(10000L);// TODO get it from @ or config file
                super.invokeAsync(connection, new CompletionCallback() {
                    public void onCompletion() {
                        asyncContext.complete();
                    }
                });
                asyncStarted = true;
            } else {
                super.handle(connection);
            }
        } finally {
            if (!asyncStarted) {
                callback.onCompletion();
            }
        }
    }

    /**
     * @param context Servlet Context
     * @param request Servlet Request
     * @param response Servlet Response
     * @throws IOException when there is i/o error in handling request
     *
     * @deprecated
     *      Use {@link #handle(ServletContext, HttpServletRequest, HttpServletResponse)}
     */
    public void publishWSDL(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws IOException {
        WSHTTPConnection connection = new ServletConnectionImpl(this,context,request,response);
        super.handle(connection);
    }

    public String toString() {
        return super.toString()+"[name="+name+']';
    }

    private static final Logger LOGGER = Logger.getLogger(ServletAdapter.class.getName());

    private boolean isServlet30Based = ServletUtil.isServlet30Based();

}
