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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sun.xml.ws.util.localization.Localizable;

/**
 * A helper class to invoke javac.
 *
 * @author WS Development Team
 */
public class JavaCompilerHelper extends ToolBase {

    public JavaCompilerHelper(OutputStream out) {
        super(out, " ");
        this.out = out;
    }

    public boolean compile(String[] args) {
        return internalCompile(args);
    }

    protected String getResourceBundleName() {
        return "com.sun.tools.ws.resources.javacompiler";
    }

    protected boolean internalCompile(String[] args) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class comSunToolsJavacMainClass = null;
        try {
            /* try to use the new compiler */
            comSunToolsJavacMainClass =
                    cl.loadClass("com.sun.tools.javac.Main");
            try {
                Method compileMethod =
                        comSunToolsJavacMainClass.getMethod(
                                "compile",
                                compile141MethodSignature);
                try {
                    Object result =
                            compileMethod.invoke(
                                    null,
                                    new Object[] { args, new PrintWriter(out)});
                    if (!(result instanceof Integer)) {
                        return false;
                    }
                    return ((Integer) result).intValue() == 0;
                } catch (IllegalAccessException e3) {
                    return false;
                } catch (IllegalArgumentException e3) {
                    return false;
                } catch (InvocationTargetException e3) {
                    return false;
                }
            } catch (NoSuchMethodException e2) {
                //tryout 1.3.1 signature
                return internalCompilePre141(args);
                //onError(getMessage("javacompiler.nosuchmethod.error", "getMethod(\"compile\", compile141MethodSignature)"));
                //return false;
            }
        } catch (ClassNotFoundException e) {
            onError(
                    getMessage(
                            "javacompiler.classpath.error",
                            "com.sun.tools.javac.Main"));
            return false;
        } catch (SecurityException e) {
            return false;
        }
    }

    protected boolean internalCompilePre141(String[] args) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                    Class sunToolsJavacMainClass = cl.loadClass("sun.tools.javac.Main");
                    try {
                            Constructor constructor =
                                    sunToolsJavacMainClass.getConstructor(constructorSignature);
                            try {
                                    Object javacMain =
                                            constructor.newInstance(new Object[] { out, "javac" });
                                    Method compileMethod =
                                            sunToolsJavacMainClass.getMethod(
                                                    "compile",
                                                    compileMethodSignature);
                                    Object result =
                                            compileMethod.invoke(javacMain, new Object[] { args });
                                    if (!(result instanceof Boolean)) {
                                            return false;
                                    }
                                    return ((Boolean) result).booleanValue();
                            } catch (InstantiationException e4) {
                                    return false;
                            } catch (IllegalAccessException e4) {
                                    return false;
                            } catch (IllegalArgumentException e4) {
                                    return false;
                            } catch (InvocationTargetException e4) {
                                    return false;
                            }

                    } catch (NoSuchMethodException e3) {
                            onError(
                                    getMessage(
                                            "javacompiler.nosuchmethod.error",
                                            "getMethod(\"compile\", compileMethodSignature)"));
                            return false;
                    }
            } catch (ClassNotFoundException e2) {
                    return false;
            }
    }

    protected String getGenericErrorMessage() {
            return "javacompiler.error";
    }

    protected void run() {
    }

    protected boolean parseArguments(String[] args) {
            return false;
    }

    public void onError(Localizable msg) {
            report(getMessage("javacompiler.error", localizer.localize(msg)));
    }

    protected OutputStream out;

    protected static final Class[] compile141MethodSignature;
    protected static final Class[] constructorSignature;
    protected static final Class[] compileMethodSignature;

    static {
            compile141MethodSignature = new Class[2];
            compile141MethodSignature[0] = (new String[0]).getClass();
            compile141MethodSignature[1] = PrintWriter.class;
            //jdk version < 1.4.1 signature
            constructorSignature = new Class[2];
            constructorSignature[0] = OutputStream.class;
            constructorSignature[1] = String.class;
            compileMethodSignature = new Class[1];
            compileMethodSignature[0] = compile141MethodSignature[0]; // String[]

    }
}
