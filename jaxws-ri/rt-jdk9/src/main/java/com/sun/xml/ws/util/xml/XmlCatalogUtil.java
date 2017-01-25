/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.xml.ws.util.xml;

import com.sun.istack.Nullable;
import com.sun.xml.ws.server.ServerRtException;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogFeatures.Feature;
import javax.xml.catalog.CatalogManager;
import javax.xml.ws.WebServiceException;
import org.xml.sax.EntityResolver;

/**
 *
 * @author lukas
 */
public class XmlCatalogUtil {

    // Cache CatalogFeatures instance for future usages.
    // Resolve feature is set to "continue" value for backward compatibility.
    private static final CatalogFeatures CATALOG_FEATURES
            = CatalogFeatures.builder().with(Feature.RESOLVE, "continue").build();

    /**
     * Gets an EntityResolver using XML catalog
     *
     * @param catalogUrl
     * @return
     */
    public static EntityResolver createEntityResolver(@Nullable URL catalogUrl) {
        ArrayList<URL> urlsArray = new ArrayList<>();
        EntityResolver er;
        if (catalogUrl != null) {
            urlsArray.add(catalogUrl);
        }
        try {
            er = createCatalogResolver(urlsArray);
        } catch (Exception e) {
            throw new ServerRtException("server.rt.err", e);
        }
        return er;
    }

    /**
     * Gets a default EntityResolver for catalog at META-INF/jaxws-catalog.xml
     *
     * @return
     */
    public static EntityResolver createDefaultCatalogResolver() {
        EntityResolver er;
        try {
            /**
             * Gets a URLs for catalog defined at META-INF/jaxws-catalog.xml
             */
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> catalogEnum;
            if (cl == null) {
                catalogEnum = ClassLoader.getSystemResources("META-INF/jax-ws-catalog.xml");
            } else {
                catalogEnum = cl.getResources("META-INF/jax-ws-catalog.xml");
            }
            er = createCatalogResolver(Collections.list(catalogEnum));
        } catch (Exception e) {
            throw new WebServiceException(e);
        }

        return er;
    }

    /**
     * Instantiate catalog resolver using new catalog API (javax.xml.catalog.*)
     * added in JDK9. Usage of new API removes dependency on internal API
     * (com.sun.org.apache.xml.internal) for modular runtime.
     */
    private static EntityResolver createCatalogResolver(ArrayList<URL> urls) throws Exception {
        // Prepare array of catalog URIs
        URI[] uris = urls.stream()
                             .map(u -> URI.create(u.toExternalForm()))
                             // TODO: REMOVE FOLLOWING LINE - workaround for https://bugs.openjdk.java.net/browse/JDK-8173260
                             .filter(u -> ("file".equals(u.getScheme()) && new File(u.toString()).exists()) || !"file".equals(u.getScheme()))
                             .toArray(URI[]::new);

        //Create CatalogResolver with new JDK9+ API
        return (EntityResolver) CatalogManager.catalogResolver(CATALOG_FEATURES, uris);
    }

}
