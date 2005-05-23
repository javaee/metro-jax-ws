/**
 * $Id: JAXBMapping.java,v 1.1 2005-05-23 23:18:52 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.model.jaxb;

import java.util.List;
import java.util.ArrayList;

import com.sun.tools.ws.processor.model.Persistent;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.api.TypeAndAnnotation;

/**
 * @author Kohsuke Kawaguchi, Vivek Pandey
 */
public class JAXBMapping {

    /**
     * @see Mapping#getElement()
     */
    @Persistent
    private QName elementName;

    /**
     * @see Mapping#getTypeClass()
     */
    @Persistent
    private String typeClass;

    private List<String> annotations;

    /**
     * @see Mapping#getWrapperStyleDrilldown()
     */
    @Persistent
    private List<JAXBProperty> wrapperStyleDrilldown;

    /**
     * Default constructor for the persistence.
     */
    public JAXBMapping() {}

    /**
     * Constructor that fills in the values from the given raw model
     */
    /*package*/ JAXBMapping( com.sun.tools.xjc.api.Mapping rawModel ) {
        elementName = rawModel.getElement();
        TypeAndAnnotation typeAndAnno = rawModel.getType();
        typeClass = typeAndAnno.getTypeClass();
        annotations = typeAndAnno.getAnnotations();
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

    /**
     * @see Mapping#getTypeClass()
     */
    public String getTypeClass() {
        return typeClass;
    }

    public void setTypeClass(String typeClass) {
        this.typeClass = typeClass;
    }


    /**
     * @return Returns the annotations.
     */
    public List<String> getAnnotations() {
        return annotations;
    }

    /**
     * @param annotations The annotations to set.
     */
    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }


    /**
     * @see Mapping#getWrapperStyleDrilldown()
     */
    public List<JAXBProperty> getWrapperStyleDrilldown() {
        return wrapperStyleDrilldown;
    }

    public void setWrapperStyleDrilldown(List<JAXBProperty> wrapperStyleDrilldown) {
        this.wrapperStyleDrilldown = wrapperStyleDrilldown;
    }
}
