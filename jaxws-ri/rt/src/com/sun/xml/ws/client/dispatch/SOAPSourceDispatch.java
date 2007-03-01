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

package com.sun.xml.ws.client.dispatch;

import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.client.WSServiceDelegate;
import com.sun.xml.ws.message.source.PayloadSourceMessage;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;


/**
 * The <code>SOAPSourceDispatch</code> class provides support
 * for the dynamic invocation of a service endpoint operation using XML
 * constructs. The <code>javax.xml.ws.Service</code>
 * interface acts as a factory for the creation of <code>SOAPSourceDispatch</code>
 * instances.
 *
 * @author WS Development Team
 * @version 1.0
 * @see RESTSourceDispatch
 */
final class SOAPSourceDispatch extends DispatchImpl<Source> {

    public SOAPSourceDispatch(QName port, Mode mode, WSServiceDelegate owner, Tube pipe, BindingImpl binding, WSEndpointReference epr) {
        super(port, mode, owner, pipe, binding, epr);
        assert !isXMLHttp(binding);
    }

    Source toReturnValue(Packet response) {
        Message msg = response.getMessage();

        switch (mode) {
        case PAYLOAD:
            return msg.readPayloadAsSource();
        case MESSAGE:
            return msg.readEnvelopeAsSource();
        default:
            throw new WebServiceException("Unrecognized dispatch mode");
        }
    }

    @Override
    Packet createPacket(Source msg) {

        final Message message;

        if (msg == null)
            message = Messages.createEmpty(soapVersion);
        else {
            switch (mode) {
            case PAYLOAD:
                message = new PayloadSourceMessage(null, msg, setOutboundAttachments(), soapVersion);
                break;
            case MESSAGE:
                message = Messages.create(msg, soapVersion);
                break;
            default:
                throw new WebServiceException("Unrecognized message mode");
            }
        }

        return new Packet(message);
    }


}
