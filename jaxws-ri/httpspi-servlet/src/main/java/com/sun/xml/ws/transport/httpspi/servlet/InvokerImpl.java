/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.transport.httpspi.servlet;

import javax.xml.ws.spi.Invoker;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.sun.xml.ws.util.InjectionPlan;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author Jitendra Kotamraju
 */
class InvokerImpl extends Invoker {
    private final Class implType;
    private final Object impl;
    private final Method postConstructMethod;
//    private final Method preDestroyMethod;

    InvokerImpl(Class implType) {
        this.implType = implType;
        postConstructMethod = findAnnotatedMethod(implType, PostConstruct.class);
//        preDestroyMethod = findAnnotatedMethod(implType, PreDestroy.class);
        try {
            impl = implType.newInstance();
        } catch (InstantiationException e) {
            throw new WebServiceException(e);
        } catch (IllegalAccessException e) {
            throw new WebServiceException(e);
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

    public void inject(WebServiceContext webServiceContext) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        InjectionPlan.buildInjectionPlan(
            implType, WebServiceContext.class,false).inject(impl,webServiceContext);
        invokeMethod(postConstructMethod, impl);
    }

    public Object invoke(Method m, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return m.invoke(impl, args);
    }

    /*
     * Finds the method that has the given annotation, while making sure that
     * there's only at most one such method.
     */
    private static Method findAnnotatedMethod(Class clazz, Class<? extends Annotation> annType) {
        boolean once = false;
        Method r = null;
        for(Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(annType) != null) {
                if (once)
                    throw new WebServiceException("Only one method should have the annotation"+annType);
                if (method.getParameterTypes().length != 0)
                    throw new WebServiceException("Method"+method+"shouldn't have any arguments");
                r = method;
                once = true;
            }
        }
        return r;
    }
}
