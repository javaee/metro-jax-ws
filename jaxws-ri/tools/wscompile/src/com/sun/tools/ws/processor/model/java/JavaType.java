/*
 * $Id: JavaType.java,v 1.1 2005-05-23 23:15:59 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.model.java;

/**
 *
 * @author JAX-RPC Development Team
 */
public abstract class JavaType {

    private String name;
    private String realName;
    private boolean present;
    private boolean holder;
    private boolean holderPresent;
    private String initString;
    private String holderName;

    public JavaType() {}

    public JavaType(String name, boolean present, String initString) {
        init(name, present, initString, null);
    }

    public JavaType(String name, boolean present, String initString,
        String holderName) {

        init(name, present, initString, holderName);
    }

    private void init(String name, boolean present, String initString,
        String holderName) {

        this.realName = name;
        this.name = name.replace('$', '.');
        this.present = present;
        this.initString = initString;
        this.holderName = holderName;
        holder = holderName != null;
    }

    public String getName() {
        return name;
    }

    public void doSetName(String name) {

        // renamed to avoid creating a "name" property with broken semantics
        this.realName = name;
        this.name = name.replace('$', '.');
    }

    public String getRealName() {
        return realName;
    }

    /* serialization */
    public void setRealName(String s) {
        realName = s;
    }

    public String getFormalName() {
        return name;
    }

    public void setFormalName(String s) {
        name = s;
    }

    public boolean isPresent() {
        return present;
    }

    /* serialization */
    public void setPresent(boolean b) {
        present = b;
    }

    public boolean isHolder() {
        return holder;
    }

    public void setHolder(boolean holder) {
        this.holder = holder;
    }

    public boolean isHolderPresent() {
        return holderPresent;
    }
    public void setHolderPresent(boolean holderPresent) {
        this.holderPresent = holderPresent;
    }

    public String getInitString() {
        return initString;
    }

    /* serialization */
    public void setInitString(String s) {
        initString = s;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }
}
