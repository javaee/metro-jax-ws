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

package server.endpoint.client;

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
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

import testutil.PortAllocator;

/**
 * @author Jitendra Kotamraju
 */
public class EndpointAPITest extends TestCase {

    public EndpointAPITest(String name) {
        super(name);
    }

    public void testEndpoint() {
        int port = PortAllocator.getFreePort();
        String address = "http://localhost:"+port+"/hello";
        Endpoint endpoint = Endpoint.publish(address, new RpcLitEndpoint());

        doAPItesting(endpoint);
    }

    public void testHttpsAddress() {
        int port = PortAllocator.getFreePort();
        String address = "https://localhost:"+port+"/hello";
        // Doesn't support https scheme, so expect IllegalArgumentException
        try {
            Endpoint endpoint = Endpoint.publish(address, new RpcLitEndpoint());
            assertTrue(false);
        } catch(IllegalArgumentException e) {
        }
    }

    /**
     * @param endpoint - Already published endpoint
     */
    private void doAPItesting(Endpoint endpoint) {

        // Since it is already published, isPublished should return true
        if (!endpoint.isPublished()) {
            assertTrue(false);
        }

        // Binding shouldn't be null
        assertTrue(endpoint.getBinding() != null);

        // Since it is already published, cannot set metadata
        // Expect IllegalStateException
        try {
            endpoint.setMetadata(null);
            assertTrue(false);
        } catch(IllegalStateException e) {
        }

        // Since it is already published, cannot publish again.
        // Expect IllegalStateException
        try {
            endpoint.publish("http://localhost");
            assertTrue(false);
        } catch(IllegalStateException e) {
        }

        endpoint.stop();

        // Since it is stopped, isPublished should return false
        if (endpoint.isPublished()) {
            assertTrue(false);
        }

        // Since it is stopped, cannot publish again.
        // Expect IllegalStateException
        try {
            endpoint.publish("http://localhost");
            assertTrue(false);
        } catch(IllegalStateException e) {
        }
    }

    public void testEndpoint1() {
        int port = PortAllocator.getFreePort();
        String address = "http://localhost:"+port+"/hello";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        endpoint.publish(address);
        doAPItesting(endpoint);
    }

    public void testNoPath() throws Exception {
        int port = PortAllocator.getFreePort();
        String address = "http://localhost:"+port;
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        try {
            endpoint.publish(address);
            assert(false);
        } catch(IllegalArgumentException ie) {
            // Intentionally left empty
        }
    }

    public void testSlashPath() throws Exception {
        int port = PortAllocator.getFreePort();
        String address = "http://localhost:"+port+"/";
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        endpoint.publish(address);
        assertTrue(isWSDL(address));
        endpoint.stop();
    }

    private boolean isWSDL(String address) throws Exception {
        URL url = new URL(address+"?wsdl");
        InputStream in = url.openStream();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
        String str;
        while ((str=rdr.readLine()) != null) {
            if (str.indexOf("definitions") != -1) {
                return true;
            }
        }
        return false;
    }


    public void testStopBeforePublish() {
        Endpoint endpoint = Endpoint.create(new RpcLitEndpoint());
        // Stop before publish() shouldn't have any effect
        endpoint.stop();
    }

}

