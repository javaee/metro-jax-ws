/*
 * $Id: NullIterator.java,v 1.1 2005-05-23 23:06:26 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iterator on an empty collection.
 *
 * @author JAX-RPC Development Team
 */
public final class NullIterator implements Iterator {

    public static NullIterator getInstance() {
        return _instance;
    }

    private static final NullIterator _instance = new NullIterator();

    private NullIterator() {
    }

    public boolean hasNext() {
        return false;
    }

    public Object next() {
        throw new NoSuchElementException();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
