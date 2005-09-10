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
package com.sun.xml.ws.handler;

import java.util.HashMap;

import com.sun.xml.ws.spi.runtime.MessageContext;
import java.lang.reflect.Method;
import java.util.Map;
import javax.xml.ws.handler.MessageContext.Scope;

/**
 * Implementation of MessageContext. This class holds properties as
 * well as keeping track of their scope.
 *
 * @author WS Development Team
 */
public class MessageContextImpl extends HashMap<String, Object>
    implements MessageContext {

    // implementation specific properties
    private Map<String, Object> internalMap = new HashMap<String, Object>();
    
    private HashMap<String, Scope> propertyScopes =
        new HashMap<String, Scope>();

    // todo: check property names
    public void setScope(String name, Scope scope) {
        propertyScopes.put(name, scope);
    }
    
    // No public access
    Map<String, Object> getInternalMap() {
        return internalMap;
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
