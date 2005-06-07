/**
 * $Id: JavaMethod.java,v 1.3 2005-06-07 03:38:31 vivekp Exp $
 */
/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.model.soap.SOAPBinding;
import com.sun.pept.presentation.MessageStruct;

/**
 * @author Vivek Pandey
 * 
 * Build this runtime model using java SEI and annotations
 */
public class JavaMethod {
    /**
     * 
     */
    public JavaMethod(Method method) {
        this.method = method;
    }

    /**
     * @return Returns the method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return Returns the mep.
     */
    public int getMEP() {
        return mep;
    }

    /**
     * @param mep
     *            The mep to set.
     */
    public void setMEP(int mep) {
        this.mep = mep;
    }

    /**
     * @return
     */
    public Object getBinding() {
        if (binding == null)
            return new SOAPBinding();
        return binding;
    }

    /**
     * @param binding
     */
    public void setBinding(Object binding) {
        this.binding = binding;
    }

    
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
    
    public String getOperationName() {
        return operationName;
    }
    
    
    
    /**
     * @return returns unmodifiable list of request parameters
     */
    public List<Parameter> getRequestParameters() {
        return Collections.unmodifiableList(requestParams);
    }

    /**
     * @return returns unmodifiable list of response parameters
     */
    public List<Parameter> getResponseParameters() {
        return Collections.unmodifiableList(responseParams);
    }

    /**
     * @param parameter
     * @throws XYZException
     *             when a parameter is already added. Its ok to have same in and
     *             out parameter but not duplicate inout.
     */
    public void addParameter(Parameter p) {
        if (p.isIN() || p.isINOUT()) {
            if (requestParams.contains(p)) {
                // TODO throw exception
            }
            requestParams.add(p);
        }

        if (p.isOUT() || p.isINOUT()) {
            // this check is only for out parameters
            if (requestParams.contains(p)) {
                // TODO throw exception
            }
            responseParams.add(p);
        }
    }

    /**
     * @return Returns number of java method parameters - that will be all the
     *         IN, INOUT and OUT holders
     */
    public int getInputParametersCount() {
        int count = 0;
        for (Parameter param : requestParams) {
            if (param.isWrapperStyle()) {
                count += ((WrapperParameter) param).getWrapperChildren().size();
            } else {
                count++;
            }
        }

        for (Parameter param : responseParams) {
            if (param.isWrapperStyle()) {
                for (Parameter wc : ((WrapperParameter) param).getWrapperChildren()) {
                    if (!wc.isResponse() && wc.isOUT()) {
                        count++;
                    }
                }
            } else if (!param.isResponse() && param.isOUT()) {
                count++;
            }
        }

        return count;
    }

    /**
     * @param ce
     */
    public void addException(CheckedException ce) {        
        if (!exceptions.contains(ce))
            exceptions.add(ce);
    }

    /**
     * @param exceptionType
     * @return CheckedException corresponding to the exceptionClass. Returns
     *         null if not found.
     */
    public CheckedException getCheckedException(Class exceptionClass) {
        for (CheckedException ce : exceptions) {
            if (ce.getExcpetionClass().equals(exceptionClass))
                return ce;
        }
        return null;
    }

    /**
     * @return
     */
    public List<CheckedException> getCheckedExceptions(){
        return Collections.unmodifiableList(exceptions);
    }
    /**
     * @param detailType
     * @return Gets the CheckedException corresponding to detailType. Returns
     *         null if no CheckedExcpetion with the detailType found.
     */
    public CheckedException getCheckedException(TypeReference detailType) {
        for (CheckedException ce : exceptions) {
            TypeReference actual = ce.getDetailType();
            if (actual.tagName.equals(detailType.tagName)
                    && actual.type.getClass().getName()
                            .equals(detailType.type.getClass().getName())) {
                return ce;
            }
        }
        return null;
    }

    /**
     * Returns if the java method MEP is async
     * @return
     */
    public boolean isAsync(){
        return mep == MessageStruct.ASYNC_CALLBACK_MEP || mep == MessageStruct.ASYNC_POLL_MEP;
    }

    private List<CheckedException> exceptions = new ArrayList<CheckedException>();
    private Method method;
    private final List<Parameter> requestParams = new ArrayList<Parameter>();
    private final List<Parameter> responseParams = new ArrayList<Parameter>();
    private Object binding;
    private int mep;
    private String operationName;
}