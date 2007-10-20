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

package com.sun.xml.ws.server;

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.ResourceInjector;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.resources.ServerMessages;
import com.sun.xml.ws.util.localization.Localizable;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Partial implementation of {@link InstanceResolver} with
 * convenience methods to do the resource injection.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractInstanceResolver<T> extends InstanceResolver<T> {

    /**
     * Encapsulates which field/method the injection is done,
     * and performs the injection.
     */
    protected static interface InjectionPlan<T,R> {
        void inject(T instance,R resource);
        /**
         * Gets the number of injections to be performed.
         */
        int count();
    }

    /**
     * Injects to a field.
     */
    protected static class FieldInjectionPlan<T,R> implements InjectionPlan<T,R> {
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
                        field.set(instance,resource);
                        return null;
                    } catch (IllegalAccessException e) {
                        throw new ServerRtException("server.rt.err",e);
                    }
                }
            });
        }

        public int count() {
            return 1;
        }
    }

    /**
     * Injects to a method.
     */
    protected static class MethodInjectionPlan<T,R> implements InjectionPlan<T,R> {
        private final Method method;

        public MethodInjectionPlan(Method method) {
            this.method = method;
        }

        public void inject(T instance, R resource) {
            invokeMethod(method, instance, resource);
        }

        public int count() {
            return 1;
        }
    }

    /**
     * Combines multiple {@link InjectionPlan}s into one.
     */
    private static class Compositor<T,R> implements InjectionPlan<T,R> {
        private final InjectionPlan<T,R>[] children;

        public Compositor(Collection<InjectionPlan<T,R>> children) {
            this.children = children.toArray(new InjectionPlan[children.size()]);
        }

        public void inject(T instance, R res) {
            for (InjectionPlan<T,R> plan : children)
                plan.inject(instance,res);
        }

        public int count() {
            int r = 0;
            for (InjectionPlan<T, R> plan : children)
                r += plan.count();
            return r;
        }
    }

    protected static ResourceInjector getResourceInjector(WSEndpoint endpoint) {
        ResourceInjector ri = endpoint.getContainer().getSPI(ResourceInjector.class);
        if(ri==null)
            ri = ResourceInjector.STANDALONE;
        return ri;
    }

    /**
     * Helper for invoking a method with elevated privilege.
     */
    protected static void invokeMethod(final @Nullable Method method, final Object instance, final Object... args) {
        if(method==null)    return;
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                try {
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    method.invoke(instance,args);
                } catch (IllegalAccessException e) {
                    throw new ServerRtException("server.rt.err",e);
                } catch (InvocationTargetException e) {
                    throw new ServerRtException("server.rt.err",e);
                }
                return null;
            }
        });
    }

    /**
     * Finds the method that has the given annotation, while making sure that
     * there's only at most one such method.
     */
    protected final @Nullable Method findAnnotatedMethod(Class clazz, Class<? extends Annotation> annType) {
        boolean once = false;
        Method r = null;
        for(Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(annType) != null) {
                if (once)
                    throw new ServerRtException(ServerMessages.ANNOTATION_ONLY_ONCE(annType));
                if (method.getParameterTypes().length != 0)
                    throw new ServerRtException(ServerMessages.NOT_ZERO_PARAMETERS(method));
                r = method;
                once = true;
            }
        }
        return r;
    }

    /**
     * Creates an {@link InjectionPlan} that injects the given resource type to the given class.
     *
     * @param isStatic
     *      Only look for static field/method
     *
     */
    protected static <T,R>
    InjectionPlan<T,R> buildInjectionPlan(Class<? extends T> clazz, Class<R> resourceType, boolean isStatic) {
        List<InjectionPlan<T,R>> plan = new ArrayList<InjectionPlan<T,R>>();

        Class<?> cl = clazz;
        while(cl != Object.class) {
            for(Field field: cl.getDeclaredFields()) {
                Resource resource = field.getAnnotation(Resource.class);
                if (resource != null) {
                    if(isInjectionPoint(resource, field.getType(),
                        ServerMessages.localizableWRONG_FIELD_TYPE(field.getName()),resourceType)) {

                        if(isStatic && !Modifier.isStatic(field.getModifiers()))
                            throw new WebServiceException(ServerMessages.STATIC_RESOURCE_INJECTION_ONLY(resourceType,field));

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
                        throw new ServerRtException(ServerMessages.WRONG_NO_PARAMETERS(method));
                    if(isInjectionPoint(resource,paramTypes[0],
                        ServerMessages.localizableWRONG_PARAMETER_TYPE(method.getName()),resourceType)) {

                        if(isStatic && !Modifier.isStatic(method.getModifiers()))
                            throw new WebServiceException(ServerMessages.STATIC_RESOURCE_INJECTION_ONLY(resourceType,method));

                        plan.add(new MethodInjectionPlan<T,R>(method));
                    }
                }
            }
            cl = cl.getSuperclass();
        }

        return new Compositor<T,R>(plan);
    }

    /**
     * Returns true if the combination of {@link Resource} and the field/method type
     * are consistent for {@link WebServiceContext} injection.
     */
    private static boolean isInjectionPoint(Resource resource, Class fieldType, Localizable errorMessage, Class resourceType ) {
        Class t = resource.type();
        if (t.equals(Object.class)) {
            return fieldType.equals(resourceType);
        } else if (t.equals(resourceType)) {
            if (fieldType.isAssignableFrom(resourceType)) {
                return true;
            } else {
                // type compatibility error
                throw new ServerRtException(errorMessage);
            }
        }
        return false;
    }
}
