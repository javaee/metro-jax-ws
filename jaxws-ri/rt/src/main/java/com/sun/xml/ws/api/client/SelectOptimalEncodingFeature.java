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

package com.sun.xml.ws.api.client;

import com.sun.xml.ws.api.FeatureConstructor;

import javax.xml.ws.WebServiceFeature;

import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

/**
 * Client side feature to enable or disable the selection of the optimal 
 * encoding by the client when sending outbound messages.
 * <p>
 * The following describes the affects of this feature with respect
 * to being enabled or disabled:
 * <ul>
 *  <li> ENABLED: In this Mode, the most optimal encoding will be selected
 *       depending on the configuration and capabilities of the client
 *       the capabilities of the Web service.
 *  <li> DISABLED: In this Mode, the default encoding will be selected.
 * </ul>
 * <p>
 * If this feature is not present on a Web service then the default behaviour
 * is equivalent to this feature being present and disabled.
 * <p>
 * If this feature is enabled by the client and the Service supports the
 * Fast Infoset encoding, as specified by the {@link com.sun.xml.ws.api.fastinfoset.FastInfosetFeature},
 * and Fast Infoset is determined to be the most optimal encoding, then the 
 * Fast Infoset encoding will be automatically selected by the client.
 * <p>
 * TODO: Still not sure if a feature is a server side only thing or can 
 * also be a client side thing. If the former then this class should be
 * removed.
 * @author Paul.Sandoz@Sun.Com
 */
@ManagedData
public class SelectOptimalEncodingFeature extends WebServiceFeature {
    /**
     * Constant value identifying the {@link SelectOptimalEncodingFeature}
     */
    public static final String ID = "http://java.sun.com/xml/ns/jaxws/client/selectOptimalEncoding";

    /**
     * Create a {@link SelectOptimalEncodingFeature}. 
     * The instance created will be enabled.
     */
    public SelectOptimalEncodingFeature() {
        this.enabled = true;
    }

    /**
     * Create a {@link SelectOptimalEncodingFeature}
     *
     * @param enabled specifies whether this feature should
     *                be enabled or not.
     */
    @FeatureConstructor({"enabled"})
    public SelectOptimalEncodingFeature(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * {@inheritDoc}
     */
    @ManagedAttribute
    public String getID() {
        return ID;
    }
}
