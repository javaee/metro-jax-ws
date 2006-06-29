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

package com.sun.tools.ws;

import com.sun.tools.xjc.api.util.APTClassLoader;
import com.sun.tools.xjc.api.util.ToolsJarNotFoundException;

import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes JAX-WS tools in a special class loader that can pick up APT classes,
 * even if it's not available in the tool launcher classpath.
 *
 * @author Kohsuke Kawaguchi
 */
final class Invoker {
    /**
     * List of packages that need to be loaded in {@link APTClassLoader}.
     */
    private static final String[] prefixes = {
        "com.sun.tools.jxc.",
        "com.sun.tools.xjc.",
        "com.sun.tools.apt.",
        "com.sun.tools.ws.",
        "com.sun.tools.javac.",
        "com.sun.tools.javadoc.",
        "com.sun.mirror."
    };

    static void main(String toolName, String[] args) throws Throwable {
        ClassLoader oldcc = Thread.currentThread().getContextClassLoader();
        try {
            APTClassLoader cl = new APTClassLoader(Invoker.class.getClassLoader(),prefixes);
            Thread.currentThread().setContextClassLoader(cl);

            Class compileTool = cl.loadClass("com.sun.tools.ws.wscompile.CompileTool");
            Constructor ctor = compileTool.getConstructor(OutputStream.class,String.class);
            Object tool = ctor.newInstance(System.out,toolName);
            Method runMethod = compileTool.getMethod("run",String[].class);
            boolean r = (Boolean)runMethod.invoke(tool,new Object[]{args});
            System.exit(r ? 0 : 1);
        } catch (ToolsJarNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } finally {
            Thread.currentThread().setContextClassLoader(oldcc);
        }

        System.exit(1);
    }
}
