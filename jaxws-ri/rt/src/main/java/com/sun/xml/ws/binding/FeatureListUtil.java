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

package com.sun.xml.ws.binding;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

/**
 * Experimental: Utility methods that operate on WebServiceFeatureLists.
 *
 * @author WS Development Team
 */
public class FeatureListUtil {

    /**
     * Merge all features into one list. Returns an empty list if no lists were
     * passed as parameter.
     * 
     * @param lists The WebServiceFeatureLists.
     * @return A new WebServiceFeatureList that contains all features.
     */
    public static @NotNull WebServiceFeatureList mergeList(WebServiceFeatureList... lists) {
        final WebServiceFeatureList result = new WebServiceFeatureList();
        for (WebServiceFeatureList list : lists) {
            result.addAll(list);
        }
        return result;
    }
            
    public static @Nullable <F extends WebServiceFeature> F mergeFeature(final @NotNull Class<F> featureType,
            @Nullable WebServiceFeatureList list1, @Nullable WebServiceFeatureList list2) 
            throws WebServiceException {
        final F feature1 = list1 != null ? list1.get(featureType) : null;
        final F feature2 = list2 != null ? list2.get(featureType) : null;
        if (feature1 == null) {
            return feature2;
        }
        else if (feature2 == null) {
            return feature1;
        }
        else {
            if (feature1.equals(feature2)) {
                return feature1;
            }
            else {
                // TODO exception text
                throw new WebServiceException(feature1 + ", " + feature2);
            }
        }
    }
    
    public static boolean isFeatureEnabled(@NotNull Class<? extends WebServiceFeature> featureType,
            @Nullable WebServiceFeatureList list1, @Nullable WebServiceFeatureList list2)
            throws WebServiceException {
        final WebServiceFeature mergedFeature = mergeFeature(featureType, list1, list2);
        return (mergedFeature != null) && mergedFeature.isEnabled();
    }
    
}
