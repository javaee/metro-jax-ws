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

package com.sun.tools.ws.processor.generator;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author WS Development Team
 */
public final class SimpleToBoxedUtil {

    public static String getBoxedExpressionOfType(String s, String c) {
        if (isPrimitive(c)) {
            StringBuffer sb = new StringBuffer();
            sb.append("new ");
            sb.append(getBoxedClassName(c));
            sb.append('(');
            sb.append(s);
            sb.append(')');
            return sb.toString();
        } else
            return s;
    }

    public static String getUnboxedExpressionOfType(String s, String c) {
        if (isPrimitive(c)) {
            StringBuffer sb = new StringBuffer();
            sb.append('(');
            sb.append(s);
            sb.append(").");
            sb.append(c);
            sb.append("Value()");
            return sb.toString();
        } else
            return s;
    }

    public static String convertExpressionFromTypeToType(
        String s,
        String from,
        String to)
        throws Exception {
        if (from.equals(to))
            return s;
        else {
            if (!isPrimitive(to) && isPrimitive(from))
                return getBoxedExpressionOfType(s, from);
            else if (isPrimitive(to) && isPrimitive(from))
                return getUnboxedExpressionOfType(s, to);
            else
                return s;
        }
    }

    public static String getBoxedClassName(String className) {
        if (isPrimitive(className)) {
            StringBuffer sb = new StringBuffer();
            sb.append("java.lang.");
            if (className.equals(int.class.getName()))
                sb.append("Integer");
            else if (className.equals(char.class.getName()))
                sb.append("Character");
            else {
                sb.append(Character.toUpperCase(className.charAt(0)));
                sb.append(className.substring(1));
            }
            return sb.toString();
        } else
            return className;
    }

    public static boolean isPrimitive(String className) {
        return primitiveSet.contains(className);
    }

    static Set primitiveSet = null;

    static {
        primitiveSet = new HashSet();
        primitiveSet.add("boolean");
        primitiveSet.add("byte");
        primitiveSet.add("double");
        primitiveSet.add("float");
        primitiveSet.add("int");
        primitiveSet.add("long");
        primitiveSet.add("short");
    }
}
