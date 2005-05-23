/*
 * $Id: PrefixFactoryImpl.java,v 1.1 2005-05-23 22:59:35 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

import java.util.HashMap;
import java.util.Map;

/**
 * <p> A prefix factory that caches the prefixes it creates. </p>
 *
 * @author JAX-RPC Development Team
 */
public class PrefixFactoryImpl implements PrefixFactory {

    public PrefixFactoryImpl(String base) {
        _base = base;
        _next = 1;
    }

    public String getPrefix(String uri) {
        String prefix = null;

        if (_cachedUriToPrefixMap == null) {
            _cachedUriToPrefixMap = new HashMap();
        } else {
            prefix = (String) _cachedUriToPrefixMap.get(uri);
        }

        if (prefix == null) {
            prefix = _base + Integer.toString(_next++);
            _cachedUriToPrefixMap.put(uri, prefix);
        }

        return prefix;
    }

    private String _base;
    private int _next;
    private Map _cachedUriToPrefixMap;
}
