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

package com.sun.xml.ws.policy.jaxws.spi;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.policy.PolicyMap;
import javax.xml.ws.WebServiceFeature;

/**
 * Store a policy map on the endpoint.
 *
 * This feature should be set on the binding. It does not make sense to set a
 * policy map per port because the map contains the policies for all ports in a
 * WSDL document.
 */
public class PolicyFeature extends WebServiceFeature {

    private static final String featureId = PolicyMap.class.getName();
    private final PolicyMap policyMap;
    private final WSDLModel wsdlModel;    

    /**
     * Creates a new instance of PolicyFeature and sets this feature to enabled.
     *
     * @param map A PolicyMap
     * @param model The associated WSDL model
     */
    public PolicyFeature(PolicyMap map, WSDLModel model) {
        this.policyMap = map;
        this.wsdlModel = model;
        this.enabled = true;
    }

    /**
     * Returns the ID of this feature.
     *
     * @return The ID of this feature
     */
    public String getID() {
        return this.featureId;
    }

    /**
     * Returns the PolicyMap stored with this feature instance.
     *
     * @return The PolicyMap stored with this feature instance
     */
    public PolicyMap getPolicyMap() {
        return this.policyMap;
    }
    
    /**
     * Returns the WSDLModel stored with this feature instance.
     *
     * @return The WSDLModel stored with this feature instance
     */
    public WSDLModel getWsdlModel() {
        return this.wsdlModel;
    }
}