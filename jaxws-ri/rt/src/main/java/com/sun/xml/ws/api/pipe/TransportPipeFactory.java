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

package com.sun.xml.ws.api.pipe;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.pipe.helper.PipeAdapter;
import com.sun.xml.ws.util.pipe.StandalonePipeAssembler;

import javax.xml.ws.WebServiceException;

/**
 * Factory for transport pipes that enables transport pluggability.
 *
 * <p>
 * At runtime, on the client side, JAX-WS (more specifically the default {@link PipelineAssembler}
 * of JAX-WS client runtime) relies on this factory to create a suitable transport {@link Pipe}
 * that can handle the given {@link EndpointAddress endpoint address}.
 *
 * <p>
 * JAX-WS extensions that provide additional transport support can
 * extend this class and implement the {@link #doCreate} method.
 * They are expected to check the scheme of the endpoint address
 * (and possibly some other settings from bindings), and create
 * their transport pipe implementations accordingly.
 * For example,
 *
 * <pre>
 * class MyTransportPipeFactoryImpl {
 *   Pipe doCreate(...) {
 *     String scheme = address.getURI().getScheme();
 *     if(scheme.equals("foo"))
 *       return new MyTransport(...);
 *     else
 *       return null;
 *   }
 * }
 * </pre>
 *
 * <p>
 * {@link TransportPipeFactory} look-up follows the standard service
 * discovery mechanism, so you need
 * {@code META-INF/services/com.sun.xml.ws.api.pipe.TransportPipeFactory}.
 *
 *
 *
 * <h2>TODO</h2>
 * <p>
 * One of the JAX-WS operation mode is supposedly where it doesn't have no WSDL whatsoever.
 * How do we identify the endpoint in such case?
 *
 * @author Kohsuke Kawaguchi
 * @see StandalonePipeAssembler
 */
public abstract class TransportPipeFactory {
    /**
     * Creates a transport {@link Pipe} for the given port, if this factory can do so,
     * or return null.
     *
     * @param context
     *      Object that captures various contextual information
     *      that can be used to determine the pipeline to be assembled.
     *
     * @return
     *      null to indicate that this factory isn't capable of creating a transport
     *      for this port (which causes the caller to search for other {@link TransportPipeFactory}s
     *      that can. Or non-null.
     *
     * @throws WebServiceException
     *      if this factory is capable of creating a transport pipe but some fatal
     *      error prevented it from doing so. This exception will be propagated
     *      back to the user application, and no further {@link TransportPipeFactory}s
     *      are consulted.
     */
    public abstract Pipe doCreate(@NotNull ClientPipeAssemblerContext context);

    /**
     * Locates {@link PipelineAssemblerFactory}s and create
     * a suitable {@link PipelineAssembler}.
     *
     * @param classLoader
     *      used to locate {@code META-INF/servces} files.
     * @return
     *      Always non-null, since we fall back to our default {@link PipelineAssembler}.
     *
     * @deprecated
     *      Use {@link TransportTubeFactory#create(ClassLoader, ClientTubeAssemblerContext)}
     */
    public static Pipe create(@Nullable ClassLoader classLoader, @NotNull ClientPipeAssemblerContext context) {
        return PipeAdapter.adapt(TransportTubeFactory.create(classLoader,context));
    }
}
