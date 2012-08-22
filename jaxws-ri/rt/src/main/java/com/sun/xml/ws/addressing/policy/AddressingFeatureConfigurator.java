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

package com.sun.xml.ws.addressing.policy;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.addressing.W3CAddressingMetadataConstants;
import com.sun.xml.ws.resources.ModelerMessages;
import com.sun.xml.bind.util.Which;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
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
public class AddressingFeatureConfigurator implements PolicyFeatureConfigurator {

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(AddressingFeatureConfigurator.class);

    private static final QName[] ADDRESSING_ASSERTIONS = {
        new QName(AddressingVersion.MEMBER.policyNsUri, "UsingAddressing")};

    /**
     * Creates a new instance of AddressingFeatureConfigurator
     */
    public AddressingFeatureConfigurator() {
    }

    public Collection<WebServiceFeature> getFeatures(final PolicyMapKey key, final PolicyMap policyMap) throws PolicyException {
        LOGGER.entering(key, policyMap);
        final Collection<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
        if ((key != null) && (policyMap != null)) {
            final Policy policy = policyMap.getEndpointEffectivePolicy(key);
            for (QName addressingAssertionQName : ADDRESSING_ASSERTIONS) {
                if ((policy != null) && policy.contains(addressingAssertionQName)) {
                    final Iterator <AssertionSet> assertions = policy.iterator();
                    while(assertions.hasNext()){
                        final AssertionSet assertionSet = assertions.next();
                        final Iterator<PolicyAssertion> policyAssertion = assertionSet.iterator();
                        while(policyAssertion.hasNext()){
                            final PolicyAssertion assertion = policyAssertion.next();
                            if(assertion.getName().equals(addressingAssertionQName)){
                                final WebServiceFeature feature = AddressingVersion.getFeature(addressingAssertionQName.getNamespaceURI(), true, !assertion.isOptional());
                                if (LOGGER.isLoggable(Level.FINE)) {
                                    LOGGER.fine("Added addressing feature \"" + feature + "\" for element \"" + key + "\"");
                                }
                                features.add(feature);
                            } // end-if non optional wsa assertion found
                        } // next assertion
                    } // next alternative
                } // end-if policy contains wsa assertion
            } //end foreach addr assertion

            // Deal with WS-Addressing 1.0 Metadata assertions
            if (policy != null && policy.contains(W3CAddressingMetadataConstants.WSAM_ADDRESSING_ASSERTION)) {
                for (AssertionSet assertions : policy) {
                    for (PolicyAssertion assertion : assertions) {
                        if (assertion.getName().equals(W3CAddressingMetadataConstants.WSAM_ADDRESSING_ASSERTION)) {
                            NestedPolicy nestedPolicy = assertion.getNestedPolicy();
                            boolean requiresAnonymousResponses = false;
                            boolean requiresNonAnonymousResponses = false;
                            if (nestedPolicy != null) {
                                requiresAnonymousResponses = nestedPolicy.contains(W3CAddressingMetadataConstants.WSAM_ANONYMOUS_NESTED_ASSERTION);
                                requiresNonAnonymousResponses = nestedPolicy.contains(W3CAddressingMetadataConstants.WSAM_NONANONYMOUS_NESTED_ASSERTION);
                            }
                            if(requiresAnonymousResponses && requiresNonAnonymousResponses) {
                                throw new WebServiceException("Only one among AnonymousResponses and NonAnonymousResponses can be nested in an Addressing assertion");
                            }

                            final WebServiceFeature feature;
                            try {
                                if (requiresAnonymousResponses) {
                                    feature = new AddressingFeature(true, !assertion.isOptional(), AddressingFeature.Responses.ANONYMOUS);
                                } else if (requiresNonAnonymousResponses) {
                                    feature = new AddressingFeature(true, !assertion.isOptional(), AddressingFeature.Responses.NON_ANONYMOUS);
                                } else {
                                    feature = new AddressingFeature(true, !assertion.isOptional());
                                }
                            } catch (NoSuchMethodError e) {
                                throw LOGGER.logSevereException(new PolicyException(ModelerMessages.RUNTIME_MODELER_ADDRESSING_RESPONSES_NOSUCHMETHOD(toJar(Which.which(AddressingFeature.class))), e));
                            }
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine("Added addressing feature \"" + feature + "\" for element \"" + key + "\"");
                            }
                            features.add(feature);
                        }
                    }
                }
            }
        }
        LOGGER.exiting(features);
        return features;
    }

    /**
     * Given the URL String inside jar, returns the URL to the jar itself.
     */
    private static String toJar(String url) {
        if(!url.startsWith("jar:"))
            return url;
        url = url.substring(4); // cut off jar:
        return url.substring(0,url.lastIndexOf('!'));    // cut off everything after '!'
    }
}
