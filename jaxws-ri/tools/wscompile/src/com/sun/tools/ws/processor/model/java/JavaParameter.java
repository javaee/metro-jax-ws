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

package com.sun.tools.ws.processor.model.java;

import com.sun.tools.ws.processor.model.Parameter;

/**
 *
 * @author WS Development Team
 */
public class JavaParameter {

    public JavaParameter() {}

    public JavaParameter(String name, JavaType type, Parameter parameter) {
        this(name, type, parameter, false);
    }

    public JavaParameter(String name, JavaType type, Parameter parameter,
        boolean holder) {

        this.name = name;
        this.type = type;
        this.parameter = parameter;
        this.holder = holder;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public JavaType getType() {
        return type;
    }

    public void setType(JavaType t) {
        type = t;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter p) {
        parameter = p;
    }

    public boolean isHolder() {
        return holder;
    }

    public void setHolder(boolean b) {
        holder = b;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    private String name;
    private JavaType type;
    private Parameter parameter;
    private boolean holder;
    private String holderName;
}
