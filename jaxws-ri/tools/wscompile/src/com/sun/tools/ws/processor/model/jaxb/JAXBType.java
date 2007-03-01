/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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
        return getJaxbMapping().getWrapperStyleDrilldown() != null;
    }

    public boolean hasWrapperChildren(){
        return (getWrapperChildren().size() > 0) ? true : false;
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
