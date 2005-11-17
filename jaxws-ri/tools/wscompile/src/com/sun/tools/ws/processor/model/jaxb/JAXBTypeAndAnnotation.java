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
package com.sun.tools.ws.processor.model.jaxb;

import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.codemodel.JType;
import com.sun.codemodel.JAnnotatable;

/**
 * Holds JAXB JType and TypeAndAnnotation. This provides abstration over
 * types from JAXBMapping and Property.
 */
public class JAXBTypeAndAnnotation {
    TypeAndAnnotation typeAnn;
    JType type;

    public JAXBTypeAndAnnotation(TypeAndAnnotation typeAnn) {
        this.typeAnn = typeAnn;
        this.type = typeAnn.getTypeClass();
    }

    public JAXBTypeAndAnnotation(JType type) {
        this.type = type;
    }

    public JAXBTypeAndAnnotation(TypeAndAnnotation typeAnn, JType type) {
        this.typeAnn = typeAnn;
        this.type = type;
    }

    public void annotate(JAnnotatable typeVar) {
        if(typeAnn != null)
            typeAnn.annotate(typeVar);
    }

    public JType getType() {
        return type;
    }

    public String getName(){
        return type.fullName();
    }

    public TypeAndAnnotation getTypeAnn() {
        return typeAnn;
    }

    public void setTypeAnn(TypeAndAnnotation typeAnn) {
        this.typeAnn = typeAnn;
    }

    public void setType(JType type) {
        this.type = type;
    }
}
