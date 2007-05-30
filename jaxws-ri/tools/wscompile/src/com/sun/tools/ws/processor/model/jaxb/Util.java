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
