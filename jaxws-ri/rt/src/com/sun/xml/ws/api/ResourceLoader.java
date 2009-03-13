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

package com.sun.xml.ws.api;

import com.sun.xml.ws.api.server.Container;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Used to locate resources for jax-ws extensions. Using this, extensions
 * do not to have to write container specific code to locate resources.
 *
 * @author Jitendra Kotamraju
 */
public abstract class ResourceLoader {

    /**
     * Returns the actual location of the resource from the 'resource' arg
     * that represents a virtual locaion of a file understood by a container.
     * ResourceLoader impl for a Container knows how to map this
     * virtual location to actual location.
     * <p>
     * Extensions can get hold of this object using {@link Container}.
     * <p/>
     * for e.g.:
     * <pre>
     * ResourceLoader loader = container.getSPI(ResourceLoader.class);
     * URL catalog = loader.get("jax-ws-catalog.xml");
     * </pre>
     * A ResourceLoader for servlet environment, may do the following.
     * <pre>
     * URL getResource(String resource) {
     *     return servletContext.getResource("/WEB-INF/"+resource);
     * }
     * </pre>
     *
     * @param resource Designates a path that is understood by the container. The
     *             implementations must support "jax-ws-catalog.xml" resource.
     * @return the actual location, if found, or null if not found.
     * @throws MalformedURLException if there is an error in creating URL
     */
    public abstract URL getResource(String resource) throws MalformedURLException;

}
