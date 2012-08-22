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

package com.sun.xml.ws.policy.jaxws.spi;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicySubject;
import java.util.Collection;

/**
 * The service provider implementing this interface will be discovered and called to extend created PolicyMap instance with additional policy
 * bindings. The call is performed directly after WSIT config file is parsed.
 *
 * @since JAX-WS 2.2
 * @author Marek Potociar (marek.potociar at sun.com)
 * @author Fabian Ritzmann
 * @author Rama.Pulavarthi@sun.com
 */
public interface PolicyMapConfigurator {

  /**
   * A callback method that allows to retrieve policy related information from provided parameters
   * return a collection of new policies that are added to the map.
   *
   * When new policies are returned, the caller may merge them with existing policies
   * in the policy map.
   *
   * @param policyMap This map contains the policies that were already created
   * @param model The WSDL model of the service
   * @param wsBinding The binding of the service
   * @return A collection of policies and the subject to which they are attached.
   *   May return null or an empty collection.
   * @throws PolicyException Throw this exception if an error occurs
   */
  Collection<PolicySubject> update(PolicyMap policyMap, SEIModel model, WSBinding wsBinding) throws PolicyException;
}
