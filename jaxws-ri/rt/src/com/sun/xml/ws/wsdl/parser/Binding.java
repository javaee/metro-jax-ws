/**
 * $Id: Binding.java,v 1.1 2005-08-13 19:30:35 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import com.sun.xml.ws.model.soap.SOAPBlock;
import com.sun.xml.ws.model.Mode;

import javax.xml.namespace.QName;
import java.util.HashMap;

public class Binding extends HashMap<QName, BindingOperation> {
    private QName name;
    private QName portTypeName;
    private PortType portType;
    private String bindingId;

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

    public SOAPBlock getBinding(QName operation, String part, Mode mode){
        BindingOperation op = get(operation);
        if(Mode.IN == mode)
            return op.getInputBinding(part);
        else
            return op.getOutputBinding(part);
    }
}
