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

package com.sun.xml.ws.transport;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * HTTP request and response headers are represented by this class which implements
 * the interface {@link java.util.Map}&lt;{@link String},
 * {@link List}&lt;{@link String}&gt;&gt;.
 * The keys are case-insensitive Strings representing the header names and
 * the value associated with each key is a {@link List}&lt;{@link String}&gt; with one
 * element for each occurrence of the header name in the request or response.
 * <p>
 * For example, if the request has the the following headers:
 * <blockquote><pre>
 * HeaderName: value1
 * HeadernaMe: value2
 * </blockquote></pre>
 * Then get("hEaDeRnAmE") would give both "value1", and "value2" values in a list
 * <p>
 * All the normal {@link Map} methods are provided, but the following
 * additional convenience methods are most likely to be used:
 * <ul>
 * <li>{@link #getFirst(String)} returns a single valued header or the first
 * value of a multi-valued header.</li>
 * <li>{@link #add(String,String)} adds the given header value to the list
 * for the given key</li>
 * <li>{@link #set(String,String)} sets the given header field to the single
 * value given overwriting any existing values in the value list.
 * </ul><p>
 * All methods in this class accept <code>null</code> values for keys and values.
 * However, null keys will never will be present in HTTP request headers, and
 * will not be output/sent in response headers. Null values can be represented
 * as either a null entry for the key (i.e. the list is null) or where the key
 * has a list, but one (or more) of the list's values is null. Null values are
 * output as a header line containing the key but no associated value.
 *
 * @author Jitendra Kotamraju
 */
public class Headers extends TreeMap<String,List<String>> {

    public Headers() {
        super(INSTANCE);
    }

    private static final InsensitiveComparator INSTANCE = new InsensitiveComparator();

    // case-insensitive string comparison of HTTP header names.
    private static final class InsensitiveComparator implements Comparator<String>, Serializable {
        public int compare(String o1, String o2) {
            if (o1 == null && o2 == null)
                return 0;
            if (o1 == null)
                return -1;
            if (o2 == null)
                return 1;
            return o1.compareToIgnoreCase(o2);
        }
    }

    /**
     * Adds the given value to the list of headers for the given key. If the
     * mapping does not already exist, then it is created.
     *
     * @param key the header name
     * @param value the header value to add to the header
     */
    public void add (String key, String value) {
        List<String> list = this.get(key);
        if (list == null) {
            list = new LinkedList<String>();
            put(key,list);
        }
        list.add (value);
   }

    /**
     * Returns the first value from the List of String values for the given key
     * (if at least one exists).
     *
     * @param key the key to search for
     * @return the first string value associated with the key
     */
    public String getFirst (String key) {
        List<String> l = get(key);
        return (l == null) ? null : l.get(0);
    }

    /**
     * Sets the given value as the sole header value for the given key. If the
     * mapping does not already exist, then it is created.
     *
     * @param key the header name
     * @param value the header value to set.
     */
    public void set (String key, String value) {
        LinkedList<String> l = new LinkedList<String>();
        l.add (value);
        put(key, l);
    }
    /**
     * Added to fix issue
     * putAll() is easier to deal with as it doesn't return anything
     */
    public void putAll(Map<? extends String,? extends List<String>> map) {
        for (String k : map.keySet()) {
            List<String> list = map.get(k);
            for (String v : list) {
                add(k,v); 
            }
        }
    }

}
