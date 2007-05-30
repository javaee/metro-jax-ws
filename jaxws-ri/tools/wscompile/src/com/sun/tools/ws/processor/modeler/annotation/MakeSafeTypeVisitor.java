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
