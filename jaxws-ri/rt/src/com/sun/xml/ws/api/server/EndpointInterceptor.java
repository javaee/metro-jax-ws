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

import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.ws.WebServiceFeature;
import java.util.List;
import java.util.Collections;

/**
 * Interception point for inner working of {@link WSEndpoint}.
 *
 * <p>
 * System-level code could hook an implementation of this to
 * {@link WSEndpoint} to augument its behavior before the WSEndpoint is created.
 *
 * @author Rama Pulavarthi
 * @since 2.2
 */
public abstract class EndpointInterceptor{

    /**
         * Called before {@link com.sun.xml.ws.api.WSBinding} is created, to allow interceptors
         * to add {@link WebServiceFeature}s to the created {@link com.sun.xml.ws.api.WSBinding}.
         *
         * @param port
         *      Nullable WSDLModel for that port
         * @param endpointImplClass
         *      Not Null ednpointImplementation Class
         * @param defaultFeatures
         *      The list of features that are currently scheduled to be set for
         *      the newly created {@link com.sun.xml.ws.api.WSBinding}.
         *
         * @return
         *      A set of features to be added to the newly created {@link com.sun.xml.ws.api.WSBinding}.
         *      Can be empty but never null.
         *      <tt>defaultFeatures</tt> will take precedence over what this method
         *      would return (because it includes user-specified ones which will
         *      take the at-most priority), but features you return from this method
         *      will take precedence over {@link com.sun.xml.ws.api.BindingID}'s
         *      {@link com.sun.xml.ws.api.BindingID#createBuiltinFeatureList() implicit features}.
         */
        public List<WebServiceFeature> postCreateBinding(@Nullable WSDLPort port, @NotNull Class<?> endpointImplClass, @NotNull WSFeatureList defaultFeatures) {
            return Collections.emptyList();
        }


}
