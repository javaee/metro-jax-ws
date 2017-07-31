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

package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.FeatureConstructor;
import com.sun.xml.ws.server.DraconianValidationErrorHandler;

import javax.xml.ws.WebServiceFeature;

import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

/**
 * {@link WebServiceFeature} for schema validation.
 *
 * @since JAX-WS 2.1.3
 * @author Jitendra Kotamraju
 * @see SchemaValidation
 */
@ManagedData
public class SchemaValidationFeature extends WebServiceFeature {
    /**
     * Constant value identifying the SchemaValidationFeature
     */
    public static final String ID = "http://jax-ws.dev.java.net/features/schema-validation";

    private final Class<? extends ValidationErrorHandler> clazz;
    private final boolean inbound;
    private final boolean outbound;

    public SchemaValidationFeature() {
        this(true, true, DraconianValidationErrorHandler.class);
    }

    /**
     * Create an <code>SchemaValidationFeature</code>.
     * The instance created will be enabled.
     */
    public SchemaValidationFeature(Class<? extends ValidationErrorHandler> clazz) {
        this(true, true, clazz);
    }

    /**
     * @since JAX-WS RI 2.2.2
     */
    public SchemaValidationFeature(boolean inbound, boolean outbound) {
        this(inbound, outbound, DraconianValidationErrorHandler.class);
    }

    /**
     * @since JAX-WS RI 2.2.2
     */
    @FeatureConstructor({"inbound", "outbound", "handler"})
    public SchemaValidationFeature(boolean inbound, boolean outbound, Class<? extends ValidationErrorHandler> clazz) {
        this.enabled = true;
        this.inbound = inbound;
        this.outbound = outbound;
        this.clazz = clazz;
    }

    @ManagedAttribute
    @Override
    public String getID() {
        return ID;
    }

    /**
     * Invalid schema instances are rejected, a SOAP fault message is created
     * for any invalid request and response message. If it is set to false, schema
     * validation messages are just logged.
     */
    @ManagedAttribute
    public Class<? extends ValidationErrorHandler> getErrorHandler() {
        return clazz;
    }

    /**
     * Turns validation on/off for inbound messages
     *
     * @since JAX-WS RI 2.2.2
     */
    public boolean isInbound() {
        return inbound;
    }

    /**
     * Turns validation on/off for outbound messages
     *
     * @since JAX-WS RI 2.2.2
     */
    public boolean isOutbound() {
        return outbound;
    }
}
