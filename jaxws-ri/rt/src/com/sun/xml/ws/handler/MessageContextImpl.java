/*
 * $Id: MessageContextImpl.java,v 1.1 2005-05-23 22:37:25 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.handler;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

/**
 * @author JAX-WS RI Development Team
 */
public class MessageContextImpl extends HashMap<String, Object>
    implements MessageContext {
    
    private HashMap<String, Scope> propertyScopes =
        new HashMap<String, Scope>();

    // todo: check property names
    public void setScope(String name, Scope scope) {
        propertyScopes.put(name, scope);
    }

    public Scope getScope(String name) {
        if (!this.containsKey(name)) {
            throw new IllegalArgumentException("todo: text");
        }
        Scope scope = propertyScopes.get(name);
        if (scope == null) {
            scope = Scope.HANDLER; // the default
        }
        return scope;
    }

}
