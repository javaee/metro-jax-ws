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

package com.sun.xml.ws.server;

import com.sun.xml.ws.api.server.AbstractInstanceResolver;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.ResourceInjector;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WSWebServiceContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;

/**
 * Partial implementation of {@link InstanceResolver} with code
 * to handle multiple instances per server.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractMultiInstanceResolver<T> extends AbstractInstanceResolver<T> {
    protected final Class<T> clazz;

    // fields for resource injection.
    private /*almost final*/ WSWebServiceContext webServiceContext;
    protected /*almost final*/ WSEndpoint owner;
    private final Method postConstructMethod;
    private final Method preDestroyMethod;
    private /*almost final*/ ResourceInjector resourceInjector;

    public AbstractMultiInstanceResolver(Class<T> clazz) {
        this.clazz = clazz;

        postConstructMethod = findAnnotatedMethod(clazz, PostConstruct.class);
        preDestroyMethod = findAnnotatedMethod(clazz, PreDestroy.class);
    }

    /**
     * Perform resource injection on the given instance.
     */
    protected final void prepare(T t) {
        // we can only start creating new instances after the start method is invoked.
        assert webServiceContext!=null;

        resourceInjector.inject(webServiceContext,t);
        invokeMethod(postConstructMethod,t);
    }

    /**
     * Creates a new instance via the default constructor.
     */
    protected final T create() {
        T t = createNewInstance(clazz);
        prepare(t);
        return t;
    }

    @Override
    public void start(WSWebServiceContext wsc, WSEndpoint endpoint) {
        resourceInjector = getResourceInjector(endpoint);
        this.webServiceContext = wsc;
        this.owner = endpoint;
    }

    protected final void dispose(T instance) {
        invokeMethod(preDestroyMethod,instance);
    }
}
