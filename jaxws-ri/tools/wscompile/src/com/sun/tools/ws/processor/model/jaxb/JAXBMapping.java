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

import java.util.List;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.codemodel.JType;

/**
 * @author Kohsuke Kawaguchi, Vivek Pandey
 */
public class JAXBMapping {

    /**
     * @see Mapping#getElement()
     */
    private QName elementName;

    /**
     *
     */
    private JAXBTypeAndAnnotation type;

    /**
     * @see Mapping#getWrapperStyleDrilldown()
     */
    private List<JAXBProperty> wrapperStyleDrilldown;

    /**
     * Default constructor for the persistence.
     */
    public JAXBMapping() {}

    /**
     * Constructor that fills in the values from the given raw model
     */
    JAXBMapping( com.sun.tools.xjc.api.Mapping rawModel ) {
        elementName = rawModel.getElement();
        TypeAndAnnotation typeAndAnno = rawModel.getType();
        type = new JAXBTypeAndAnnotation(typeAndAnno);
        List<? extends Property> list = rawModel.getWrapperStyleDrilldown();
        if(list==null)
            wrapperStyleDrilldown = null;
        else {
            wrapperStyleDrilldown = new ArrayList<JAXBProperty>(list.size());
            for( Property p : list )
                wrapperStyleDrilldown.add(new JAXBProperty(p));
        }

    }

    /**
     * @see Mapping#getElement()
     */
    public QName getElementName() {
        return elementName;
    }

    public void setElementName(QName elementName) {
        this.elementName = elementName;
    }


    public JAXBTypeAndAnnotation getType() {
        return type;
    }

    /**
     * @see Mapping#getWrapperStyleDrilldown()
     */
    public List<JAXBProperty> getWrapperStyleDrilldown() {
        return wrapperStyleDrilldown;
    }
}
