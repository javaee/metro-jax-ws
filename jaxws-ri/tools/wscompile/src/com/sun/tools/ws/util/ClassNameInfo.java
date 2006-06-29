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

package com.sun.tools.ws.util;

/**
 * @author WS Development Team
 */

public final class ClassNameInfo {

    public static String getName(String className) {
        String qual = getQualifier(className);
        int len = className.length();
        int closingBracket = className.indexOf('>');
        if(closingBracket > 0)
            len = closingBracket;
        return qual != null
            ? className.substring(qual.length() + 1, len)
            : className;
    }


    /**
     *
     *
     * @param className Generic class, such as java.util.List<java.lang.String>
     * @return the generic class, such as java.util.List
     */
    public static String getGenericClass(String className) {
       int index = className.indexOf('<');
       if(index < 0)
           return className;
       return (index > 0)?className.substring(0, index):className;
    }


    public static String getQualifier(String className) {
        int idot = className.indexOf(' ');
        if (idot <= 0)
            idot = className.length();
        else
            idot -= 1; // back up over previous dot
        int index = className.lastIndexOf('.', idot - 1);
        return (index < 0) ? null : className.substring(0, index);
    }

    public static String replaceInnerClassSym(String name) {
        return name.replace('$', '_');
    }
}
