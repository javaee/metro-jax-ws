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

package com.sun.xml.ws.client.dispatch;

import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.client.WSServiceDelegate;
import com.sun.xml.ws.message.saaj.SAAJMessage;
import com.sun.xml.ws.resources.DispatchMessages;

import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The <code>SOAPMessageDispatch</code> class provides support
 * for the dynamic invocation of a service endpoint operation using
 * the <code>SOAPMessage</code> class. The <code>javax.xml.ws.Service</code>
 * interface acts as a factory for the creation of <code>SOAPMessageDispatch</code>
 * instances.
 *
 * @author WS Development Team
 * @version 1.0
 */
public class SOAPMessageDispatch extends com.sun.xml.ws.client.dispatch.DispatchImpl<SOAPMessage> {
    public SOAPMessageDispatch(QName port, Service.Mode mode, WSServiceDelegate owner, Tube pipe, BindingImpl binding, WSEndpointReference epr) {
        super(port, mode, owner, pipe, binding, epr);
    }

    Packet createPacket(SOAPMessage arg) {

        if (arg == null && !isXMLHttp(binding))
           throw new WebServiceException(DispatchMessages.INVALID_NULLARG_SOAP_MSGMODE(mode.name(), Service.Mode.PAYLOAD.toString()));

        MimeHeaders mhs = arg.getMimeHeaders();
        // TODO: these two lines seem dangerous. It should be left up to the transport and codec
        // to decide how they are sent. remove after 2.1 FCS - KK.
        mhs.addHeader("Content-Type", "text/xml");
        mhs.addHeader("Content-Transfer-Encoding", "binary");
        Map<String, List<String>> ch = new HashMap<String, List<String>>();
        for (Iterator iter = arg.getMimeHeaders().getAllHeaders(); iter.hasNext();)
        {
            MimeHeader mh = (MimeHeader) iter.next();
            List<String> h = new ArrayList<String>();
            h.add(mh.getValue());
            ch.put(mh.getName(), h);
        }

        Packet packet = new Packet(new SAAJMessage(arg));
        packet.invocationProperties.put(MessageContext.HTTP_REQUEST_HEADERS,ch);
        return packet;
    }

    SOAPMessage toReturnValue(Packet response) {
        try {

            //not sure if this is the correct way to deal with this.
            if ( response ==null || response.getMessage() == null )
                     throw new WebServiceException(DispatchMessages.INVALID_RESPONSE());
            else
                return response.getMessage().readAsSOAPMessage();            
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
    }
}
