/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package server.endpoint_features.client;

import junit.framework.TestCase;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.Endpoint;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPBinding;
import java.io.InputStream;
import java.net.URL;
import testutil.PortAllocator;


/**
 * Tests Endpoint API with web service features
 *
 * @author Jitendra Kotamraju
 */
public class EndpointFeaturesTest extends TestCase {

    private static final QName ANONYMOUS =
            new QName("http://www.w3.org/2007/05/addressing/metadata", "AnonymousResponses");

    // Tests Endpoint.create(impl, features)
    public void testCreateImplFeatures() throws Exception {
        int port = PortAllocator.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.create(new FeaturesEndpoint(),
                new AddressingFeature(true, true, AddressingFeature.Responses.ANONYMOUS));
        publishVerifyStop(address, endpoint);
    }

    // Tests Endpoint.publish(address, impl, features)
    public void testPublish() throws Exception {
        int port = PortAllocator.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.publish(address, new FeaturesEndpoint(),
                new AddressingFeature(true, true, AddressingFeature.Responses.ANONYMOUS));
        assertTrue(endpoint.isPublished());
        publishVerifyStop(address, endpoint);
    }

    // Tests Endpoint.create(bindingId, impl, features)
    public void testCreateBindingImplFeatures() throws Exception {
        int port = PortAllocator.getFreePort();
        String address = "http://localhost:" + port + "/hello";
        Endpoint endpoint = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, new FeaturesEndpoint(),
                new AddressingFeature(true, true, AddressingFeature.Responses.ANONYMOUS));
        publishVerifyStop(address, endpoint);
    }

    // Publishes the endpoint, if it is not published.
    // Checks if the generated WSDL contains wsam:AnonymousResponses policy
    // Stops the endpoint
    private void publishVerifyStop(String address, Endpoint endpoint) throws Exception {
        if (!endpoint.isPublished()) {
            endpoint.publish(address);
        }

        URL pubUrl = new URL(address + "?wsdl");
        boolean anon = isAnonymousResponses(pubUrl);
        assertTrue(anon);

        endpoint.stop();
    }


    // Does the generated WSDL have wsam:AnonymousResponses policy ?
    private boolean isAnonymousResponses(URL url) throws Exception {
        InputStream in = url.openStream();
        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLStreamReader rdr = xif.createXMLStreamReader(in);
            while(rdr.hasNext()) {
                rdr.next();
                if (rdr.getEventType() == XMLStreamReader.START_ELEMENT) {
                    QName name = rdr.getName();
                    if (name.equals(ANONYMOUS)) {
                        return true;
                    }
                }
            }
            rdr.close();
        } finally {
            in.close();
        }
        return false;
    }


    // DO NOT set @Addressing since is enabled via Endpoint.create()
    @WebService
    public static final class FeaturesEndpoint {
        public String echo(String name) {
            return name;
        }
    }
}
