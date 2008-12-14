/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.addressing.policy;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.jaxws.spi.PolicyMapUpdateProvider;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.AddressingFeature;

/**
 * Generate an addressing policy.
 *
 * @author Fabian Ritzmann
 */
public class AddressingMapUpdateProvider implements PolicyMapUpdateProvider {

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(AddressingMapUpdateProvider.class);

    static final class AddressingAssertion extends PolicyAssertion {

        AddressingAssertion(AssertionData data) {
            super(data, null);
        }
    }

    /**
     * Puts an addressing policy into the PolicyMap if the addressing feature was set.
     */
    public void update(final PolicyMapExtender policyMapMutator, final PolicyMap policyMap, final SEIModel model, final WSBinding wsBinding)
        throws PolicyException {
        LOGGER.entering(policyMapMutator, policyMap, model, wsBinding);

        if (policyMap != null) {
            final AddressingFeature addressingFeature = wsBinding.getFeature(AddressingFeature.class);
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("addressingFeature = " + addressingFeature);
            }
            if ((addressingFeature != null) && addressingFeature.isEnabled()) {
                final AddressingVersion addressingVersion = AddressingVersion.fromFeature(addressingFeature);
                final QName usingAddressing = new QName(addressingVersion.policyNsUri, "UsingAddressing");
                final PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(model.getServiceQName(), model.getPortName());
                final Policy existingPolicy = policyMap.getEndpointEffectivePolicy(endpointKey);
                if ((existingPolicy == null) || !existingPolicy.contains(usingAddressing)) {
                    final QName bindingName = model.getBoundPortTypeName();
                    final Policy addressingPolicy = createAddressingPolicy(bindingName, usingAddressing, addressingFeature.isRequired());
                    final PolicySubject addressingPolicySubject = new PolicySubject(bindingName, addressingPolicy);
                    final PolicyMapKey aKey = PolicyMap.createWsdlEndpointScopeKey(model.getServiceQName(), model.getPortName());
                    policyMapMutator.putEndpointSubject(aKey, addressingPolicySubject);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Added addressing policy with ID \"" + addressingPolicy.getIdOrName() + "\" to binding element \"" + bindingName + "\"");
                    }
                } else {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Addressing policy exists already, doing nothing");
                    }
                }
            }
        } // endif policy map not null
        LOGGER.exiting();
    }

    /**
     * Create a policy with an MTOM assertion.
     *
     * @param bindingName The wsdl:binding element name. Used to generate a (locally) unique ID for the policy.
     * @param assertionName The fully qualified name of the addressing policy assertion.
     * @param isRequired True, if the addressing feature was set to required, false otherwise.
     * @return A policy that contains one policy assertion that corresponds to the given assertion name.
     */
    private Policy createAddressingPolicy(final QName bindingName, final QName assertionName, final boolean isRequired) {
        final ArrayList<AssertionSet> assertionSets = new ArrayList<AssertionSet>(1);
        final ArrayList<PolicyAssertion> assertions = new ArrayList<PolicyAssertion>(1);
        final AssertionData addressingData =
            AssertionData.createAssertionData(assertionName);
        if (!isRequired) {
            addressingData.setOptionalAttribute(true);
        }
        assertions.add(new AddressingAssertion(addressingData));
        assertionSets.add(AssertionSet.createAssertionSet(assertions));
        return Policy.createPolicy(null, bindingName.getLocalPart() + "_Addressing_Policy", assertionSets);
    }
}
