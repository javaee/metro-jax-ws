/*
 * $Id: AsyncOperation.java,v 1.2 2005-07-21 19:53:23 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.model;

import javax.xml.namespace.QName;

import com.sun.tools.ws.processor.model.jaxb.JAXBType;
import com.sun.tools.ws.processor.model.jaxb.JAXBTypeAndAnnotation;
import com.sun.tools.ws.processor.model.java.JavaType;
import com.sun.tools.ws.processor.model.java.JavaSimpleType;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;


/**
 * @author Vivek Pandey
 *
 *
 */
public class AsyncOperation extends Operation {

    /**
     *
     */
    public AsyncOperation() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param operation
     */
    public AsyncOperation(Operation operation) {
        super(operation);
        this.operation = operation;
    }

    /**
     * @param name
     */
    public AsyncOperation(QName name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    /**
     * @return Returns the async.
     */
    public boolean isAsync() {
        return _async;
    }

    public void setAsyncType(AsyncOperationType type) {
        this._asyncOpType = type;
        _async = true;
    }

    public AsyncOperationType getAsyncType(){
        return _asyncOpType;
    }

    public void setResponseBean(AbstractType type){
        _responseBean = type;
    }

    public AbstractType getResponseBeanType(){
        return _responseBean;
    }

    public JavaType getResponseBeanJavaType(){
        JCodeModel cm = _responseBean.getJavaType().getType().getType().owner();
        if(_asyncOpType.equals(AsyncOperationType.CALLBACK)){
            JClass future = cm.ref(java.util.concurrent.Future.class).narrow(cm.ref(Object.class).wildcard());
            return new JavaSimpleType(new JAXBTypeAndAnnotation(future));
        }else if(_asyncOpType.equals(AsyncOperationType.POLLING)){
            JClass polling = cm.ref(javax.xml.ws.Response.class).narrow(_responseBean.getJavaType().getType().getType().boxify());
            return new JavaSimpleType(new JAXBTypeAndAnnotation(polling));
        }
        return null;
    }

    public JavaType getCallBackType(){
        if(_asyncOpType.equals(AsyncOperationType.CALLBACK)){
            JCodeModel cm = _responseBean.getJavaType().getType().getType().owner();
            JClass cb = cm.ref(javax.xml.ws.AsyncHandler.class).narrow(_responseBean.getJavaType().getType().getType().boxify());
            return new JavaSimpleType(new JAXBTypeAndAnnotation(cb));

        }
        return null;        
    }

    public Operation getNormalOperation(){
        return operation;
    }

    public void setNormalOperation(Operation operation){
        this.operation = operation;
    }

    //Normal operation
    private Operation operation;
    private boolean _async;
    private AsyncOperationType _asyncOpType;
    private AbstractType _responseBean;

}
