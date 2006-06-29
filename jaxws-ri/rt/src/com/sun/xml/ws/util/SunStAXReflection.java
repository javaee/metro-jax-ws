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

import java.lang.reflect.Method;

public class SunStAXReflection {
    /**
     * Sun StAX impl <code>XMLReaderImpl.setInputSource()</code> method via reflection.
     */
    public static Method XMLReaderImpl_setInputSource;
    
    /**
     * Sun StAX impl <code>XMLReaderImpl.reset()</code> method via reflection.
     */
    public static Method XMLReaderImpl_reset;

    static {
        try {
            Class clazz = Class.forName("com.sun.xml.stream.XMLReaderImpl");
            // Are we running on top of JAXP 1.4?
            if (clazz == null) {
                clazz = Class.forName("com.sun.xml.stream.XMLStreamReaderImpl");
            }
            XMLReaderImpl_setInputSource = 
                clazz.getMethod("setInputSource", org.xml.sax.InputSource.class);
            XMLReaderImpl_reset = clazz.getMethod("reset");
        } catch (Exception e) {
            // falls through
        }
    }
}
