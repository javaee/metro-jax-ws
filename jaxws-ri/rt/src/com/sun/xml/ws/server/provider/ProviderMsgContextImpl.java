/**
 * $Id: ProviderMsgContextImpl.java,v 1.4 2005-08-05 01:03:32 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server.provider;

import com.sun.xml.ws.handler.HandlerContext;
import com.sun.xml.ws.handler.MessageContextImpl;
import com.sun.xml.ws.spi.runtime.MessageContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.ws.handler.MessageContext.Scope;

public class ProviderMsgContextImpl extends MessageContextImpl {

    private HandlerContext handlerCtxt;
    private MessageContext ctxt;
    private Map<String, Object> appContext; // properties in APPLICATION scope

    public ProviderMsgContextImpl(HandlerContext handlerCtxt) {
        this.handlerCtxt = handlerCtxt;
        ctxt = handlerCtxt.getMessageContext();        
        appContext = new HashMap<String, Object>();
        
        Iterator<Entry<String, Object>> i = ctxt.entrySet().iterator();
        while(i.hasNext()) {
            Entry<String, Object> entry = i.next();
            if (ctxt.getScope(entry.getKey()) == Scope.APPLICATION) {
                appContext.put(entry.getKey(), entry.getValue());
            }
        }        
    }
    
    /* java.util.Map methods below here */
    
    public void clear() {
        Set<Entry<String, Object>> props = appContext.entrySet();
        for (Entry<String, Object> prop : props) {
            ctxt.remove(prop.getKey());
        }
        appContext.clear();
    }

    public boolean containsKey(Object obj) {
        return appContext.containsKey(obj);
    }

    public boolean containsValue(Object obj) {
        return appContext.containsValue(obj);
    }

    public Set<Entry<String, Object>> entrySet() {
        return appContext.entrySet();
    }

    public Object get(Object obj) {
        return appContext.get(obj);
    }

    public boolean isEmpty() {
        return appContext.isEmpty();
    }

    public Set<String> keySet() {
        return appContext.keySet();
    }

    public Object put(String str, Object obj) {
        Scope scope = null;
        try {
            scope = ctxt.getScope(str);
        } catch(IllegalArgumentException ie) {
            // It's okay, MessageContext didn't have this property
        }
        if (scope != null && scope == Scope.HANDLER) {
            throw new IllegalArgumentException(
                    "Cannot overwrite property in HANDLER scope");
        }
        ctxt.put(str, obj);
        ctxt.setScope(str, Scope.APPLICATION);
        return appContext.put(str, obj);
    }

    public void putAll(Map<? extends String, ? extends Object> map) {
        Set<? extends Entry<? extends String, ? extends Object>> props = map.entrySet();
        for(Entry<? extends String, ? extends Object> prop : props) {
            put(prop.getKey(), prop.getValue());
        }
    }

    public Object remove(Object key) {
        Scope scope = null;
        try {
            scope = ctxt.getScope((String)key);
        } catch(IllegalArgumentException ie) {
            // It's okay, MessageContext didn't have this property
        }
        if (scope != null && scope == Scope.HANDLER) {
            throw new IllegalArgumentException(
                    "Cannot remove property in HANDLER scope");
        }
        ctxt.remove(key);
        return appContext.remove(key);
    }

    public int size() {
        return appContext.size();
    }

    public Collection<Object> values() {
        return appContext.values();
    }

}
