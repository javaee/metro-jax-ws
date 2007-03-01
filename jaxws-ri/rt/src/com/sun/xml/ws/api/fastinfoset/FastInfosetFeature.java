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

package com.sun.xml.ws.api.fastinfoset;

import com.sun.xml.ws.api.FeatureConstructor;

import javax.xml.ws.WebServiceFeature;

/**
 * Enable or disable Fast Infoset on a Web service.
 * <p>
 * The following describes the affects of this feature with respect
 * to being enabled or disabled:
 * <ul>
 *  <li> ENABLED: In this Mode, Fast Infoset will be enabled.
 *  <li> DISABLED: In this Mode, Fast Infoset will be disabled and the 
 *       Web service will not process incoming messages or produce outgoing
 *       messages encoded using Fast Infoset.
 * </ul>
 * <p>
 * If this feature is not present on a Web service then the default behaviour
 * is equivalent to this feature being present and enabled.
 * @author Paul.Sandoz@Sun.Com
 */
public class FastInfosetFeature extends WebServiceFeature {
    /**
     * Constant value identifying the {@link FastInfosetFeature}
     */
    public static final String ID = "http://java.sun.com/xml/ns/jaxws/fastinfoset";

    /**
     * Create a {@link FastInfosetFeature}. The instance created will be enabled.
     */
    public FastInfosetFeature() {
        this.enabled = true;
    }

    /**
     * Create a {@link FastInfosetFeature}
     *
     * @param enabled specifies whether this feature should
     *                be enabled or not.
     */
    @FeatureConstructor({"enabled"})
    public FastInfosetFeature(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * {@inheritDoc}
     */
    public String getID() {
        return ID;
    }
}