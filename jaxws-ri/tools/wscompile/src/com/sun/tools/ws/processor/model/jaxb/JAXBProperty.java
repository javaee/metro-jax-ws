/**
 * $Id: JAXBProperty.java,v 1.1 2005-05-23 23:18:52 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.model.jaxb;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.api.Property;

import com.sun.tools.ws.processor.model.Persistent;
/**
 * @author Kohsuke Kawaguchi
 */
public class JAXBProperty {

    /**
     * @see Property#name()
     */
    @Persistent
    private String name;
    /**
     * @see Property#type()
     */
    @Persistent
    private String type;
    /**
     * @see Property#elementName()
     */
    @Persistent
    private QName elementName;

    /**
     * Default constructor for the persistence.
     */
    public JAXBProperty() {}

    /**
     * Constructor that fills in the values from the given raw model
     */
    /*package*/ JAXBProperty( Property prop ) {
        this.name = prop.name();
        this.type = prop.type();
        this.elementName = prop.elementName();
    }

    /**
     * @see Property#name()
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see Property#type()
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @see Property#elementName()
     */
    public QName getElementName() {
        return elementName;
    }

    public void setElementName(QName elementName) {
        this.elementName = elementName;
    }
}
