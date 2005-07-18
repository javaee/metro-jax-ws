/*
 * $Id: JavaParameter.java,v 1.2 2005-07-18 18:14:01 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.model.java;

import com.sun.tools.ws.processor.model.Parameter;

/**
 *
 * @author WS Development Team
 */
public class JavaParameter {

    public JavaParameter() {}

    public JavaParameter(String name, JavaType type, Parameter parameter) {
        this(name, type, parameter, false);
    }

    public JavaParameter(String name, JavaType type, Parameter parameter,
        boolean holder) {

        this.name = name;
        this.type = type;
        this.parameter = parameter;
        this.holder = holder;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public JavaType getType() {
        return type;
    }

    public void setType(JavaType t) {
        type = t;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter p) {
        parameter = p;
    }

    public boolean isHolder() {
        return holder;
    }

    public void setHolder(boolean b) {
        holder = b;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    private String name;
    private JavaType type;
    private Parameter parameter;
    private boolean holder;
    private String holderName;
}
