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
package com.sun.xml.ws.server.provider;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.fault.SOAPFaultBuilder;

/**
 * @author Kohsuke Kawaguchi
 */
final class MessageProviderArgumentBuilder extends ProviderArgumentsBuilder<Message> {
    private final SOAPVersion soapVersion;

    public MessageProviderArgumentBuilder(SOAPVersion soapVersion) {
        this.soapVersion = soapVersion;
    }

    @Override
    protected Message getParameter(Packet packet) {
        return packet.getMessage();
    }

    @Override
    protected Message getResponseMessage(Message returnValue) {
        return returnValue;
    }

    @Override
    protected Message getResponseMessage(Exception e) {
        return SOAPFaultBuilder.createSOAPFaultMessage(soapVersion, null, e);
    }
}
