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

import com.sun.istack.tools.APTTypeVisitor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.*;
import com.sun.mirror.util.Types;

import java.util.Collection;

/**
 *
 * @author dkohlert
 */
public class MakeSafeTypeVisitor extends APTTypeVisitor<TypeMirror, Types> implements WebServiceConstants {
    TypeDeclaration collectionDecl;
    TypeDeclaration mapDecl;

    /**
     * Creates a new instance of MakeSafeTypeVisitor
     */
    public MakeSafeTypeVisitor(AnnotationProcessorEnvironment apEnv) {
        collectionDecl = apEnv.getTypeDeclaration(COLLECTION_CLASSNAME);        
        mapDecl = apEnv.getTypeDeclaration(MAP_CLASSNAME);
    }
    
    protected TypeMirror onArrayType(ArrayType type, Types apTypes) {
        return apTypes.getErasure(type);   
    }
    
    protected TypeMirror onPrimitiveType(PrimitiveType type, Types apTypes) {
        return apTypes.getErasure(type);   
    }
     
    protected TypeMirror onClassType(ClassType type, Types apTypes) {
        return processDeclaredType(type, apTypes);
    }
    
    protected TypeMirror onInterfaceType(InterfaceType type, Types apTypes) {
        return processDeclaredType(type, apTypes);        
    }
    
    private TypeMirror processDeclaredType(DeclaredType type, Types apTypes) {
        if (TypeModeler.isSubtype(type.getDeclaration(), collectionDecl) ||
            TypeModeler.isSubtype(type.getDeclaration(), mapDecl)) {
            Collection<TypeMirror> args = type.getActualTypeArguments();
            TypeMirror[] safeArgs = new TypeMirror[args.size()];
            int i = 0;
            for (TypeMirror arg : args) {
                safeArgs[i++]= apply(arg, apTypes);                    
            }
            return apTypes.getDeclaredType(type.getDeclaration(), safeArgs);
        }
        return apTypes.getErasure(type);
    }
    
    protected TypeMirror onTypeVariable(TypeVariable type, Types apTypes) {
        return apTypes.getErasure(type);        
    }
    
    protected TypeMirror onVoidType(VoidType type, Types apTypes) {
        return type;        
    }

    protected TypeMirror onWildcard(WildcardType type, Types apTypes) {
        return apTypes.getErasure(type);   
    }
}
