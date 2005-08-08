/*
 * $Id: NodeListIterator.java,v 1.3 2005-08-08 21:49:25 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.util.xml;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.NodeList;

/**
 * @author WS Development Team
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

    public Object next() throws NoSuchElementException {
        if (_list.getLength() == 0)
            throw new NoSuchElementException();
        Object obj = _list.item(_index);
        if (obj != null)
            ++_index;
        return obj;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
