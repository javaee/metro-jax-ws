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

package server.cookie_subdomain.client;

import junit.framework.TestCase;

import javax.xml.ws.BindingProvider;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Host in web service address is uppercase. That causes problems for
 * sticking cookies
 *
 * @author Jitendra Kotamraju
 */
public class SubDomainTest extends TestCase {

    public SubDomainTest(String name) {
        super(name);
    }
    
    /*
     * With maintain property set to true, session
     * should be maintained.
     */
    public void test3() throws Exception {
        Hello proxy = new HelloService().getHelloPort();

        // Set the adress with upper case hostname
        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
        // proxy.introduce(); Doing this below as setting endpoint address
        // resets the cookie store. Will be handled in future revisions

        String addr = (String)requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        URL url = new URL(addr);
        String host = url.getHost();
        String fqdn = getFqdn(host);
        if (fqdn == null) {
            System.out.println("Cannot use this hostname="+host+" for the test");
            return;
        }

        addr = addr.replace(host, fqdn);
        System.out.println("Setting Web Service Addr="+addr);
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, addr);

        proxy.introduce(fqdn);
        assertTrue("client session should be maintained", proxy.rememberMe());
    }

    // return null if it cannot find a fqdn with two dots
    private String getFqdn(String host) {
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            System.out.println("UnknownHostException for host="+host);
            return null;
        }

        for(InetAddress tempAddr : addresses) {
            String fqdn = tempAddr.getCanonicalHostName();
            System.out.println("See if fqdn="+fqdn+" has two dots");
            int index = fqdn.indexOf('.');
            if (index == -1) {
                continue;
            }
            index = fqdn.indexOf('.', index+1);
            if (index != -1) {
                return fqdn;
            }
        }
        return null;
    }

}
