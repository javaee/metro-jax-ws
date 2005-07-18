/*
 * $Id: JavaInterface.java,v 1.2 2005-07-18 18:14:01 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.model.java;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.tools.ws.processor.model.ModelException;

/**
 *
 * @author WS Development Team
 */
public class JavaInterface {

    public JavaInterface() {}

    public JavaInterface(String name) {
        this(name, null);
    }

    public JavaInterface(String name, String impl) {
        this.realName = name;
        this.name = name.replace('$', '.');
        this.impl = impl;
    }

    public String getName() {
        return name;
    }

    public String getFormalName() {
        return name;
    }

    public void setFormalName(String s) {
        name = s;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String s) {
        realName = s;
    }

    public String getImpl() {
        return impl;
    }

    public void setImpl(String s) {
        impl = s;
    }

    public Iterator getMethods() {
        return methods.iterator();
    }

    public boolean hasMethod(JavaMethod method) {
        for (int i=0; i<methods.size();i++) {
            if (method.equals(((JavaMethod)methods.get(i)))) {
                return true;
            }
        }
        return false;
    }

    public void addMethod(JavaMethod method) {

        if (hasMethod(method)) {
            throw new ModelException("model.uniqueness");
        }
        methods.add(method);
    }

    /* serialization */
    public List getMethodsList() {
        return methods;
    }

    /* serialization */
    public void setMethodsList(List l) {
        methods = l;
    }

    public boolean hasInterface(String interfaceName) {
        for (int i=0; i<interfaces.size();i++) {
            if (interfaceName.equals((String)interfaces.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void addInterface(String interfaceName) {

        // verify that an exception with this name does not already exist
        if (hasInterface(interfaceName)) {
            return;
        }
        interfaces.add(interfaceName);
    }

    public Iterator getInterfaces() {
        return interfaces.iterator();
    }

    /* serialization */
    public List getInterfacesList() {
        return interfaces;
    }

    /* serialization */
    public void setInterfacesList(List l) {
        interfaces = l;
    }

    /* NOTE - all these fields (except "interfaces") were final, but had to
     * remove this modifier to enable serialization
     */
    private String name;
    private String realName;
    private String impl;
    private List methods = new ArrayList();
    private List interfaces = new ArrayList();
}
