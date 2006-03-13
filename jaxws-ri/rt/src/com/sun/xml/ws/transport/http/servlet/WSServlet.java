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

package com.sun.xml.ws.transport.http.servlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;

/**
 * The JAX-WS dispatcher servlet.
 *
 * @author WS Development Team
 */
public class WSServlet extends HttpServlet {

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        localizer = new Localizer();
        messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.wsservlet");

        try {
            delegate = new WSServletDelegate();
            delegate.init(servletConfig);
        } catch (ServletException e) {
            logger.log(Level.SEVERE,e.getMessage(), e);
            throw e;
        } catch (Throwable e) {
            String message =
                localizer.localize(
                    messageFactory.getMessage(
                        "error.servlet.caughtThrowableInInit",
                        new Object[] { e }));
            logger.log(Level.SEVERE, message, e);
            throw new ServletException(message);
        }
    }

    public void destroy() {
        if (delegate != null) {
            delegate.destroy();
        }
    }

    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException {
        if (delegate != null) {
            delegate.doPost(request, response);
        }
    }

    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException {
        if (delegate != null) {
            delegate.doGet(request, response);
        }
    }

    protected void doPut(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException {
        if (delegate != null) {
            delegate.doPut(request, response);
        }
    }
    
    protected void doDelete(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException {
        if (delegate != null) {
            delegate.doDelete(request, response);
        }
    }


    
    protected WSServletDelegate delegate = null;
    private LocalizableMessageFactory messageFactory;
    private Localizer localizer;

    public static final String JAXWS_RI_RUNTIME_INFO =
        "com.sun.xml.ws.server.http.info";
    public static final String JAXWS_RI_PROPERTY_PUBLISH_WSDL =
        "com.sun.xml.ws.server.http.publishWSDL";
    public static final String JAXWS_RI_PROPERTY_PUBLISH_STATUS_PAGE =
        "com.sun.xml.ws.server.http.publishStatusPage";

    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
}
