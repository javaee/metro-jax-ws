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
package com.sun.tools.ws.processor.model.jaxb;

/**
 * @author Kohsuke Kawaguchi
 */
class Util {
    /**
     * Replaces the marcros in the first string by the actual given arguments.
     */
    static String replace( String macro, String... args ) {
        int len = macro.length();
        StringBuilder buf = new StringBuilder(len);
        for( int i=0; i<len; i++ ) {
            char ch = macro.charAt(i);
            if(ch=='=' && i+2<len) {
                char tail = macro.charAt(i+1);
                char ch2 = macro.charAt(i+2);
                if('0'<=ch2 && ch2<='9' && tail==':') {
                    buf.append(args[ch2-'0']);
                    i+=2;
                    continue;
                }
            }
            buf.append(ch);
        }
        return buf.toString();
    }

    /**
     * Creates a macro tempate so that it can be later used with {@link #replace(String, String[])}.
     */
    static String createMacroTemplate( String s ) {
        return s;
    }

    static final String MAGIC = "=:";

    static final String MAGIC0 = MAGIC+"0";
    static final String MAGIC1 = MAGIC+"1";
    static final String MAGIC2 = MAGIC+"2";
}
