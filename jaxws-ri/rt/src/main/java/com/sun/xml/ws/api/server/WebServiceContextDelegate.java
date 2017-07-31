/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Pipe;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import java.security.Principal;

/**
 * This object is set to {@link Packet#webServiceContextDelegate}
 * to serve {@link WebServiceContext} methods for a {@link Packet}.
 *
 * <p>
 * When the user application calls a method on {@link WebServiceContext},
 * the JAX-WS RI goes to the {@link Packet} that represents the request,
 * then check {@link Packet#webServiceContextDelegate}, and forwards
 * the method calls to {@link WebServiceContextDelegate}. 
 *
 * <p>
 * All the methods defined on this interface takes {@link Packet}
 * (whose {@link Packet#webServiceContextDelegate} points to
 * this object), so that a single stateless {@link WebServiceContextDelegate}
 * can be used to serve multiple concurrent {@link Packet}s,
 * if the implementation wishes to do so.
 *
 * <p>
 * (It is also allowed to create one instance of
 * {@link WebServiceContextDelegate} for each packet,
 * and thus effectively ignore the packet parameter.)
 *
 * <p>
 * Attaching this on a {@link Packet} allows {@link Pipe}s to
 * intercept and replace them, if they wish.
 *
 *
 * @author Kohsuke Kawaguchi
 */
public interface WebServiceContextDelegate {
    /**
     * Implements {@link WebServiceContext#getUserPrincipal()}
     * for the given packet.
     *
     * @param request
     *      Always non-null. See class javadoc.
     * @see WebServiceContext#getUserPrincipal()
     */
    Principal getUserPrincipal(@NotNull Packet request);

    /**
     * Implements {@link WebServiceContext#isUserInRole(String)}
     * for the given packet.
     *
     * @param request
     *      Always non-null. See class javadoc.
     * @see WebServiceContext#isUserInRole(String)
     */
    boolean isUserInRole(@NotNull Packet request,String role);

    /**
     * Gets the address of the endpoint.
     *
     * <p>
     * The "address" of endpoints is always affected by a particular
     * client being served, hence it's up to transport to provide this
     * information.
     *
     * @param request
     *      Always non-null. See class javadoc.
     * @param endpoint
     *      The endpoint whose address will be returned.
     *
     * @throws WebServiceException
     *      if this method could not compute the address for some reason.
     * @return
     *      Absolute URL of the endpoint. This shold be an address that the client
     *      can use to talk back to this same service later.
     *
     * @see WebServiceContext#getEndpointReference
     */
    @NotNull String getEPRAddress(@NotNull Packet request, @NotNull WSEndpoint endpoint);

    /**
     * Gets the address of the primary WSDL.
     *
     * <p>
     * If a transport supports publishing of WSDL by itself (instead/in addition to MEX),
     * then it should implement this method so that the rest of the JAX-WS RI can
     * use that information.
     *
     * For example, HTTP transports often use the convention {@code getEPRAddress()+"?wsdl"}
     * for publishing WSDL on HTTP.
     *
     * <p>
     * Some transports may not have such WSDL publishing mechanism on its own.
     * Those transports may choose to return null, indicating that WSDL
     * is not published. If such transports are always used in conjunction with
     * other transports that support WSDL publishing (such as SOAP/TCP used
     * with Servlet transport), then such transport may
     * choose to find the corresponding servlet endpoint by {@link Module#getBoundEndpoints()}
     * and try to obtain the address from there. 
     *
     * <p>
     * This information is used to put a metadata reference inside an EPR,
     * among other things. Clients that do not support MEX rely on this
     * WSDL URL to retrieve metadata, it is desirable for transports to support
     * this, but not mandatory.
     *
     * <p>
     * This method will be never invoked if the {@link WSEndpoint}
     * does not have a corresponding WSDL to begin with
     * (IOW {@link WSEndpoint#getServiceDefinition() returning null}.
     *
     * @param request
     *      Always non-null. See class javadoc.
     * @param endpoint
     *      The endpoint whose address will be returned.
     *
     * @return
     *      null if the implementation does not support the notion of
     *      WSDL publishing.
     */
    @Nullable String getWSDLAddress(@NotNull Packet request, @NotNull WSEndpoint endpoint);
}
