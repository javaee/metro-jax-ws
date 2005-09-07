/**
 * $Id: Part.java,v 1.1 2005-09-07 19:40:03 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import com.sun.xml.ws.model.ParameterBinding;

public class Part {
    private String name;
    private ParameterBinding binding;
    private int index;

    public Part(String name, ParameterBinding binding, int index) {
        this.name = name;
        this.binding = binding;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public ParameterBinding getBinding() {
        return binding;
    }

    public int getIndex() {
        return index;
    }
}
