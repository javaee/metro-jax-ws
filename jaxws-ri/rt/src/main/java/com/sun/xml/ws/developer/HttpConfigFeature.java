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

package com.sun.xml.ws.developer;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import java.lang.reflect.Constructor;
import java.net.CookieHandler;

/**
 * A proxy's HTTP configuration (e.g cookie handling) can be configured using
 * this feature. While creating the proxy, this can be passed just like other
 * features.
 *
 * <p>
 * <b>THIS feature IS EXPERIMENTAL AND IS SUBJECT TO CHANGE WITHOUT NOTICE IN FUTURE.</b>
 *
 * @author Jitendra Kotamraju
 */
public final class HttpConfigFeature extends WebServiceFeature {
    /**
     * Constant value identifying the {@link HttpConfigFeature} feature.
     */
    public static final String ID = "http://jax-ws.java.net/features/http-config";

    private static final Constructor cookieManagerConstructor;
    private static final Object cookiePolicy;
    static {
        Constructor tempConstructor;
        Object tempPolicy;
        try {
            /*
             * Using reflection to create CookieManger so that RI would continue to
             * work with JDK 5.
             */
            Class policyClass = Class.forName("java.net.CookiePolicy");
            Class storeClass = Class.forName("java.net.CookieStore");
            tempConstructor = Class.forName("java.net.CookieManager").getConstructor(storeClass, policyClass);
            // JDK's default policy is ACCEPT_ORIGINAL_SERVER, but ACCEPT_ALL
            // is used for backward compatibility
            tempPolicy = policyClass.getField("ACCEPT_ALL").get(null);
        } catch(Exception e) {
            try {
                /*
                 * Using reflection so that these classes won't have to be
                 * integrated in JDK 6.
                 */
                Class policyClass = Class.forName("com.sun.xml.ws.transport.http.client.CookiePolicy");
                Class storeClass = Class.forName("com.sun.xml.ws.transport.http.client.CookieStore");
                tempConstructor = Class.forName("com.sun.xml.ws.transport.http.client.CookieManager").getConstructor(storeClass, policyClass);
                // JDK's default policy is ACCEPT_ORIGINAL_SERVER, but ACCEPT_ALL
                // is used for backward compatibility
                tempPolicy = policyClass.getField("ACCEPT_ALL").get(null);
            } catch(Exception ce) {
                throw new WebServiceException(ce);
            }
        }
        cookieManagerConstructor = tempConstructor;
        cookiePolicy = tempPolicy;
    }

    private final CookieHandler cookieJar;      // shared object among the tubes

    public HttpConfigFeature() {
        this(getInternalCookieHandler());
    }

    public HttpConfigFeature(CookieHandler cookieJar) {
        this.enabled = true;
        this.cookieJar = cookieJar;
    }

    private static CookieHandler getInternalCookieHandler() {
        try {
            return (CookieHandler)cookieManagerConstructor.newInstance(null, cookiePolicy);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }

    public String getID() {
        return ID;
    }

    public CookieHandler getCookieHandler() {
        return cookieJar;
    }

}
