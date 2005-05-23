/**
 * $Id: ServletSecondDelegate.java,v 1.1 2005-05-23 22:54:50 bbissett Exp $
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
 * This is the delegate of the ServletDelegate, which allows some
 * implementation of the ServletDelegate to be overwritten.  Though
 * it screams for a better name.  ServletDelegateDelegate??
 * <p>
 * S1AS will extend this class provide its implementation of
 * the ServletDelegate behavior.
 */
public abstract class ServletSecondDelegate {

    public ServletSecondDelegate() {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        //no op
    }

    /**
     * This method should be called after ServletDelegate.init()
     * is done.  Any initialization needed by the second delegate
     * should be done by overriding this method, i.e. the implementation
     * of ServletDelegate should call _secondDelegate.postInit()
     * at the end of its init() call.
     * @see ServletDelegate
     */
    public void postInit(ServletConfig config) throws ServletException {
        //no op
    }

    public void warnMissingContextInformation() {
        // context info not used within j2ee integration, so override
        // this method to prevent warning message
    }

}
