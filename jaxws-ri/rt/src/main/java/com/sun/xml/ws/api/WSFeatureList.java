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

package com.sun.xml.ws.api;import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.ws.WebServiceFeature;

/**
 * Read-only list of {@link WebServiceFeature}s.
 *
 * @author Kohsuke Kawaguchi
 */
public interface WSFeatureList extends Iterable<WebServiceFeature> {
    /**
     * Checks if a particular {@link WebServiceFeature} is enabled.
     *
     * @return
     *      true if enabled.
     */
    boolean isEnabled(@NotNull Class<? extends WebServiceFeature> feature);

    /**
     * Gets a {@link WebServiceFeature} of the specific type.
     *
     * @param featureType
     *      The type of the feature to retrieve.
     * @return
     *      If the feature is present and enabled, return a non-null instance.
     *      Otherwise null.
     */
    @Nullable <F extends WebServiceFeature> F get(@NotNull Class<F> featureType);

    /**
     * Obtains all the features in the array.
      */
    @NotNull WebServiceFeature[] toArray();

    /**
     * Merges the extra features that are not already set on binding.
     * i.e, if a feature is set already on binding through some other API
     * the corresponding wsdlFeature is not set.
     *
     * @param features          Web Service features that need to be merged with already configured features.
     * @param reportConflicts   If true, checks if the feature setting in WSDL (wsdl extension or
     *                          policy configuration) conflicts with feature setting in Deployed Service and
     *                          logs warning if there are any conflicts.
     */
    void mergeFeatures(@NotNull WebServiceFeature[] features, boolean reportConflicts);

   /**
    * Merges the extra features that are not already set on binding.
    * i.e, if a feature is set already on binding through some other API
    * the corresponding wsdlFeature is not set.
    *
    * @param features          Web Service features that need to be merged with already configured features.
    * @param reportConflicts   If true, checks if the feature setting in WSDL (wsdl extension or
    *                          policy configuration) conflicts with feature setting in Deployed Service and
    *                          logs warning if there are any conflicts.
    */
   void mergeFeatures(@NotNull Iterable<WebServiceFeature> features, boolean reportConflicts);
}
