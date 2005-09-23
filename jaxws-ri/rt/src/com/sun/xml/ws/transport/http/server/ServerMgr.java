/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.transport.http.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import com.sun.xml.ws.server.ServerRtException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;

/**
 * Manages all the WebService HTTP/HTTPS servers created by JAXWS runtime.
 *
 * @author WS Development Team
 */
public class ServerMgr {
    
    private static final ServerMgr serverMgr = new ServerMgr();
    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
    private Map<InetSocketAddress, ServerState> servers = new HashMap(); 
            
    protected ServerMgr() {
    }
    
    public static ServerMgr getInstance() {
        return serverMgr;
    }
    
    /*
     * Creates a HttpContext at the given address. If there is already a server
     * it uses that server to create a context. Otherwise, it creates a new
     * HTTP or HTTPS server. This sever is added to servers Map.
     */
    public HttpContext createContext(String address) {
        try {
            HttpServer server = null;
            ServerState state = null;
            boolean started = false;
            URL url = new URL(address);
            InetSocketAddress inetAddress = new InetSocketAddress(url.getHost(),
                    url.getPort());
            synchronized(servers) {
                state = servers.get(inetAddress);
                if (state == null) {
                    if (url.getProtocol().equals("http")) {
                        logger.fine("Creating new HTTP Server at "+inetAddress);
                        server = HttpServer.create(inetAddress, 5);
                    } else {
                        throw new IllegalArgumentException("https URL is not supported");
                    }
                    server.setExecutor(Executors.newFixedThreadPool(5));
                    logger.fine("Creating HTTP Context at = "+url.getPath());
                    HttpContext context = server.createContext(url.getPath());
                    server.start();
                    logger.fine("HTTP(S) server started = "+inetAddress);
                    state = new ServerState(server);
                    servers.put(inetAddress, state);
                    return context;
                }
            }
            server = state.getServer();
            if (url.getProtocol().equals("https") && !(server instanceof HttpsServer)) {
                throw new ServerRtException("already.http.server",
                    new Object[] { inetAddress } );
            }
            if (url.getProtocol().equals("http") && server instanceof HttpsServer) {
                throw new ServerRtException("already.https.server",
                    new Object[] { inetAddress } );
            }
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
    public void removeContext(HttpContext context) {
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
    
    private static class ServerState {
        private HttpServer server;
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
