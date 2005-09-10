/*
 * $Id: StructMap.java,v 1.3 2005-09-10 19:48:13 kohsuke Exp $
 */

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

package com.sun.xml.ws.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p> A Map that keeps track of the order in which entries are made. The
 * <code>values()</code> method returns an unmodifiable List of the values
 * in the order in which they were added. A new method,
 * <code>keys()</code> has been added. It returns an unmodifiable List of the
 * keys in the order in which they were added. </p>
 *
 * @author WS Development Team
 */
public class StructMap implements Map {
    protected HashMap map = new HashMap();
    protected ArrayList keys = new ArrayList();
    protected ArrayList values = new ArrayList();

    public int size() {
        return map.size();
    }
    public boolean isEmpty() {
        return map.isEmpty();
    }
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }
    public Object get(Object key) {
        return map.get(key);
    }
    public Object put(Object key, Object value) {
        keys.add(key);
        values.add(value);
        return map.put(key, value);
    }
    public Object remove(Object key) {
        Object value = map.get(key);
        keys.remove(key);
        values.remove(value);
        return map.remove(key);
    }
    public void putAll(Map t) {
        if (!(t instanceof StructMap))
            throw new IllegalArgumentException("Cannot putAll members of anything other than a StructMap");
        StructMap that = (StructMap) t;
        for (int i = 0; i < that.keys.size(); ++i) {
            put(that.keys.get(i), that.values.get(i));
        }
    }
    public void clear() {
        keys.clear();
        values.clear();
        map.clear();
    }
    public Set keySet() {
        return map.keySet();
    }
    public Collection values() {
        return Collections.unmodifiableList(values);
    }
    public Set entrySet() {
        return map.entrySet();
    }
    public boolean equals(Object o) {
        return map.equals(o);
    }
    public int hashCode() {
        return map.hashCode() ^ keys.hashCode() ^ values.hashCode();
    }

    // new
    public Collection keys() {
        return Collections.unmodifiableList(keys);
    }
    public void set(int index, Object key, Object value) {
        keys.set(index, key);
        values.set(index, value);
        map.put(key, value);
    }
    public void set(int index, Object value) {
        Object key = keys.get(index);
        values.set(index, value);
        map.put(key, value);
    }
}