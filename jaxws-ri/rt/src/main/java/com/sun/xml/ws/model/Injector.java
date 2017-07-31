/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.model;

import java.lang.reflect.Field;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link ClassLoader} used to "inject" wrapper and exception bean classes
 * into the VM.
 *
 * @author Jitendra kotamraju
 */
final class Injector {

    private static final Logger LOGGER = Logger.getLogger(Injector.class.getName());

    private static Method defineClass;
    private static Method resolveClass;
    private static Method getPackage;
    private static Method definePackage;
    private static Object U;

    static {
        try {
            Method[] m = AccessController.doPrivileged(
                    new PrivilegedAction<Method[]>() {
                @Override
                public Method[] run() {
                    return new Method[]{
                        getMethod(ClassLoader.class, "defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE),
                        getMethod(ClassLoader.class, "resolveClass", Class.class),
                        getMethod(ClassLoader.class, "getPackage", String.class),
                        getMethod(ClassLoader.class, "definePackage",
                            String.class, String.class, String.class, String.class,
                            String.class, String.class, String.class, URL.class)
                    };
                }
            }
            );
            defineClass = m[0];
            resolveClass = m[1];
            getPackage = m[2];
            definePackage = m[3];

        } catch (Throwable t) {
            try {
                U = AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    @Override
                    public Object run() throws Exception {
                        Class u = Class.forName("sun.misc.Unsafe");
                        Field theUnsafe = u.getDeclaredField("theUnsafe");
                        theUnsafe.setAccessible(true);
                        return theUnsafe.get(null);
                    }
                });
                defineClass = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>() {
                    @Override
                    public Method run() throws Exception {
                        try {
                            return U.getClass().getMethod("defineClass",
                                    new Class[]{String.class,
                                        byte[].class,
                                        Integer.TYPE,
                                        Integer.TYPE,
                                        ClassLoader.class,
                                        ProtectionDomain.class});
                        } catch (NoSuchMethodException | SecurityException ex) {
                            throw ex;
                        }
                    }
                });
            } catch (SecurityException | PrivilegedActionException ex) {
                Logger.getLogger(Injector.class.getName()).log(Level.SEVERE, null, ex);
                WebServiceException we = new WebServiceException(ex);
                we.addSuppressed(t);
                throw we;
            }
        }
    }

    private static Method getMethod(final Class<?> c, final String methodname, final Class<?>... params) {
        try {
            Method m = c.getDeclaredMethod(methodname, params);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            // impossible
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    static synchronized Class inject(ClassLoader cl, String className, byte[] image) {
        // To avoid race conditions let us check if the classloader
        // already contains the class
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            // nothing to do
        }
        try {
            if (definePackage == null) {
                return (Class) defineClass.invoke(U, className.replace('/', '.'), image, 0, image.length, cl, Injector.class.getProtectionDomain());
            }
            int packIndex = className.lastIndexOf('.');
            if (packIndex != -1) {
                String pkgname = className.substring(0, packIndex);
                // Check if package already loaded.
                Package pkg = (Package) getPackage.invoke(cl, pkgname);
                if (pkg == null) {
                    definePackage.invoke(cl, pkgname, null, null, null, null, null, null, null);
                }
            }

            Class c = (Class) defineClass.invoke(cl, className.replace('/', '.'), image, 0, image.length);
            resolveClass.invoke(cl, c);
            return c;
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Unable to inject " + className, e);
            }
            throw new WebServiceException(e);
        }
    }

}

