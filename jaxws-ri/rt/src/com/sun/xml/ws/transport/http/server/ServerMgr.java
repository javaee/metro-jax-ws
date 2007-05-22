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
                    server = HttpServer.create(inetAddress, 5);
                    server.setExecutor(Executors.newCachedThreadPool());
                    String path = url.toURI().getPath();
                    logger.fine("Creating HTTP Context at = "+path);
                    HttpContext context = server.createContext(path);
                    server.start();
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
