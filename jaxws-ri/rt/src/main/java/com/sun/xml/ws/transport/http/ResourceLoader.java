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

package com.sun.xml.ws.transport.http;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Set;

/**
 * Used to locate resources for {@link DeploymentDescriptorParser}.
 *
 * <p>
 * This allows {@link DeploymentDescriptorParser} to be used outside a servlet container,
 * but it still needs to work with a layout similar to the web application.
 * If this can be abstracted away better, that would be nice.
 *
 * @author Kohsuke Kawaguchi
 */
public interface ResourceLoader {
    /**
     * Returns the actual location of the resource from the 'path'
     * that represents a virtual locaion of a file inside a web application.
     *
     * @param path
     *      Desiganates an absolute path within an web application, such as:
     *      '/WEB-INF/web.xml' or some such.
     *
     * @return
     *      the actual location, if found, or null if not found.
     */
    URL getResource(String path) throws MalformedURLException;

    /**
     * Gets the catalog XML file that should be consulted when
     * loading resources from this {@link ResourceLoader}.
     */
    URL getCatalogFile() throws MalformedURLException;

    /**
     * Returns the list of files in the given directory.
     *
     * @return
     *      null if the path is invalid. empty if the path didn't contain
     *      any entry in it.
     *
     * @see javax.servlet.ServletContext#getResourcePaths(String)
     */
    Set<String> getResourcePaths(String path);
}
