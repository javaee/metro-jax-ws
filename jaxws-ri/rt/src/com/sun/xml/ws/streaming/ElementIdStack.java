/*
 * $Id: ElementIdStack.java,v 1.3 2005-09-10 19:48:02 kohsuke Exp $
 */

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

package com.sun.xml.ws.streaming;

/**
 * <p> A stack of int-valued element IDs. </p>
 *
 * @author WS Development Team
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
