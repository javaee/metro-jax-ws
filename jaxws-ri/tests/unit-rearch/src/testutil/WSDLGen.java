/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
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

package testutil;

import com.sun.xml.ws.api.server.DocumentAddressResolver;
import com.sun.xml.ws.api.server.PortAddressResolver;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;
import com.sun.xml.ws.transport.local.FileSystemResourceLoader;
import com.sun.xml.ws.util.ByteArrayBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Generates WSDL for local transport if there is no primary wsdl for the
 * endpoint in the WAR.
 *
 * @author Jitendra Kotamraju
 */
public class WSDLGen {

    static public boolean useLocal() {
        return Boolean.getBoolean("uselocal");
    }

    public static void main(String[] args) throws Exception {
        System.exit(run());
    }

    public static int run() throws Exception {
        if (!useLocal()) {
            return 0;
        }

        String outputDir = System.getProperty("tempdir");
        if (outputDir == null) {
            System.err.println("**** Set tempdir system property ****");
            return -1;
        }
        String riFile = outputDir+"/WEB-INF/sun-jaxws.xml";

        DeploymentDescriptorParser<WSEndpoint> parser = new DeploymentDescriptorParser<WSEndpoint>(
            Thread.currentThread().getContextClassLoader(),
            new FileSystemResourceLoader(new File(outputDir)), null,
            new AdapterFactory<WSEndpoint>() {
                public WSEndpoint createAdapter(String name, String urlPattern, WSEndpoint<?> endpoint) {
                    return endpoint;
                }
            });

        List<WSEndpoint> endpoints = parser.parse(new File(riFile));

        final String addr = new File(outputDir).toURL().toExternalForm();
        final String address = "local"+addr.substring(4);// file:// -> local://
        for(WSEndpoint endpoint : endpoints) {
			ServiceDefinition def = endpoint.getServiceDefinition();
            if (def == null) {
				continue;
			}
			SDDocument primary = def.getPrimary();
			File file = new File(primary.getURL().toURI());
			if (file.exists()) {
				System.out.println("**** Primary WSDL "+file+" already exists - not generating any WSDL artifacts ****");
				continue;				// Primary WSDL already exists
			}
			for(SDDocument doc : def) {
                int index= doc.getURL().getFile().indexOf("/WEB-INF/wsdl");
                String name = "";
                if(index == -1)
                    name = outputDir+"/WEB-INF/wsdl"+ doc.getURL().getFile();
                else
                    name = doc.getURL().getFile();
				System.out.println("Creating WSDL artifact="+name);
                ByteArrayBuffer buffer = new ByteArrayBuffer();
                doc.writeTo(
					new PortAddressResolver() {
						public String getAddressFor(QName serviceName, String portName) {
							return address;
						}
					},
					new DocumentAddressResolver() {
						public String getRelativeAddressFor(
							SDDocument current, SDDocument referenced) {
							String rel = referenced.getURL().toExternalForm();
							return rel.substring(6);	// remove file:/
						}
					},
					buffer);
                FileOutputStream fos = new FileOutputStream(name);
				buffer.writeTo(fos);
                fos.close();
			}
		}
        return 0;
    }

}
