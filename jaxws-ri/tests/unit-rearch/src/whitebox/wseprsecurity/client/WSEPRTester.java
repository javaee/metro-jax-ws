/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

package whitebox.wseprsecurity.client;

import com.sun.xml.ws.api.addressing.WSEndpointReference;
import junit.framework.TestCase;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.EndpointReference;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

/**
 * @author Jitendra Kotamraju
 */

public class WSEPRTester extends TestCase {

    public WSEPRTester(String name) {
        super(name);
        Policy core = Policy.getPolicy();
        Policy custom = new CustomPolicy();
        Policy composite = new CompositePolicy(core, custom);
        Policy.setPolicy(composite);
        System.setSecurityManager(new SecurityManager());
    }

    public void testEPRsWithSecurityMgr() throws Exception {
        new Test();
    }

    private static final class Test {
        Test() throws Exception {
            URL res = getClass().getResource("../epr/ms_epr_metadata.xml");
            File folder = new File(res.getFile()).getParentFile();   // assuming that this is a file:// URL.

            for (File f : folder.listFiles()) {
                if(!f.getName().endsWith(".xml"))
                    continue;
                InputStream is = new FileInputStream(f);
                StreamSource s = new StreamSource(is);
                EndpointReference epr = EndpointReference.readFrom(s);
                WSEndpointReference wsepr = new WSEndpointReference(epr);
                WSEndpointReference.Metadata metadata = wsepr.getMetaData();
            }
        }
    }

    private static final class CustomPolicy extends Policy {

        public PermissionCollection getPermissions(CodeSource codesource) {
            //System.out.println("CodeSource=" + codesource);
            Permissions col = new Permissions();
            URL location = codesource.getLocation();

            // Hack for now to find out the application code
            if (location.toExternalForm().contains("jaxws-ri/test/build/temp/classes")) {
                // Give appropriate permissions to application code
                System.out.println("NOT giving all permissions to app");

                col.add(new PropertyPermission("*", "read,write"));

                // Without FilePermission cannot read META-INF/services
                // So cannot create XMLInputFactory etc
                col.add(new FilePermission("<<ALL FILES>>", "read,write"));
            } else {
                // Give all permssions to JAX-WS runtime
                col.add(new AllPermission());
            }
            return col;
        }

        public void refresh() {
            // no op
        }
    }

    private static final class CompositePolicy extends Policy {

        private final Policy[] policies;

        public CompositePolicy(Policy... policies) {
            this.policies = policies;
        }

        public PermissionCollection getPermissions(ProtectionDomain domain) {
            Permissions perms = new Permissions();

            for (Policy p : policies) {
                PermissionCollection permCol = p.getPermissions(domain);
                for (Enumeration<Permission> en = permCol.elements(); en.hasMoreElements();) {
                    perms.add(en.nextElement());
                }
            }
            return perms;
        }

        public boolean implies(ProtectionDomain domain, Permission permission) {

            for (Policy p : policies) {
                if (p.implies(domain, permission)) {
                    return true;
                }
            }
            return false;
        }


        public PermissionCollection getPermissions(CodeSource codesource) {
            Permissions perms = new Permissions();
            for (Policy p : policies) {
                PermissionCollection permsCol = p.getPermissions(codesource);
                for (Enumeration<Permission> en = permsCol.elements(); en.hasMoreElements();) {
                    perms.add(en.nextElement());
                }
            }
            return perms;
        }

        public void refresh() {
            for (Policy p : policies) {
                p.refresh();
            }
        }
    }

}
