/*
 * $Id: MessageContextImpl.java,v 1.2 2005-07-15 02:09:03 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.handler;

import java.util.HashMap;

import com.sun.xml.ws.spi.runtime.MessageContext;
import java.lang.reflect.Method;
import javax.xml.ws.handler.MessageContext.Scope;

/**
 * @author JAX-WS RI Development Team
 */
public class MessageContextImpl extends HashMap<String, Object>
    implements MessageContext {
    
    private Method method;
    
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
    
    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
