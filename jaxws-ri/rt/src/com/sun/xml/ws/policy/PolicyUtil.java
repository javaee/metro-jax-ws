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

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.policy.PolicyMap.ScopeType;
import com.sun.xml.ws.policy.jaxws.spi.ModelConfiguratorProvider;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.subject.PolicyMapKeyConverter;
import com.sun.xml.ws.policy.subject.WsdlBindingSubject;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Rama Pulavarthi
 * @author Fabian Ritzmann
 */
public class PolicyUtil {

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyUtil.class);
    private static final PolicyMerger MERGER = PolicyMerger.getMerger();
    private static ModelConfiguratorProvider[] configurators = PolicyUtils.ServiceProvider.load(ModelConfiguratorProvider.class);

    /**
     * Iterates through the ports in the WSDL model, for each policy in the policy
     * map that is attached at endpoint scope computes a list of corresponding
     * WebServiceFeatures and sets them on the port.
     *
     * @param model The WSDL model
     * @param policyMap The policy map
     * @throws PolicyException If the list of WebServiceFeatures could not be computed
     */
    public static void configureModel(final WSDLModel model, PolicyMap policyMap) throws PolicyException {
        LOGGER.entering(model, policyMap);
        for (WSDLService service : model.getServices().values()) {
            for (WSDLPort port : service.getPorts()) {
                final Collection<WebServiceFeature> features = getPortScopedFeatures(policyMap, service.getName(), port.getName());
                for (WebServiceFeature feature : features) {
                    port.addFeature(feature);
                    port.getBinding().addFeature(feature);
                }
            }
        }
        LOGGER.exiting();
    }

    /**
     * Returns the list of features that correspond to the policies in the policy
     * map for a give port
     *
     * @param policyMap The service policies
     * @param serviceName The service name
     * @param portName The service port name
     * @return List of features for the given port corresponding to the policies in the map
     */
    public static Collection<WebServiceFeature> getPortScopedFeatures(PolicyMap policyMap, QName serviceName, QName portName) {
        LOGGER.entering(policyMap, serviceName, portName);
        Collection<WebServiceFeature> features = new ArrayList<WebServiceFeature>();
        try {
            final PolicyMapKey key = PolicyMap.createWsdlEndpointScopeKey(serviceName, portName);
            for (ModelConfiguratorProvider configurator : configurators) {
                Collection<WebServiceFeature> additionalFeatures = configurator.getFeatures(key, policyMap);
                if (additionalFeatures != null) {
                    features.addAll(additionalFeatures);
                }
            }
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
        LOGGER.exiting(features);
        return features;
    }

    /**
     * Inserts all PolicySubjects of type WsdlBindingSubject into the given policy map.
     *
     * @param policyMap The policy map
     * @param policySubjects The policy subjects. The actual subject must have the
     *   type WsdlBindingSubject, otherwise it will not be processed.
     * @param serviceName The name of the current WSDL service
     * @ param portName The name of the current WSDL port
     * @throws PolicyException Thrown if the effective policy of a polic subject
     *   could not be computed
     */
    static void insertPolicies(final PolicyMap policyMap, final Collection<PolicySubject> policySubjects, QName serviceName, QName portName)
            throws PolicyException {
        LOGGER.entering(policyMap, policySubjects, serviceName, portName);

        final HashMap<WsdlBindingSubject, Collection<Policy>> subjectToPolicies = new HashMap<WsdlBindingSubject, Collection<Policy>>();
        for (PolicySubject subject: policySubjects) {
            final Object actualSubject = subject.getSubject();
            if (actualSubject instanceof WsdlBindingSubject) {
                final WsdlBindingSubject wsdlSubject = (WsdlBindingSubject) actualSubject;
                final Collection<Policy> subjectPolicies = new LinkedList<Policy>();
                subjectPolicies.add(subject.getEffectivePolicy(MERGER));
                final Collection<Policy> existingPolicies = subjectToPolicies.put(wsdlSubject, subjectPolicies);
                if (existingPolicies != null) {
                    subjectPolicies.addAll(existingPolicies);
                }
            }
        }

        final PolicyMapKeyConverter converter = new PolicyMapKeyConverter(serviceName, portName);
        for (WsdlBindingSubject wsdlSubject : subjectToPolicies.keySet()) {
            final PolicySubject newSubject = new PolicySubject(wsdlSubject, subjectToPolicies.get(wsdlSubject));
            PolicyMapKey mapKey = converter.getPolicyMapKey(wsdlSubject);

            if (wsdlSubject.isBindingSubject()) {
                policyMap.putSubject(ScopeType.ENDPOINT, mapKey, newSubject);
            }
            else if (wsdlSubject.isBindingOperationSubject()) {
                policyMap.putSubject(ScopeType.OPERATION, mapKey, newSubject);
            }
            else if (wsdlSubject.isBindingMessageSubject()) {
                switch (wsdlSubject.getMessageType()) {
                    case INPUT:
                        policyMap.putSubject(ScopeType.INPUT_MESSAGE, mapKey, newSubject);
                        break;
                    case OUTPUT:
                        policyMap.putSubject(ScopeType.OUTPUT_MESSAGE, mapKey, newSubject);
                        break;
                    case FAULT:
                        policyMap.putSubject(ScopeType.FAULT_MESSAGE, mapKey, newSubject);
                        break;
                }
            }
        }

        LOGGER.exiting();
    }

}