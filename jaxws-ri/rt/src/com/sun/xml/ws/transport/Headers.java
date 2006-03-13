
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

package com.sun.xml.ws.transport;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.*;

/**
 * HTTP request and response headers are represented by this class which implements
 * the interface {@link java.util.Map}&lt;
 * {@link java.lang.String},{@link java.util.List}&lt;{@link java.lang.String}&gt;&gt;.
 * The keys are case-insensitive Strings representing the header names and
 * the value associated with each key is a {@link List}&lt;{@link String}&gt; with one
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

    public Headers () {
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
        char[] b = new char [len];
        String s = null;
            b = key.toCharArray();
            if (b[0] >= 'a' && b[0] <= 'z') {
                b[0] = (char)(b[0] - ('a' - 'A'));
            }
            for (int i=1; i<len; i++) {
                if (b[i] >= 'A' && b[i] <= 'Z') {
                    b[i] = (char) (b[i] + ('a' - 'A'));
                }
            }
            s = new String (b);
        return s;
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object key) {
        if (key == null) {
            return false;
        }
        if (!(key instanceof String)) {
            return false;
        }
        return map.containsKey (normalize((String)key));
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

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


    public List<String> remove(Object key) {
        return map.remove(normalize((String)key));
    }

    public void putAll(Map<? extends String,? extends List<String>> t)  {
        map.putAll (t);
    }

    public void clear() {
        map.clear();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Collection<List<String>> values() {
        return map.values();
    }

    public Set<Map.Entry<String, List<String>>> entrySet() {
        return map.entrySet();
    }

    public boolean equals(Object o) {
        return map.equals(o);
    }

    public int hashCode() {
        return map.hashCode();
    }
    
    public String toString() {
        return map.toString();
    }
}
