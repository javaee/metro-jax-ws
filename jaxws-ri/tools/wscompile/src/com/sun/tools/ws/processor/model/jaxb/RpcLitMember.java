/**
 * $Id: RpcLitMember.java,v 1.1 2005-05-23 23:18:53 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.model.jaxb;

import javax.xml.namespace.QName;

import com.sun.tools.ws.processor.model.AbstractType;

/**
 * @author Vivek Pandey
 *
 * Represents RpcLit member parts
 */
public class RpcLitMember extends AbstractType {

    //wsdl:part type attribute java mapped object
    private String javaTypeName;
    private QName schemaTypeName;

    /**
     *
     */
    public RpcLitMember() {
        super();
        // TODO Auto-generated constructor stub
    }
    public RpcLitMember(QName name, String javaTypeName){
        setName(name);
        this.javaTypeName = javaTypeName;
    }
    public RpcLitMember(QName name, String javaTypeName, QName schemaTypeName){
        setName(name);
        this.javaTypeName = javaTypeName;
        this.schemaTypeName = schemaTypeName;
    }

    /**
     * @return Returns the type.
     */
    public String getJavaTypeName() {
        return javaTypeName;
    }
    /**
     * @param type The type to set.
     */
    public void setJavaTypeName(String type) {
        this.javaTypeName = type;
    }

    /**
     * @return Returns the type.
     */
    public QName getSchemaTypeName() {
        return schemaTypeName;
    }
    /**
     * @param type The type to set.
     */
    public void setSchemaTypeName(QName type) {
        this.schemaTypeName = type;
    }
}
