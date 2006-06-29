/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.tools.ws.processor.model.java;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.tools.ws.util.ClassNameInfo;

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
    
    public String getSimpleName() {
        return ClassNameInfo.getName(name);
    }

    /* NOTE - all these fields (except "interfaces") were final, but had to
     * remove this modifier to enable serialization
     */
    private String javadoc;

    public String getJavaDoc() {
        return javadoc;
    }

    public void setJavaDoc(String javadoc) {
        this.javadoc = javadoc;
    }

    private String name;
    private String realName;
    private String impl;
    private List methods = new ArrayList();
    private List interfaces = new ArrayList();
}
