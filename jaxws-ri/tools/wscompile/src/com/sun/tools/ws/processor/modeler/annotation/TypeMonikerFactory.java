/**
 * $Id: TypeMonikerFactory.java,v 1.2 2005-06-01 00:38:30 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.modeler.annotation;


import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.*;
import com.sun.mirror.util.Types;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;

import java.util.Collection;
import java.util.ArrayList;
import com.sun.tools.ws.processor.modeler.annotation.*;

/**
 *
 * @author  dkohlert
 */
public class TypeMonikerFactory {

    public static TypeMoniker getTypeMoniker(TypeMirror typeMirror) {
        if (typeMirror instanceof PrimitiveType)
            return new PrimitiveTypeMoniker((PrimitiveType)typeMirror);
        else if (typeMirror instanceof ArrayType)
            return new ArrayTypeMoniker((ArrayType)typeMirror);
        else if (typeMirror instanceof DeclaredType)
            return new DeclaredTypeMoniker((DeclaredType)typeMirror);
        return getTypeMoniker(typeMirror.toString());
    }

    public static TypeMoniker getTypeMoniker(String typeName) {
        return new StringMoniker(typeName);
    }

    static class ArrayTypeMoniker implements TypeMoniker {
        private TypeMoniker arrayType;

        public ArrayTypeMoniker(ArrayType type) {
            arrayType = TypeMonikerFactory.getTypeMoniker(type.getComponentType());
        }

        public TypeMirror create(AnnotationProcessorEnvironment apEnv) {
            return apEnv.getTypeUtils().getArrayType(arrayType.create(apEnv));
        }
    }
    static class DeclaredTypeMoniker implements TypeMoniker {
        private String typeDeclName;
        private Collection<TypeMoniker> typeArgs = new ArrayList<TypeMoniker>();

        public DeclaredTypeMoniker(DeclaredType type) {
            typeDeclName = type.getDeclaration().getQualifiedName();
            for (TypeMirror arg : type.getActualTypeArguments())
                typeArgs.add(TypeMonikerFactory.getTypeMoniker(arg));
        }

        public TypeMirror create(AnnotationProcessorEnvironment apEnv) {
            TypeDeclaration typeDecl = apEnv.getTypeDeclaration(typeDeclName);
            TypeMirror[] tmpArgs = new TypeMirror[typeArgs.size()];
            int idx = 0;
            for (TypeMoniker moniker : typeArgs)
                tmpArgs[idx++] = moniker.create(apEnv);

            return apEnv.getTypeUtils().getDeclaredType(typeDecl, tmpArgs);
        }
    }
    static class PrimitiveTypeMoniker implements TypeMoniker {
        private PrimitiveType.Kind kind;

        public PrimitiveTypeMoniker(PrimitiveType type) {
            kind = type.getKind();
        }

        public TypeMirror create(AnnotationProcessorEnvironment apEnv) {
            return apEnv.getTypeUtils().getPrimitiveType(kind);
        }
    }
    static class StringMoniker implements TypeMoniker {
        private String typeName;

        public StringMoniker(String typeName) {
            this.typeName = typeName;
        }

        public TypeMirror create(AnnotationProcessorEnvironment apEnv) {
            return apEnv.getTypeUtils().getDeclaredType(apEnv.getTypeDeclaration(typeName));
        }
    }
}
