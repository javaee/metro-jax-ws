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
package com.sun.xml.ws.model;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.MEP;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.model.soap.SOAPBindingImpl;
import com.sun.xml.ws.model.wsdl.WSDLBoundOperationImpl;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.istack.Nullable;

import javax.xml.namespace.QName;
import javax.xml.ws.Action;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Build this runtime model using java SEI and annotations
 *
 * @author Vivek Pandey
 */
public final class JavaMethodImpl implements JavaMethod {

    private String inputAction;
    private String outputAction;
    private final List<CheckedExceptionImpl> exceptions = new ArrayList<CheckedExceptionImpl>();
    private final Method method;
    /*package*/ final List<ParameterImpl> requestParams = new ArrayList<ParameterImpl>();
    /*package*/ final List<ParameterImpl> responseParams = new ArrayList<ParameterImpl>();
    private final List<ParameterImpl> unmReqParams = Collections.unmodifiableList(requestParams);
    private final List<ParameterImpl> unmResParams = Collections.unmodifiableList(responseParams);
    private SOAPBindingImpl binding;
    private MEP mep;
    private String operationName;
    private WSDLBoundOperationImpl wsdlOperation;
    /*package*/ final AbstractSEIModelImpl owner;
    private final Method seiMethod;

    /**
     * @param owner
     * @param method : Implementation class method
     * @param seiMethod : corresponding SEI Method.
     *                  Is there is no SEI, it should be Implementation class method
     */
    public JavaMethodImpl(AbstractSEIModelImpl owner, Method method, Method seiMethod) {
        this.owner = owner;
        this.method = method;
        this.seiMethod = seiMethod;
        Action action = method.getAnnotation(Action.class);
        if(action != null) {
            inputAction = action.input();
            outputAction = action.output();
        }
    }

    public SEIModel getOwner() {
        return owner;
    }

    /**
     * @see {@link JavaMethod}
     *
     * @return Returns the method. 
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @see {@link JavaMethod}
     *
     * @return Returns the SEI method where annotations are present
     */
    public Method getSEIMethod() {
        return seiMethod;
    }

    /**
     * @return Returns the mep.
     */
    public MEP getMEP() {
        return mep;
    }

    /**
     * @param mep
     *            The mep to set.
     */
    void setMEP(MEP mep) {
        this.mep = mep;
    }

    /**
     * @return the Binding object
     */
    public SOAPBindingImpl getBinding() {
        if (binding == null)
            return new SOAPBindingImpl();
        return binding;
    }

    /**
     * @param binding
     */
    void setBinding(SOAPBindingImpl binding) {
        this.binding = binding;
    }

    /**
     * Returns the {@link WSDLBoundOperation} Operation associated with {@link this}
     * operation.
     *
     * @return the WSDLBoundOperation for this JavaMethod
     */
    public @NotNull WSDLBoundOperation getOperation() {
        assert wsdlOperation != null;
        return wsdlOperation;
    }

    public void setOperationName(String name) {
        this.operationName = name;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getRequestMessageName() {
        return operationName;
    }

    public String getResponseMessageName() {
        if(mep.isOneWay())
            return null;
        return operationName+"Response";
    }

    /**
     * @return soap:Body's first child name for request message.
     */
    public @Nullable QName getRequestPayloadName() {
        return wsdlOperation.getReqPayloadName();
    }

    /**
     * @return soap:Body's first child name for response message.
     */
    public @Nullable QName getResponsePayloadName() {
        return (mep == MEP.ONE_WAY) ? null : wsdlOperation.getResPayloadName();
    }

    /**
     * @return returns unmodifiable list of request parameters
     */
    public List<ParameterImpl> getRequestParameters() {
        return unmReqParams;
    }

    /**
     * @return returns unmodifiable list of response parameters
     */
    public List<ParameterImpl> getResponseParameters() {
        return unmResParams;
    }

    void addParameter(ParameterImpl p) {
        if (p.isIN() || p.isINOUT()) {
            assert !requestParams.contains(p);
            requestParams.add(p);
        }

        if (p.isOUT() || p.isINOUT()) {
            // this check is only for out parameters
            assert !responseParams.contains(p);
            responseParams.add(p);
        }
    }

    void addRequestParameter(ParameterImpl p){
        if (p.isIN() || p.isINOUT()) {
            requestParams.add(p);
        }
    }

    void addResponseParameter(ParameterImpl p){
        if (p.isOUT() || p.isINOUT()) {
            responseParams.add(p);
        }
    }

    /**
     * @return Returns number of java method parameters - that will be all the
     *         IN, INOUT and OUT holders
     *
     * @deprecated no longer use in the new architecture
     */
    public int getInputParametersCount() {
        int count = 0;
        for (ParameterImpl param : requestParams) {
            if (param.isWrapperStyle()) {
                count += ((WrapperParameter) param).getWrapperChildren().size();
            } else {
                count++;
            }
        }

        for (ParameterImpl param : responseParams) {
            if (param.isWrapperStyle()) {
                for (ParameterImpl wc : ((WrapperParameter) param).getWrapperChildren()) {
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
    void addException(CheckedExceptionImpl ce) {
        if (!exceptions.contains(ce))
            exceptions.add(ce);
    }

    /**
     * @param exceptionClass
     * @return CheckedException corresponding to the exceptionClass. Returns
     *         null if not found.
     */
    public CheckedExceptionImpl getCheckedException(Class exceptionClass) {
        for (CheckedExceptionImpl ce : exceptions) {
            if (ce.getExceptionClass()==exceptionClass)
                return ce;
        }
        return null;
    }


    /**
     * @return a list of checked Exceptions thrown by this method
     */
    public List<CheckedExceptionImpl> getCheckedExceptions(){
        return Collections.unmodifiableList(exceptions);
    }

    public String getInputAction() {
        return inputAction;
    }

    public String getOutputAction() {
        return outputAction;
    }

    /**
     * @param detailType
     * @return Gets the CheckedException corresponding to detailType. Returns
     *         null if no CheckedExcpetion with the detailType found.
     */
    public CheckedExceptionImpl getCheckedException(TypeReference detailType) {
        for (CheckedExceptionImpl ce : exceptions) {
            TypeReference actual = ce.getDetailType();
            if (actual.tagName.equals(detailType.tagName) && actual.type==detailType.type) {
                return ce;
            }
        }
        return null;
    }



    /**
     * Returns if the java method  is async
     * @return if this is an Asynch 
     */
    public boolean isAsync(){
        return mep.isAsync;
    }

    /*package*/ void freeze(WSDLPortImpl portType) {
        this.wsdlOperation = portType.getBinding().get(new QName(portType.getBinding().getPortType().getName().getNamespaceURI(),operationName));
        // TODO: replace this with proper error handling
        if(wsdlOperation ==null)
            throw new Error("Undefined operation name "+operationName);
    }

    final void fillTypes(List<TypeReference> types) {
        fillTypes(requestParams, types);
        fillTypes(responseParams, types);

        for (CheckedExceptionImpl ce : exceptions) {
            types.add(ce.getDetailType());
        }
    }

    private void fillTypes(List<ParameterImpl> params, List<TypeReference> types) {
        for (ParameterImpl p : params) {
            p.fillTypes(types);
        }
    }

}

