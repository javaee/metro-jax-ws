/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.ws.processor.model;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.tools.ws.processor.model.java.JavaSimpleType;
import com.sun.tools.ws.processor.model.java.JavaType;
import com.sun.tools.ws.processor.model.jaxb.JAXBTypeAndAnnotation;
import com.sun.tools.ws.wsdl.framework.Entity;

import javax.xml.namespace.QName;


/**
 * @author Vivek Pandey
 *
 *
 */
public class AsyncOperation extends Operation {

    /**
     *
     */
    public AsyncOperation(Entity entity) {
        super(entity);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param operation
     */
    public AsyncOperation(Operation operation, Entity entity) {
        super(operation, entity);
        this.operation = operation;
    }

    /**
     * @param name
     */
    public AsyncOperation(QName name, Entity entity) {
        super(name, entity);
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
