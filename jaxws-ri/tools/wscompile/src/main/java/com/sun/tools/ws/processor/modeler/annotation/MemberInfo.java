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

package com.sun.tools.ws.processor.modeler.annotation;

import javax.lang.model.type.TypeMirror;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 * @author  WS Development Team
 */
final class MemberInfo implements Comparable<MemberInfo> {
    private final TypeMirror paramType;
    private final String paramName;
    private final List<Annotation> jaxbAnnotations;

    public MemberInfo(TypeMirror paramType, String paramName, List<Annotation> jaxbAnnotations) {
        this.paramType = paramType;
        this.paramName = paramName;
        this.jaxbAnnotations = jaxbAnnotations;
    }

    public List<Annotation> getJaxbAnnotations() {
        return jaxbAnnotations;
    }

    public TypeMirror getParamType() {
        return paramType;
    }

    public String getParamName() {
        return paramName;
    }

    @Override
    public int compareTo(MemberInfo member) {
        return paramName.compareTo(member.paramName);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.paramType != null ? this.paramType.hashCode() : 0);
        hash = 47 * hash + (this.paramName != null ? this.paramName.hashCode() : 0);
        hash = 47 * hash + (this.jaxbAnnotations != null ? this.jaxbAnnotations.hashCode() : 0);
        return hash;
    }

}
