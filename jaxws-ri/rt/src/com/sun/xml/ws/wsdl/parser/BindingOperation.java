/**
 * $Id: BindingOperation.java,v 1.1 2005-08-13 19:30:35 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import com.sun.xml.ws.model.soap.SOAPBlock;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BindingOperation {
    private QName name;

    // map of wsdl:part to the binding
    private Map<String, SOAPBlock> inputParts;
    private Map<String, SOAPBlock> outputParts;

    private boolean explicitInputSOAPBodyParts = false;
    private boolean explicitOutputSOAPBodyParts = false;

    private Boolean emptyInputBody;
    private Boolean emptyOutputBody;

    /**
     *
     * @param name wsdl:operation name qualified value
     */
    public BindingOperation(QName name) {
        this.name = name;
        inputParts = new HashMap<String, SOAPBlock>();
        outputParts = new HashMap<String, SOAPBlock>();
    }

    public QName getName(){
        return name;
    }

    public Map<String, SOAPBlock> getInputParts() {
        return inputParts;
    }

    public Map<String, SOAPBlock> getOutputParts() {
        return outputParts;
    }

    public SOAPBlock getInputBinding(String part){
        if(emptyInputBody == null){
            if(inputParts.get(" ") != null)
                emptyInputBody = true;
            else
                emptyInputBody = false;
        }
        SOAPBlock block = inputParts.get(part);
        if(block == null){
            if(explicitInputSOAPBodyParts || (boolean)emptyInputBody)
                return SOAPBlock.UNBOUND;
            return SOAPBlock.BODY;
        }

        return block;
    }

    public SOAPBlock getOutputBinding(String part){
        if(emptyOutputBody == null){
            if(outputParts.get(" ") != null)
                emptyOutputBody = true;
            else
                emptyOutputBody = false;
        }
        SOAPBlock block = inputParts.get(part);
        if(block == null){
            if(explicitOutputSOAPBodyParts || (boolean)emptyOutputBody)
                return SOAPBlock.UNBOUND;
            return SOAPBlock.BODY;
        }

        return block;
    }

    public void setInputExplicitBodyParts(boolean b) {
        explicitInputSOAPBodyParts = b;
    }

    public void setOutputExplicitBodyParts(boolean b) {
        explicitOutputSOAPBodyParts = b;
    }

}
