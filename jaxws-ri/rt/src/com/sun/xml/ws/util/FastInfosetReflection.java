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

package com.sun.xml.ws.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


/**
 *
 * @author Santiago.PericasGeertsen@sun.com
 * @author Paul.Sandoz@sun.com
 */
public class FastInfosetReflection {
    /**
     * FI StAXDocumentParser constructor using reflection.
     */
    public static final Constructor fiStAXDocumentParser_new;
    
    /**
     * FI <code>StAXDocumentParser.setInputStream()</code> method via reflection.
     */
    public static final Method fiStAXDocumentParser_setInputStream;
    
    /**
     * FI <code>StAXDocumentParser.setStringInterning()</code> method via reflection.
     */
    public static final Method fiStAXDocumentParser_setStringInterning;
    
    static {
        Constructor tmp_new = null;
        Method tmp_setInputStream = null;
        Method tmp_setStringInterning = null;

        // Use reflection to avoid static dependency with FI jar
        try {
            Class clazz = Class.forName("com.sun.xml.fastinfoset.stax.StAXDocumentParser");
            tmp_new = clazz.getConstructor();
            tmp_setInputStream =
                clazz.getMethod("setInputStream", java.io.InputStream.class);
            tmp_setStringInterning =
                clazz.getMethod("setStringInterning", boolean.class);
        } 
        catch (Exception e) {
            // falls through
        }
        fiStAXDocumentParser_new = tmp_new;
        fiStAXDocumentParser_setInputStream = tmp_setInputStream;
        fiStAXDocumentParser_setStringInterning = tmp_setStringInterning;
    }

}
