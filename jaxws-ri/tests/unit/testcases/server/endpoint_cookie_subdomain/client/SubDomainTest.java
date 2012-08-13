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
package server.endpoint_cookie_subdomain.client;

import junit.framework.TestCase;
import testutil.PortAllocator;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.util.Map;

/**
 * Service sends cookies for domain, and sub-domain. This tests whether
 * client sends the all cookies or not.
 *
 * @author Jitendra Kotamraju
 */
public class SubDomainTest extends TestCase {

    public SubDomainTest(String name) {
        super(name);
    }

    public void testSubDomainCookies() throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        String host = addr.getCanonicalHostName();
        if (!isFqdn(host)) {
            System.out.println("Cannot run test with hostname="+host+
                    " Need a hostname with two dots");
            return;
        }

        int port = PortAllocator.getFreePort();
        String address = "http://"+host+":"+port+"/hello";
        Endpoint endpoint = Endpoint.publish(address, new HelloServiceImpl());        
        invoke(host, address);
        endpoint.stop();
    }

    private void invoke(String host, String address) {
        // access service
        QName portName = new QName("", "");
        QName serviceName = new QName("", "");
        Service service = Service.create(serviceName);
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, address);
        Dispatch<Source> d = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        Map<String, Object> requestContext = d.getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);

        // introduce() request
        String body  = "<ns2:introduce xmlns:ns2='urn:test'><arg0>"+host+"</arg0></ns2:introduce>";
        Source request = new StreamSource(new ByteArrayInputStream(body.getBytes()));
        d.invoke(request);

        // rememeberMe() request
        body  = "<ns2:rememberMe xmlns:ns2='urn:test'/>";
        request = new StreamSource(new ByteArrayInputStream(body.getBytes()));
        d.invoke(request);
    }

    // return true if it is a fqdn with two dots
    private boolean isFqdn(String host) {
        System.out.println("See if host="+host+" has two dots");
        int index = host.indexOf('.');
        if (index == -1) {
            return false;
        }
        index = host.indexOf('.', index+1);
        return index != -1;
    }

}
