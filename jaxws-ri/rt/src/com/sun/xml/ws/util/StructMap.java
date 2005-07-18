/*
 * $Id: StructMap.java,v 1.2 2005-07-18 16:52:31 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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