/**
 * $Id: Parameter.java,v 1.11 2005-09-24 04:51:22 kohlert Exp $
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

package com.sun.xml.ws.model;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import com.sun.xml.bind.api.RawAccessor;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.model.soap.SOAPBlock;

/**
 * runtime Parameter that abstracts the annotated java parameter
 * 
 * @author Vivek Pandey
 */

public class Parameter {
    /**
     * 
     */
    public Parameter(TypeReference type, Mode mode, int index) {
        this.typeReference = type;
        this.name = type.tagName;
        this.mode = mode;
        this.index = index;
    }

    /**
     * @return Returns the name.
     */
    public QName getName() {
        return name;
    }

    /**
     * @return Returns the TypeReference associated with this Parameter
     */
    public TypeReference getTypeReference() {
        return typeReference;
    }

    /**
     * @return Returns the mode.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * @return Returns the index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return WrapperStyle == true
     */
    public boolean isWrapperStyle() {
        return false;
    }

    /**
     * @return the Binding for this Parameter
     */
    public ParameterBinding getBinding() {
        if(binding == null)
            return new ParameterBinding(SOAPBlock.BODY);
        return binding;
    }

    /**
     * @param binding
     */
    public void setBinding(ParameterBinding binding) {
        this.binding = binding;
    }

    public void setInBinding(ParameterBinding binding){
        this.binding = binding;
    }

    public void setOutBinding(ParameterBinding binding){
        this.outBinding = binding;
    }

    public ParameterBinding getInBinding(){
        return binding;
    }

    public ParameterBinding getOutBinding(){
        if(outBinding == null)
            return binding;
        return outBinding;
    }

    public boolean isIN() {
        return mode.equals(Mode.IN);
    }

    public boolean isOUT() {
        return mode.equals(Mode.OUT);
    }

    public boolean isINOUT() {
        return mode.equals(Mode.INOUT);
    }

    public boolean isResponse() {
        return index == -1;
    }

    /**
     * Creates a holder if applicable else gives the object as it is. To be
     * called on the inbound message.
     * 
     * @param value
     * @return the non-holder value if its Response or IN otherwise creates a
     *         holder with the passed value and returns it back.
     * 
     */
    public Object createHolderValue(Object value) {
        if (isResponse() || isIN()) {
            return value;
        }
        return new Holder(value);
    }

    /**
     * Gets the holder value if applicable. To be called for inbound client side
     * message.
     * 
     * @param obj
     * @return the holder value if applicable.
     */
    public Object getHolderValue(Object obj) {
        if (obj != null && obj instanceof Holder)
            return ((Holder) obj).value;
        return obj;
    }

    public static void setHolderValue(Object obj, Object value) {
        if (obj instanceof Holder)
            ((Holder) obj).value = value;
        else
            obj = value;
    }

    public String getPartName() {
        if(partName == null)
            return name.getLocalPart();
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }
    
    protected ParameterBinding binding;
    protected ParameterBinding outBinding;
    protected int index;
    protected Mode mode;
    protected TypeReference typeReference;
    protected QName name;
    protected String partName;
}
