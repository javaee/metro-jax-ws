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
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;
import com.sun.xml.ws.addressing.W3CAddressingMetadataConstants;

import java.util.ArrayList;
import javax.xml.namespace.QName;


/**
 * This class validates the Addressing assertions.
 * If the assertion is wsam:Addressing, it makes sure that only valid assertions are nested.
 *  
 * @author japod
 * @author Rama Pulavarthi
 */
public class AddressingPolicyValidator implements PolicyAssertionValidator {

    private static final ArrayList<QName> supportedAssertions = new ArrayList<QName>();

    static {
        supportedAssertions.add(new QName(AddressingVersion.MEMBER.policyNsUri, "UsingAddressing"));
        supportedAssertions.add(W3CAddressingMetadataConstants.WSAM_ADDRESSING_ASSERTION);
        supportedAssertions.add(W3CAddressingMetadataConstants.WSAM_ANONYMOUS_NESTED_ASSERTION);
        supportedAssertions.add(W3CAddressingMetadataConstants.WSAM_NONANONYMOUS_NESTED_ASSERTION);
    }

    /**
     * Creates a new instance of AddressingPolicyValidator
     */
    public AddressingPolicyValidator() {
    }

    public Fitness validateClientSide(PolicyAssertion assertion) {
        return supportedAssertions.contains(assertion.getName()) ? Fitness.SUPPORTED : Fitness.UNKNOWN;
    }

    public Fitness validateServerSide(PolicyAssertion assertion) {
        if (!supportedAssertions.contains(assertion.getName()))
            return Fitness.UNKNOWN;

        //Make sure wsam:Addressing contains only one of the allowed nested assertions.
        if (assertion.getName().equals(W3CAddressingMetadataConstants.WSAM_ADDRESSING_ASSERTION)) {
            NestedPolicy nestedPolicy = assertion.getNestedPolicy();
            if (nestedPolicy != null) {
                boolean requiresAnonymousResponses = false;
                boolean requiresNonAnonymousResponses = false;
                for (PolicyAssertion nestedAsser : nestedPolicy.getAssertionSet()) {
                    if (nestedAsser.getName().equals(W3CAddressingMetadataConstants.WSAM_ANONYMOUS_NESTED_ASSERTION)) {
                        requiresAnonymousResponses = true;
                    } else if (nestedAsser.getName().equals(W3CAddressingMetadataConstants.WSAM_NONANONYMOUS_NESTED_ASSERTION)) {
                        requiresNonAnonymousResponses = true;
                    } else {
                        LOGGER.warning("Found unsupported assertion:\n" + nestedAsser + "\nnested into assertion:\n" + assertion);
                        return Fitness.UNSUPPORTED;
                    }
                }

                if (requiresAnonymousResponses && requiresNonAnonymousResponses) {
                    LOGGER.warning("Only one among AnonymousResponses and NonAnonymousResponses can be nested in an Addressing assertion");
                    return Fitness.INVALID;
                }
            }
        }

        return Fitness.SUPPORTED;
    }

    public String[] declareSupportedDomains() {
        return new String[]{AddressingVersion.MEMBER.policyNsUri, AddressingVersion.W3C.policyNsUri, W3CAddressingMetadataConstants.WSAM_NAMESPACE_NAME};
    }

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(AddressingPolicyValidator.class);
}
