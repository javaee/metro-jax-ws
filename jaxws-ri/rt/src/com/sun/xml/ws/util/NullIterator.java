/*
 * $Id: NullIterator.java,v 1.2 2005-07-18 16:52:31 kohlert Exp $
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
 * @author WS Development Team
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
