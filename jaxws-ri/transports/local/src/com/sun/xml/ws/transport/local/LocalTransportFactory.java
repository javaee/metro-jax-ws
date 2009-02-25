/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.transport.local;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.TransportPipeFactory;
import com.sun.xml.ws.api.pipe.TransportTubeFactory;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * {@link TransportPipeFactory} for the local transport.
 *
 * <p>
 * The syntax of the endpoint address is:
 * <pre><xmp>
 * local:///path/to/exploded/war/image?portLocalName
 * </xmp></pre>
 *
 * <p>
 * If the service only contains one port, the <tt>?portLocalName</tt> portion
 * can be omitted.
 *
 * @author Kohsuke Kawaguchi
 */
public final class LocalTransportFactory extends TransportTubeFactory {
    public Tube doCreate(@NotNull ClientTubeAssemblerContext context) {
        URI adrs = context.getAddress().getURI();
        if(!(adrs.getScheme().equals("local") || adrs.getScheme().equals("local-async")))
            return null;
        return adrs.getScheme().equals("local")
                ? new LocalTransportTube(adrs,createServerService(adrs),context.getCodec())
                : new LocalAsyncTransportTube(adrs,createServerService(adrs),context.getCodec());
    }

    /**
     * The local transport works by looking at the exploded war file image on
     * a file system.
     * TODO: Currently it expects the PortName to be appended to the endpoint address
     *       This needs to be expanded to take Service and Port QName as well.
     */
    protected static WSEndpoint createServerService(URI adrs) {
        try {
            String outputDir = adrs.getPath();
            List<WSEndpoint> endpoints = parseEndpoints(outputDir);

            WSEndpoint endpoint = endpoints.get(0);
            if (endpoints.size() > 1) {
                for (WSEndpoint rei : endpoints) {
                    //TODO: for now just compare local part
                    if(rei.getPortName().getLocalPart().equals(adrs.getQuery())) {
                        endpoint = rei;
                        break;
                    }
                }
            }

            return endpoint;
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    protected static List<WSEndpoint> parseEndpoints(String outputDir) throws IOException {
        String riFile = outputDir+"/WEB-INF/sun-jaxws.xml";
        DeploymentDescriptorParser<WSEndpoint> parser = new DeploymentDescriptorParser<WSEndpoint>(
            Thread.currentThread().getContextClassLoader(),
            new FileSystemResourceLoader(new File(outputDir)), null,
            new AdapterFactory<WSEndpoint>() {
                public WSEndpoint createAdapter(String name, String urlPattern, WSEndpoint<?> endpoint) {
                    return endpoint;
                }
            });

        return parser.parse(new File(riFile));
    }

}
