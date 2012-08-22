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
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.server.provider.AsyncProviderInvokerTube;
import com.sun.xml.ws.server.provider.ProviderArgumentsBuilder;
import com.sun.xml.ws.server.provider.ProviderInvokerTube;
import com.sun.xml.ws.server.provider.SyncProviderInvokerTube;
import com.sun.xml.ws.util.ServiceFinder;
import java.util.logging.Logger;

/**
 * Factory for Provider invoker tubes that know how to handle specific
 * types of Providers (i.e., javax.xml.ws.Provider).
 *
 */

public abstract class ProviderInvokerTubeFactory<T> {
    /**
     *
     */
    protected abstract ProviderInvokerTube<T> doCreate(@NotNull final Class<T> implType,
                                                       @NotNull final Invoker invoker,
                                                       @NotNull final ProviderArgumentsBuilder<?> argsBuilder,
                                                                final boolean isAsync);

    private static final ProviderInvokerTubeFactory DEFAULT = new DefaultProviderInvokerTubeFactory();

    private static class DefaultProviderInvokerTubeFactory<T> extends ProviderInvokerTubeFactory<T> {
        @Override
        public ProviderInvokerTube<T> doCreate(@NotNull final Class<T> implType,
                                               @NotNull final Invoker invoker,
                                               @NotNull final ProviderArgumentsBuilder<?> argsBuilder,
                                                        final boolean isAsync)
        {
            return createDefault(implType, invoker, argsBuilder, isAsync);
        }
    }

    /**
     * @param classLoader
     * @param container
     * @param implType
     * @param invoker
     * @param argsBuilder
     * @param isAsync
     * 
     * @return
     */
    public static <T> ProviderInvokerTube<T> create(@Nullable final ClassLoader classLoader,
                                                    @NotNull  final Container container,
                                                    @NotNull  final Class<T> implType,
                                                    @NotNull  final Invoker invoker,
                                                    @NotNull  final ProviderArgumentsBuilder<?> argsBuilder,
                                                              final boolean isAsync)
    {
        for (ProviderInvokerTubeFactory factory : ServiceFinder.find(ProviderInvokerTubeFactory.class,
                                                                     classLoader, container))
        {
            ProviderInvokerTube<T> tube = factory.doCreate(implType, invoker, argsBuilder, isAsync);
            if (tube != null) {
                ProviderInvokerTubeFactory.logger.fine(factory.getClass() + " successfully created " + tube);
                return tube;
            }
        }
        return DEFAULT.createDefault(implType, invoker, argsBuilder, isAsync);
    }

    protected ProviderInvokerTube<T> createDefault(@NotNull final Class<T> implType,
                                                   @NotNull final Invoker invoker,
                                                   @NotNull final ProviderArgumentsBuilder<?> argsBuilder,
                                                            final boolean isAsync)
    {
        return
            isAsync
            ? new AsyncProviderInvokerTube(invoker, argsBuilder)
            : new SyncProviderInvokerTube (invoker, argsBuilder);
    }

    private static final Logger logger = Logger.getLogger(ProviderInvokerTubeFactory.class.getName());
}
