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

package externalmetadata.fromwsdl.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceFeature;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.oracle.webservices.api.databinding.ExternalMetadataFeature;
import com.oracle.webservices.api.databinding.ExternalMetadataFeature.Builder;

/**
 * simple J2SE tester class for this sample
 */
public class J2SETester {

    public static void main (String[] args) throws Exception {

        File file = new File("etc/external-metadata.xml");
        WebServiceFeature feature = ExternalMetadataFeature.builder().addFiles(file).build();
        Endpoint endpoint = Endpoint.create(new BlackboxServiceImpl(), feature);
        endpoint.publish("http://localhost:8080/jaxws-external-metadata-fromwsdl/WS");

        // Stops the endpoint if it receives request http://localhost:9090/stop
        new EndpointStopper(9090, endpoint);
    }

    static class EndpointStopper {

        EndpointStopper(final int port, final Endpoint endpoint) throws IOException {
            final HttpServer server = HttpServer.create(new InetSocketAddress(port), 5);
            final ExecutorService threads  = Executors.newFixedThreadPool(2);
            server.setExecutor(threads);
            server.start();

            HttpContext context = server.createContext("/stop");
            context.setHandler(new HttpHandler() {
                public void handle(HttpExchange msg) throws IOException {
                    System.out.println("Shutting down the Endpoint");
                    endpoint.stop();
                    System.out.println("Endpoint is down");
                    msg.sendResponseHeaders(200, 0);
                    msg.close();
                    server.stop(1);
                    threads.shutdown();
                }
            });
        }
    }

}
