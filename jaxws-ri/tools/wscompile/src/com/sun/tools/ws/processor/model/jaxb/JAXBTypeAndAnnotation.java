/**
 * $Id: JAXBTypeAndAnnotation.java,v 1.1 2005-07-21 01:51:49 vivekp Exp $
 */
package com.sun.tools.ws.processor.model.jaxb;

import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.codemodel.JType;
import com.sun.codemodel.JAnnotatable;

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

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
}
