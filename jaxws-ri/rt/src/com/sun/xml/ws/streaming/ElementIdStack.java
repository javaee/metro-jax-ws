/*
 * $Id: ElementIdStack.java,v 1.1 2005-05-23 22:59:34 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

/**
 * <p> A stack of int-valued element IDs. </p>
 *
 * @author JAX-RPC Development Team
 */
public final class ElementIdStack {

    public ElementIdStack() {
        this(INITIAL_SIZE);
    }

    public ElementIdStack(int size) {
        _values = new int[size];
        reset();
    }

    public void reset() {
        _tos = 0;
        _nextElementId = 1;
    }
    
    public int getCurrent() {
        return _values[_tos - 1];
    }

    public int pushNext() {
        ensureCapacity();
        _values[_tos++] = _nextElementId;
        return _nextElementId++;
    }

    public int pop() {
        --_tos;
        return _values[_tos];
    }

    private void ensureCapacity() {
        if (_tos >= _values.length) {
            int[] newValues = new int[_values.length * 2];
            System.arraycopy(_values, 0, newValues, 0, _values.length);
            _values = newValues;
        }
    }

    private int[] _values;
    private int _tos;
    private int _nextElementId;

    private static final int INITIAL_SIZE = 32;
}
