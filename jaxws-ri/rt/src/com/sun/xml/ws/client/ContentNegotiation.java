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

package com.sun.xml.ws.client;

import com.sun.xml.ws.pept.presentation.MessageStruct;
import static com.sun.xml.ws.client.BindingProviderProperties.CONTENT_NEGOTIATION_PROPERTY;

import java.util.Map;

public class ContentNegotiation {

    /**
     * Initializes content negotiation property in <code>MessageStruct</code>
     * based on request context and system properties.
     */
    static public void initialize(Map context, MessageStruct messageStruct) {
        String value = (String) context.get(CONTENT_NEGOTIATION_PROPERTY);
        if (value != null) {
            if (value.equals("none") || value.equals("pessimistic") || value.equals("optimistic")) {
                messageStruct.setMetaData(CONTENT_NEGOTIATION_PROPERTY, value.intern());
            } else {
                throw new SenderException("sender.request.illegalValueForContentNegotiation", value);
            }
        } else {
            initFromSystemProperties(context, messageStruct);
        }
    }

    /**
     * Initializes content negotiation property in <code>MessageStruct</code>
     * based on system property of the same name.
     */
    static public void initFromSystemProperties(Map context, MessageStruct messageStruct)
        throws SenderException {
        String value = System.getProperty(CONTENT_NEGOTIATION_PROPERTY);

        if (value == null) {
            messageStruct.setMetaData(
                CONTENT_NEGOTIATION_PROPERTY, "none");      // FI is off by default
        } else if (value.equals("none") || value.equals("pessimistic") || value.equals("optimistic")) {
            messageStruct.setMetaData(CONTENT_NEGOTIATION_PROPERTY, value.intern());
            context.put(CONTENT_NEGOTIATION_PROPERTY, value.intern());
        } else {
            throw new SenderException("sender.request.illegalValueForContentNegotiation", value);
        }
    }
}
