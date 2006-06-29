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
package com.sun.xml.ws.server;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.ws.handler.MessageContext.Scope;

public class AppMsgContextImpl implements MessageContext {

    private MessageContext ctxt;
    private Map<String, Object> appContext; // properties in APPLICATION scope
    
    private void init() {
        if (appContext == null) {
            appContext = new HashMap<String, Object>();
            Iterator<Entry<String, Object>> i = ctxt.entrySet().iterator();
            while(i.hasNext()) {
                Entry<String, Object> entry = i.next();
                if (ctxt.getScope(entry.getKey()) == Scope.APPLICATION) {
                    appContext.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public AppMsgContextImpl(MessageContext ctxt) {
        this.ctxt = ctxt;
    }
    
    /* java.util.Map methods below here */
    
    public void clear() {
        init();
        Set<Entry<String, Object>> props = appContext.entrySet();
        for (Entry<String, Object> prop : props) {
            ctxt.remove(prop.getKey());
        }
        appContext.clear();
    }

    public boolean containsKey(Object obj) {
        init();
        return appContext.containsKey(obj);
    }

    public boolean containsValue(Object obj) {
        init();
        return appContext.containsValue(obj);
    }

    public Set<Entry<String, Object>> entrySet() {
        init();
        return appContext.entrySet();
    }

    public Object get(Object obj) {
        init();
        return appContext.get(obj);
    }

    public boolean isEmpty() {
        init();
        return appContext.isEmpty();
    }

    public Set<String> keySet() {
        init();
        return appContext.keySet();
    }

    public Object put(String str, Object obj) {
        init();
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
        init();
        Set<? extends Entry<? extends String, ? extends Object>> props = map.entrySet();
        for(Entry<? extends String, ? extends Object> prop : props) {
            put(prop.getKey(), prop.getValue());
        }
    }

    public Object remove(Object key) {
        init();
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
        init();
        return appContext.size();
    }

    public Collection<Object> values() {
        init();
        return appContext.values();
    }
    
    public void setScope(String name, Scope scope) {

    }

    public Scope getScope(String name) {
        return null;
    }

}
