/*
 * $Id: Parameter.java,v 1.1 2005-05-24 13:53:28 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wsdl.document.jaxrpc;

import javax.xml.namespace.QName;

/**
 * @author Vivek Pandey
 *
 * class representing jaxrpc:parameter
 *
 */
public class Parameter {
    private String part;
    private QName element;
    private String name;

    /**
     * @param part
     * @param element
     * @param name
     */
    public Parameter(String part, QName element, String name) {
        super();
        this.part = part;
        this.element = element;
        this.name = name;
    }

    /**
     * @return Returns the element.
     */
    public QName getElement() {
        return element;
    }

    /**
     * @param element The element to set.
     */
    public void setElement(QName element) {
        this.element = element;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the part.
     */
    public String getPart() {
        return part;
    }

    /**
     * @param part The part to set.
     */
    public void setPart(String part) {
        this.part = part;
    }
}
