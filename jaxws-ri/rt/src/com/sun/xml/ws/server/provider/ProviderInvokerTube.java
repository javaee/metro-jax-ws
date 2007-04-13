/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.server.provider;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.server.AsyncProvider;
import com.sun.xml.ws.api.server.Invoker;
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
    create(Class<T> implType, WSBinding binding, Invoker invoker) {

        ProviderEndpointModel<T> model = new ProviderEndpointModel<T>(implType, binding);
        ProviderArgumentsBuilder<?> argsBuilder = ProviderArgumentsBuilder.create(model, binding);
        return model.isAsync ? new AsyncProviderInvokerTube(invoker, argsBuilder)
            : new SyncProviderInvokerTube(invoker, argsBuilder);
    }
}
