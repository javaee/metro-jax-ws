/**
 * $Id: WrapperParameter.java,v 1.1 2005-05-23 22:42:10 bbissett Exp $
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
 * @author Vivek Pandey
 *
 * Models Wrapper parameter
 * 
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
