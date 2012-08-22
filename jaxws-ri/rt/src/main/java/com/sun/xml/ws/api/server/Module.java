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
import com.sun.xml.ws.api.Component;
import com.sun.xml.ws.transport.http.HttpAdapterList;

import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.List;

/**
 * Represents an object scoped to the current "module" (like a JavaEE web appliation).
 *
 * <p>
 * This object can be obtained from {@link Container#getSPI(Class)}.
 *
 * <p>
 * The scope of the module is driven by {@link W3CEndpointReferenceBuilder#build()}'s
 * requirement that we need to identify a {@link WSEndpoint} that has a specific
 * service/port name.
 *
 * <p>
 * For JavaEE containers this should be scoped to a JavaEE application. For
 * other environment, this could be scoped to any similar notion. If no such
 * notion is available, the implementation of {@link Container} can return
 * a new {@link Module} object each time {@link Container#getSPI(Class)} is invoked.
 *
 * <p>
 * There's a considerable overlap between this and {@link HttpAdapterList}.
 * The SPI really needs to be reconsidered 
 * 
 *
 * @see Container
 * @author Kohsuke Kawaguchi
 * @since 2.1 EA3
 */
public abstract class Module implements Component {
    /**
     * Gets the list of {@link BoundEndpoint} deployed in this module.
     *
     * <p>
     * From the point of view of the {@link Module} implementation,
     * it really only needs to provide a {@link List} object as a data store.
     * JAX-WS will update this list accordingly. 
     *
     * @return
     *      always return the same non-null instance.
     */
    public abstract @NotNull List<BoundEndpoint> getBoundEndpoints();
    
    public @Nullable <S> S getSPI(@NotNull Class<S> spiType) {
    	return null;
    }

}
