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

package com.sun.xml.ws.api.policy;

import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapMutator;
import com.sun.xml.ws.api.server.Container;
import com.sun.istack.Nullable;
import java.util.Arrays;
import java.util.Collection;
import javax.xml.ws.WebServiceException;

/**
 * PolicyResolver  will be used to resolve the PolicyMap created by configuration understood by JAX-WS.
 *
 * Extensions of this can return effective PolicyMap after merge policies from other configurations.
 * @author Rama Pulavarthi
 */
public interface PolicyResolver {
    /**
     * Creates a PolicyResolver
     *
     * @param context
     *      ServerContext that captures information useful for resolving Policy on server-side
     *
     * @return  
     *      A PolicyMap with single policy alternative that gets created after consulting various configuration models.
     * 
     * @throws WebServiceException
     *      If resolution failed
     */
    PolicyMap resolve(ServerContext context) throws WebServiceException;

    /**
     * Creates a PolicyResolver
     *
     * @param context
     *      ServerContext that captures information useful for resolving Policy on client-side
     *
     * @return
     *      A PolicyMap with single policy alternative that gets created after consulting various configuration models.
     *
     * @throws WebServiceException
     *      If resolution failed
     */
    PolicyMap resolve(ClientContext context) throws WebServiceException;

   public class ServerContext {
       private final PolicyMap policyMap;
       private final Class endpointClass;
       private final Container container;
       private final boolean hasWsdl;
       private final Collection<PolicyMapMutator> mutators;

        /**
         * The abstraction of PolicyMap is not finalized, and will change in few months. It is highly discouraged to use
         * PolicyMap until it is finalized.
         *
         * In presence of WSDL, JAX-WS by default creates PolicyMap from Policy Attachemnts in WSDL.
         * In absense of WSDL, JAX-WS creates PolicyMap from WebServiceFeatures configured on the endpoint implementation
         *
         * @param policyMap
         *      PolicyMap created from PolicyAttachments in WSDL or Feature annotations on endpoint implementation class.
         * @param container
         * @param endpointClass
         * @param mutators
         *      List of PolicyMapMutators that are run eventually when a PolicyMap is created
         */
        public ServerContext(@Nullable PolicyMap policyMap, Container container,
                             Class endpointClass, final PolicyMapMutator... mutators) {
            this.policyMap = policyMap;
            this.endpointClass = endpointClass;
            this.container = container;
            this.hasWsdl = true;
            this.mutators = Arrays.asList(mutators);
        }

        /**
         * The abstraction of PolicyMap is not finalized, and will change in few months. It is highly discouraged to use
         * PolicyMap until it is finalized.
         *
         * In presence of WSDL, JAX-WS by default creates PolicyMap from Policy Attachemnts in WSDL.
         * In absense of WSDL, JAX-WS creates PolicyMap from WebServiceFeatures configured on the endpoint implementation
         *
         * @param policyMap
         *      PolicyMap created from PolicyAttachments in WSDL or Feature annotations on endpoint implementation class.
         * @param container
         * @param endpointClass
         * @param hasWsdl Set to true, if this service is bundled with WSDL, false otherwise
         * @param mutators
         *      List of PolicyMapMutators that are run eventually when a PolicyMap is created
         */
        public ServerContext(@Nullable PolicyMap policyMap, Container container,
                             Class endpointClass, boolean hasWsdl, final PolicyMapMutator... mutators) {
            this.policyMap = policyMap;
            this.endpointClass = endpointClass;
            this.container = container;
            this.hasWsdl = hasWsdl;
            this.mutators = Arrays.asList(mutators);
        }

        public @Nullable PolicyMap getPolicyMap() {
            return policyMap;
        }

        public @Nullable Class getEndpointClass() {
           return endpointClass;
        }

        public Container getContainer() {
           return container;
        }

        /**
         * Return true, if this service is bundled with WSDL, false otherwise
         * @return
         */
        public boolean hasWsdl() {
            return hasWsdl;
        }

        public Collection<PolicyMapMutator> getMutators() {
            return mutators;
        }
    }

    public class ClientContext {
        private PolicyMap policyMap;
        private Container container;

        /**
         * The abstraction of PolicyMap is not finalized, and will change in few months. It is highly discouraged to use
         * PolicyMap until it is finalized.
         *
         * In presence of WSDL, JAX-WS by default creates PolicyMap from Policy Attachemnts in WSDL.
         *
         * @param policyMap PolicyMap created from PolicyAttachemnts in WSDL
         * @param container 
         */
        public ClientContext(@Nullable PolicyMap policyMap, Container container) {
            this.policyMap = policyMap;
            this.container = container;
        }

        public @Nullable PolicyMap getPolicyMap() {
            return policyMap;
        }

        public Container getContainer() {
           return container;
        }
    }
}
