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

package com.sun.xml.ws.api.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.server.DefaultResourceInjector;

import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;

/**
 * Represents a functionality of the container to inject resources
 * to application service endpoint object (usually but not necessarily as per JavaEE spec.)
 *
 * <p>
 * If {@link Container#getSPI(Class)} returns a valid instance of {@link ResourceInjector},
 * The JAX-WS RI will call the {@link #inject} method for each service endpoint
 * instance that it manages.
 *
 * <p>
 * The JAX-WS RI will be responsible for calling {@link PostConstruct} callback,
 * so implementations of this class need not do so.
 *
 * @author Kohsuke Kawaguchi
 * @see Container
 */
public abstract class ResourceInjector {
    /**
     * Performs resource injection.
     *
     * @param context
     *      {@link WebServiceContext} implementation to be injected into the instance.
     * @param instance
     *      Instance of the service endpoint class to which resources will be injected.
     *
     * @throws WebServiceException
     *      If the resource injection fails.
     */
    public abstract void inject(@NotNull WSWebServiceContext context, @NotNull Object instance);

    /**
     * Fallback {@link ResourceInjector} implementation used when the {@link Container}
     * doesn't provide one.
     *
     * <p>
     * Just inject {@link WSWebServiceContext} and done.
     */
    public static final ResourceInjector STANDALONE = new DefaultResourceInjector();
}
