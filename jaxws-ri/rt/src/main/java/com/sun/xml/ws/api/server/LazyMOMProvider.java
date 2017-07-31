/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.server;

import com.sun.xml.ws.server.WSEndpointImpl;
import java.util.HashSet;
import java.util.Set;
import org.glassfish.external.amx.AMXGlassfish;
import org.glassfish.external.amx.MBeanListener;
import org.glassfish.gmbal.ManagedObjectManager;

/**
 * The lazy provider is intended to defer Gmbal API calls until there is a JMX connection. The provider is scope
 * (environment) aware and behaves accordingly to actual scope. The default behaviour does not defers Gmbal API calls.
 *
 * There are two kind of method allowing registration of an object as a listener. One is for {@code WSEndpointImpl}
 * instances (implementing {@code WSEndpointScopeChangeListener}) and the other is for arbitrary objects
 * (implementing {@code DefaultScopeChangeListener}) that want to be notified about scope changes. The distinction is made
 * because of the fact that endpoints should be registered first and other objects (e.g. dependants on endpoints) must
 * be registered after all the endpoints are processed so no inconsistency is present.
 *
 * Glassfish:
 * {@link WebServicesContainer} is one of two classes for which a {@link ManagedObjectManager} instance (see Gmbal API)
 * is created when a webservice application is deployed into the Glassfish. For the purpose of postponing Gmbal API calls
 * the {@code WebServicesContainer} extends {@link MBeanListener.CallbackImpl} so it can register itself as a listener of
 * {@link AMXGlassfish} and receive a notification about a connection of a JMX client to the Glassfish server (see
 * {@code WebServicesContainer#postConstruct} for registration details). The moment the JMX client is connected a notification
 * is sent to the listeners of {@code AMXGlassfish}. When this event is received by {@code WebServiceContainer} (see the
 * callback method {@code mbeanRegistered}) a {@code ManagedObjectManager} instance is created and no further deferring
 * of Gmbal API calls is needed.
 *
 * Metro/JAX-WS:
 * The situation in Metro/JAX-WS is slightly different from the one described above. Metro/JAX-WS can be used as standalone
 * libraries (outside of Glassfish) so no notification from the Glassfish server can be expected in this case. This leads
 * to a situation when Metro/JAX-WS has to be aware of context in which it is used and acts appropriately. There are 3
 * scopes an application using Metro/JAX-WS can be in: {@code STANDALONE}, {@code GLASSFISH_NO_JMX}, {@code GLASSFISH_JMX}
 * ({@link LazyMOMProvider#scope}). The default scope is {@code STANDALONE} and all Gmbal API calls are invoked as they
 * are requested. The other two scopes are applied only when an application is deployed into a Glassfish server. The
 * {@code GLASSFISH_NO_JMX} is set at the moment the application is deployed (see below) and its purpose is to defer Gmbal
 * API calls for as long as possible. For some classes e.g. {@code ManagedObjectManager} proxy classes were introduced to
 * avoid the necessity of creating the real Gmbal objects but if a method is invoked on these proxies the creation of real
 * Gmbal objects is forced even in this scope. The {@code GLASSFISH_JMX} scope is set when a JMX client is connected to
 * the Glassfish server and it processes Gmbal API calls without deferring (as if the application was in the
 * {@code STANDALONE} scope). The entry point for postponing the Gmbal API calls / creating Gmbal objects in Metro/JAX-WS
 * is {@link LazyMOMProvider}. This class is capable of receiving notifications from the Glassfish server
 * ({@code LazyMOMProvider.initMOMForScope}) about the scope change and it also spread this information to its listeners.
 * The listeners of {@code LazyMOMProvider} are of two kinds: {@link LazyMOMProvider.WSEndpointScopeChangeListener} and
 * {@link LazyMOMProvider.DefaultScopeChangeListener}. Extensions of {@link WSEndpoint} (e.g. {@link WSEndpointImpl})
 * should implement the {@code LazyMOMProvider.WSEndpointScopeChangeListener} and register themselves as endpoint listeners
 * of {@code LazyMOMProvider}. Other classes should implement the latter mentioned interface and register themselves as
 * a non-endpoint listener. The differences between these two kind of listeners are described in {@code LazyMOMProvider}
 * class comment. An implementation of {@code LazyMOMProvider.DefaultScopeChangeListener} is provided in Metro
 * ({@link WSEndpointCollectionBasedMOMListener}). As mentioned above this listener register itself as a non-endpoint
 * listener of {@code LazyMOMProvider} ({@code WSEndpointCollectionBasedMOMListener.init}). An instance of this class is
 * used in these factories: {@link SessionManager}, {@link NonceManager} and {@link SequenceManagerFactory}.
 * {@code SessionManager}, {@code NonceManager}, {@code PersistentSequenceManager} and {@code InVmSequenceManager} also
 * (indirectly) implement {@link MOMRegistrationAware} for providing information whether a manager is registered at
 * {@code ManagedObjectManager} or not. Registration of a manager at {@code ManagedObjectManager} can be processed directly
 * (if {@code WSEndpointCollectionBasedMOMListener.canRegisterAtMOM} returns {@code true}) via
 * {@code WSEndpointCollectionBasedMOMListener.registerAtMOM} or is deferred by putting the manager into
 * {@code WSEndpointCollectionBasedMOMListener.registrationAwareMap}. Managers stored in for deferred registration are
 * processed at the moment the {@code LazyMOMProvider} notifies the {@code WSEndpointCollectionBasedMOMListener} about
 * the scope change.
 * The mentioned postponing of Gmbal API calls applies only to the server side of the webservice application.
 */
