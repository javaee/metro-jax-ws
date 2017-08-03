/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
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

package whitebox.endpoint.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import testutil.ClientServerTestUtil;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.Endpoint;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.transform.stream.StreamSource;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.*;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.BasicAuthenticator;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.BindingProvider;


/**
 * @author Jitendra Kotamraju
 */
public class HttpContextTester extends TestCase {

    public void testContext() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:"+port+"/hello";

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 5);
        ExecutorService threads  = Executors.newFixedThreadPool(5);
        server.setExecutor(threads);
        server.start();

        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        HttpContext context = server.createContext("/hello");
        endpoint.publish(context);
        // Gets WSDL from the published endpoint
        int code = getHttpStatusCode(address);
        assertEquals(HttpURLConnection.HTTP_OK, code);
        endpoint.stop();

        server.stop(1);
        threads.shutdown();
    }

    public void testAuthentication() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:"+port+"/hello";

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 5);
        ExecutorService threads  = Executors.newFixedThreadPool(5);
        server.setExecutor(threads);
        server.start();

        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        HttpContext context = server.createContext("/hello");
        final String realm = "localhost.realm.com";
        context.setAuthenticator(new BasicAuthenticator(realm) {
            public boolean checkCredentials (String username, String password) {
                System.out.println("Authenticator is called");
                if (username.equals("auth-user") && password.equals("auth-pass")) {
                    return true;
                }
                return false;
            }
        });
        endpoint.publish(context);

/*

        Works but the next request hangs
 
        // Gets WSDL from the published endpoint
        int code = getHttpStatusCode(address);
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, code);
 */
        
        // access service
        QName portName = new QName("http://echo.org/", "RpcLitPort");
        QName serviceName = new QName("http://echo.org/", "RpcLitEndpoint");
        Service service = Service.create(serviceName);
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, address);
        Dispatch<Source> d = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        d.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "auth-user");
        d.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "auth-pass");
        String body  = "<ns0:echoInteger xmlns:ns0=\"http://echo.abstract.org/\"><arg0>2</arg0></ns0:echoInteger>";
        Source request = new StreamSource(new ByteArrayInputStream(body.getBytes()));
        Source response = d.invoke(request);
        request = new StreamSource(new ByteArrayInputStream(body.getBytes()));
        response = d.invoke(request);

        endpoint.stop();
        server.stop(1);
        threads.shutdown();
    }

    private int getHttpStatusCode(String address) throws Exception {
        URL url = new URL(address+"?wsdl");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.connect();
        return con.getResponseCode();
    }

}

