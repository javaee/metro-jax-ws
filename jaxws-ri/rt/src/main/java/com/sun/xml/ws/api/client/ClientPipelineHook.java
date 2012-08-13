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
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;

/**
 * Allow the container (primarily Glassfish) to inject
 * their own pipes into the client pipeline.
 *
 * <p>
 * This interface has a rather ad-hoc set of methods, because
 * we didn't want to define an autonomous pipe-assembly process.
 * (We thought this is a smaller evil compared to that.)
 *
 * <p>
 * JAX-WS obtains this through {@link com.sun.xml.ws.api.server.Container#getSPI(Class)}.
 *
 * @author Jitendra Kotamraju
 */
public abstract class ClientPipelineHook {

    /**
     * Called during the pipeline construction process once to allow a container
     * to register a pipe for security.
     *
     * This pipe will be injected to a point very close to the transport, allowing
     * it to do some security operations.
     *
     * @param ctxt
     *      Represents abstraction of SEI, WSDL abstraction etc. Context can be used
     *      whether add a new pipe to the head or not.
     *
     * @param tail
     *      Head of the partially constructed pipeline. If the implementation
     *      wishes to add new pipes, it should do so by extending
     *      {@link com.sun.xml.ws.api.pipe.helper.AbstractFilterPipeImpl} and making sure that this {@link com.sun.xml.ws.api.pipe.Pipe}
     *      eventually processes messages.
     *
     * @return
     *      The default implementation just returns <tt>tail</tt>, which means
     *      no additional pipe is inserted. If the implementation adds
     *      new pipes, return the new head pipe.
     */
    public @NotNull Pipe createSecurityPipe(ClientPipeAssemblerContext ctxt, @NotNull Pipe tail) {
        return tail;
    }
}
