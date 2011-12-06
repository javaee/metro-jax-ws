/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.policy.jaxws.spi;

import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import java.util.Collection;
import javax.xml.ws.WebServiceFeature;

/**
 * The service provider implementing this interface will be discovered and called to configure
 * wsdl model based on PolicyMap bound to it.
 *
 * @since JAX-WS 2.2
 *
 * @author japod
 * @author Fabian Ritzmann
 * @author Rama.Pulavarthi@sun.com
 */
public interface PolicyFeatureConfigurator {

    /**
     * A callback method that allows to retrieve policy related information from provided PolicyMap
     * and return a list of corresponding WebServiceFeatures.
     *
     * @param key Identifies the policy in the policy map
     * @param policyMap Provides policies as a source of information on proper configuration
     * @return A list of features that correspond to the policy identified by the policy map key. May be empty but not null.
     * @throws PolicyException If an error occurred
     */
    public Collection<WebServiceFeature> getFeatures(PolicyMapKey key, PolicyMap policyMap) throws PolicyException;
    
}
