/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package server.endpoint_http_context.client;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import junit.framework.TestCase;
import testutil.ClientServerTestUtil;
import testutil.PortAllocator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jitendra Kotamraju
 */
public class HttpContextTest extends TestCase {

    public void testContext() throws Exception {
        int port = PortAllocator.getFreePort();
        String address = "http://localhost:"+port+"/hello";

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 5);
        ExecutorService threads  = Executors.newFixedThreadPool(5);
        server.setExecutor(threads);
        server.start();

        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        HttpContext context = server.createContext("/hello");
        endpoint.publish(context);

        // access HTML page and check the wsdl location
        String wsdlAddress = address+"?wsdl";
        String str = getHtmlPage(address);
        assertTrue(str+"doesn't have "+wsdlAddress, str.contains(wsdlAddress));

        // See if WSDL is published at the correct address
        int code = getHttpStatusCode(wsdlAddress);
        assertEquals(HttpURLConnection.HTTP_OK, code);

        // Check abstract wsdl address
        String wsdlImportAddress = getWsdlImportAddress(wsdlAddress);
        assertEquals(address+"?wsdl=1", wsdlImportAddress);

        // See if abstract WSDL is published at the correct address
        code = getHttpStatusCode(wsdlImportAddress);
        assertEquals(HttpURLConnection.HTTP_OK, code);

        // Check published web service soap address
        String pubAddress = getSoapAddress(wsdlAddress);
        assertEquals(address, pubAddress);

        // Invoke service
        invoke(address);

        endpoint.stop();

        server.stop(1);
        threads.shutdown();
    }

    private void invoke(String address) {
        // access service
        QName portName = new QName("http://echo.org/", "RpcLitPort");
        QName serviceName = new QName("http://echo.org/", "RpcLitEndpoint");
        Service service = Service.create(serviceName);
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, address);
        Dispatch<Source> d = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        String body  = "<ns0:echoInteger xmlns:ns0=\"http://echo.abstract.org/\"><arg0>2</arg0></ns0:echoInteger>";
        Source request = new StreamSource(new ByteArrayInputStream(body.getBytes()));
        d.invoke(request);
    }

    private int getHttpStatusCode(String address) throws Exception {
        URL url = new URL(address);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.connect();
        return con.getResponseCode();
    }

    private String getHtmlPage(String address) throws Exception {
        URL url = new URL(address);
        BufferedReader rdr = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();
        String str;
        while ((str=rdr.readLine()) != null) {
            sb.append(str);
        }
        rdr.close();
        return sb.toString();
    }


    private String getWsdlImportAddress(String wsdlAddress) throws Exception {
        URL url = new URL(wsdlAddress);
        XMLStreamReader rdr = XMLInputFactory.newInstance().createXMLStreamReader(url.openStream());
        try {
            while(rdr.hasNext()) {
                int event = rdr.next();
                if (event == XMLStreamReader.START_ELEMENT) {
                    if (rdr.getName().equals(new QName("http://schemas.xmlsoap.org/wsdl/", "import"))) {
                        return rdr.getAttributeValue(null, "location");
                    }
                }
            }
        } finally {
            rdr.close();
        }
        return null;
    }

    private String getSoapAddress(String wsdlAddress) throws Exception {
        URL url = new URL(wsdlAddress);
        XMLStreamReader rdr = XMLInputFactory.newInstance().createXMLStreamReader(url.openStream());
        try {
            while(rdr.hasNext()) {
                int event = rdr.next();
                if (event == XMLStreamReader.START_ELEMENT) {
                    if (rdr.getName().equals(new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address"))) {
                        return rdr.getAttributeValue(null, "location");
                    }
                }
            }
        } finally {
            rdr.close();
        }
        return null;
    }

}

