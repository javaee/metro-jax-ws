/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
import javax.xml.ws.WebServiceException;
import javax.jws.WebMethod;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Build this runtime model using java SEI and annotations
 *
 * @author Vivek Pandey
 */
public final class JavaMethodImpl implements JavaMethod {

    private String inputAction = "";
    private String outputAction = "";
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
        setWsaActions();
    }

    private void setWsaActions() {
        Action action = seiMethod.getAnnotation(Action.class);
        if(action != null) {
            inputAction = action.input();
            outputAction = action.output();
        }

        //@Action(input) =="", get it from @WebMethod(action)
        WebMethod webMethod = seiMethod.getAnnotation(WebMethod.class);
        String soapAction = "";
        if (webMethod != null )
            soapAction = webMethod.action();
        if(!soapAction.equals("")) {
            //non-empty soapAction
            if(inputAction.equals(""))
                // set input action to non-empty soapAction
                inputAction = soapAction;
            else if(!inputAction.equals(soapAction)){
                //both are explicitly set via annotations, make sure @Action == @WebMethod.action
                throw new WebServiceException("@Action and @WebMethod(action=\"\" does not match on operation "+ method.getName());
            }
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
            throw new Error("Method "+seiMethod.getName()+" is exposed as WebMethod, but there is no corresponding wsdl operation with name "+operationName+" in the wsdl:portType" + portType.getBinding().getPortType().getName());

        //so far, the inputAction, outputAction and fault actions are set from the @Action and @FaultAction
        //set the values from WSDLModel, if such annotations are not present or defaulted
        if(inputAction.equals("")) {
                inputAction = wsdlOperation.getOperation().getInput().getAction();                
        } else if(!inputAction.equals(wsdlOperation.getOperation().getInput().getAction()))
                //TODO input action might be from @Action or WebMethod(action)
                LOGGER.warning("Input Action on WSDL operation "+wsdlOperation.getName().getLocalPart() + " and @Action on its associated Web Method " + seiMethod.getName() +" did not match and will cause problems in dispatching the requests");

        if (!mep.isOneWay()) {
            if (outputAction.equals(""))
                outputAction = wsdlOperation.getOperation().getOutput().getAction();

            for (CheckedExceptionImpl ce : exceptions) {
                if (ce.getFaultAction().equals("")) {
                    QName detailQName = ce.getDetailType().tagName;
                    ce.setFaultAction(wsdlOperation.getOperation().getFault(detailQName).getAction());
                }
            }
        }
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

    private static final Logger LOGGER = Logger.getLogger(com.sun.xml.ws.model.JavaMethodImpl.class.getName());

}

