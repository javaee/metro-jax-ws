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

package com.sun.xml.ws.assembler.dev;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.policy.PolicyMap;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public interface ServerTubelineAssemblyContext extends TubelineAssemblyContext {

    /**
     * Gets the {@link Codec} that is set by {@link #setCodec} or the default codec
     * based on the binding. The codec is a full codec that is responsible for
     * encoding/decoding entire protocol message(for e.g: it is responsible to
     * encode/decode entire MIME messages in SOAP binding)
     *
     * @return codec to be used for web service requests
     * @see com.sun.xml.ws.api.pipe.Codecs
     */
    @NotNull
    Codec getCodec();

    /**
     *
     * The created pipeline is used to serve this {@link com.sun.xml.ws.api.server.WSEndpoint}.
     * Specifically, its {@link com.sun.xml.ws.api.WSBinding} should be of interest to  many
     * {@link com.sun.xml.ws.api.pipe.Pipe}s.
     * @return Always non-null.
     */
    @NotNull
    WSEndpoint getEndpoint();

    PolicyMap getPolicyMap();

    /**
     * The created pipeline will use seiModel to get java concepts for the endpoint
     *
     * @return Null if the service doesn't have SEI model e.g. Provider endpoints,
     * and otherwise non-null.
     */
    @Nullable
    SEIModel getSEIModel();

    /**
     * The last {@link com.sun.xml.ws.api.pipe.Pipe} in the pipeline. The assembler is expected to put
     * additional {@link com.sun.xml.ws.api.pipe.Pipe}s in front of it.
     *
     * <p>
     * (Just to give you the idea how this is used, normally the terminal pipe
     * is the one that invokes the user application or {@link javax.xml.ws.Provider}.)
     *
     * @return always non-null terminal pipe
     */
    @NotNull
    Tube getTerminalTube();

    ServerTubeAssemblerContext getWrappedContext();

    /**
     * The created pipeline will be used to serve this port.
     *
     * @return Null if the service isn't associated with any port definition in WSDL,
     * and otherwise non-null.
     */
    @Nullable
    WSDLPort getWsdlPort();

    boolean isPolicyAvailable();

    /**
     * If this server pipeline is known to be used for serving synchronous transport,
     * then this method returns true. This can be potentially use as an optimization
     * hint, since often synchronous versions are cheaper to execute than asycnhronous
     * versions.
     */
    boolean isSynchronous();

    /**
     * Interception point to change {@link Codec} during {@link Tube}line assembly. The
     * new codec will be used by jax-ws server runtime for encoding/decoding web service
     * request/response messages. {@link WSEndpoint#createCodec()} will return a copy
     * of this new codec and will be used in the server runtime.
     *
     * <p>
     * The codec is a full codec that is responsible for
     * encoding/decoding entire protocol message(for e.g: it is responsible to
     * encode/decode entire MIME messages in SOAP binding)
     *
     * <p>
     * the codec should correctly implement {@link Codec#copy} since it is used while
     * serving requests concurrently.
     *
     * @param codec codec to be used for web service requests
     * @see com.sun.xml.ws.api.pipe.Codecs
     */
    void setCodec(@NotNull
    Codec codec);

}
