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

package com.sun.xml.ws.assembler.dev;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.policy.PolicyMap;

/**
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public interface ClientTubelineAssemblyContext extends TubelineAssemblyContext {

    /**
     * The endpoint address. Always non-null. This parameter is taken separately
     * from {@link com.sun.xml.ws.api.model.wsdl.WSDLPort} (even though there's {@link com.sun.xml.ws.api.model.wsdl.WSDLPort#getAddress()})
     * because sometimes WSDL is not available.
     */
    @NotNull
    EndpointAddress getAddress();

    /**
     * The binding of the new pipeline to be created.
     */
    @NotNull
    WSBinding getBinding();

    /**
     * Gets the {@link Codec} that is set by {@link #setCodec} or the default codec
     * based on the binding.
     *
     * @return codec to be used for web service requests
     */
    @NotNull
    Codec getCodec();

    /**
     * Returns the Container in which the client is running
     *
     * @return Container in which client is running
     */
    Container getContainer();

    PolicyMap getPolicyMap();

    WSPortInfo getPortInfo();

    /**
     * The created pipeline will use seiModel to get java concepts for the endpoint
     *
     * @return Null if the service doesn't have SEI model e.g. Dispatch,
     *         and otherwise non-null.
     */
    @Nullable
    SEIModel getSEIModel();

    /**
     * The pipeline is created for this {@link com.sun.xml.ws.api.WSService}.
     * Always non-null. (To be precise, the newly created pipeline
     * is owned by a proxy or a dispatch created from this {@link com.sun.xml.ws.api.WSService}.)
     */
    @NotNull
    WSService getService();

    ClientTubeAssemblerContext getWrappedContext();

    /**
     * The created pipeline will be used to serve this port.
     * Null if the service isn't associated with any port definition in WSDL,
     * and otherwise non-null.
     * <p/>
     * Replaces {@link com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext#getWsdlModel()}
     */
    WSDLPort getWsdlPort();

    boolean isPolicyAvailable();

    /**
     * Interception point to change {@link Codec} during {@link com.sun.xml.ws.api.pipe.Tube}line assembly. The
     * new codec will be used by jax-ws client runtime for encoding/decoding web service
     * request/response messages. The new codec should be used by the transport tubes.
     * <p/>
     * <p/>
     * the codec should correctly implement {@link Codec#copy} since it is used while
     * serving requests concurrently.
     *
     * @param codec codec to be used for web service requests
     */
    void setCodec(@NotNull
                  Codec codec);

}
