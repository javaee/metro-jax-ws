/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.api.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.resources.ServerMessages;
import com.sun.xml.ws.resources.WsservletMessages;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.server.SingletonResolver;

import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Determines the instance that serves
 * the given request packet.
 *
 * <p>
 * The JAX-WS spec always use a singleton instance
 * to serve all the requests, but this hook provides
 * a convenient way to route messages to a proper receiver.
 *
 * <p>
 * Externally, an instance of {@link InstanceResolver} is
 * associated with {@link WSEndpoint}.
 *
 * <h2>Possible Uses</h2>
 * <p>
 * One can use WS-Addressing message properties to
 * decide which instance to deliver a message. This
 * would be an important building block for a stateful
 * web services.
 *
 * <p>
 * One can associate an instance of a service
 * with a specific WS-RM session.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class InstanceResolver<T> {
    /**
     * Decides which instance of 'T' serves the given request message.
     *
     * <p>
     * This method is called concurrently by multiple threads.
     * It is also on a criticail path that affects the performance.
     * A good implementation should try to avoid any synchronization,
     * and should minimize the amount of work as much as possible.
     *
     * @param request
     *      Always non-null. Represents the request message to be served.
     *      The caller may not consume the {@link Message}.
     */
    public abstract @NotNull T resolve(@NotNull Packet request);

    /**
     * Called by the default {@link Invoker} after the method call is done.
     * This gives {@link InstanceResolver} a chance to do clean up.
     *
     * <p>
     * Alternatively, one could override {@link #createInvoker()} to
     * create a custom invoker to do this in more flexible way.
     *
     * <p>
     * The default implementation is a no-op.
     *
     * @param request
     *      The same request packet given to {@link #resolve(Packet)} method.
     * @param servant
     *      The object returned from the {@link #resolve(Packet)} method.
     * @since 2.1.2
     */
    public void postInvoke(@NotNull Packet request, @NotNull T servant) {
    }

    /**
     * Called by {@link WSEndpoint} when it's set up.
     *
     * <p>
     * This is an opportunity for {@link InstanceResolver}
     * to do a endpoint-specific initialization process.
     *
     * @param wsc
     *      The {@link WebServiceContext} instance to be injected
     *      to the user instances (assuming {@link InstanceResolver}
     */
    public void start(@NotNull WSWebServiceContext wsc, @NotNull WSEndpoint endpoint) {
        // backward compatibility
        start(wsc);
    }

    /**
     * @deprecated
     *      Use {@link #start(WSWebServiceContext,WSEndpoint)}.
     */
    public void start(@NotNull WebServiceContext wsc) {}

    /**
     * Called by {@link WSEndpoint}
     * when {@link WSEndpoint#dispose()} is called.
     *
     * This allows {@link InstanceResolver} to do final clean up.
     *
     * <p>
     * This method is guaranteed to be only called once by {@link WSEndpoint}.
     */
    public void dispose() {}


    /**
     * Creates a {@link InstanceResolver} implementation that always
     * returns the specified singleton instance.
     */
    public static <T> InstanceResolver<T> createSingleton(T singleton) {
        assert singleton!=null;
        InstanceResolver ir = createFromInstanceResolverAnnotation(singleton.getClass());
        if(ir==null)
            ir = new SingletonResolver<T>(singleton);
        return ir;
    }

    /**
     * @deprecated
     *      This is added here because a Glassfish integration happened
     *      with this signature. Please do not use this. Will be removed
     *      after the next GF integration.
     */
    public static <T> InstanceResolver<T> createDefault(@NotNull Class<T> clazz, boolean bool) {
        return createDefault(clazz);
    }

    /**
     * Creates a default {@link InstanceResolver} that serves the given class.
     */
    public static <T> InstanceResolver<T> createDefault(@NotNull Class<T> clazz) {
        InstanceResolver<T> ir = createFromInstanceResolverAnnotation(clazz);
        if(ir==null)
            ir = new SingletonResolver<T>(createNewInstance(clazz));
        return ir;
    }

    /**
     * Checks for {@link InstanceResolverAnnotation} and creates an instance resolver from it if any.
     * Otherwise null.
     */
    public static <T> InstanceResolver<T> createFromInstanceResolverAnnotation(@NotNull Class<T> clazz) {
        for( Annotation a : clazz.getAnnotations() ) {
            InstanceResolverAnnotation ira = a.annotationType().getAnnotation(InstanceResolverAnnotation.class);
            if(ira==null)   continue;
            Class<? extends InstanceResolver> ir = ira.value();
            try {
                return ir.getConstructor(Class.class).newInstance(clazz);
            } catch (InstantiationException e) {
                throw new WebServiceException(ServerMessages.FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(
                    ir.getName(),a.annotationType(),clazz.getName()));
            } catch (IllegalAccessException e) {
                throw new WebServiceException(ServerMessages.FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(
                    ir.getName(),a.annotationType(),clazz.getName()));
            } catch (InvocationTargetException e) {
                throw new WebServiceException(ServerMessages.FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(
                    ir.getName(),a.annotationType(),clazz.getName()));
            } catch (NoSuchMethodException e) {
                throw new WebServiceException(ServerMessages.FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(
                    ir.getName(),a.annotationType(),clazz.getName()));
            }
        }

        return null;
    }

    protected static <T> T createNewInstance(Class<T> cl) {
        try {
            return cl.newInstance();
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServerRtException(
                WsservletMessages.ERROR_IMPLEMENTOR_FACTORY_NEW_INSTANCE_FAILED(cl));
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServerRtException(
                WsservletMessages.ERROR_IMPLEMENTOR_FACTORY_NEW_INSTANCE_FAILED(cl));
        }
    }

    /**
     * Wraps this {@link InstanceResolver} into an {@link Invoker}.
     */
    public @NotNull Invoker createInvoker() {
        return new Invoker() {
            @Override
            public void start(@NotNull WSWebServiceContext wsc, @NotNull WSEndpoint endpoint) {
                InstanceResolver.this.start(wsc,endpoint);
            }

            @Override
            public void dispose() {
                InstanceResolver.this.dispose();
            }

            @Override
            public Object invoke(Packet p, Method m, Object... args) throws InvocationTargetException, IllegalAccessException {
                T t = resolve(p);
                try {
                    return MethodUtil.invoke(t, m, args );
                } finally {
                    postInvoke(p,t);
                }
            }

            @Override
            public <U> U invokeProvider(@NotNull Packet p, U arg) {
                T t = resolve(p);
                try {
                    return ((Provider<U>) t).invoke(arg);
                } finally {
                    postInvoke(p,t);
                }
            }

            public String toString() {
                return "Default Invoker over "+InstanceResolver.this.toString();
            }
        };
    }

    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server");
}
