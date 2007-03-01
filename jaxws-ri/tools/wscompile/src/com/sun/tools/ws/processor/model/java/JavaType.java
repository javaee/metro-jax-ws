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

import com.sun.tools.ws.processor.model.jaxb.JAXBTypeAndAnnotation;

/**
 *
 * @author WS Development Team
 */
public abstract class JavaType {

    private String name;
    private String realName;
    private boolean present;
    private boolean holder;
    private boolean holderPresent;
    private String initString;
    private String holderName;
    private JAXBTypeAndAnnotation type;

    public JavaType() {}

    public JavaType(JAXBTypeAndAnnotation type){
        this.type = type;
        init(type.getName(), false, null, null);
    }

    public JavaType(String name, boolean present, String initString) {
        init(name, present, initString, null);
    }

    public JavaType(String name, boolean present, String initString,
        String holderName) {

        init(name, present, initString, holderName);
    }

    public JAXBTypeAndAnnotation getType(){
        return type;
    }

    private void init(String name, boolean present, String initString,
        String holderName) {

        this.realName = name;
        this.name = name.replace('$', '.');
        this.present = present;
        this.initString = initString;
        this.holderName = holderName;
        holder = holderName != null;
    }

    public String getName() {
        return name;
    }

    public void doSetName(String name) {

        // renamed to avoid creating a "name" property with broken semantics
        this.realName = name;
        this.name = name.replace('$', '.');
    }

    public String getRealName() {
        return realName;
    }

    /* serialization */
    public void setRealName(String s) {
        realName = s;
    }

    public String getFormalName() {
        return name;
    }

    public void setFormalName(String s) {
        name = s;
    }

    public boolean isPresent() {
        return present;
    }

    /* serialization */
    public void setPresent(boolean b) {
        present = b;
    }

    public boolean isHolder() {
        return holder;
    }

    public void setHolder(boolean holder) {
        this.holder = holder;
    }

    public boolean isHolderPresent() {
        return holderPresent;
    }
    public void setHolderPresent(boolean holderPresent) {
        this.holderPresent = holderPresent;
    }

    public String getInitString() {
        return initString;
    }

    /* serialization */
    public void setInitString(String s) {
        initString = s;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }
}
