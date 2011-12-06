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

package com.sun.xml.ws.transport.local;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.server.WSEndpoint;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-VM transport.
 *
 * <p>
 * This transport lets you deploy services in a servlet-like
 * environment within the same VM. Unlike the local transport,
 * which deploys a new server instance every time a new transport
 * tube is created, in-VM transport maintains a server instance
 * outside the transport, allowing multiple clients to talk to the
 * same in-VM service instance.
 *
 * <p>
 * For this reason, in-VM transport requires explicit "deploy"
 * and "undeploy" operations.
 *
 * @author Kohsuke Kawaguchi
 */
public final class InVmServer {
    /**
     * Services that are running.
     */
    private final List<WSEndpoint> endpoints;

    /**
     * Unique name that distinguishes this in-VM server among other running servers.
     */
    private final String id;

    /**
     * Running servers.
     *
     * Use {@link WeakReference} so that {@link InVmServer}
     * instances get GC-ed.
     *
     */
    private static final Map<String, WeakReference<InVmServer>> servers =
        new HashMap<String,WeakReference<InVmServer>>();

    /**
     * Deploys a new server instance.
     *
     * @param id
     *      Every server instance needs to have an unique ID.
     *      If you want to set the ID by yourself, use this version.
     *      Otherwise use the single argument version.
     * @param explodedWarDir
     *      The exploded war file image in the file system,
     *      where services are loaded from.
     */
    public InVmServer(@NotNull String id, File explodedWarDir) throws IOException {
        this(id,LocalTransportFactory.parseEndpoints(explodedWarDir.getPath()));
    }

    public InVmServer(@NotNull String id, List<WSEndpoint> endpoints) throws IOException {
        synchronized(servers) {
            if(servers.containsKey(id))
                throw new IllegalArgumentException("InVmServer with id="+id+" is already running");
            servers.put(id,new WeakReference<InVmServer>(this));
        }
        this.id = id;
        this.endpoints = new ArrayList<WSEndpoint>(endpoints);
    }

    public InVmServer(File explodedWarDir) throws IOException {
        this(generateId(),explodedWarDir);
    }

    /**
     * Finds the {@link WSEndpoint} that matches the given port name.
     */
    @Nullable
    WSEndpoint getByPortName(String portLocalName) {
        for (WSEndpoint ep : endpoints) {
            if(ep.getPortName().getLocalPart().equals(portLocalName))
                return ep;
        }
        return null;
    }

    /**
     * Gets all the {@link WSEndpoint}s.
     */
    List<WSEndpoint> getEndpoints() {
        return Collections.unmodifiableList(endpoints);
    }

    /**
     * Returns the URI that identifies this server. Use this
     * as the endpoint address of the JAX-WS RI to talk to services in this server
     */
    URI getAddress() {
        return URI.create("in-vm://"+id+"/");
    }

    /**
     * Gracefully terminates this service.
     *
     * You can also just let it garbage collected without calling this method,
     * but that would shut down the service without proper termination
     * required by the JAX-WS specification.
     */
    public void undeploy() {
        for (WSEndpoint ep : endpoints)
            ep.dispose();
        endpoints.clear();
        synchronized(servers) {
            if(servers.get(id).get()==this)
                servers.remove(id);
        }
    }

    /**
     * Obtains the running instance from the ID, or returns null
     * if not found.
     */
    public static @Nullable InVmServer get(String id) {
        synchronized(servers) {
            WeakReference<InVmServer> r = servers.get(id);
            if(r==null) return null;
            if(r.get()==null) {
                // GC-ed.
                servers.remove(id);
                return null;
            }

            return r.get();
        }
    }

    public String toString() {
        return "InVmServer:"+id;
    }

    private static synchronized String generateId() {
        return String.valueOf(iotaGen++);
    }
    private static int iotaGen=0;

}
