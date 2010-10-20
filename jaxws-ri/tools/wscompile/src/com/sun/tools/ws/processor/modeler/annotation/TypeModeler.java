/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;
import com.sun.xml.ws.util.StringUtils;

import java.util.*;


/**
 *
 * @author WS Development Team
 */
public class TypeModeler implements WebServiceConstants {

    public static TypeDeclaration getDeclaration(TypeMirror typeMirror) {
        if (typeMirror instanceof DeclaredType)
            return ((DeclaredType)typeMirror).getDeclaration();
        return null;
    }

    public static TypeDeclaration getDeclaringClassMethod(
        TypeMirror theClass,
        String methodName,
        TypeMirror[] args) {

        return getDeclaringClassMethod(getDeclaration(theClass), methodName, args);
    }

    public static TypeDeclaration getDeclaringClassMethod(
        TypeDeclaration theClass,
        String methodName,
        TypeMirror[] args) {

        TypeDeclaration retClass = null;
        if (theClass instanceof ClassDeclaration) {
            ClassType superClass = ((ClassDeclaration)theClass).getSuperclass();
            if (superClass != null)
                retClass = getDeclaringClassMethod(superClass, methodName, args);
        }
        if (retClass == null) {
            for (InterfaceType interfaceType : theClass.getSuperinterfaces())
                retClass =
                    getDeclaringClassMethod(interfaceType, methodName, args);
        }
        if (retClass == null) {
            Collection<? extends MethodDeclaration> methods;
            methods = theClass.getMethods();
            for (MethodDeclaration method : methods) {
                if (method.getSimpleName().equals(methodName) &&
                    method.getDeclaringType().equals(theClass)) {
                    retClass = theClass;
                    break;
                }
            }
        }
        return retClass;
    }

    public static Collection<InterfaceType> collectInterfaces(TypeDeclaration type) {
        Collection<InterfaceType> superInterfaces = type.getSuperinterfaces();
        Collection<InterfaceType> interfaces = type.getSuperinterfaces();
        for (InterfaceType interfaceType : superInterfaces) {
            interfaces.addAll(collectInterfaces(getDeclaration(interfaceType)));
        }
        return interfaces;
    }

    public static boolean isSubclass(String subTypeName, String superTypeName,
        AnnotationProcessorEnvironment env) {
        return isSubclass(env.getTypeDeclaration(subTypeName),
                          env.getTypeDeclaration(superTypeName));
    }

    public static boolean isSubclass(
        TypeDeclaration subType,
        TypeDeclaration superType) {

        if (subType.equals(superType))
            return false;
        return isSubtype(subType, superType);
    }

    public static TypeMirror getHolderValueType(
        TypeMirror type,
        TypeDeclaration defHolder
    ) {

        TypeDeclaration typeDecl = getDeclaration(type);
        if (typeDecl == null)
            return null;

        if (isSubtype(typeDecl, defHolder)) {
            if  (type instanceof DeclaredType) {
                Collection<TypeMirror> argTypes = ((DeclaredType)type).getActualTypeArguments();
                if (argTypes.size() == 1) {
                    TypeMirror mirror = argTypes.iterator().next();
//                        System.out.println("argsTypes.iterator().next(): "+mirror);
                    return mirror;
                }
                else if (argTypes.size() == 0) {
                    FieldDeclaration member = getValueMember(typeDecl);
                    if (member != null) {
//                            System.out.println("member: "+member+" getType(): "+member.getType());
                        return member.getType();
                    }
                }
            }
        }
        return null;
    }

    public static FieldDeclaration getValueMember(TypeMirror classType) {
        return getValueMember(getDeclaration(classType));
    }

    public static FieldDeclaration getValueMember(TypeDeclaration type) {
        FieldDeclaration member = null;
        for (FieldDeclaration field : type.getFields()){
            if (field.getSimpleName().equals("value")) {
                member = field;
                break;
            }
        }
        if (member == null) {
            if (type instanceof ClassDeclaration)
                member = getValueMember(((ClassDeclaration)type).getSuperclass());
        }
        return member;
    }


    /* is d1 a subtype of d2 */
    public static boolean isSubtype(TypeDeclaration d1, TypeDeclaration d2) {
        if (d1.equals(d2))
            return true;
        ClassDeclaration superClassDecl = null;
        if (d1 instanceof ClassDeclaration) {
            ClassType superClass = ((ClassDeclaration)d1).getSuperclass();
            if (superClass != null) {
                superClassDecl = superClass.getDeclaration();
                if (superClassDecl.equals(d2))
                    return true;
            }
        }
        for (InterfaceType superIntf : d1.getSuperinterfaces()) {
            if (superIntf.getDeclaration().equals(d2)) {
                return true;
            }
            if (isSubtype(superIntf.getDeclaration(), d2)) {
                return true;
            } else if (superClassDecl != null && isSubtype(superClassDecl, d2)) {
                return true;
            }
        }
        return false;
    }

}

