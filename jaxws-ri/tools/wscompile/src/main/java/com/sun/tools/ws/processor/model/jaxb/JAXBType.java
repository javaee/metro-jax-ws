/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.ws.processor.model.jaxb;

import com.sun.tools.ws.processor.model.AbstractType;
import com.sun.tools.ws.processor.model.java.JavaType;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Top-level binding between JAXB generated Java type
 * and XML Schema element declaration.
 *
 * @author
 *     Vivek Pandey
 */
public class JAXBType extends AbstractType{
    public JAXBType(JAXBType jaxbType){
        setName(jaxbType.getName());
        this.jaxbMapping = jaxbType.getJaxbMapping();
        this.jaxbModel = jaxbType.getJaxbModel();
        init();
    }

    public JAXBType(){}

    public JAXBType(QName name, JavaType type){
        super(name, type);
    }

    public JAXBType(QName name, JavaType type, JAXBMapping jaxbMapping, JAXBModel jaxbModel){
        super(name, type);
        this.jaxbMapping = jaxbMapping;
        this.jaxbModel = jaxbModel;
        init();
    }

    public void accept(JAXBTypeVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    private void init() {
        if (jaxbMapping != null)
            wrapperChildren = jaxbMapping.getWrapperStyleDrilldown();
        else
            wrapperChildren =  new ArrayList<JAXBProperty>();
    }

    public boolean isUnwrappable(){
        return jaxbMapping != null && jaxbMapping.getWrapperStyleDrilldown() != null;
    }

    public boolean hasWrapperChildren(){
        return wrapperChildren.size() > 0;
    }

    public boolean isLiteralType() {
        return true;
    }

    public List<JAXBProperty> getWrapperChildren(){
        return wrapperChildren;
    }

    public void setWrapperChildren(List<JAXBProperty> children) {
        wrapperChildren = children;
    }

    public JAXBMapping getJaxbMapping() {
        return jaxbMapping;
    }

    public void setJaxbMapping(JAXBMapping jaxbMapping) {
        this.jaxbMapping = jaxbMapping;
        init();
    }

    public void setUnwrapped(boolean unwrapped) {
        this.unwrapped = unwrapped;
    }

    public boolean isUnwrapped() {
        return unwrapped;
    }

    private JAXBMapping jaxbMapping;

    public JAXBModel getJaxbModel() {
        return jaxbModel;
    }

    public void setJaxbModel(JAXBModel jaxbModel) {
        this.jaxbModel = jaxbModel;
    }

    private JAXBModel jaxbModel;
    private boolean unwrapped = false;
    private List<JAXBProperty> wrapperChildren;
}
