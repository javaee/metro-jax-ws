/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.xml.ws.addressing.v200408;

import com.sun.istack.NotNull;
import com.sun.xml.ws.addressing.WsaServerTube;
import com.sun.xml.ws.addressing.model.MissingAddressingHeaderException;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.developer.MemberSubmissionAddressing;
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;

/**
 * @author Rama Pulavarthi
 */
public class MemberSubmissionWsaServerTube extends WsaServerTube {
    private final MemberSubmissionAddressing.Validation validation;
    public MemberSubmissionWsaServerTube(WSEndpoint endpoint, @NotNull WSDLPort wsdlPort, WSBinding binding, Tube next) {
        super(endpoint, wsdlPort, binding, next);
        validation = binding.getFeature(MemberSubmissionAddressingFeature.class).getValidation();
    }

    public MemberSubmissionWsaServerTube(MemberSubmissionWsaServerTube that, TubeCloner cloner) {
        super(that, cloner);
        this.validation = that.validation;
    }

    public MemberSubmissionWsaServerTube copy(TubeCloner cloner) {
        return new MemberSubmissionWsaServerTube(this, cloner);
    }

    @Override
    protected void checkMandatoryHeaders(Packet packet, boolean foundAction, boolean foundTo, boolean foundReplyTo,
            boolean foundFaultTo, boolean foundMessageId, boolean foundRelatesTo) {

        super.checkMandatoryHeaders(packet, foundAction, foundTo, foundReplyTo,
                foundFaultTo, foundMessageId, foundRelatesTo);
        
        // if no wsa:To header is found
        if (!foundTo)
            throw new MissingAddressingHeaderException(addressingVersion.toTag);

        //we can find Req/Response or Oneway only with WSDLModel
        if (wsdlPort != null) {
            WSDLBoundOperation wbo = getWSDLBoundOperation(packet);

            // if two-way, must contain wsa:ReplyTo
            // Unlike W3C version, we cannot assume default value as anonymous if not present.
            if (!wbo.getOperation().isOneWay() && !foundReplyTo) {
                throw new MissingAddressingHeaderException(addressingVersion.replyToTag);
            }
        }
        if (!validation.equals(MemberSubmissionAddressing.Validation.LAX)) {
            // wsa:MessageId is required if wsa:ReplyTo is present.
            if ((foundReplyTo || foundFaultTo) && !foundMessageId)
                throw new MissingAddressingHeaderException(addressingVersion.messageIDTag);
        }
    }
}
