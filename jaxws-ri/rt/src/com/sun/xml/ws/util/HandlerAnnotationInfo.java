/*
 * $Id: HandlerAnnotationInfo.java,v 1.3 2005-06-24 18:04:34 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.util;

import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.xml.ws.handler.Handler;

/**
 * Used to hold a list of handlers and a set of roles from an
 * annotated endpoint.
 *
 * @author JAX-WS Development Team
 */
public class HandlerAnnotationInfo {
    
    private List<Handler> handlers;
    private Set<URI> roles;
    
    public List<Handler> getHandlers() {
        return handlers;
    }
    
    public void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }
    
    public Set<URI> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<URI> roles) {
        this.roles = roles;
    }
    
}
