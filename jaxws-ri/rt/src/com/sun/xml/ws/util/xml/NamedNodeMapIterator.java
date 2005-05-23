/*
 * $Id: NamedNodeMapIterator.java,v 1.1 2005-05-23 23:05:08 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util.xml;

import java.util.Iterator;

import org.w3c.dom.NamedNodeMap;

/**
 * @author JAX-RPC Development Team
 */
public class NamedNodeMapIterator implements Iterator {

    protected NamedNodeMap _map;
    protected int _index;

    public NamedNodeMapIterator(NamedNodeMap map) {
        _map = map;
        _index = 0;
    }

    public boolean hasNext() {
        if (_map == null)
            return false;
        return _index < _map.getLength();
    }

    public Object next() {
        Object obj = _map.item(_index);
        if (obj != null)
            ++_index;
        return obj;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
