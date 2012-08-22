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

package com.sun.xml.ws.policy.jaxws;

import com.sun.xml.ws.api.policy.ModelTranslator;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.resources.PolicyMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */
abstract class BuilderHandler{

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(BuilderHandler.class);

    Map<String,PolicySourceModel> policyStore;
    Collection<String> policyURIs;
    Object policySubject;
    
    /**
     * Creates a new instance of BuilderHandler
     */
    BuilderHandler(Collection<String> policyURIs, Map<String,PolicySourceModel> policyStore, Object policySubject) {
        this.policyStore = policyStore;
        this.policyURIs = policyURIs;
        this.policySubject = policySubject;
    }
    
    final void populate(final PolicyMapExtender policyMapExtender) throws PolicyException {
        if (null == policyMapExtender) {
            throw LOGGER.logSevereException(new PolicyException(PolicyMessages.WSP_1006_POLICY_MAP_EXTENDER_CAN_NOT_BE_NULL()));
        }
        
        doPopulate(policyMapExtender);
    }
    
    protected abstract void doPopulate(final PolicyMapExtender policyMapExtender) throws PolicyException;
    
    final Collection<Policy> getPolicies() throws PolicyException {
        if (null == policyURIs) {
            throw LOGGER.logSevereException(new PolicyException(PolicyMessages.WSP_1004_POLICY_URIS_CAN_NOT_BE_NULL()));
        }
        if (null == policyStore) {
            throw LOGGER.logSevereException(new PolicyException(PolicyMessages.WSP_1010_NO_POLICIES_DEFINED()));
        }
        
        final Collection<Policy> result = new ArrayList<Policy>(policyURIs.size());
        
        for (String policyURI : policyURIs) {
            final PolicySourceModel sourceModel = policyStore.get(policyURI);
            if (sourceModel == null) {
                throw LOGGER.logSevereException(new PolicyException(PolicyMessages.WSP_1005_POLICY_REFERENCE_DOES_NOT_EXIST(policyURI)));
            } else {
                result.add(ModelTranslator.getTranslator().translate(sourceModel));
            }
        }
        
        return result;
    }
    
    final Collection<PolicySubject> getPolicySubjects() throws PolicyException {
        final Collection<Policy> policies = getPolicies();
        final Collection<PolicySubject> result =  new ArrayList<PolicySubject>(policies.size());
        for (Policy policy : policies) {
            result.add(new PolicySubject(policySubject, policy));
        }
        return result;
    }
}
