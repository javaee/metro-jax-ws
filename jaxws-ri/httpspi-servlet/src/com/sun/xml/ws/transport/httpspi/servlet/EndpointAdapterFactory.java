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

package com.sun.xml.ws.transport.httpspi.servlet;


import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointContext;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.spi.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
/**
 * @author Jitendra Kotamraju
*/
public final class EndpointAdapterFactory implements DeploymentDescriptorParser.AdapterFactory<EndpointAdapter> {
    private static final Logger LOGGER = Logger.getLogger(EndpointAdapterFactory.class.getName());

    private final EndpointContext appContext;

    public EndpointAdapterFactory() {
        this.appContext = new EndpointContextImpl();
    }

    public EndpointAdapter createAdapter(String name, String urlPattern, Class implType,
        QName serviceName, QName portName, String bindingId,
        List<Source> metadata, WebServiceFeature... features) {

        LOGGER.info("Creating Endpoint using JAX-WS 2.2 HTTP SPI");
        InvokerImpl endpointInvoker = new InvokerImpl(implType);
        Endpoint endpoint = Provider.provider().createEndpoint(bindingId, implType, endpointInvoker, features);

        endpoint.setEndpointContext(appContext);

        // Use DD's service name, port names as WSDL_SERVICE and WSDL_PORT
        if (portName != null || serviceName != null) {
            Map<String, Object> props = new HashMap<String, Object>();
            if (portName != null) {
                props.put(Endpoint.WSDL_PORT, portName);
            }
            if (serviceName != null) {
                props.put(Endpoint.WSDL_SERVICE, serviceName);
            }
            LOGGER.info("Setting Endpoint Properties="+props);
            endpoint.setProperties(props);
        }

        // Set bundle's wsdl, xsd docs as metadata
        if (metadata != null) {
            endpoint.setMetadata(metadata);
            List<String> docId = new ArrayList<String>();
            for(Source source : metadata) {
                docId.add(source.getSystemId());
            }
            LOGGER.info("Setting metadata="+docId);
        }

        // Set DD's handlers
        // endpoint.getBinding().setHandlerChain(binding.getHandlerChain());

        return new EndpointAdapter(endpoint, urlPattern, appContext);
    }

}
