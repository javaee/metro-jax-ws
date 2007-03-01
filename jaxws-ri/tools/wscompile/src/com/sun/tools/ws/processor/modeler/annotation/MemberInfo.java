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
package com.sun.tools.ws.processor.modeler.annotation;

import com.sun.istack.NotNull;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.type.TypeMirror;

import javax.xml.namespace.QName;
import java.lang.annotation.Annotation;

/**
 *
 * @author  WS Development Team
 */
public final class MemberInfo implements Comparable<MemberInfo> {
    private final TypeMirror paramType;
    private final String paramName;
    private final QName elementName;
    private final Annotation[] jaxbAnnotations;
    /**
     * Use this to look up annotations on this parameter/return type.
     */
    private final Declaration decl;

    public MemberInfo(TypeMirror paramType, String paramName, QName elementName, @NotNull Declaration decl, Annotation... jaxbAnnotations) {
        this.paramType = paramType;
        this.paramName = paramName;
        this.elementName = elementName;
        this.decl = decl;
        this.jaxbAnnotations = jaxbAnnotations;
    }


    public Annotation[] getJaxbAnnotations() {
        return jaxbAnnotations;
    }

    public TypeMirror getParamType() {
        return paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public QName getElementName() {
        return elementName;
    }

    public @NotNull Declaration getDecl() {
        return decl;
    }

    public int compareTo(MemberInfo member) {
        return paramName.compareTo(member.paramName);
    }
}
