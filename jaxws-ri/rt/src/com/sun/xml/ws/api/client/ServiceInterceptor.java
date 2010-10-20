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
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.developer.WSBindingProvider;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceFeature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interception point for inner working of {@link WSService}.
 *
 * <p>
 * System-level code could hook an implementation of this to
 * {@link WSService} to augument its behavior.
 *
 * @author Kohsuke Kawaguchi
 * @since 2.1 EA3
 * @see ServiceInterceptorFactory
 */
public abstract class ServiceInterceptor {
    /**
     * Called before {@link WSBinding} is created, to allow interceptors
     * to add {@link WebServiceFeature}s to the created {@link WSBinding}.
     *
     * @param port
     *      Information about the port for which dispatch/proxy will be created.
     * @param serviceEndpointInterface
     *      Null if the created binding is for {@link Dispatch}. Otheriwse
     *      it represents the port interface of the proxy to be created.
     * @param defaultFeatures
     *      The list of features that are currently scheduled to be set for
     *      the newly created {@link WSBinding}.
     *
     * @return
     *      A set of features to be added to the newly created {@link WSBinding}.
     *      Can be empty but never null.
     *      <tt>defaultFeatures</tt> will take precedence over what this method
     *      would return (because it includes user-specified ones which will
     *      take the at-most priority), but features you return from this method
     *      will take precedence over {@link BindingID}'s
     *      {@link BindingID#createBuiltinFeatureList() implicit features}.
     */
    public List<WebServiceFeature> preCreateBinding(@NotNull WSPortInfo port, @Nullable Class<?> serviceEndpointInterface, @NotNull WSFeatureList defaultFeatures) {
        return Collections.emptyList();
    }


    /**
     * A callback to notify the event of creation of proxy object for SEI endpoint. The
     * callback could set some properties on the {@link BindingProvider}.
     *
     * @param bp created proxy instance
     * @param serviceEndpointInterface SEI of the endpoint
     */
    public void postCreateProxy(@NotNull WSBindingProvider bp,@NotNull Class<?> serviceEndpointInterface) {
    }


    /**
     * A callback to notify that a {@link Dispatch} object is created. The callback
     * could set some properties on the {@link BindingProvider}.
     *
     * @param bp BindingProvider of dispatch object
     */
    public void postCreateDispatch(@NotNull WSBindingProvider bp) {
    }

    /**
     * Aggregates multiple interceptors into one facade.
     */
    public static ServiceInterceptor aggregate(final ServiceInterceptor... interceptors) {
        if(interceptors.length==1)
            return interceptors[0];
        return new ServiceInterceptor() {
            public List<WebServiceFeature> preCreateBinding(@NotNull WSPortInfo port, @Nullable Class<?> portInterface, @NotNull WSFeatureList defaultFeatures) {
                List<WebServiceFeature> r = new ArrayList<WebServiceFeature>();
                for (ServiceInterceptor si : interceptors)
                    r.addAll(si.preCreateBinding(port,portInterface,defaultFeatures));
                return r;
            }

            public void postCreateProxy(@NotNull WSBindingProvider bp, @NotNull Class<?> serviceEndpointInterface) {
                for (ServiceInterceptor si : interceptors)
                    si.postCreateProxy(bp,serviceEndpointInterface);
            }

            public void postCreateDispatch(@NotNull WSBindingProvider bp) {
                for (ServiceInterceptor si : interceptors)
                    si.postCreateDispatch(bp);
            }
        };
    }
}
