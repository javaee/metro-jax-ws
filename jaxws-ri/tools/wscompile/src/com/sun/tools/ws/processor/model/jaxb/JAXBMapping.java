/**
 * $Id: JAXBMapping.java,v 1.3 2005-09-01 21:06:19 arungupta Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
