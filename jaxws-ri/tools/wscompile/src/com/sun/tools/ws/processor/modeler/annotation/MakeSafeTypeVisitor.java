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

package com.sun.tools.ws.processor.modeler.annotation;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;


import com.sun.istack.tools.APTTypeVisitor;

import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.type.WildcardType;
import com.sun.mirror.util.Types;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author dkohlert
 */
public class MakeSafeTypeVisitor extends APTTypeVisitor<TypeMirror, Types> {
    

    /**
     * Creates a new instance of MakeSafeTypeVisitor
     */
    public MakeSafeTypeVisitor() {
    }
    
    protected TypeMirror onArrayType(ArrayType type, Types apTypes) {
        TypeMirror itemType = apply(type.getComponentType(), apTypes);
        return apTypes.getArrayType(itemType);
    }
    
    protected TypeMirror onPrimitiveType(PrimitiveType type, Types apTypes) {
        return type;
    }
     
    protected TypeMirror onClassType(ClassType type, Types apTypes) {
        return processDeclaredType(type, apTypes);
    }
    
    protected TypeMirror onInterfaceType(InterfaceType type, Types apTypes) {
        return processDeclaredType(type, apTypes);        
    }
    
    private TypeMirror processDeclaredType(DeclaredType type, Types apTypes) {
        Collection<TypeMirror> args = type.getActualTypeArguments();
        TypeMirror[] safeArgs = new TypeMirror[args.size()];
        int i = 0;
        for (TypeMirror arg : args) {
            safeArgs[i++]= apply(arg, apTypes);                    
        }
        return apTypes.getDeclaredType(type.getDeclaration(), safeArgs);
    }
    
    protected TypeMirror onTypeVariable(TypeVariable type, Types apTypes) {
        return apTypes.getErasure(type);        
    }
    
    protected TypeMirror onVoidType(VoidType type, Types apTypes) {
        return type;        
    }

    protected TypeMirror onWildcard(WildcardType type, Types apTypes) {
        Collection<ReferenceType> bounds = type.getLowerBounds();
        Collection<ReferenceType> safeLowerBounds = new ArrayList<ReferenceType>(bounds.size());
        for (ReferenceType lType : bounds) {
            safeLowerBounds.add((ReferenceType)apply(lType, apTypes));
        }
        bounds = type.getUpperBounds();
        Collection<ReferenceType> safeUpperBounds = new ArrayList<ReferenceType>(bounds.size());
        for (ReferenceType lType : bounds) {
            safeUpperBounds.add((ReferenceType)apply(lType, apTypes));
        }
        return apTypes.getWildcardType(safeUpperBounds, safeLowerBounds);
    }
}
