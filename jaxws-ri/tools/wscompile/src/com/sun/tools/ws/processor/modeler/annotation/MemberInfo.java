/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
