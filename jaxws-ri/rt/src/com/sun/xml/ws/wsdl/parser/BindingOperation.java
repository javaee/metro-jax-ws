/**
 * $Id: BindingOperation.java,v 1.2 2005-08-15 22:44:07 vivekp Exp $
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
    private String name;

    // map of wsdl:part to the binding
    private Map<String, SOAPBlock> inputParts;
    private Map<String, SOAPBlock> outputParts;
    private Map<String, String> inputMimeTypes;
    private Map<String, String> outputMimeTypes;

    private boolean explicitInputSOAPBodyParts = false;
    private boolean explicitOutputSOAPBodyParts = false;

    private Boolean emptyInputBody;
    private Boolean emptyOutputBody;

    /**
     *
     * @param name wsdl:operation name qualified value
     */
    public BindingOperation(String name) {
        this.name = name;
        inputParts = new HashMap<String, SOAPBlock>();
        outputParts = new HashMap<String, SOAPBlock>();
        inputMimeTypes = new HashMap<String, String>();
        outputMimeTypes = new HashMap<String, String>();
    }

    public String getName(){
        return name;
    }

    public Map<String, SOAPBlock> getInputParts() {
        return inputParts;
    }

    public Map<String, SOAPBlock> getOutputParts() {
        return outputParts;
    }

    public Map<String, String> getInputMimeTypes() {
        return inputMimeTypes;
    }

    public Map<String, String> getOutputMimeTypes() {
        return outputMimeTypes;
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
        SOAPBlock block = outputParts.get(part);
        if(block == null){
            if(explicitOutputSOAPBodyParts || (boolean)emptyOutputBody)
                return SOAPBlock.UNBOUND;
            return SOAPBlock.BODY;
        }

        return block;
    }

    public String getMimeTypeForInputPart(String part){
        return inputMimeTypes.get(part);
    }

    public String getMimeTypeForOutputPart(String part){
        return outputMimeTypes.get(part);
    }

    public void setInputExplicitBodyParts(boolean b) {
        explicitInputSOAPBodyParts = b;
    }

    public void setOutputExplicitBodyParts(boolean b) {
        explicitOutputSOAPBodyParts = b;
    }

}
