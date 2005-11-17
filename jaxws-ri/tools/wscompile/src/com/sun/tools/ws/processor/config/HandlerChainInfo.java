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

package com.sun.tools.ws.processor.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author WS Development Team
 */
public class HandlerChainInfo {

    public HandlerChainInfo() {
        handlers = new ArrayList();
        roles = new HashSet();
    }

    public void add(HandlerInfo i) {
        handlers.add(i);
    }

    public Iterator getHandlers() {
        return handlers.iterator();
    }

    public int getHandlersCount() {
        return handlers.size();
    }

    /* serialization */
    public List getHandlersList() {
        return handlers;
    }

    /* serialization */
    public void setHandlersList(List l) {
        handlers = l;
    }

    public void addRole(String s) {
        roles.add(s);
    }

    public Set getRoles() {
        return roles;
    }

    /* serialization */
    public void setRoles(Set s) {
        roles = s;
    }

    private List handlers;
    private Set roles;
}
