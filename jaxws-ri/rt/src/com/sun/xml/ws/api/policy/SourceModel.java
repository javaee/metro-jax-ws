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

package com.sun.xml.ws.api.policy;

import com.sun.xml.ws.addressing.policy.AddressingPrefixMapper;
import com.sun.xml.ws.config.management.policy.ManagementPrefixMapper;
import com.sun.xml.ws.encoding.policy.EncodingPrefixMapper;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import com.sun.xml.ws.policy.spi.PrefixMapper;

import java.util.Arrays;

/**
 * This class is a root of unmarshalled policy source structure. Each instance of
 * the class contains factory method to create new  com.sun.xml.ws.policy.sourcemodel.ModelNode
 * instances associated with the actual model instance.
 *
 * @author Fabian Ritzmann
 */
public class SourceModel extends PolicySourceModel {

    private static final PrefixMapper[] JAXWS_PREFIX_MAPPERS = {
        new AddressingPrefixMapper(),
        new EncodingPrefixMapper(),
        new ManagementPrefixMapper()
    };


    /**
     * Private constructor that creates a new policy source model instance without any
     * id or name identifier. The namespace-to-prefix map is initialized with mapping
     * of policy namespace to the default value set by
     * {@link PolicyConstants#POLICY_NAMESPACE_PREFIX POLICY_NAMESPACE_PREFIX constant}.
     *
     * @param nsVersion The WS-Policy version.
     */
    private SourceModel(NamespaceVersion nsVersion) {
        this(nsVersion, null, null);
    }

    /**
     * Private constructor that creates a new policy source model instance with given
     * id or name identifier and a set of PrefixMappers.
     *
     * @param nsVersion The WS-Policy version.
     * @param policyId Relative policy reference within an XML document. May be {@code null}.
     * @param policyName Absolute IRI of policy expression. May be {@code null}.
     */
    private SourceModel(NamespaceVersion nsVersion, String policyId, String policyName) {
        super(nsVersion, policyId, policyName, Arrays.asList(JAXWS_PREFIX_MAPPERS));
    }

    /**
     * Factory method that creates new policy source model instance.
     *
     * @param nsVersion The policy version
     * @return Newly created policy source model instance.
     */
    public static PolicySourceModel createSourceModel(final NamespaceVersion nsVersion) {
        return new SourceModel(nsVersion);
    }

    /**
     * Factory method that creates new policy source model instance and initializes it according to parameters provided.
     *
     * @param nsVersion The policy version
     * @param policyId local policy identifier - relative URI. May be {@code null}.
     * @param policyName global policy identifier - absolute policy expression URI. May be {@code null}.
     * @return Newly created policy source model instance with its name and id properly set.
     */
    public static PolicySourceModel createSourceModel(final NamespaceVersion nsVersion,
            final String policyId, final String policyName) {
        return new SourceModel(nsVersion, policyId, policyName);
    }

}