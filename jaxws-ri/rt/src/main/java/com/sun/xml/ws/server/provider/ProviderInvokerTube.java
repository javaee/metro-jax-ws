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

package com.sun.xml.ws.server.provider;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.server.AsyncProvider;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.ProviderInvokerTubeFactory;
import com.sun.xml.ws.binding.SOAPBindingImpl;
import com.sun.xml.ws.server.InvokerTube;

import javax.xml.ws.Provider;

/**
 * This {@link Tube} is used to invoke the {@link Provider} and {@link AsyncProvider} endpoints.
 *
 * @author Jitendra Kotamraju
 */
public abstract class ProviderInvokerTube<T> extends InvokerTube<Provider<T>> {

    protected ProviderArgumentsBuilder<T> argsBuilder;

    /*package*/ ProviderInvokerTube(Invoker invoker, ProviderArgumentsBuilder<T> argsBuilder) {
        super(invoker);
        this.argsBuilder = argsBuilder;
    }

    public static <T> ProviderInvokerTube<T>
    create(final Class<T> implType, final WSBinding binding, final Invoker invoker, final Container container) {

        final ProviderEndpointModel<T> model = new ProviderEndpointModel<T>(implType, binding);
        final ProviderArgumentsBuilder<?> argsBuilder = ProviderArgumentsBuilder.create(model, binding);
        if (binding instanceof SOAPBindingImpl) {
            //set portKnownHeaders on Binding, so that they can be used for MU processing
            ((SOAPBindingImpl) binding).setMode(model.mode);
        }

        return ProviderInvokerTubeFactory.create(null, container, implType, invoker, argsBuilder, model.isAsync);
    }
}
