/*
 * $Id: AsyncOperation.java,v 1.4 2005-09-10 19:49:37 kohsuke Exp $
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

    @Override public String getJavaMethodName() {
        return super.getJavaMethodName() + "Async";
    }

    //Normal operation
    private Operation operation;
    private boolean _async;
    private AsyncOperationType _asyncOpType;
    private AbstractType _responseBean;

}
