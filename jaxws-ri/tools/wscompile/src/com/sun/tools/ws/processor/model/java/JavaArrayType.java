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

package com.sun.tools.ws.processor.model.java;

/**
 *
 * @author WS Development Team
 */
public class JavaArrayType extends JavaType {

    public JavaArrayType() {
    }

    public JavaArrayType(String name) {
        super(name, true, "null");
    }

    public JavaArrayType(String name, String elementName,
        JavaType elementType) {

        super(name, true, "null");
        this.elementName = elementName;
        this.elementType = elementType;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String name) {
        elementName = name;
    }

    public JavaType getElementType() {
        return elementType;
    }

    public void setElementType(JavaType type) {
        elementType = type;
    }

    // bug fix:4904604
    public String getSOAPArrayHolderName() {
        return soapArrayHolderName;
    }

    public void setSOAPArrayHolderName(String holderName) {
        this.soapArrayHolderName = holderName;
    }

    private String elementName;
    private JavaType elementType;
    private String soapArrayHolderName;
}
