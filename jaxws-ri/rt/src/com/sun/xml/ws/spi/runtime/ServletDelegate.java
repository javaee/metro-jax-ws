/**
 * $Id: ServletDelegate.java,v 1.1 2005-05-23 22:54:49 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A delegate for the JAX-RPC dispatcher servlet.
 * <p>
 * This interface is implemented by
 * com.sun.xml.rpc.server.http.ServletDelegate
 *
 */
public interface ServletDelegate {
    public void init(ServletConfig servletConfig) throws ServletException;
    public void destroy();
    public void doPost(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException;
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException;
    public void registerEndpointUrlPattern(RuntimeEndpointInfo info);
    public void setSecondDelegate(ServletSecondDelegate delegate);
}
