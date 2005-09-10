/*
 * $Id: JavaMethod.java,v 1.4 2005-09-10 19:49:39 kohsuke Exp $
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

package com.sun.tools.ws.processor.model.java;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.tools.ws.processor.model.ModelException;
import com.sun.codemodel.JClass;

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

    public void addExceptionClass(JClass ex){
        exceptionClasses.add(ex);
    }

    public List<JClass> getExceptionClasses(){
        return exceptionClasses;
    }

    private String name;
    private List<JavaParameter> parameters = new ArrayList<JavaParameter>();
    private List<String> exceptions = new ArrayList<String>();
    private List<JClass> exceptionClasses = new ArrayList<JClass>();

    private JavaType returnType;
    private String declaringClass;
    private boolean throwsRemoteException = true;
}
