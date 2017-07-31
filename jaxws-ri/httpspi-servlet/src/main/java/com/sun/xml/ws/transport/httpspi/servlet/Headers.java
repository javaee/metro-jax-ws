/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HTTP request and response headers are represented by this class which implements
 * the interface {@link java.util.Map}&lt;
 * {@link String},{@link java.util.List}&lt;{@link String}&gt;&gt;.
 * The keys are case-insensitive Strings representing the header names and
 * the value associated with each key is a {@link java.util.List}&lt;{@link String}&gt; with one
 * element for each occurence of the header name in the request or response.
 * <p>
 * For example, if a response header instance contains one key "HeaderName" with two values "value1 and value2"
 * then this object is output as two header lines:
 * <blockquote><pre>
 * HeaderName: value1
 * HeaderName: value2
 * </blockquote></pre>
 * <p>
 * All the normal {@link java.util.Map} methods are provided, but the following
 * additional convenience methods are most likely to be used:
 * <ul>
 * <li>{@link #getFirst(String)} returns a single valued header or the first value of
 * a multi-valued header.</li>
 * <li>{@link #add(String,String)} adds the given header value to the list for the given key</li>
 * <li>{@link #set(String,String)} sets the given header field to the single value given
 * overwriting any existing values in the value list.
 * </ul><p>
 * All methods in this class accept <code>null</code> values for keys and values. However, null
 * keys will never will be present in HTTP request headers, and will not be output/sent in response headers.
 * Null values can be represented as either a null entry for the key (i.e. the list is null) or
 * where the key has a list, but one (or more) of the list's values is null. Null values are output
 * as a header line containing the key but no associated value.
 * @since 1.6
 */
public class Headers implements Map<String,List<String>> {

    HashMap<String,List<String>> map;

    public Headers() {
        map = new HashMap<String,List<String>>(32);
    }

    /* Normalize the key by converting to following form.
     * First char upper case, rest lower case.
     * key is presumed to be ASCII
     */
    private String normalize (String key) {
        if (key == null) {
            return null;
        }
        int len = key.length();
        if (len == 0) {
            return key;
        }
        char[] b;
        String s;
        b = key.toCharArray();
        if (b[0] >= 'a' && b[0] <= 'z') {
            b[0] = (char) (b[0] - ('a' - 'A'));
        }
        for (int i = 1; i < len; i++) {
            if (b[i] >= 'A' && b[i] <= 'Z') {
                b[i] = (char) (b[i] + ('a' - 'A'));
            }
        }
        s = new String(b);
        return s;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) {
            return false;
        }
        if (!(key instanceof String)) {
            return false;
        }
        return map.containsKey (normalize((String)key));
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public List<String> get(Object key) {
        return map.get(normalize((String)key));
    }

    /**
     * returns the first value from the List of String values
     * for the given key (if at least one exists).
     * @param key the key to search for
     * @return the first string value associated with the key
     */
    public String getFirst (String key) {
        List<String> l = map.get(normalize((String)key));
        if (l == null) {
            return null;
        }
        return l.get(0);
    }

    @Override
    public List<String> put(String key, List<String> value) {
        return map.put (normalize(key), value);
    }

    /**
     * adds the given value to the list of headers
     * for the given key. If the mapping does not
     * already exist, then it is created
     * @param key the header name
     * @param value the header value to add to the header
     */
    public void add (String key, String value) {
        String k = normalize(key);
        List<String> l = map.get(k);
        if (l == null) {
            l = new LinkedList<String>();
            map.put(k,l);
        }
        l.add (value);
    }

    /**
     * sets the given value as the sole header value
     * for the given key. If the mapping does not
     * already exist, then it is created
     * @param key the header name
     * @param value the header value to set.
     */
    public void set (String key, String value) {
        LinkedList<String> l = new LinkedList<String>();
        l.add (value);
        put (key, l);
    }


    @Override
    public List<String> remove(Object key) {
        return map.remove(normalize((String)key));
    }

    @Override
    public void putAll(Map<? extends String,? extends List<String>> t)  {
        for(Entry<? extends String, ? extends List<String>> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<List<String>> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
