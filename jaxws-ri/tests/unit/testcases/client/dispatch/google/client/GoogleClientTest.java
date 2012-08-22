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

package client.dispatch.google.client;

import junit.framework.TestCase;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;

/**
 * RESTful client for Google Base web service.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class GoogleClientTest extends TestCase {
    public void testGoogleClient() throws Exception {

        // Create resource representation
        URI address = new URI("http", null,
            "www.google.com", 80,
            "/base/feeds/snippets",
            "bq=sun+tech+days", null);

        System.out.println("Getting URL = '" + address + "' ...");

        // Create Dispatch object and invoke WS
        Dispatch<Source> d = createDispatch(address);
        Source result = d.invoke(null);

        // Output result returned from service
        outputSource(result);
    }

    private Dispatch<Source> createDispatch(URI uri) {
        // Create service and port to obtain Dispatch instance
        Service s = javax.xml.ws.Service.create(
                new QName("http://google.com", "google"));
        QName portName = new QName("http://google.com", "port");
        s.addPort(portName, HTTPBinding.HTTP_BINDING,
            uri.toString());

        // Create Dispatch instance and setup HTTP headers
        Dispatch<Source> d = s.createDispatch(portName,
                Source.class, Service.Mode.PAYLOAD);
        setupHTTPHeaders(d);
        return d;
    }

    private void setupHTTPHeaders(Dispatch<Source> d) {
        Map<String, Object> requestContext = d.getRequestContext();

        // Set HTTP operation to GET
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, new String("GET"));

        // Setup HTTP headers as required by service
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("X-Google-Key",
            Arrays.asList("key=ABQIAAAA7VerLsOcLuBYXR7vZI2NjhTRERdeAiwZ9EeJWta3L_JZVS0bOBRIFbhTrQjhHE52fqjZvfabYYyn6A"));
        headers.put("Accept", Arrays.asList("application/atom+xml"));
        headers.put("Content-Type", Arrays.asList("application/atom+xml"));
        requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
    }

    private void outputSource(Source s) throws Exception {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty("indent", "yes");
        File output = new File("google.xml");
        t.transform(s, new StreamResult(output));
        System.out.println("Output written to '" + output.toURL() + "'");
        System.out.println("Done.");
    }
}
