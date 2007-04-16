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

import com.sun.istack.Nullable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The JAX-WS dispatcher servlet.
 *
 * <p>
 * It really just forwards processing to {@link WSServletDelegate}.
 *
 * @author WS Development Team
 */
public class WSServlet extends HttpServlet {
    private WSServletDelegate delegate = null;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        delegate = getDelegate(servletConfig);
    }

    /**
     * Gets the {@link WSServletDelegate} that we will be forwarding the requests to.
     *
     * @return
     *      null if the deployment have failed and we don't have the delegate.
     */
    protected @Nullable WSServletDelegate getDelegate(ServletConfig servletConfig) {
        return (WSServletDelegate) servletConfig.getServletContext().getAttribute(JAXWS_RI_RUNTIME_INFO);
    }

    protected void doPost( HttpServletRequest request, HttpServletResponse response) throws ServletException {
        if (delegate != null) {
            delegate.doPost(request,response,getServletContext());
        }
    }

    protected void doGet( HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        if (delegate != null) {
            delegate.doGet(request,response,getServletContext());
        }
    }
    
    protected void doPut( HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        if (delegate != null) {
            delegate.doPut(request,response,getServletContext());
        }
    }
        
    protected void doDelete( HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        if (delegate != null) {
            delegate.doDelete(request,response,getServletContext());
        }
    }

    /**
     * {@link WSServletDelegate}.
     */
    public static final String JAXWS_RI_RUNTIME_INFO =
        "com.sun.xml.ws.server.http.servletDelegate";
    public static final String JAXWS_RI_PROPERTY_PUBLISH_WSDL =
        "com.sun.xml.ws.server.http.publishWSDL";
    public static final String JAXWS_RI_PROPERTY_PUBLISH_STATUS_PAGE =
        "com.sun.xml.ws.server.http.publishStatusPage";

}
