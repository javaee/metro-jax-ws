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

package com.sun.xml.ws.api.pipe;

import com.sun.istack.NotNull;

/**
 * Creates a tubeline.
 *
 * <p>
 * This pluggability layer enables the upper layer to
 * control exactly how the tubeline is composed.
 *
 * <p>
 * JAX-WS is going to have its own default implementation
 * when used all by itself, but it can be substituted by
 * other implementations.
 *
 * <p>
 * See {@link TubelineAssemblerFactory} for how {@link TubelineAssembler}s
 * are located.
 *
 *
 * @see com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
public interface TubelineAssembler {
    /**
     * Creates a new tubeline for clients.
     *
     * <p>
     * When a JAX-WS client creates a proxy or a {@link javax.xml.ws.Dispatch} from
     * a {@link javax.xml.ws.Service}, JAX-WS runtime internally uses this method
     * to create a new tubeline as a part of the initilization.
     *
     * @param context
     *      Object that captures various contextual information
     *      that can be used to determine the tubeline to be assembled.
     *
     * @return
     *      non-null freshly created tubeline.
     *
     * @throws javax.xml.ws.WebServiceException
     *      if there's any configuration error that prevents the
     *      tubeline from being constructed. This exception will be
     *      propagated into the application, so it must have
     *      a descriptive error.
     */
    @NotNull Tube createClient(@NotNull ClientTubeAssemblerContext context);

    /**
     * Creates a new tubeline for servers.
     *
     * <p>
     * When a JAX-WS server deploys a new endpoint, it internally
     * uses this method to create a new tubeline as a part of the
     * initialization.
     *
     * <p>
     * Note that this method is called only once to set up a
     * 'master tubeline', and it gets {@link Tube#copy(TubeCloner) copied}
     * from it.
     *
     * @param context
     *      Object that captures various contextual information
     *      that can be used to determine the tubeline to be assembled.
     *
     * @return
     *      non-null freshly created tubeline.
     *
     * @throws javax.xml.ws.WebServiceException
     *      if there's any configuration error that prevents the
     *      tubeline from being constructed. This exception will be
     *      propagated into the container, so it must have
     *      a descriptive error.
     *
     */
    @NotNull Tube createServer(@NotNull ServerTubeAssemblerContext context);
}
