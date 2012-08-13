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

package com.sun.xml.ws.api.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.Component;

import java.net.URI;

/**
 * Represents the {@link WSEndpoint} bound to a particular transport.
 *
 * @see Module#getBoundEndpoints() 
 * @author Kohsuke Kawaguchi
 */
public interface BoundEndpoint extends Component {
    /**
     * The endpoint that was bound.
     *
     * <p>
     * Multiple {@link BoundEndpoint}s may point to the same {@link WSEndpoint},
     * if it's bound to multiple transports.
     *
     * @return the endpoint
     */
    @NotNull WSEndpoint getEndpoint();

    /**
     * The address of the bound endpoint.
     *
     * <p>
     * For example, if this endpoint is bound to a servlet endpoint
     * "http://foobar/myapp/myservice", then this method should
     * return that address.
     *
     * @return address of the endpoint
     */
    @NotNull URI getAddress();

    /**
     * The address of the bound endpoint using the base address. Often
     * times, baseAddress is only avaialble during the request.
     *
     * <p>
     * If the endpoint is bound to a servlet endpoint, the base address
     * won't include the url-pattern, so the base address would be
     * "http://host:port/context". This method would include url-pattern
     * for the endpoint and return that address
     * for e.g. "http://host:port/context/url-pattern"
     * 
     * @param baseAddress that is used in computing the full address
     * @return address of the endpoint
     */
    @NotNull URI getAddress(String baseAddress);
}
