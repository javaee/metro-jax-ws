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

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.model.wsdl.WSDLBoundPortTypeImpl;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.jaxws.spi.ModelConfiguratorProvider;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.addressing.W3CAddressingMetadataConstants;

import java.util.Iterator;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.AddressingFeature;

/**
 * This Policy extension configures the WSDLModel with AddressingFeature when Addressing assertions are present in the
 * PolicyMap.
 *
 * @author japod
 * @author Rama Pulavarthi
 */
public class AddressingModelConfiguratorProvider implements ModelConfiguratorProvider{

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(AddressingModelConfiguratorProvider.class);

    private static final QName[] ADDRESSING_ASSERTIONS = {
        new QName(AddressingVersion.MEMBER.policyNsUri, "UsingAddressing"),
        new QName(AddressingVersion.W3C.policyNsUri, "UsingAddressing")};

    /**
     * Creates a new instance of AddressingModelConfiguratorProvider
     */
    public AddressingModelConfiguratorProvider() {
    }

    /**
     * process addressing policy assertions and if found and are not optional then addressing is enabled on the
     * {@link com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType}
     *
     * @param model must be non-null
     * @param policyMap must be non-null
     */
    public void configure(final WSDLModel model, final PolicyMap policyMap) throws PolicyException {
        LOGGER.entering(model, policyMap);
        if ((null==model) || (null==policyMap)) {
            LOGGER.exiting();
            return;
        }
        for (WSDLService service:model.getServices().values()) {
            for (WSDLPort port : service.getPorts()) {
                final PolicyMapKey key = PolicyMap.createWsdlEndpointScopeKey(service.getName(),port.getName());
                final Policy policy = policyMap.getEndpointEffectivePolicy(key);
                for (QName addressingAssertionQName : ADDRESSING_ASSERTIONS) {
                    if (null!=policy && policy.contains(addressingAssertionQName)) {
                        final Iterator <AssertionSet> assertions = policy.iterator();
                        while(assertions.hasNext()){
                            final AssertionSet assertionSet = assertions.next();
                            final Iterator<PolicyAssertion> policyAssertion = assertionSet.iterator();
                            while(policyAssertion.hasNext()){
                                final PolicyAssertion assertion = policyAssertion.next();
                                if(assertion.getName().equals(addressingAssertionQName)){
                                    final WebServiceFeature feature = AddressingVersion.getFeature(addressingAssertionQName.getNamespaceURI(), true, !assertion.isOptional());
                                    port.addFeature(feature);
                                    if (LOGGER.isLoggable(Level.FINE)) {
                                        LOGGER.fine("Added addressing feature \"" + feature + "\" to port \"" + port + "\"");
                                    }
                                } // end-if non optional wsa assertion found
                            } // next assertion
                        } // next alternative
                    } // end-if policy contains wsa assertion
                } //end foreach addr assertion
                
                // Deal with WS-Addressing 1.0 Metadata assertions
                if (policy != null && policy.contains(W3CAddressingMetadataConstants.WSAM_ADDRESSING_ASSSSERTION)) {
                    for (AssertionSet assertions : policy) {
                        for (PolicyAssertion assertion : assertions) {
                            if (assertion.getName().equals(W3CAddressingMetadataConstants.WSAM_ADDRESSING_ASSSSERTION)) {
                                NestedPolicy nestedPolicy = assertion.getNestedPolicy();
                                boolean requiresAnonymousResponses = false;
                                boolean requiresNonAnonymousResponses = false;
                                if (nestedPolicy != null) {
                                    requiresAnonymousResponses = nestedPolicy.contains(W3CAddressingMetadataConstants.WSAM_ANONYMOUS_NESTED_ASSSSERTION);
                                    requiresNonAnonymousResponses = nestedPolicy.contains(W3CAddressingMetadataConstants.WSAM_NONANONYMOUS_NESTED_ASSSSERTION);
                                }
                                if(requiresAnonymousResponses && requiresNonAnonymousResponses) {
                                    throw new WebServiceException("Only one among AnonymousResponses and NonAnonymousResponses can be nested in an Addressing assertion");
                                }

                                final WebServiceFeature feature;
                                if(requiresAnonymousResponses) {
                                    feature  = new AddressingFeature(true, !assertion.isOptional(), new AddressingFeature.Responses[] {AddressingFeature.Responses.ANONYMOUS});
                                } else if(requiresNonAnonymousResponses){
                                    feature = new AddressingFeature(true, !assertion.isOptional(), new AddressingFeature.Responses[] {AddressingFeature.Responses.NON_ANONYMOUS});
                                } else {
                                    feature = new AddressingFeature(true, !assertion.isOptional());
                                }
                                port.addFeature(feature);
                                if (LOGGER.isLoggable(Level.FINE)) {
                                    LOGGER.fine("Added addressing feature \"" + feature + "\" to port \"" + port + "\"");
                                }
                            }
                        }
                    }
                }
                
            } // end foreach port
        } // end foreach service
        LOGGER.exiting();
    }
}
