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
package com.sun.xml.ws.wsdl.parser;

import com.sun.xml.ws.model.Mode;
import com.sun.xml.ws.model.ParameterBinding;

import javax.xml.namespace.QName;
import java.util.HashMap;

public class Binding extends HashMap<String, BindingOperation> {
    private QName name;
    private QName portTypeName;
    private PortType portType;
    private String bindingId;
    private WSDLDocument wsdlDoc;
    private boolean finalized = false;

    public Binding(QName name, QName portTypeName) {
        super();
        this.name = name;
        this.portTypeName = portTypeName;
    }

    public QName getName() {
        return name;
    }

    public QName getPortTypeName(){
        return portTypeName;
    }

    public PortType getPortType() {
        return portType;
    }

    public void setPortType(PortType portType) {
        this.portType = portType;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public void setWsdlDocument(WSDLDocument wsdlDoc) {
        this.wsdlDoc = wsdlDoc;
    }

    public ParameterBinding getBinding(String operation, String part, Mode mode){
        BindingOperation op = get(operation);
        if(op == null){
            //TODO throw exception
            return null;
        }
        if((Mode.IN == mode)||(Mode.INOUT == mode))
            return op.getInputBinding(part);
        else
            return op.getOutputBinding(part);
    }

    /**
     * Gives binding for a given {@link BindingOperation} a wsdl part and {@link Mode}
     * @param op  must be non-null
     * @param part must be non-null
     * @param mode must be non-null
     * @return  parameter Binding, null the binding could not be determined.
     */
    public ParameterBinding getBinding(BindingOperation op, String part, Mode mode){
        if((Mode.IN == mode)||(Mode.INOUT == mode))
            return op.getInputBinding(part);
        else
            return op.getOutputBinding(part);
    }

    public String getMimeType(String operation, String part, Mode mode){
        BindingOperation op = get(operation);
        if(Mode.IN == mode)
            return op.getMimeTypeForInputPart(part);
        else
            return op.getMimeTypeForOutputPart(part);
    }

    public void finalizeBinding(){
        if(!finalized){
            wsdlDoc.finalizeBinding(this);
            finalized = true;
        }
    }

}
