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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.Collection;
import javax.lang.model.element.Element;

/**
 * @author WS Development Team
 */
final class TypeModeler {

    private static final String REMOTE = "java.rmi.Remote";
    private static final String REMOTE_EXCEPTION = "java.rmi.RemoteException";

    private TypeModeler() {
    }

    public static TypeElement getDeclaration(TypeMirror typeMirror) {
        if (typeMirror != null && typeMirror.getKind().equals(TypeKind.DECLARED))
            return (TypeElement) ((DeclaredType) typeMirror).asElement();
        return null;
    }

    public static TypeElement getDeclaringClassMethod(TypeMirror theClass, String methodName, TypeMirror[] args) {
        return getDeclaringClassMethod(getDeclaration(theClass), methodName, args);
    }

    public static TypeElement getDeclaringClassMethod(TypeElement theClass, String methodName, TypeMirror[] args) {

        TypeElement retClass = null;
        if (theClass.getKind().equals(ElementKind.CLASS)) {
            TypeMirror superClass = theClass.getSuperclass();
            if (!superClass.getKind().equals(TypeKind.NONE))
                retClass = getDeclaringClassMethod(superClass, methodName, args);
        }
        if (retClass == null) {
            for (TypeMirror interfaceType : theClass.getInterfaces()) {
                retClass = getDeclaringClassMethod(interfaceType, methodName, args);
            }
        }
        if (retClass == null) {
            Collection<? extends ExecutableElement> methods = ElementFilter.methodsIn(theClass.getEnclosedElements());
            for (ExecutableElement method : methods) {
                if (method.getSimpleName().toString().equals(methodName)) {
                    retClass = theClass;
                    break;
                }
            }
        }
        return retClass;
    }

    public static Collection<DeclaredType> collectInterfaces(TypeElement type) {
        @SuppressWarnings({"unchecked"})
        Collection<DeclaredType> interfaces = (Collection<DeclaredType>) type.getInterfaces();
        for (TypeMirror interfaceType : type.getInterfaces()) {
            interfaces.addAll(collectInterfaces(getDeclaration(interfaceType)));
        }
        return interfaces;
    }

    public static boolean isSubclass(String subTypeName, String superTypeName, ProcessingEnvironment env) {
        return isSubclass(env.getElementUtils().getTypeElement(subTypeName), env.getElementUtils().getTypeElement(superTypeName), env);
    }

    public static boolean isSubclass(TypeElement subType, TypeElement superType, ProcessingEnvironment env) {
        return !subType.equals(superType) && isSubElement(subType, superType);
    }

    public static TypeMirror getHolderValueType(TypeMirror type, TypeElement defHolder, ProcessingEnvironment env) {
        TypeElement typeElement = getDeclaration(type);
        if (typeElement == null)
            return null;

        if (isSubElement(typeElement, defHolder)) {
            if (type.getKind().equals(TypeKind.DECLARED)) {
                Collection<? extends TypeMirror> argTypes = ((DeclaredType) type).getTypeArguments();
                if (argTypes.size() == 1) {
                    return argTypes.iterator().next();
                } else if (argTypes.isEmpty()) {
                    VariableElement member = getValueMember(typeElement);
                    if (member != null) {
                        return member.asType();
                    }
                }
            }
        }
        return null;
    }

    public static VariableElement getValueMember(TypeMirror classType) {
        return getValueMember(getDeclaration(classType));
    }

    public static VariableElement getValueMember(TypeElement type) {
        VariableElement member = null;
        for (VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            if ("value".equals(field.getSimpleName().toString())) {
                member = field;
                break;
            }
        }
        if (member == null && type.getKind().equals(ElementKind.CLASS))
            member = getValueMember(type.getSuperclass());
        return member;
    }

    public static boolean isSubElement(TypeElement d1, TypeElement d2) {
        if (d1.equals(d2))
            return true;
        TypeElement superClassDecl = null;
        if (d1.getKind().equals(ElementKind.CLASS)) {
            TypeMirror superClass = d1.getSuperclass();
            if (!superClass.getKind().equals(TypeKind.NONE)) {
                superClassDecl = (TypeElement) ((DeclaredType) superClass).asElement();
                if (superClassDecl.equals(d2))
                    return true;
            }
        }
        for (TypeMirror superIntf : d1.getInterfaces()) {
            DeclaredType declaredSuperIntf = (DeclaredType) superIntf;
            if (declaredSuperIntf.asElement().equals(d2)) {
                return true;
            }
            if (isSubElement((TypeElement) declaredSuperIntf.asElement(), d2)) {
                return true;
            } else if (superClassDecl != null && isSubElement(superClassDecl, d2)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRemoteException(ProcessingEnvironment env, TypeMirror typeMirror) {
        Element element = env.getTypeUtils().asElement(typeMirror);
        if (element.getKind() == ElementKind.CLASS) {
            TypeElement te = (TypeElement) element;
            TypeKind tk = typeMirror.getKind();
            while (tk != TypeKind.NONE && !te.getQualifiedName().contentEquals(REMOTE_EXCEPTION)) {
                TypeMirror superType = te.getSuperclass();
                te = (TypeElement) env.getTypeUtils().asElement(superType);
                tk = superType.getKind();
            }
            return tk != TypeKind.NONE;
        }
        return false;
    }

    public static boolean isRemote(/*@NotNull*/ TypeElement typeElement) {
        for (TypeMirror superType : typeElement.getInterfaces()) {
            TypeElement name = (TypeElement) ((DeclaredType) superType).asElement();
            if (name.getQualifiedName().contentEquals(REMOTE)) {
                return true;
            }
            isRemote(name);
        }
        return false;
    }
}

