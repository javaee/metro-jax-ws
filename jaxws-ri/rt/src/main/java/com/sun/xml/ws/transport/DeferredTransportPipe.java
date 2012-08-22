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

package com.sun.xml.ws.transport;

import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.*;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.istack.NotNull;
import com.sun.xml.ws.developer.HttpConfigFeature;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceFeature;

/**
 * Proxy transport {@link Tube} and {@link Pipe} that lazily determines the
 * actual transport pipe by looking at {@link Packet#endpointAddress}.
 *
 * <p>
 * This pseudo transport is used when there's no statically known endpoint address,
 * and thus it's expected that the application will configure {@link BindingProvider}
 * at runtime before making invocation.
 *
 * <p>
 * Since a typical application makes multiple invocations with the same endpoint
 * address, this class implements a simple cache strategy to avoid re-creating
 * transport pipes excessively.
 *
 * @author Kohsuke Kawaguchi
 */
public final class DeferredTransportPipe extends AbstractTubeImpl {

    private Tube transport;
    private EndpointAddress address;

    // parameter to TransportPipeFactory
    private final ClassLoader classLoader;
    private final ClientTubeAssemblerContext context;

    public DeferredTransportPipe(ClassLoader classLoader, ClientTubeAssemblerContext context) {
        this.classLoader = classLoader;
        this.context = context;
        if (context.getBinding().getFeature(HttpConfigFeature.class) == null) {
            context.getBinding().getFeatures().mergeFeatures(
                    new WebServiceFeature[] { new HttpConfigFeature() }, false);
        }
        //See if we can create the transport pipe from the available information.
        try {
            this.transport = TransportTubeFactory.create(classLoader, context);
            this.address = context.getAddress();
        } catch(Exception e) {
            //No problem, transport will be initialized while processing the requests
        }
    }

    public DeferredTransportPipe(DeferredTransportPipe that, TubeCloner cloner) {
        super(that,cloner);
        this.classLoader = that.classLoader;
        this.context = that.context;
        if(that.transport!=null) {
            this.transport = cloner.copy(that.transport);
            this.address = that.address;
       }
    }
    public NextAction processException(@NotNull Throwable t) {
        return transport.processException(t);
    }

    public NextAction processRequest(@NotNull Packet request) {
        if(request.endpointAddress==address)
            // cache hit
            return transport.processRequest(request);

        // cache miss

        if(transport!=null) {
            // delete the current entry
            transport.preDestroy();
            transport = null;
            address = null;
        }

        // otherwise find out what transport will process this.

        ClientTubeAssemblerContext newContext = new ClientTubeAssemblerContext(
            request.endpointAddress,
            context.getWsdlModel(),
            context.getBindingProvider(),
            context.getBinding(),
            context.getContainer(),
            context.getCodec().copy(),
            context.getSEIModel(),
            context.getSEI()
        );

        address = request.endpointAddress;
        transport = TransportTubeFactory.create(classLoader, newContext);
        // successful return from the above method indicates a successful pipe creation
        assert transport!=null;

        return transport.processRequest(request);
    }

    public NextAction processResponse(@NotNull Packet response) {
    	if (transport != null)
    		return transport.processResponse(response);
    	return doReturnWith(response);
    }

    public void preDestroy() {
        if(transport!=null) {
            transport.preDestroy();
            transport = null;
            address = null;
        }
    }

    public DeferredTransportPipe copy(TubeCloner cloner) {
        return new DeferredTransportPipe(this,cloner);
    }
}
