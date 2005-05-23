/*
 * $Id: NodeListIterator.java,v 1.1 2005-05-23 23:05:08 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util.xml;

import java.util.Iterator;

import org.w3c.dom.NodeList;

/**
 * @author JAX-RPC Development Team
 */
public class NodeListIterator implements Iterator {

    protected NodeList _list;
    protected int _index;

    public NodeListIterator(NodeList list) {
        _list = list;
        _index = 0;
    }

    public boolean hasNext() {
        if (_list == null)
            return false;
        return _index < _list.getLength();
    }

    public Object next() {
        Object obj = _list.item(_index);
        if (obj != null)
            ++_index;
        return obj;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