public enum LazyMOMProvider {

    INSTANCE;

    /**
     * Possible scopes (environments) in which the provider (and object associated with it) could be in.
     * Default scope is STANDALONE - applied during initialization of classes. For now, only possible scope change for a
     * object can be in this direction: STANDALONE -> GLASSFISH_NO_JMX -> GLASSFISH_JMX.
     */
    public static enum Scope {

        //** Default scope where lazy flag is not applied and all Gmbal API calls are processed immediately. */
        STANDALONE,

        /** In this scope almost all Gmbal API call are deferred until a JMX connection to a Glassfish server is created */
        GLASSFISH_NO_JMX,

        /** Same as STANDALONE. Gmbal API calls are processed immediately. */
        GLASSFISH_JMX

    }

    /**
     * Interface for all object that want to be notified about scope change, introducing required methods.
     */
    public static interface ScopeChangeListener {

        void scopeChanged(Scope scope);

    }

    /**
     * Default interface for all object that want to be notified about scope change. This interface should not be
     * implemented directly.
     */
    public static interface DefaultScopeChangeListener extends ScopeChangeListener {
    }

    /**
     * Interface used for distinguishing between a registration of a WSEndpointImpl rather than of other classes.
     * Webservice Endpoints should get a notification about scope change sooner than the rest of the registered listeners
     * (there is a possibility that other listeners are dependant on Webservice Endpoints).
     */
    public static interface WSEndpointScopeChangeListener extends ScopeChangeListener {
    }

    private final Set<WSEndpointScopeChangeListener> endpointsWaitingForMOM = new HashSet<WSEndpointScopeChangeListener>();
    private final Set<DefaultScopeChangeListener> listeners = new HashSet<DefaultScopeChangeListener>();

    private volatile Scope scope = Scope.STANDALONE;

    /**
     * Initializes this provider with a given scope. If the given scope is different than the one this provider is
     * currently in and the transition between scopes is valid then a event is fired to all registered listeners.
     *
     * @param scope a scope to initialize this provider with
     */
    public void initMOMForScope(LazyMOMProvider.Scope scope) {
        // cannot go backwards between scopes, for possible scope changes see #Scope
        if ((this.scope == Scope.GLASSFISH_JMX)
                || (scope == Scope.STANDALONE && (this.scope == Scope.GLASSFISH_JMX || this.scope == Scope.GLASSFISH_NO_JMX))
                || this.scope == scope) {
            return;
        }

        this.scope = scope;
        
        fireScopeChanged();
    }

    /**
     * Notifies the registered listeners about the scope change.
     */
    private void fireScopeChanged() {
        for (ScopeChangeListener wsEndpoint : endpointsWaitingForMOM) {
            wsEndpoint.scopeChanged(this.scope);
        }
        
        for (ScopeChangeListener listener : listeners) {
            listener.scopeChanged(this.scope);
        }
    }

    /**
     * Registers the given object as a listener.
     *
     * @param listener a listener to be registered
     */
    public void registerListener(DefaultScopeChangeListener listener) {
        listeners.add(listener);

        if (!isProviderInDefaultScope()) {
            listener.scopeChanged(this.scope);
        }
    }

    /**
     * Returns {@code true} if this provider is in the default scope.
     *
     * @return {@code true} if this provider is in the default scope,
     *          {@code false} otherwise
     */
    private boolean isProviderInDefaultScope() {
        return this.scope == Scope.STANDALONE;
    }

    public Scope getScope() {
        return scope;
    }

    /**
     * Registers a Webservice Endpoint as a listener.
     * Webservice Endpoints should rather register through this method than through LazyMOMProvider#registerListener
     * because generally they need to be notified sooner than arbitrary listener (i.e. object that is dependant on
     * Webservice Endpoint)
     *
     * @param wsEndpoint a Webservice Endpoint to be registered
     */
    public void registerEndpoint(WSEndpointScopeChangeListener wsEndpoint) {
        endpointsWaitingForMOM.add(wsEndpoint);

        if (!isProviderInDefaultScope()) {
            wsEndpoint.scopeChanged(this.scope);
        }
    }

    /**
     * Unregisters a Webservice Endpoint from the list of listeners.
     *
     * @param wsEndpoint a Webservice Endpoint to be unregistered
     */
    public void unregisterEndpoint(WSEndpointScopeChangeListener wsEndpoint) {
        endpointsWaitingForMOM.remove(wsEndpoint);
    }

}
