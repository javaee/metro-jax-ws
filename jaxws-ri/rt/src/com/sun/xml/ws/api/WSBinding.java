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

package com.sun.xml.ws.api;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Tube;

import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import java.util.List;

/**
 * JAX-WS implementation of {@link Binding}.
 *
 * <p>
 * This object can be created by {@link BindingID#createBinding()}.
 *
 * <p>
 * Binding conceptually includes the on-the-wire format of the message,
 * this this object owns {@link Codec}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface WSBinding extends Binding {
    /**
     * Gets the SOAP version of this binding.
     *
     * TODO: clarify what to do with XML/HTTP binding
     *
     * <p>
     * This is just a shor-cut for  {@code getBindingID().getSOAPVersion()}
     *
     * @return
     *      If the binding is using SOAP, this method returns
     *      a {@link SOAPVersion} constant.
     *
     *      If the binding is not based on SOAP, this method
     *      returns null. See {@link Message} for how a non-SOAP
     *      binding shall be handled by {@link Tube}s.
     */
    SOAPVersion getSOAPVersion();
    /**
     * Gets the WS-Addressing version of this binding.
     * <p/>
     * TODO: clarify what to do with XML/HTTP binding
     *
     * @return If the binding is using SOAP and WS-Addressing is enabled,
     *         this method returns a {@link AddressingVersion} constant.
     *         If binding is not using SOAP or WS-Addressing is not enabled,
     *         this method returns null.
     *
     *          This might be little slow as it has to go over all the features on binding.
     *          Its advisable to cache the addressingVersion wherever possible and reuse it.
     */
    AddressingVersion getAddressingVersion();

    /**
     * Gets the binding ID, which uniquely identifies the binding.
     *
     * <p>
     * The relevant specs define the binding IDs and what they mean.
     * The ID is used in many places to identify the kind of binding
     * (such as SOAP1.1, SOAP1.2, REST, ...)
     *
     * @return
     *      Always non-null same value.
     */
    @NotNull BindingID getBindingId();

    @NotNull List<Handler> getHandlerChain();

    /**
     * Checks if a particular {@link WebServiceFeature} is enabled.
     *
     * @return
     *      true if enabled.
     */
    boolean isFeatureEnabled(@NotNull Class<? extends WebServiceFeature> feature);

    /**
     * Gets a {@link WebServiceFeature} of the specific type.
     *
     * @param featureType
     *      The type of the feature to retrieve.
     * @return
     *      If the feature is present and enabled, return a non-null instance.
     *      Otherwise null.
     */
    @Nullable <F extends WebServiceFeature> F getFeature(@NotNull Class<F> featureType);

    /**
     * Returns a list of features associated with {@link WSBinding}.
     */
    @NotNull WSFeatureList getFeatures();
}
