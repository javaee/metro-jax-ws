/*
 * $Id: JAXRPCServlet.java,v 1.1 2005-05-23 23:01:39 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.servlet;

import com.sun.xml.ws.spi.runtime.ServletDelegate;
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
 * The JAX-RPC dispatcher servlet.
 *
 * @author JAX-RPC Development Team
 */
public class JAXRPCServlet extends HttpServlet {

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        localizer = new Localizer();
        messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.jaxrpcservlet");

        try {
            String delegateClassName =
                servletConfig.getInitParameter(DELEGATE_PROPERTY);

            if (delegateClassName == null
                && servletConfig.getInitParameter(EA_CONFIG_FILE_PROPERTY)
                    != null) {
                // use EA backward compatibility mode
                delegateClassName = EA_DELEGATE_CLASS_NAME;
            }

            if (delegateClassName == null) {
                delegateClassName = DEFAULT_DELEGATE_CLASS_NAME;
            }

            Class delegateClass =
                Class.forName(
                    delegateClassName,
                    true,
                    Thread.currentThread().getContextClassLoader());
            delegate = (ServletDelegate) delegateClass.newInstance();
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

    protected ServletDelegate delegate = null;
    private LocalizableMessageFactory messageFactory;
    private Localizer localizer;

    private static final String DELEGATE_PROPERTY = "delegate";
    private static final String DEFAULT_DELEGATE_CLASS_NAME =
        "com.sun.xml.ws.transport.http.servlet.JAXRPCServletDelegate";

    private static final String EA_CONFIG_FILE_PROPERTY = "configuration.file";
    private static final String EA_DELEGATE_CLASS_NAME =
        "com.sun.xml.rpc.server.http.ea.JAXRPCServletDelegate";

    public static final String JAXRPC_RI_RUNTIME_INFO =
        "com.sun.xml.rpc.server.http.info";
    public static final String JAXRPC_RI_PROPERTY_PUBLISH_WSDL =
        "com.sun.xml.rpc.server.http.publishWSDL";
    public static final String JAXRPC_RI_PROPERTY_PUBLISH_MODEL =
        "com.sun.xml.rpc.server.http.publishModel";
    public static final String JAXRPC_RI_PROPERTY_PUBLISH_STATUS_PAGE =
        "com.sun.xml.rpc.server.http.publishStatusPage";

    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
}
