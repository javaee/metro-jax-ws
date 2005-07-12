/**
 * $Id: WrapperParameter.java,v 1.2 2005-07-12 23:32:51 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.xml.bind.api.TypeReference;

/**
 * Models Wrapper parameter
 * 
 * @author Vivek Pandey
 */
public class WrapperParameter extends Parameter{
    public WrapperParameter(TypeReference type, Mode mode, int index) {
        super(type, mode, index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.xml.rpc.rt.model.Parameter#isWrapperStyle()
     */
    @Override
    public boolean isWrapperStyle() {
        return true;
    }

    /**
     * @return Returns the wrapperChildren.
     */
    public List<Parameter> getWrapperChildren() {
        return Collections.unmodifiableList(wrapperChildren);
    }

    /**
     * @param wrapperChildren
     *            The wrapperChildren to set.
     */
    public void addWrapperChildren(List<Parameter> wrapperChildren) {
        this.wrapperChildren.addAll(wrapperChildren);
    }

    /**
     * @param wrapperChild
     */
    public void addWrapperChild(Parameter wrapperChild) {
        wrapperChildren.add(wrapperChild);
    }
    
    protected final List<Parameter> wrapperChildren = new ArrayList<Parameter>();
}
