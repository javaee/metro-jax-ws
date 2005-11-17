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
     * @see com.sun.xml.ws.rt.model.Parameter#isWrapperStyle()
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

    /**
     * removes the wrapper child from the given index
     * @param index
     * @return
     */
    public Parameter removeWrapperChild(int index){
        return wrapperChildren.remove(index);
    }

    public void clear(){
        wrapperChildren.clear();
    }
    protected final List<Parameter> wrapperChildren = new ArrayList<Parameter>();
}
