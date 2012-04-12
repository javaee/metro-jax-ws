/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceException;

/**
 * Encapsulates which field/method the injection is done, and performs the
 * injection.
 */
public abstract class InjectionPlan<T, R> {
    /**
     * Perform injection
     * 
     * @param instance
     *            Instance
     * @param resource
     *            Resource
     */
    public abstract void inject(T instance, R resource);

    /**
     * Perform injection, but resource is only generated if injection is
     * necessary.
     * 
     * @param instance
     * @param resource
     */
    public void inject(T instance, Callable<R> resource) {
        try {
            inject(instance, resource.call());
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }

    /*
     * Injects to a field.
     */
    private static class FieldInjectionPlan<T, R> extends
            InjectionPlan<T, R> {
        private final Field field;

        public FieldInjectionPlan(Field field) {
            this.field = field;
        }

        public void inject(final T instance, final R resource) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    try {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        field.set(instance, resource);
                        return null;
                    } catch (IllegalAccessException e) {
                        throw new WebServiceException(e);
                    }
                }
            });
        }
    }

    /*
     * Injects to a method.
     */
    private static class MethodInjectionPlan<T, R> extends
            InjectionPlan<T, R> {
        private final Method method;

        public MethodInjectionPlan(Method method) {
            this.method = method;
        }

        public void inject(T instance, R resource) {
            invokeMethod(method, instance, resource);
        }
    }

    /*
     * Helper for invoking a method with elevated privilege.
     */
    private static void invokeMethod(final Method method, final Object instance, final Object... args) {
        if(method==null)    return;
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                try {
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    method.invoke(instance,args);
                } catch (IllegalAccessException e) {
                    throw new WebServiceException(e);
                } catch (InvocationTargetException e) {
                    throw new WebServiceException(e);
                }
                return null;
            }
        });
    }

    /*
     * Combines multiple {@link InjectionPlan}s into one.
     */
    private static class Compositor<T, R> extends InjectionPlan<T, R> {
        private final Collection<InjectionPlan<T, R>> children;

        public Compositor(Collection<InjectionPlan<T, R>> children) {
            this.children = children;
        }

        public void inject(T instance, R res) {
            for (InjectionPlan<T, R> plan : children)
                plan.inject(instance, res);
        }
        
        public void inject(T instance, Callable<R> resource) {
            if (!children.isEmpty()) {
                super.inject(instance, resource);
            }
        }
    }

    /*
     * Creates an {@link InjectionPlan} that injects the given resource type to the given class.
     *
     * @param isStatic
     *      Only look for static field/method
     *
     */
    public static <T,R>
    InjectionPlan<T,R> buildInjectionPlan(Class<? extends T> clazz, Class<R> resourceType, boolean isStatic) {
        List<InjectionPlan<T,R>> plan = new ArrayList<InjectionPlan<T,R>>();

        Class<?> cl = clazz;
        while(cl != Object.class) {
            for(Field field: cl.getDeclaredFields()) {
                Resource resource = field.getAnnotation(Resource.class);
                if (resource != null) {
                    if(isInjectionPoint(resource, field.getType(),
                        "Incorrect type for field"+field.getName(),
                        resourceType)) {

                        if(isStatic && !Modifier.isStatic(field.getModifiers()))
                            throw new WebServiceException("Static resource "+resourceType+" cannot be injected to non-static "+field);

                        plan.add(new FieldInjectionPlan<T,R>(field));
                    }
                }
            }
            cl = cl.getSuperclass();
        }

        cl = clazz;
        while(cl != Object.class) {
            for(Method method : cl.getDeclaredMethods()) {
                Resource resource = method.getAnnotation(Resource.class);
                if (resource != null) {
                    Class[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length != 1)
                        throw new WebServiceException("Incorrect no of arguments for method "+method);
                    if(isInjectionPoint(resource,paramTypes[0],
                        "Incorrect argument types for method"+method.getName(),
                        resourceType)) {

                        if(isStatic && !Modifier.isStatic(method.getModifiers()))
                            throw new WebServiceException("Static resource "+resourceType+" cannot be injected to non-static "+method);

                        plan.add(new MethodInjectionPlan<T,R>(method));
                    }
                }
            }
            cl = cl.getSuperclass();
        }

        return new Compositor<T,R>(plan);
    }

    /*
     * Returns true if the combination of {@link Resource} and the field/method type
     * are consistent for {@link WebServiceContext} injection.
     */
    private static boolean isInjectionPoint(Resource resource, Class fieldType, String errorMessage, Class resourceType ) {
        Class t = resource.type();
        if (t.equals(Object.class)) {
            return fieldType.equals(resourceType);
        } else if (t.equals(resourceType)) {
            if (fieldType.isAssignableFrom(resourceType)) {
                return true;
            } else {
                // type compatibility error
                throw new WebServiceException(errorMessage);
            }
        }
        return false;
    }
}
