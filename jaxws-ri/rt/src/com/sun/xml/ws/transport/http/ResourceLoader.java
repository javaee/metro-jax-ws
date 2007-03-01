/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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
     * @see javax.servlet.http.ServletContext#getResourcePaths(String)
     */
    Set<String> getResourcePaths(String path);
}
