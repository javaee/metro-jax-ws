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

package com.sun.xml.ws.api.client;

import com.sun.xml.ws.api.FeatureConstructor;

import javax.xml.ws.WebServiceFeature;

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
 * Fast Infoset encoding, as specified by the {@link FastInfosetFeature},
 * and Fast Infoset is determined to be the most optimal encoding, then the 
 * Fast Infoset encoding will be automatically selected by the client.
 * <p>
 * TODO: Still not sure if a feature is a server side only thing or can 
 * also be a client side thing. If the former then this class should be
 * removed.
 * @author Paul.Sandoz@Sun.Com
 */
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
    public String getID() {
        return ID;
    }
}