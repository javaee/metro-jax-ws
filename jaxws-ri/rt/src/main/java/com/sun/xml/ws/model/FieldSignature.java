/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.model;

import java.lang.reflect.*;

/**
 * Creates vm signature string from Type
 *
 * TypeSignature: Z | C | B | S | I | F | J | D | FieldTypeSignature
 * FieldTypeSignature: ClassTypeSignature | [ TypeSignature | TypeVar
 * ClassTypeSignature: L Id ( / Id )* TypeArgs? ( . Id TypeArgs? )* ;
 * TypeArgs: < TypeArg+ >
 * TypeArg: * | ( + | - )? FieldTypeSignature
 * TypeVar: T Id ;
 *
 * @author Jitendra Kotamraju
 */
final class FieldSignature {

    static String vms(Type t) {
        if (t instanceof Class && ((Class)t).isPrimitive()) {
            Class c = (Class)t;
            if (c == Integer.TYPE) {
                return "I";
            } else if (c == Void.TYPE) {
                return "V";
            } else if (c == Boolean.TYPE) {
                return "Z";
            } else if (c == Byte.TYPE) {
                return "B";
            } else if (c == Character.TYPE) {
                return "C";
            } else if (c == Short.TYPE) {
                return "S";
            } else if (c == Double.TYPE) {
                return "D";
            } else if (c == Float.TYPE) {
                return "F";
            } else if (c == Long.TYPE) {
                return "J";
            }
        } else if (t instanceof Class && ((Class)t).isArray()) {
            return "["+vms(((Class)t).getComponentType());
        } else if (t instanceof Class || t instanceof ParameterizedType) {
            return "L"+fqcn(t)+";";
        } else if (t instanceof GenericArrayType) {
            return "["+vms(((GenericArrayType)t).getGenericComponentType());
        } else if (t instanceof TypeVariable) {
            // While creating wrapper bean fields, it doesn't create with TypeVariables
            // Otherwise, the type variable need to be declared in the wrapper bean class
            // return "T"+((TypeVariable)t).getName()+";";
            return "Ljava/lang/Object;";
        } else if (t instanceof WildcardType) {
            WildcardType w = (WildcardType)t;
            if (w.getLowerBounds().length > 0) {
                return "-"+vms(w.getLowerBounds()[0]);
            } else if (w.getUpperBounds().length > 0) {
                Type wt = w.getUpperBounds()[0];
                if (wt.equals(Object.class)) {
                    return "*";
                } else {
                    return "+"+vms(wt);
                }
            }
        }
        throw new IllegalArgumentException("Illegal vms arg " + t);
    }

    private static String fqcn(Type t) {
        if (t instanceof Class) {
            Class c = (Class)t;
            if (c.getDeclaringClass() == null) {
                return c.getName().replace('.', '/');
            } else {
                return fqcn(c.getDeclaringClass())+"$"+c.getSimpleName();
            }
        } else if (t instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType)t;
            if (p.getOwnerType() == null) {
                return fqcn(p.getRawType())+args(p);
            } else {
                assert p.getRawType() instanceof Class;
                return fqcn(p.getOwnerType())+"."+
                        ((Class)p.getRawType()).getSimpleName()+args(p);
            }
        }
        throw new IllegalArgumentException("Illegal fqcn arg = "+t);
    }

    private static String args(ParameterizedType p) {
        String sig = "<";
        for(Type t : p.getActualTypeArguments()) {
            sig += vms(t);
        }
        return sig+">";
    }

}
