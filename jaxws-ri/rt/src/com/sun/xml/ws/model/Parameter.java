/**
 * $Id: Parameter.java,v 1.1 2005-05-23 22:42:10 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.model;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.model.soap.SOAPBlock;

/**
 * @author Vivek Pandey
 * 
 * runtime Parameter that abstracts the annotated java parameter
 * 
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
     * @return
     */
    public Object getBinding() {
        if(binding == null)
            return SOAPBlock.BODY;
        return binding;
    }

    /**
     * @param binding
     */
    public void setBinding(Object binding) {
        this.binding = binding;
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
     * @param value
     * @return
     */
    public Object getHolderValue(Object obj) {
        if (obj instanceof Holder)
            return ((Holder) obj).value;
        else if (obj instanceof Holder) {
            try {
                return obj.getClass().getField("value").get(obj);
            } catch (Exception e) {
                // TODO dosomething with this.
                e.printStackTrace();
            }
        }
        return obj;
    }

    public static void setHolderValue(Object obj, Object value) {
        if (obj instanceof Holder)
            ((Holder) obj).value = value;
        else
            obj = value;
    }

    protected Object binding;

    protected int index;

    protected Mode mode;

    protected TypeReference typeReference;

    protected QName name;
}
