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

package com.sun.xml.ws.api.server;

import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.istack.Nullable;

import javax.xml.namespace.QName;

/**
 * Implementations of this class can contribute properties associated with an Endpoint. The properties appear as
 * extensibility elements inside the EndpointReference of the endpoint. If any EPR extensibility elements are configured
 * for an endpoint, the EndpointReference is published inside the WSDL.
 * 
 * @since JAX-WS 2.2
 * @author Rama Pulavarthi
 */
public abstract class EndpointReferenceExtensionContributor {
    /**
     *
     * @param extension EPRExtension is passed if an extension with same QName is already configured on the endpoint
     *      via other means (one possible way is by embedding EndpointReference in WSDL).
     *
     * @return  EPRExtension that should be finally configured on an Endpoint.
     */
    public abstract WSEndpointReference.EPRExtension getEPRExtension(WSEndpoint endpoint, @Nullable WSEndpointReference.EPRExtension extension );

    /**
     *
     * @return QName of the extensibility element that is contributed by this extension.
     */
    public abstract QName getQName();
}
