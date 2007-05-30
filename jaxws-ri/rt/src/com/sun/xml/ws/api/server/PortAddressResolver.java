/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.api.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.namespace.QName;

/**
 * Resolves port address for an endpoint. A WSDL may contain multiple
 * endpoints, and some of the endpoints may be packaged in a single WAR file.
 * If an endpoint is serving the WSDL, it would be nice to fill the port addresses
 * of other endpoints in the WAR.
 *
 * <p>
 * This interface is implemented by the caller of
 * {@link SDDocument#writeTo} method so
 * that the {@link SDDocument} can correctly fills the addresses of known
 * endpoints.
 *
 *
 * @author Jitendra Kotamraju
 */
public abstract class PortAddressResolver {
    /**
     * Gets the endpoint address for a WSDL port
     *
     * @param serviceName
     *       WSDL service name(wsd:service in WSDL) for which address is needed. Always non-null.
     * @param portName
     *       WSDL port name(wsdl:port in WSDL) for which address is needed. Always non-null.
     * @return
     *      The address needs to be put in WSDL for port element's location
     *      attribute. Can be null. If it is null, existing port address
     *      is written as it is (without any patching).
     */
    public abstract @Nullable String getAddressFor(@NotNull QName serviceName, @NotNull String portName);
}
