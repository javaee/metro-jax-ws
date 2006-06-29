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

package com.sun.tools.ws.processor.model.java;

/**
 *
 * @author WS Development Team
 */
public class JavaStructureMember {

    public JavaStructureMember() {}

    public JavaStructureMember(String name, JavaType type, Object owner) {
        this(name, type, owner, false);
    }
    public JavaStructureMember(String name, JavaType type,
        Object owner, boolean isPublic) {

        this.name = name;
        this.type = type;
        this.owner = owner;
        this.isPublic = isPublic;
        constructorPos = -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public JavaType getType() {
        return type;
    }

    public void setType(JavaType t) {
        type = t;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean b) {
        isPublic = b;
    }

    public boolean isInherited() {
        return isInherited;
    }

    public void setInherited(boolean b) {
        isInherited = b;
    }

    public String getReadMethod() {
        return readMethod;
    }

    public void setReadMethod(String readMethod) {
        this.readMethod = readMethod;
    }

    public String getWriteMethod() {
        return writeMethod;
    }

    public void setWriteMethod(String writeMethod) {
        this.writeMethod = writeMethod;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }
    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    public Object getOwner() {
        return owner;
    }

    public void setOwner(Object owner) {
        this.owner = owner;
    }

    public int getConstructorPos() {
        return constructorPos;
    }

    public void setConstructorPos(int idx) {
        constructorPos = idx;
    }

    private String name;
    private JavaType type;
    private boolean isPublic = false;
    private boolean isInherited = false;
    private String readMethod;
    private String writeMethod;
    private String declaringClass;
    private Object owner;
    private int constructorPos;
}
