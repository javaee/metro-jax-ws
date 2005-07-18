/*
 * $Id: JavaMethod.java,v 1.2 2005-07-18 18:14:01 kohlert Exp $
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
public class JavaMethod {

    public JavaMethod() {}

    public JavaMethod(String name) {
        this.name = name;
        this.returnType = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JavaType getReturnType() {
        return returnType;
    }

    public void setReturnType(JavaType returnType) {
        this.returnType = returnType;
    }

    public boolean hasParameter(String paramName) {
        for (int i=0; i<parameters.size();i++) {
            if (paramName.equals(
                ((JavaParameter)parameters.get(i)).getName())) {

                return true;
            }
        }
        return false;
    }

    public void addParameter(JavaParameter param) {
        // verify that this member does not already exist
        if (hasParameter(param.getName())) {
            throw new ModelException("model.uniqueness");
        }
        parameters.add(param);
    }

    public JavaParameter getParameter(String paramName){
        for (int i=0; i<parameters.size();i++) {
            JavaParameter jParam = parameters.get(i);
            if (paramName.equals(jParam.getParameter().getName())) {
                return jParam;
            }
        }
        return null;
    }

    public Iterator<JavaParameter> getParameters() {
        return parameters.iterator();
    }

    public int getParameterCount() {
        return parameters.size();
    }

    /* serialization */
    public List<JavaParameter> getParametersList() {
        return parameters;
    }

    /* serialization */
    public void setParametersList(List<JavaParameter> l) {
        parameters = l;
    }

    public boolean hasException(String exception) {
        return exceptions.contains(exception);
    }

    public void addException(String exception) {

        // verify that this exception does not already exist
        if (hasException(exception)) {
            throw new ModelException("model.uniqueness");
        }
        exceptions.add(exception);
    }

    public Iterator getExceptions() {
        return exceptions.iterator();
    }

    /* serialization */
    public List getExceptionsList() {
        return exceptions;
    }

    /* serialization */
    public void setExceptionsList(List l) {
        exceptions = l;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }
    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    // TODO fix model importer/exporter to handle this
    public boolean getThrowsRemoteException() {
        return throwsRemoteException;
    }
    public void setThrowsRemoteException(boolean throwsRemoteException) {
        this.throwsRemoteException = throwsRemoteException;
    }

    private String name;
    private List<JavaParameter> parameters = new ArrayList<JavaParameter>();
    private List exceptions = new ArrayList();
    private JavaType returnType;
    private String declaringClass;
    private boolean throwsRemoteException = true;
}
