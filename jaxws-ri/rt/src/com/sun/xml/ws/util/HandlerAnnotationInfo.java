/*
 * $Id: HandlerAnnotationInfo.java,v 1.1 2005-06-01 19:06:28 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.util;

import java.util.List;
import java.util.Set;

import javax.xml.ws.handler.Handler;

/**
 * Used to hold a list of handlers and a set of roles from an
 * annotated endpoint.
 */
public class HandlerAnnotationInfo {
    
    private List<Handler> handlers;
    private Set<String> roles;
    
    public List<Handler> getHandlers() {
        return handlers;
    }
    
    public void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }
    
    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
    
}
