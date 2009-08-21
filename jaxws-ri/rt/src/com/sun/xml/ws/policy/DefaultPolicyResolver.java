/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.policy;

import com.sun.xml.ws.api.policy.PolicyResolver;
import com.sun.xml.ws.api.policy.ValidationProcessor;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator.Fitness;
import com.sun.xml.ws.resources.PolicyMessages;

import javax.xml.ws.WebServiceException;

/**
 * @author Rama Pulavarthi
 */
public class DefaultPolicyResolver implements PolicyResolver {

    public PolicyMap resolve(ServerContext context) {
        PolicyMap map = context.getPolicyMap();
        if(map != null)
            validateServerPolicyMap(map);
        return map;
    }

    public PolicyMap resolve(ClientContext context) {
        PolicyMap map = context.getPolicyMap();
        if(map != null)
            map =  doAlternativeSelection(map);
        return map;
    }
    
    /**
     * Checks if the PolicyMap has only single alternative in the scope.
     *
     * @param policyMap
     *      PolicyMap that needs to be validated.
     */
    private void validateServerPolicyMap(PolicyMap policyMap) {
        try {
            final ValidationProcessor validationProcessor = ValidationProcessor.getInstance();

            for (Policy policy : policyMap) {

                // TODO:  here is a good place to check if the actual policy has only one alternative...

                for (AssertionSet assertionSet : policy) {
                    for (PolicyAssertion assertion : assertionSet) {
                        Fitness validationResult = validationProcessor.validateServerSide(assertion);
                        if (validationResult != Fitness.SUPPORTED) {
                            throw new PolicyException(PolicyMessages.WSP_1015_SERVER_SIDE_ASSERTION_VALIDATION_FAILED(
                                    assertion.getName(),
                                    validationResult));
                        }
                    }
                }
            }
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
    }

    /**
     * Selects a best alternative if there are multiple policy alternatives.
     *
     * @param policyMap
     * @return
     */
    private PolicyMap doAlternativeSelection(PolicyMap policyMap) {
        final EffectivePolicyModifier modifier = EffectivePolicyModifier.createEffectivePolicyModifier();
        modifier.connect(policyMap);
        try {
            EffectiveAlternativeSelector.doSelection(modifier);
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
        return policyMap;
    }
}