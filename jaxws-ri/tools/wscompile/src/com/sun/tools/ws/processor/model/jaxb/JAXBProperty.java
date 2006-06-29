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

import javax.xml.namespace.QName;

import com.sun.tools.xjc.api.Property;

import com.sun.codemodel.JType;
/**
 * @author Kohsuke Kawaguchi
 */
public class JAXBProperty {

    /**
     * @see Property#name()
     */
    private String name;

    private JAXBTypeAndAnnotation type;
    /**
     * @see Property#elementName()
     */
    private QName elementName;

    /**
     * Default constructor for the persistence.
     */
    public JAXBProperty() {}

    /**
     * Constructor that fills in the values from the given raw model
     */
    JAXBProperty( Property prop ) {
        this.name = prop.name();
        this.type = new JAXBTypeAndAnnotation(prop.type());
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
    
    public JAXBTypeAndAnnotation getType() {
        return type;
    }

    /**
     * @see Property#elementName()
     */
    public QName getElementName() {
        return elementName;
    }
}
