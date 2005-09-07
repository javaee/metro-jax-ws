/**
 * $Id: BindingOperation.java,v 1.5 2005-09-07 19:40:06 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import com.sun.xml.ws.model.ParameterBinding;
import com.sun.xml.ws.model.Mode;
import com.sun.xml.ws.model.soap.SOAPBlock;

import java.util.HashMap;
import java.util.Map;

public class BindingOperation {
    private String name;

    // map of wsdl:part to the binding
    private Map<String, ParameterBinding> inputParts;
    private Map<String, ParameterBinding> outputParts;
    private Map<String, String> inputMimeTypes;
    private Map<String, String> outputMimeTypes;

    private boolean explicitInputSOAPBodyParts = false;
    private boolean explicitOutputSOAPBodyParts = false;

    private Boolean emptyInputBody;
    private Boolean emptyOutputBody;

    private Map<String, Part> inParts;
    private Map<String, Part> outParts;

    /**
     *
     * @param name wsdl:operation name qualified value
     */
    public BindingOperation(String name) {
        this.name = name;
        inputParts = new HashMap<String, ParameterBinding>();
        outputParts = new HashMap<String, ParameterBinding>();
        inputMimeTypes = new HashMap<String, String>();
        outputMimeTypes = new HashMap<String, String>();
        inParts = new HashMap<String, Part>();
        outParts = new HashMap<String, Part>();
    }

    public String getName(){
        return name;
    }

    public Part getPart(String partName, Mode mode){
        if(mode.equals(Mode.IN)){
            return inParts.get(partName);
        }else if(mode.equals(Mode.OUT)){
            return outParts.get(partName);
        }
        return null;
    }

    public void addPart(Part part, Mode mode){
        if(mode.equals(Mode.IN))
            inParts.put(part.getName(), part);
        else if(mode.equals(Mode.OUT))
            outParts.put(part.getName(), part);
    }

    public Map<String, ParameterBinding> getInputParts() {
        return inputParts;
    }

    public Map<String, ParameterBinding> getOutputParts() {
        return outputParts;
    }

    public Map<String, String> getInputMimeTypes() {
        return inputMimeTypes;
    }

    public Map<String, String> getOutputMimeTypes() {
        return outputMimeTypes;
    }

    public ParameterBinding getInputBinding(String part){
        if(emptyInputBody == null){
            if(inputParts.get(" ") != null)
                emptyInputBody = true;
            else
                emptyInputBody = false;
        }
        ParameterBinding block = inputParts.get(part);
        if(block == null){
            if(explicitInputSOAPBodyParts || (boolean)emptyInputBody)
                return new ParameterBinding(SOAPBlock.UNBOUND);
            return new ParameterBinding(SOAPBlock.BODY);
        }

        return block;
    }

    public ParameterBinding getOutputBinding(String part){
        if(emptyOutputBody == null){
            if(outputParts.get(" ") != null)
                emptyOutputBody = true;
            else
                emptyOutputBody = false;
        }
        ParameterBinding block = outputParts.get(part);
        if(block == null){
            if(explicitOutputSOAPBodyParts || (boolean)emptyOutputBody)
                return new ParameterBinding(SOAPBlock.UNBOUND);;
            return new ParameterBinding(SOAPBlock.BODY);
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
