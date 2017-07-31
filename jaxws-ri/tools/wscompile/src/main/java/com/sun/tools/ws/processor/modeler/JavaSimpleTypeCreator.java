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

package com.sun.tools.ws.processor.modeler;

import com.sun.tools.ws.processor.model.java.JavaSimpleType;

import java.util.HashMap;
import java.util.Map;

import static com.sun.tools.ws.processor.modeler.ModelerConstants.*;
/**
 *
 * @author WS Development Team
 */
public final class JavaSimpleTypeCreator {

    /*
     * Mapped JavaSimpleTypes
     */
    public static final JavaSimpleType BOOLEAN_JAVATYPE = new JavaSimpleType(BOOLEAN_CLASSNAME.getValue(), FALSE_STR.getValue());
    public static final JavaSimpleType BOXED_BOOLEAN_JAVATYPE = new JavaSimpleType(BOXED_BOOLEAN_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType BYTE_JAVATYPE = new JavaSimpleType(BYTE_CLASSNAME.getValue(), "(byte)" + ZERO_STR.getValue());
    public static final JavaSimpleType BYTE_ARRAY_JAVATYPE = new JavaSimpleType(BYTE_ARRAY_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType BOXED_BYTE_JAVATYPE = new JavaSimpleType(BOXED_BYTE_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType BOXED_BYTE_ARRAY_JAVATYPE = new JavaSimpleType(BOXED_BYTE_ARRAY_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType DOUBLE_JAVATYPE = new JavaSimpleType(DOUBLE_CLASSNAME.getValue(), ZERO_STR.getValue());
    public static final JavaSimpleType BOXED_DOUBLE_JAVATYPE = new JavaSimpleType(BOXED_DOUBLE_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType FLOAT_JAVATYPE = new JavaSimpleType(FLOAT_CLASSNAME.getValue(), ZERO_STR.getValue());
    public static final JavaSimpleType BOXED_FLOAT_JAVATYPE = new JavaSimpleType(BOXED_FLOAT_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType INT_JAVATYPE = new JavaSimpleType(INT_CLASSNAME.getValue(), ZERO_STR.getValue());
    public static final JavaSimpleType BOXED_INTEGER_JAVATYPE = new JavaSimpleType(BOXED_INTEGER_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType LONG_JAVATYPE = new JavaSimpleType(LONG_CLASSNAME.getValue(), ZERO_STR.getValue());
    public static final JavaSimpleType BOXED_LONG_JAVATYPE = new JavaSimpleType(BOXED_LONG_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType SHORT_JAVATYPE = new JavaSimpleType(SHORT_CLASSNAME.getValue(), "(short)" + ZERO_STR.getValue());
    public static final JavaSimpleType BOXED_SHORT_JAVATYPE = new JavaSimpleType(BOXED_SHORT_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType DECIMAL_JAVATYPE = new JavaSimpleType(BIGDECIMAL_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType BIG_INTEGER_JAVATYPE = new JavaSimpleType(BIGINTEGER_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType CALENDAR_JAVATYPE = new JavaSimpleType(CALENDAR_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType DATE_JAVATYPE = new JavaSimpleType(DATE_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType STRING_JAVATYPE = new JavaSimpleType(STRING_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType STRING_ARRAY_JAVATYPE = new JavaSimpleType(STRING_ARRAY_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType QNAME_JAVATYPE = new JavaSimpleType(QNAME_CLASSNAME.getValue(), NULL_STR.getValue());
    public static final JavaSimpleType VOID_JAVATYPE = new JavaSimpleType(VOID_CLASSNAME.getValue(), null);
    public static final JavaSimpleType OBJECT_JAVATYPE = new JavaSimpleType(OBJECT_CLASSNAME.getValue(), null);
    public static final JavaSimpleType SOAPELEMENT_JAVATYPE = new JavaSimpleType(SOAPELEMENT_CLASSNAME.getValue(), null);
    public static final JavaSimpleType URI_JAVATYPE = new JavaSimpleType(URI_CLASSNAME.getValue(), null);

    // Attachment types
    public static final JavaSimpleType IMAGE_JAVATYPE = new JavaSimpleType(IMAGE_CLASSNAME.getValue(), null);
    public static final JavaSimpleType MIME_MULTIPART_JAVATYPE = new JavaSimpleType(MIME_MULTIPART_CLASSNAME.getValue(), null);
    public static final JavaSimpleType SOURCE_JAVATYPE = new JavaSimpleType(SOURCE_CLASSNAME.getValue(), null);
    public static final JavaSimpleType DATA_HANDLER_JAVATYPE = new JavaSimpleType(DATA_HANDLER_CLASSNAME.getValue(), null);

    // bug fix: 4923650
    private static final Map<String, JavaSimpleType> JAVA_TYPES = new HashMap<String, JavaSimpleType>(31);

    static {
        JAVA_TYPES.put(BOOLEAN_CLASSNAME.getValue(), BOOLEAN_JAVATYPE);
        JAVA_TYPES.put(BOXED_BOOLEAN_CLASSNAME.getValue(), BOXED_BOOLEAN_JAVATYPE);
        JAVA_TYPES.put(BYTE_CLASSNAME.getValue(), BYTE_JAVATYPE);
        JAVA_TYPES.put(BYTE_ARRAY_CLASSNAME.getValue(), BYTE_ARRAY_JAVATYPE);
        JAVA_TYPES.put(BOXED_BYTE_CLASSNAME.getValue(), BOXED_BYTE_JAVATYPE);
        JAVA_TYPES.put(BOXED_BYTE_ARRAY_CLASSNAME.getValue(), BOXED_BYTE_ARRAY_JAVATYPE);
        JAVA_TYPES.put(DOUBLE_CLASSNAME.getValue(), DOUBLE_JAVATYPE);
        JAVA_TYPES.put(BOXED_DOUBLE_CLASSNAME.getValue(), BOXED_DOUBLE_JAVATYPE);
        JAVA_TYPES.put(FLOAT_CLASSNAME.getValue(), FLOAT_JAVATYPE);
        JAVA_TYPES.put(BOXED_FLOAT_CLASSNAME.getValue(), BOXED_FLOAT_JAVATYPE);
        JAVA_TYPES.put(INT_CLASSNAME.getValue(), INT_JAVATYPE);
        JAVA_TYPES.put(BOXED_INTEGER_CLASSNAME.getValue(), BOXED_INTEGER_JAVATYPE);
        JAVA_TYPES.put(LONG_CLASSNAME.getValue(), LONG_JAVATYPE);
        JAVA_TYPES.put(BOXED_LONG_CLASSNAME.getValue(), BOXED_LONG_JAVATYPE);
        JAVA_TYPES.put(SHORT_CLASSNAME.getValue(), SHORT_JAVATYPE);
        JAVA_TYPES.put(BOXED_SHORT_CLASSNAME.getValue(), BOXED_SHORT_JAVATYPE);
        JAVA_TYPES.put(BIGDECIMAL_CLASSNAME.getValue(), DECIMAL_JAVATYPE);
        JAVA_TYPES.put(BIGINTEGER_CLASSNAME.getValue(), BIG_INTEGER_JAVATYPE);
        JAVA_TYPES.put(CALENDAR_CLASSNAME.getValue(), CALENDAR_JAVATYPE);
        JAVA_TYPES.put(DATE_CLASSNAME.getValue(), DATE_JAVATYPE);
        JAVA_TYPES.put(STRING_CLASSNAME.getValue(), STRING_JAVATYPE);
        JAVA_TYPES.put(STRING_ARRAY_CLASSNAME.getValue(), STRING_ARRAY_JAVATYPE);
        JAVA_TYPES.put(QNAME_CLASSNAME.getValue(), QNAME_JAVATYPE);
        JAVA_TYPES.put(VOID_CLASSNAME.getValue(), VOID_JAVATYPE);
        JAVA_TYPES.put(OBJECT_CLASSNAME.getValue(), OBJECT_JAVATYPE);
        JAVA_TYPES.put(SOAPELEMENT_CLASSNAME.getValue(), SOAPELEMENT_JAVATYPE);
        JAVA_TYPES.put(URI_CLASSNAME.getValue(), URI_JAVATYPE);
        JAVA_TYPES.put(IMAGE_CLASSNAME.getValue(), IMAGE_JAVATYPE);
        JAVA_TYPES.put(MIME_MULTIPART_CLASSNAME.getValue(), MIME_MULTIPART_JAVATYPE);
        JAVA_TYPES.put(SOURCE_CLASSNAME.getValue(), SOURCE_JAVATYPE);
        JAVA_TYPES.put(DATA_HANDLER_CLASSNAME.getValue(), DATA_HANDLER_JAVATYPE);
    }

    private JavaSimpleTypeCreator() {
    }

    //  bug fix: 4923650
    public static JavaSimpleType getJavaSimpleType(String className) {
        return JAVA_TYPES.get(className);
    }
}
