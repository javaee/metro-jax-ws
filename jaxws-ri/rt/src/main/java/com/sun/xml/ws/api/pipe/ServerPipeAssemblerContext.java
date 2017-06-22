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
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.helper.PipeAdapter;
import com.sun.xml.ws.api.server.WSEndpoint;

import java.io.PrintStream;

/**
 * Factory for well-known server {@link Pipe} implementations
 * that the {@link PipelineAssembler} needs to use
 * to satisfy JAX-WS requirements.
 *
 * @author Jitendra Kotamraju
 * @deprecated Use {@link ServerTubeAssemblerContext}.
 */
public final class ServerPipeAssemblerContext extends ServerTubeAssemblerContext {

    public ServerPipeAssemblerContext(@Nullable SEIModel seiModel, @Nullable WSDLPort wsdlModel, @NotNull WSEndpoint endpoint, @NotNull Tube terminal, boolean isSynchronous) {
        super(seiModel, wsdlModel, endpoint, terminal, isSynchronous);
    }

    /**
     * Creates a {@link Pipe} that performs SOAP mustUnderstand processing.
     * This pipe should be before HandlerPipes.
     */
    public @NotNull Pipe createServerMUPipe(@NotNull Pipe next) {
        return PipeAdapter.adapt(super.createServerMUTube(PipeAdapter.adapt(next)));
    }

    /**
     * creates a {@link Pipe} that dumps messages that pass through.
     */
    public Pipe createDumpPipe(String name, PrintStream out, Pipe next) {
        return PipeAdapter.adapt(super.createDumpTube(name, out, PipeAdapter.adapt(next)));
    }

    /**
     * Creates a {@link Pipe} that does the monitoring of the invocation for a
     * container
     */
    public @NotNull Pipe createMonitoringPipe(@NotNull Pipe next) {
        return PipeAdapter.adapt(super.createMonitoringTube(PipeAdapter.adapt(next)));
    }

    /**
     * Creates a {@link Pipe} that adds container specific security
     */
    public @NotNull Pipe createSecurityPipe(@NotNull Pipe next) {
        return PipeAdapter.adapt(super.createSecurityTube(PipeAdapter.adapt(next)));
    }

    /**
     * creates a {@link Pipe} that validates messages against schema
     */
    public @NotNull Pipe createValidationPipe(@NotNull Pipe next) {
       return PipeAdapter.adapt(super.createValidationTube(PipeAdapter.adapt(next)));
    }

    /**
     * Creates a {@link Pipe} that invokes protocol and logical handlers.
     */
    public @NotNull Pipe createHandlerPipe(@NotNull Pipe next) {
        return PipeAdapter.adapt(super.createHandlerTube(PipeAdapter.adapt(next)));
    }

    /**
     * The last {@link Pipe} in the pipeline. The assembler is expected to put
     * additional {@link Pipe}s in front of it.
     *
     * <p>
     * (Just to give you the idea how this is used, normally the terminal pipe
     * is the one that invokes the user application or {@link javax.xml.ws.Provider}.)
     *
     * @return always non-null terminal pipe
     */
     public @NotNull Pipe getTerminalPipe() {
         return PipeAdapter.adapt(super.getTerminalTube());
    }

     /**
     * Creates WS-Addressing pipe
     */
    public Pipe createWsaPipe(Pipe next) {
        return PipeAdapter.adapt(super.createWsaTube(PipeAdapter.adapt(next)));
    }
}
