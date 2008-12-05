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

package com.sun.xml.ws.policy;

import com.sun.xml.ws.policy.AssertionValidationProcessor;
import com.sun.xml.ws.policy.PolicyAssertion;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.ws.WebServiceException;
import com.sun.xml.ws.api.model.wsdl.WSDLExtension;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.EffectiveAlternativeSelector;
import com.sun.xml.ws.policy.EffectivePolicyModifier;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.resources.PolicyMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.jaxws.spi.ModelConfiguratorProvider;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;

/**
 * TODO: write doc
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */
public class WSDLPolicyMapWrapper implements WSDLExtension {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(WSDLPolicyMapWrapper.class);
    private static final QName NAME = new QName(null, "WSDLPolicyMapWrapper");
    
    private static ModelConfiguratorProvider[] configurators = PolicyUtils.ServiceProvider.load(ModelConfiguratorProvider.class);
    
    private PolicyMap policyMap;
    private EffectivePolicyModifier mapModifier;
    private PolicyMapExtender mapExtender;    
    
    protected WSDLPolicyMapWrapper(PolicyMap policyMap) {
        if (policyMap == null) {
            throw new IllegalArgumentException(PolicyMessages.WSP_1016_POLICY_MAP_CAN_NOT_BE_NULL());
        }
        
        this.policyMap = policyMap;
    }
    
    public WSDLPolicyMapWrapper(PolicyMap policyMap, EffectivePolicyModifier modifier, PolicyMapExtender extender) {
        this(policyMap);
        this.mapModifier = modifier;
        this.mapExtender = extender;
    }
    
    public PolicyMap getPolicyMap() {
        return policyMap;
    }
    
    public void addClientConfigToMap(final Object clientWsitConfigId, final PolicyMap clientPolicyMap) throws PolicyException {
        LOGGER.entering();
        
        try {
            for (PolicyMapKey key : clientPolicyMap.getAllServiceScopeKeys()) {
                final Policy policy = clientPolicyMap.getServiceEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putServiceSubject(key, new PolicySubject(clientWsitConfigId, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllEndpointScopeKeys()) {
                final Policy policy = clientPolicyMap.getEndpointEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putEndpointSubject(key, new PolicySubject(clientWsitConfigId, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllOperationScopeKeys()) {
                final Policy policy = clientPolicyMap.getOperationEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putOperationSubject(key, new PolicySubject(clientWsitConfigId, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllInputMessageScopeKeys()) {
                final Policy policy = clientPolicyMap.getInputMessageEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putInputMessageSubject(key, new PolicySubject(clientWsitConfigId, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllOutputMessageScopeKeys()) {
                final Policy policy = clientPolicyMap.getOutputMessageEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putOutputMessageSubject(key, new PolicySubject(clientWsitConfigId, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllFaultMessageScopeKeys()) {
                final Policy policy = clientPolicyMap.getFaultMessageEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putFaultMessageSubject(key, new PolicySubject(clientWsitConfigId, policy));
            }
            LOGGER.fine(PolicyMessages.WSP_1041_CLIENT_CFG_POLICIES_TRANSFERED_INTO_FINAL_POLICY_MAP(policyMap));
        } catch (FactoryConfigurationError ex) {
            throw LOGGER.logSevereException(new PolicyException(ex));
        }
        
        LOGGER.exiting();
    }
    
    public void doAlternativeSelection() throws PolicyException {
        EffectiveAlternativeSelector.doSelection(mapModifier);
    }
    
    void validateServerSidePolicies() throws PolicyException {
        final AssertionValidationProcessor validationProcessor = AssertionValidationProcessor.getInstance();
        for (Policy policy : policyMap) {
            
            // TODO:  here is a good place to check if the actual policy has only one alternative...
            
            for (AssertionSet assertionSet : policy) {
                for (PolicyAssertion assertion : assertionSet) {
                    PolicyAssertionValidator.Fitness validationResult = validationProcessor.validateServerSide(assertion);
                    if (validationResult != PolicyAssertionValidator.Fitness.SUPPORTED) {
                        throw LOGGER.logSevereException(new PolicyException(PolicyMessages.WSP_1046_SERVER_SIDE_ASSERTION_VALIDATION_FAILED(
                                assertion.getName(),
                                validationResult)));
                    }
                }
            }
        }
    }
    
    void configureModel(final WSDLModel model) {
        try {
            for (ModelConfiguratorProvider configurator : configurators) {
                configurator.configure(model, policyMap);
            }
        } catch (PolicyException e) {
            throw LOGGER.logSevereException(new WebServiceException(PolicyMessages.WSP_1032_FAILED_CONFIGURE_WSDL_MODEL(), e));
        }
    }
    
    void putEndpointSubject(final PolicyMapKey key, final PolicySubject subject) {
        if (null != this.mapExtender) {
            this.mapExtender.putEndpointSubject(key, subject);
        }
    }
    
    void putServiceSubject(final PolicyMapKey key, final PolicySubject subject) {
        if (null != this.mapExtender) {
            this.mapExtender.putServiceSubject(key, subject);
        }
    }
    
    void putOperationSubject(final PolicyMapKey key, final PolicySubject subject) {
        if (null != this.mapExtender) {
            this.mapExtender.putOperationSubject(key, subject);
        }
    }
    
    void putInputMessageSubject(final PolicyMapKey key, final PolicySubject subject) {
        if (null != this.mapExtender) {
            this.mapExtender.putInputMessageSubject(key, subject);
        }
    }
    
    void putOutputMessageSubject(final PolicyMapKey key, final PolicySubject subject) {
        if (null != this.mapExtender) {
            this.mapExtender.putOutputMessageSubject(key, subject);
        }
    }
    
    void putFaultMessageSubject(final PolicyMapKey key, final PolicySubject subject) {
        if (null != this.mapExtender) {
            this.mapExtender.putFaultMessageSubject(key, subject);
        }
    }
    
    public QName getName() {
        return NAME;
    }
}
