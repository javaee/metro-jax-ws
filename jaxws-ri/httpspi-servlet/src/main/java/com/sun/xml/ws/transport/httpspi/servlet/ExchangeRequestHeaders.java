/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.transport.httpspi.servlet;


import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.spi.http.HttpExchange;
import java.util.*;

/**
 * {@link HttpExchange#getRequestHeaders} impl for servlet container.
 *
 * @author Jitendra Kotamraju
 */
class ExchangeRequestHeaders extends Headers {
    private final HttpServletRequest request;
    private boolean useMap = false;

    ExchangeRequestHeaders(HttpServletRequest request) {
        this.request = request;
    }

    private void convertToMap() {
        if (!useMap) {
            Enumeration e = request.getHeaderNames();
            while(e.hasMoreElements()) {
                String name = (String)e.nextElement();
                Enumeration ev = request.getHeaders(name);
                while(ev.hasMoreElements()) {
                    String value = (String)ev.nextElement();
                    super.add(name, value);
                }
            }
            useMap = true;
        }
    }

    @Override
    public int size() {
        convertToMap();
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        convertToMap();
        return super.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            return false;
        }
        return useMap ? super.containsKey(key) : request.getHeader((String)key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        convertToMap();
        return super.containsValue(value);
    }

    @Override
    public List<String> get(Object key) {
        convertToMap();
        return super.get(key);
    }

    @Override
    public String getFirst(String key) {
        return useMap ? super.getFirst(key) : request.getHeader(key);
    }

    @Override
    public List<String> put(String key, List<String> value) {
        convertToMap();
        return super.put(key, value);
    }

    @Override
    public void add(String key, String value) {
        convertToMap();
        super.add(key, value);
    }

    @Override
    public void set(String key, String value) {
        convertToMap();
        super.set(key, value);
    }
    @Override
    public List<String> remove(Object key) {
        convertToMap();
        return super.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> t) {
        convertToMap();
        super.putAll(t);
    }

    @Override
    public void clear() {
        convertToMap();
        super.clear();
    }

    @Override
    public Set<String> keySet() {
        convertToMap();
        return super.keySet();
    }

    @Override
    public Collection<List<String>> values() {
        convertToMap();
        return super.values();
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        convertToMap();
        return super.entrySet();
    }

    @Override
    public String toString() {
        convertToMap();
        return super.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
