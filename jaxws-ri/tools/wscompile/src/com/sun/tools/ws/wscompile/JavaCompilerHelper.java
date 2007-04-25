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

package com.sun.tools.ws.wscompile;

import com.sun.istack.tools.ParallelWorldClassLoader;
import com.sun.tools.ws.resources.JavacompilerMessages;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URISyntaxException;

/**
 * A helper class to invoke javac.
 *
 * @author WS Development Team
 */
class JavaCompilerHelper{
    static File getJarFile(Class clazz) {
        URL url = null;
        try {
            url = ParallelWorldClassLoader.toJarUrl(clazz.getResource('/'+clazz.getName().replace('.','/')+".class"));
            return new File(url.toURI());
        } catch (ClassNotFoundException e) {
            // if we can't figure out where JAXB/JAX-WS API are, we couldn't have been executing this code.
            throw new Error(e);
        } catch (MalformedURLException e) {
            // if we can't figure out where JAXB/JAX-WS API are, we couldn't have been executing this code.
            throw new Error(e);
        } catch (URISyntaxException e) {
            // url.toURI() is picky and doesn't like ' ' in URL, so this is the fallback
            return new File(url.getPath());
        }
    }

    static boolean compile(String[] args, OutputStream out, ErrorReceiver receiver){
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            /* try to use the new compiler */
            Class comSunToolsJavacMainClass =
                    cl.loadClass("com.sun.tools.javac.Main");
            try {
                Method compileMethod =
                        comSunToolsJavacMainClass.getMethod(
                                "compile",
                                compileMethodSignature);
                    Object result =
                            compileMethod.invoke(
                                    null, args, new PrintWriter(out));
                    return result instanceof Integer && (Integer) result == 0;
            } catch (NoSuchMethodException e2) {
                receiver.error(JavacompilerMessages.JAVACOMPILER_NOSUCHMETHOD_ERROR("getMethod(\"compile\", Class[])"), e2);
            } catch (IllegalAccessException e) {
                receiver.error(e);
            } catch (InvocationTargetException e) {
                receiver.error(e);
            }
        } catch (ClassNotFoundException e) {
            receiver.error(JavacompilerMessages.JAVACOMPILER_CLASSPATH_ERROR("com.sun.tools.javac.Main"), e);
        } catch (SecurityException e) {
            receiver.error(e);
        }
        return false;
    }
    
    private static final Class[] compileMethodSignature = {String[].class, PrintWriter.class};
}
