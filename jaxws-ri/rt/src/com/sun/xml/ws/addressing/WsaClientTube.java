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

package com.sun.xml.ws.addressing;

import com.sun.istack.NotNull;
import com.sun.xml.ws.addressing.model.ActionNotSupportedException;
import com.sun.xml.ws.addressing.model.MapRequiredException;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.resources.AddressingMessages;

import javax.xml.ws.WebServiceException;

/**
 * WsaClientTube appears in the Tubeline only if addressing is enabled.
 * This tube checks the validity of addressing headers in the incoming messages
 * based on the WSDL model.
 *
 * @author Arun Gupta
 */
public final class WsaClientTube extends WsaTube {
    public WsaClientTube(WSDLPort wsdlPort, WSBinding binding, Tube next) {
        super(wsdlPort, binding, next);
    }

    public WsaClientTube(WsaClientTube that, TubeCloner cloner) {
        super(that, cloner);
    }

    public WsaClientTube copy(TubeCloner cloner) {
        return new WsaClientTube(this, cloner);
    }

    public @NotNull NextAction processRequest(Packet request) {
        return doInvoke(next,request);
   }

    public @NotNull NextAction processResponse(Packet response) {
        // if one-way then, no validation
        if (response.getMessage() != null) {
            response = validateInboundHeaders(response);
            response.addSatellite(new WsaPropertyBag(addressingVersion,soapVersion,response));
        }

        return doReturnWith(response);
    }


    @Override
    public void validateAction(Packet packet) {
        //There may not be a WSDL operation.  There may not even be a WSDL.
        //For instance this may be a RM CreateSequence message.
        WSDLBoundOperation wbo = getWSDLBoundOperation(packet);

        if (wbo == null)    return;

        String gotA = packet.getMessage().getHeaders().getAction(addressingVersion, soapVersion);
        if (gotA == null)
            throw new WebServiceException(AddressingMessages.VALIDATION_CLIENT_NULL_ACTION());

        String expected = helper.getOutputAction(packet);

        if (expected != null && !gotA.equals(expected))
            throw new ActionNotSupportedException(gotA);
    }

    @Override
    protected void checkMandatoryHeaders(Packet packet, boolean foundAction, boolean foundTo, boolean foundMessageID, boolean foundRelatesTo) {
        super.checkMandatoryHeaders(packet, foundAction, foundTo, foundMessageID, foundRelatesTo);
        
//        if(!foundRelatesTo)
//            // RelatesTo required as per
//            // Table 5-3 of http://www.w3.org/TR/2006/WD-ws-addr-wsdl-20060216/#wsdl11requestresponse
//            throw new MapRequiredException(addressingVersion.relatesToTag);

    }
}
