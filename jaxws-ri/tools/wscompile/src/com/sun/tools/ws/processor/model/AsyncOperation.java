/*
 * $Id: AsyncOperation.java,v 1.1 2005-05-23 23:18:54 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.model;

import javax.xml.namespace.QName;

import com.sun.tools.ws.processor.model.jaxb.JAXBType;


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

    public String getResponseBeanJavaType(){
        return _responseBean.getJavaType().getName();
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
