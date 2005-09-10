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
