/**
 * $Id: Binding.java,v 1.6 2005-09-07 19:40:06 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
