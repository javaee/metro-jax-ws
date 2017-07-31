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
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.pipe.helper.PipeAdapter;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.assembler.MetroTubelineAssembler;
import com.sun.xml.ws.util.ServiceFinder;
import java.util.logging.Level;

import java.util.logging.Logger;

/**
 * Creates {@link TubelineAssembler}.
 * <p/>
 * <p/>
 * To create a tubeline,
 * the JAX-WS runtime locates {@link TubelineAssemblerFactory}s through
 * the {@code META-INF/services/com.sun.xml.ws.api.pipe.TubelineAssemblerFactory} files.
 * Factories found are checked to see if it supports the given binding ID one by one,
 * and the first valid {@link TubelineAssembler} returned will be used to create
 * a tubeline.
 *
 * @author Jitendra Kotamraju
 */
public abstract class TubelineAssemblerFactory {
    /**
     * Creates a {@link TubelineAssembler} applicable for the given binding ID.
     *
     * @param bindingId The binding ID for which a tubeline will be created,
     *                  such as {@link javax.xml.ws.soap.SOAPBinding#SOAP11HTTP_BINDING}.
     *                  Must not be null.
     * @return null if this factory doesn't recognize the given binding ID.
     */
    public abstract TubelineAssembler doCreate(BindingID bindingId);

    /**
     * @deprecated
     *      Use {@link #create(ClassLoader, BindingID, Container)}
     */
    public static TubelineAssembler create(ClassLoader classLoader, BindingID bindingId) {
        return create(classLoader,bindingId,null);
    }

    /**
     * Locates {@link TubelineAssemblerFactory}s and create
     * a suitable {@link TubelineAssembler}.
     *
     * @param bindingId The binding ID string for which the new {@link TubelineAssembler}
     *                  is created. Must not be null.
     * @param container
     *      if specified, the container is given a chance to specify a {@link TubelineAssembler}
     *      instance. This parameter should be always given on the server, but can be null.
     * @return Always non-null, since we fall back to our default {@link TubelineAssembler}.
     */
    public static TubelineAssembler create(ClassLoader classLoader, BindingID bindingId, @Nullable Container container) {

        if(container!=null) {
            // first allow the container to control pipeline for individual endpoint.
            TubelineAssemblerFactory taf = container.getSPI(TubelineAssemblerFactory.class);
            if(taf!=null) {
                TubelineAssembler a = taf.doCreate(bindingId);
                if (a != null) {
                    return a;
                }
            }
        }

        for (TubelineAssemblerFactory factory : ServiceFinder.find(TubelineAssemblerFactory.class, classLoader)) {
            TubelineAssembler assembler = factory.doCreate(bindingId);
            if (assembler != null) {
                TubelineAssemblerFactory.logger.log(Level.FINE, "{0} successfully created {1}", new Object[]{factory.getClass(), assembler});
                return assembler;
            }
        }

        // See if there is a PipelineAssembler out there and use it for compatibility.
        for (PipelineAssemblerFactory factory : ServiceFinder.find(PipelineAssemblerFactory.class,classLoader)) {
            PipelineAssembler assembler = factory.doCreate(bindingId);
            if(assembler!=null) {
                logger.log(Level.FINE, "{0} successfully created {1}", new Object[]{factory.getClass(), assembler});
                return new TubelineAssemblerAdapter(assembler);
            }
        }

        // default binding IDs that are known
        return new MetroTubelineAssembler(bindingId, MetroTubelineAssembler.JAXWS_TUBES_CONFIG_NAMES);
    }

    private static class TubelineAssemblerAdapter implements TubelineAssembler {
        private PipelineAssembler assembler;

        TubelineAssemblerAdapter(PipelineAssembler assembler) {
            this.assembler = assembler;
        }

        @Override
        public @NotNull Tube createClient(@NotNull ClientTubeAssemblerContext context) {
            ClientPipeAssemblerContext ctxt = new ClientPipeAssemblerContext(
                    context.getAddress(), context.getWsdlModel(), context.getService(),
                    context.getBinding(), context.getContainer());
            return PipeAdapter.adapt(assembler.createClient(ctxt));
        }

        @Override
        public @NotNull Tube createServer(@NotNull ServerTubeAssemblerContext context) {
            if (!(context instanceof ServerPipeAssemblerContext)) {
                throw new IllegalArgumentException("{0} is not instance of ServerPipeAssemblerContext");
            }
            return PipeAdapter.adapt(assembler.createServer((ServerPipeAssemblerContext) context));
        }
    }

    private static final Logger logger = Logger.getLogger(TubelineAssemblerFactory.class.getName());
}
