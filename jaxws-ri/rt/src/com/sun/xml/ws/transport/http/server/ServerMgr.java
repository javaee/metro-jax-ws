/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.transport.http.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.xml.ws.server.ServerRtException;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Manages all the WebService HTTP servers created by JAXWS runtime.
 *
 * @author Jitendra Kotamraju
 */
final class ServerMgr {
    
    private static final ServerMgr serverMgr = new ServerMgr();
    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
    private final Map<InetSocketAddress,ServerState> servers = new HashMap<InetSocketAddress,ServerState>();
            
    private ServerMgr() {}

    /**
     * Gets the singleton instance.
     * @return manager instance
     */
    static ServerMgr getInstance() {
        return serverMgr;
    }
    
    /*
     * Creates a HttpContext at the given address. If there is already a server
     * it uses that server to create a context. Otherwise, it creates a new
     * HTTP server. This sever is added to servers Map.
     */
    /*package*/ HttpContext createContext(String address) {
        try {
            HttpServer server;
            ServerState state;
            URL url = new URL(address);
            int port = url.getPort();
            if (port == -1) {
                port = url.getDefaultPort();
            }
            InetSocketAddress inetAddress = new InetSocketAddress(url.getHost(),
                port);
            synchronized(servers) {
                state = servers.get(inetAddress);
                if (state == null) {
                    logger.fine("Creating new HTTP Server at "+inetAddress);
                    // Creates server with default socket backlog
                    server = HttpServer.create(inetAddress, 0);
                    server.setExecutor(Executors.newCachedThreadPool());
                    String path = url.toURI().getPath();
                    logger.fine("Creating HTTP Context at = "+path);
                    HttpContext context = server.createContext(path);
                    server.start();

                    // we have to get actual inetAddress from server, which can differ from the original in some cases.
                    // e.g. A port number of zero will let the system pick up an ephemeral port in a bind operation,
                    // or IP: 0.0.0.0 - which is used to monitor network traffic from any valid IP address
                    inetAddress = server.getAddress();

                    logger.fine("HTTP server started = "+inetAddress);
                    state = new ServerState(server);
                    servers.put(inetAddress, state);
                    return context;
                }
            }
            server = state.getServer();
            logger.fine("Creating HTTP Context at = "+url.getPath());
            HttpContext context = server.createContext(url.getPath());
            state.oneMoreContext();
            return context;
        } catch(Exception e) {
            throw new ServerRtException("server.rt.err",e );
        }
    }
    
    /*
     * Removes a context. If the server doesn't have anymore contexts, it
     * would stop the server and server is removed from servers Map.
     */
    /*package*/ void removeContext(HttpContext context) {
        InetSocketAddress inetAddress = context.getServer().getAddress();
        synchronized(servers) {
            ServerState state = servers.get(inetAddress);
            int instances = state.noOfContexts();
            if (instances < 2) {
                ((ExecutorService)state.getServer().getExecutor()).shutdown();
                state.getServer().stop(0);
                servers.remove(inetAddress);
            } else {
                state.getServer().removeContext(context);
                state.oneLessContext();
            }
        }
    }
    
    private static final class ServerState {
        private final HttpServer server;
        private int instances;
        
        ServerState(HttpServer server) {
            this.server = server;
            this.instances = 1;
        }
        
        public HttpServer getServer() {
            return server;
        }
        
        public void oneMoreContext() {
            ++instances;
        }
        
        public void oneLessContext() {
            --instances;
        }
        
        public int noOfContexts() {
            return instances;
        }
    }
}
