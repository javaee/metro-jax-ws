/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.client;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.util.ServiceFinder;

import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates {@link ServiceInterceptor}.
 *
 * <p>
 * Code that wishes to inject {@link ServiceInterceptor} into {@link WSService}
 * must implement this class. There are two ways to have the JAX-WS RI
 * recognize your {@link ServiceInterceptor}s.
 *
 * <h3>Use {@link ServiceFinder}</h3>
 * <p>
 * {@link ServiceInterceptorFactory}s discovered via {@link ServiceFinder}
 * will be incorporated to all {@link WSService} instances.
 *
 * <h3>Register per-thread</h3>
 *
 *
 * @author Kohsuke Kawaguchi
 * @see ServiceInterceptor
 * @see 2.1 EA3
 */
public abstract class ServiceInterceptorFactory {
    public abstract ServiceInterceptor create(@NotNull WSService service);

    /**
     * Loads all {@link ServiceInterceptor}s and return aggregated one.
     */
    public static @NotNull ServiceInterceptor load(@NotNull WSService service, @Nullable ClassLoader cl) {
        List<ServiceInterceptor> l = new ArrayList<ServiceInterceptor>();

        // first service look-up
        for( ServiceInterceptorFactory f : ServiceFinder.find(ServiceInterceptorFactory.class))
            l.add(f.create(service));

        // then thread-local
        for( ServiceInterceptorFactory f : threadLocalFactories.get())
            l.add(f.create(service));

        return ServiceInterceptor.aggregate(l.toArray(new ServiceInterceptor[l.size()]));
    }

    private static ThreadLocal<Set<ServiceInterceptorFactory>> threadLocalFactories = new ThreadLocal<Set<ServiceInterceptorFactory>>() {
        protected Set<ServiceInterceptorFactory> initialValue() {
            return new HashSet<ServiceInterceptorFactory>();
        }
    };

    /**
     * Registers {@link ServiceInterceptorFactory} for this thread.
     *
     * <p>
     * Once registered, {@link ServiceInterceptorFactory}s are consulted for every
     * {@link Service} created in this thread, until it gets unregistered.
     */
    public static boolean registerForThread(ServiceInterceptorFactory factory) {
        return threadLocalFactories.get().add(factory);
    }

    /**
     * Removes previously registered {@link ServiceInterceptorFactory} for this thread.
     */
    public static boolean unregisterForThread(ServiceInterceptorFactory factory) {
        return threadLocalFactories.get().remove(factory);
    }
}
