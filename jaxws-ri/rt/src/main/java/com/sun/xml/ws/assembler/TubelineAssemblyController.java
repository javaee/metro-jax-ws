/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.assembler;

import com.sun.istack.NotNull;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.assembler.dev.ClientTubelineAssemblyContext;
import com.sun.xml.ws.resources.TubelineassemblyMessages;
import com.sun.xml.ws.runtime.config.TubeFactoryConfig;
import com.sun.xml.ws.runtime.config.TubeFactoryList;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class TubelineAssemblyController {

    private final MetroConfigName metroConfigName;

    TubelineAssemblyController(MetroConfigName metroConfigName) {
        this.metroConfigName = metroConfigName;
    }

    /**
     * Provides a ordered collection of WSIT/Metro client-side tube creators that are be used to
     * construct a client-side Metro tubeline
     *
     * The order of the tube creators in the collection is last-to-first from the
     * client side request message processing perspective.
     *
     * <b>
     * WARNING: This method is part of Metro internal API and may be changed, removed or
     * replaced by a different method without a prior notice. The method SHOULD NOT be used
     * outside of Metro codebase.
     * </b>
     *
     * @param endpointUri URI of the endpoint for which the collection of tube factories should be returned
     *
     * @return collection of WSIT/Metro client-side tube creators
     */
    Collection<TubeCreator> getTubeCreators(ClientTubelineAssemblyContext context) {
        URI endpointUri;
        if (context.getPortInfo() != null) {
            endpointUri = createEndpointComponentUri(context.getPortInfo().getServiceName(), context.getPortInfo().getPortName());
        } else {
            endpointUri = null;
        }

        MetroConfigLoader configLoader = new MetroConfigLoader(context.getContainer(), metroConfigName);
        return initializeTubeCreators(configLoader.getClientSideTubeFactories(endpointUri));
    }

    /**
     * Provides a ordered collection of WSIT/Metro server-side tube creators that are be used to
     * construct a server-side Metro tubeline for a given endpoint
     *
     * The order of the tube creators in the collection is last-to-first from the
     * server side request message processing perspective.
     *
     * <b>
     * WARNING: This method is part of Metro internal API and may be changed, removed or
     * replaced by a different method without a prior notice. The method SHOULD NOT be used
     * outside of Metro codebase.
     * </b>
     * 
     * @param endpointUri URI of the endpoint for which the collection of tube factories should be returned
     *
     * @return collection of WSIT/Metro server-side tube creators
     */
    Collection<TubeCreator> getTubeCreators(DefaultServerTubelineAssemblyContext context) {
        URI endpointUri;
        if (context.getEndpoint() != null) {
            endpointUri = createEndpointComponentUri(context.getEndpoint().getServiceName(), context.getEndpoint().getPortName());
        } else {
            endpointUri = null;
        }

        MetroConfigLoader configLoader = new MetroConfigLoader(context.getEndpoint().getContainer(), metroConfigName);
        return initializeTubeCreators(configLoader.getEndpointSideTubeFactories(endpointUri));
    }

    private Collection<TubeCreator> initializeTubeCreators(TubeFactoryList tfl) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = tccl != null ? tccl : TubelineAssemblyController.class.getClassLoader();

        LinkedList<TubeCreator> tubeCreators = new LinkedList<TubeCreator>();
        for (TubeFactoryConfig tubeFactoryConfig : tfl.getTubeFactoryConfigs()) {
            tubeCreators.addFirst(new TubeCreator(tubeFactoryConfig, classLoader));
        }
        return tubeCreators;
    }

    /*
     * Example WSDL component URI: http://org.sample#wsdl11.port(PingService/HttpPingPort)
     */
    private URI createEndpointComponentUri(@NotNull QName serviceName, @NotNull QName portName) {
        StringBuilder sb = new StringBuilder(serviceName.getNamespaceURI()).append("#wsdl11.port(").append(serviceName.getLocalPart()).append('/').append(portName.getLocalPart()).append(')');
        try {
            return new URI(sb.toString());
        } catch (URISyntaxException ex) {
            Logger.getLogger(TubelineAssemblyController.class).warning(
                    TubelineassemblyMessages.MASM_0020_ERROR_CREATING_URI_FROM_GENERATED_STRING(sb.toString()),
                    ex);
            return null;
        }
    }
}
