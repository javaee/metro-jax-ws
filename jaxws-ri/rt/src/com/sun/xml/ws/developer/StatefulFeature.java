/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.FeatureConstructor;

import javax.jws.WebService;
import javax.xml.ws.WebServiceFeature;

/**
 * Designates a stateful {@link WebService}.
 * A service class that has this feature on will behave as a stateful web service.
 *
 * @since 2.1
 * @see StatefulWebServiceManager
 */
public class StatefulFeature extends WebServiceFeature {
    /**
     * Constant value identifying the StatefulFeature
     */
    public static final String ID = "http://jax-ws.dev.java.net/features/stateful";

    /**
     * Create an <code>StatefulFeature</code>.
     * The instance created will be enabled.
     */
    @FeatureConstructor
    public StatefulFeature() {
        this.enabled = true;
    }

    public String getID() {
        return ID;
    }
}
