/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
