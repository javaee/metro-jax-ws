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
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.addressing;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.addressing.model.MissingAddressingHeaderException;
import com.sun.xml.ws.addressing.model.InvalidAddressingHeaderException;
import static com.sun.xml.ws.addressing.W3CAddressingConstants.ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED;
import static com.sun.xml.ws.addressing.W3CAddressingConstants.ONLY_ANONYMOUS_ADDRESS_SUPPORTED;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.ws.soap.AddressingFeature;

/**
 * @author Rama Pulavarthi
 */
public class W3CWsaServerTube extends WsaServerTube{
    private final AddressingFeature af;

    public W3CWsaServerTube(WSEndpoint endpoint, @NotNull WSDLPort wsdlPort, WSBinding binding, Tube next) {
        super(endpoint, wsdlPort, binding, next);
        af = binding.getFeature(AddressingFeature.class);
    }

    public W3CWsaServerTube(W3CWsaServerTube that, TubeCloner cloner) {
        super(that, cloner);
        this.af = that.af;
    }

    @Override
    public W3CWsaServerTube copy(TubeCloner cloner) {
        return new W3CWsaServerTube(this, cloner);
    }

    @Override
    protected void checkMandatoryHeaders(
            Packet packet, boolean foundAction, boolean foundTo, boolean foundReplyTo,
            boolean foundFaultTo, boolean foundMessageId, boolean foundRelatesTo) {
        super.checkMandatoryHeaders(packet, foundAction, foundTo, foundReplyTo,
                foundFaultTo, foundMessageId, foundRelatesTo);

        // find Req/Response or Oneway using WSDLModel(if it is availabe)
        WSDLBoundOperation wbo = getWSDLBoundOperation(packet);
        // Taking care of protocol messages as they do not have any corresponding operations
        if (wbo != null) {
            // if two-way and no wsa:MessageID is found
            if (!wbo.getOperation().isOneWay() && !foundMessageId) {
                throw new MissingAddressingHeaderException(addressingVersion.messageIDTag,packet);
            }
        }

    }

    @Override
    protected boolean isAnonymousRequired(@Nullable WSDLBoundOperation wbo) {
        return getResponseRequirement(wbo) ==  WSDLBoundOperation.ANONYMOUS.required;
    }

    private WSDLBoundOperation.ANONYMOUS getResponseRequirement(@Nullable WSDLBoundOperation wbo) {
        try {
            if (af.getResponses() == AddressingFeature.Responses.ANONYMOUS) {
                return WSDLBoundOperation.ANONYMOUS.required;
            } else if (af.getResponses() == AddressingFeature.Responses.NON_ANONYMOUS) {
                return WSDLBoundOperation.ANONYMOUS.prohibited;
            }
        } catch (NoSuchMethodError e) {
            //Ignore error, defaut to optional
        }
        //wsaw wsdl binding case will have some value set on wbo
        return wbo != null ? wbo.getAnonymous() : WSDLBoundOperation.ANONYMOUS.optional;
    }

    @Override
    protected void checkAnonymousSemantics(WSDLBoundOperation wbo, WSEndpointReference replyTo, WSEndpointReference faultTo) {
        String replyToValue = null;
        String faultToValue = null;

        if (replyTo != null)
            replyToValue = replyTo.getAddress();

        if (faultTo != null)
            faultToValue = faultTo.getAddress();
        WSDLBoundOperation.ANONYMOUS responseRequirement = getResponseRequirement(wbo);

        switch (responseRequirement) {
            case prohibited:
                if (replyToValue != null && replyToValue.equals(addressingVersion.anonymousUri))
                    throw new InvalidAddressingHeaderException(addressingVersion.replyToTag, ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED);

                if (faultToValue != null && faultToValue.equals(addressingVersion.anonymousUri))
                    throw new InvalidAddressingHeaderException(addressingVersion.faultToTag, ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED);
                break;
            case required:
                if (replyToValue != null && !replyToValue.equals(addressingVersion.anonymousUri))
                    throw new InvalidAddressingHeaderException(addressingVersion.replyToTag, ONLY_ANONYMOUS_ADDRESS_SUPPORTED);

                if (faultToValue != null && !faultToValue.equals(addressingVersion.anonymousUri))
                    throw new InvalidAddressingHeaderException(addressingVersion.faultToTag, ONLY_ANONYMOUS_ADDRESS_SUPPORTED);
                break;
            default:
                // ALL: no check
        }
    }

}
