/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.message;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;

import com.sun.istack.NotNull;
import com.sun.xml.ws.addressing.WsaTubeHelper;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.OneWayFeature;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.message.RelatesToHeader;
import com.sun.xml.ws.message.StringHeader;
import com.sun.xml.ws.resources.AddressingMessages;
import com.sun.xml.ws.resources.ClientMessages;

public class AddressingUtils {
    //TODO is MessageHeaders to be implicitly assumed? Or moved to utility class and taken out from interface?
    public static void fillRequestAddressingHeaders(MessageHeaders headers, Packet packet, AddressingVersion av, SOAPVersion sv, boolean oneway, String action) {
        fillRequestAddressingHeaders(headers, packet, av, sv, oneway, action, false);
    }
    public static void fillRequestAddressingHeaders(MessageHeaders headers, Packet packet, AddressingVersion av, SOAPVersion sv, boolean oneway, String action, boolean mustUnderstand) {
        fillCommonAddressingHeaders(headers, packet, av, sv, action, mustUnderstand);

        // wsa:ReplyTo
        // null or "true" is equivalent to request/response MEP
        if (!oneway) {
            WSEndpointReference epr = av.anonymousEpr;
            if (headers.get(av.replyToTag, false) == null) {
              headers.add(epr.createHeader(av.replyToTag));
            }

            // wsa:FaultTo
            if (headers.get(av.faultToTag, false) == null) {
              headers.add(epr.createHeader(av.faultToTag));
            }

            // wsa:MessageID
            if (packet.getMessage().getMessageHeaders().get(av.messageIDTag, false) == null) {
                if (headers.get(av.messageIDTag, false) == null) {
                    Header h = new StringHeader(av.messageIDTag, Message.generateMessageID());
                    headers.add(h);
                }
            }
        }
    }
//  private void fillRequestAddressingHeaders(Packet packet, AddressingVersion av, SOAPVersion sv, OneWayFeature of, boolean oneway, String action);
    public static void fillRequestAddressingHeaders(MessageHeaders headers, WSDLPort wsdlPort, WSBinding binding, Packet packet) {
        if (binding == null) {
            throw new IllegalArgumentException(AddressingMessages.NULL_BINDING());
        }
        
        if (binding.isFeatureEnabled(SuppressAutomaticWSARequestHeadersFeature.class)) {
            return;
        }
        
        //See if WSA headers are already set by the user.
        MessageHeaders hl = packet.getMessage().getMessageHeaders();
        String action = AddressingUtils.getAction(hl, binding.getAddressingVersion(), binding.getSOAPVersion());
        if (action != null) {
            //assume that all the WSA headers are set by the user
            return;
        }
        AddressingVersion addressingVersion = binding.getAddressingVersion();
        //seiModel is passed as null as it is not needed.
        WsaTubeHelper wsaHelper = addressingVersion.getWsaHelper(wsdlPort, null, binding);

        // wsa:Action
        String effectiveInputAction = wsaHelper.getEffectiveInputAction(packet);
        if (effectiveInputAction == null || effectiveInputAction.equals("") && binding.getSOAPVersion() == SOAPVersion.SOAP_11) {
            throw new WebServiceException(ClientMessages.INVALID_SOAP_ACTION());
        }
        boolean oneway = !packet.expectReply;
        if (wsdlPort != null) {
            // if WSDL has <wsaw:Anonymous>prohibited</wsaw:Anonymous>, then throw an error
            // as anonymous ReplyTo MUST NOT be added in that case. BindingProvider need to
            // disable AddressingFeature and MemberSubmissionAddressingFeature and hand-craft
            // the SOAP message with non-anonymous ReplyTo/FaultTo.
            if (!oneway && packet.getMessage() != null && packet.getWSDLOperation() != null) {
                WSDLBoundOperation wbo = wsdlPort.getBinding().get(packet.getWSDLOperation());
                if (wbo != null && wbo.getAnonymous() == WSDLBoundOperation.ANONYMOUS.prohibited) {
                    throw new WebServiceException(AddressingMessages.WSAW_ANONYMOUS_PROHIBITED());
                }
            }
        }
        if (!binding.isFeatureEnabled(OneWayFeature.class)) {
            // standard oneway
            fillRequestAddressingHeaders(headers, packet, addressingVersion, binding.getSOAPVersion(), oneway, effectiveInputAction, AddressingVersion.isRequired(binding));
        } else {
            // custom oneway
            fillRequestAddressingHeaders(headers, packet, addressingVersion, binding.getSOAPVersion(), binding.getFeature(OneWayFeature.class), oneway, effectiveInputAction);
        }
    }
    
    public static String getAction(@NotNull MessageHeaders headers, @NotNull AddressingVersion av, @NotNull SOAPVersion sv) {
        if (av == null) {
            throw new IllegalArgumentException(AddressingMessages.NULL_ADDRESSING_VERSION());
        }

        String action = null;
        Header h = getFirstHeader(headers, av.actionTag, true, sv);
        if (h != null) {
            action = h.getStringContent();
        }

        return action;
    }
    
    public static WSEndpointReference getFaultTo(@NotNull MessageHeaders headers, @NotNull AddressingVersion av, @NotNull SOAPVersion sv) {
        if (av == null) {
            throw new IllegalArgumentException(AddressingMessages.NULL_ADDRESSING_VERSION());
        }

        Header h = getFirstHeader(headers, av.faultToTag, true, sv);
        WSEndpointReference faultTo = null;
        if (h != null) {
            try {
                faultTo = h.readAsEPR(av);
            } catch (XMLStreamException e) {
                throw new WebServiceException(AddressingMessages.FAULT_TO_CANNOT_PARSE(), e);
            }
        }

        return faultTo;
    }
    
    public static String getMessageID(@NotNull MessageHeaders headers, @NotNull AddressingVersion av, @NotNull SOAPVersion sv) {
        if (av == null) {
            throw new IllegalArgumentException(AddressingMessages.NULL_ADDRESSING_VERSION());
        }

        Header h = getFirstHeader(headers, av.messageIDTag, true, sv);
        String messageId = null;
        if (h != null) {
            messageId = h.getStringContent();
        }

        return messageId;
    }
    public static String getRelatesTo(@NotNull MessageHeaders headers, @NotNull AddressingVersion av, @NotNull SOAPVersion sv) {
        if (av == null) {
            throw new IllegalArgumentException(AddressingMessages.NULL_ADDRESSING_VERSION());
        }

        Header h = getFirstHeader(headers, av.relatesToTag, true, sv);
        String relatesTo = null;
        if (h != null) {
            relatesTo = h.getStringContent();
        }

        return relatesTo;
    }
    public static WSEndpointReference getReplyTo(@NotNull MessageHeaders headers, @NotNull AddressingVersion av, @NotNull SOAPVersion sv) {
        if (av == null) {
            throw new IllegalArgumentException(AddressingMessages.NULL_ADDRESSING_VERSION());
        }

        Header h = getFirstHeader(headers, av.replyToTag, true, sv);
        WSEndpointReference replyTo;
        if (h != null) {
            try {
                replyTo = h.readAsEPR(av);
            } catch (XMLStreamException e) {
                throw new WebServiceException(AddressingMessages.REPLY_TO_CANNOT_PARSE(), e);
            }
        } else {
            replyTo = av.anonymousEpr;
        }

        return replyTo;
    }
    public static String getTo(MessageHeaders headers, AddressingVersion av, SOAPVersion sv) {
        if (av == null) {
            throw new IllegalArgumentException(AddressingMessages.NULL_ADDRESSING_VERSION());
        }

        Header h = getFirstHeader(headers, av.toTag, true, sv);
        String to;
        if (h != null) {
            to = h.getStringContent();
        } else {
            to = av.anonymousUri;
        }

        return to;
    }
    
    public static Header getFirstHeader(MessageHeaders headers, QName name, boolean markUnderstood, SOAPVersion sv) {
        if (sv == null) {
            throw new IllegalArgumentException(AddressingMessages.NULL_SOAP_VERSION());
        }

        Iterator<Header> iter = headers.getHeaders(name.getNamespaceURI(), name.getLocalPart(), markUnderstood);
        while (iter.hasNext()) {
            Header h = iter.next();
            if (h.getRole(sv).equals(sv.implicitRole)) {
                return h;
            }
        }

        return null;
    }
    
    private static void fillRequestAddressingHeaders(@NotNull MessageHeaders headers, @NotNull Packet packet, @NotNull AddressingVersion av, @NotNull SOAPVersion sv, @NotNull OneWayFeature of, boolean oneway, @NotNull String action) {
        if (!oneway&&!of.isUseAsyncWithSyncInvoke() && Boolean.TRUE.equals(packet.isSynchronousMEP)) {
            fillRequestAddressingHeaders(headers, packet, av, sv, oneway, action);
        } else {
            fillCommonAddressingHeaders(headers, packet, av, sv, action, false);
    
            // wsa:ReplyTo
            // wsa:ReplyTo (add it if it doesn't already exist and OnewayFeature
            //              requests a specific ReplyTo)
            if (headers.get(av.replyToTag, false) == null) {
                WSEndpointReference replyToEpr = of.getReplyTo();
                if (replyToEpr != null) {
                    headers.add(replyToEpr.createHeader(av.replyToTag));
                    // add wsa:MessageID only for non-null ReplyTo
                    if (packet.getMessage().getMessageHeaders().get(av.messageIDTag, false) == null) {
                        // if header doesn't exist, method getID creates a new random id
                        String newID = Message.generateMessageID();
                        headers.add(new StringHeader(av.messageIDTag, newID));
                    }
                }
            }

          // wsa:FaultTo
            // wsa:FaultTo (add it if it doesn't already exist and OnewayFeature
            //              requests a specific FaultTo)
            if (headers.get(av.faultToTag, false) == null) {
                WSEndpointReference faultToEpr = of.getFaultTo();
                if (faultToEpr != null) {
                    headers.add(faultToEpr.createHeader(av.faultToTag));
                    // add wsa:MessageID only for non-null FaultTo
                    if (headers.get(av.messageIDTag, false) == null) {
                        headers.add(new StringHeader(av.messageIDTag, Message.generateMessageID()));
                  }
                }
            }

          // wsa:From
            if (of.getFrom() != null) {
                headers.addOrReplace(of.getFrom().createHeader(av.fromTag));
            }
    
            // wsa:RelatesTo
            if (of.getRelatesToID() != null) {
                headers.addOrReplace(new RelatesToHeader(av.relatesToTag, of.getRelatesToID()));
            }
        }
    }
    
    /**
     * Creates wsa:To, wsa:Action and wsa:MessageID header on the client
     *
     * @param packet request packet
     * @param av WS-Addressing version
     * @param sv SOAP version
     * @param action Action Message Addressing Property value
     * @throws IllegalArgumentException if any of the parameters is null.
     */
    private static void fillCommonAddressingHeaders(MessageHeaders headers, Packet packet, @NotNull AddressingVersion av, @NotNull SOAPVersion sv, @NotNull String action, boolean mustUnderstand) {
        if (packet == null) {
            throw new IllegalArgumentException(AddressingMessages.NULL_PACKET());
        }

        if (av == null) {
            throw new IllegalArgumentException(AddressingMessages.NULL_ADDRESSING_VERSION());
        }

        if (sv == null) {
            throw new IllegalArgumentException(AddressingMessages.NULL_SOAP_VERSION());
        }

        if (action == null && !sv.httpBindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
            throw new IllegalArgumentException(AddressingMessages.NULL_ACTION());
        }

        // wsa:To
        if (headers.get(av.toTag, false) == null) {
          StringHeader h = new StringHeader(av.toTag, packet.endpointAddress.toString());
          headers.add(h);
        }

        // wsa:Action
        if (action != null) {
            packet.soapAction = action;
            if (headers.get(av.actionTag, false) == null) {
                //As per WS-I BP 1.2/2.0, if one of the WSA headers is MU, then all WSA headers should be treated as MU.,
                // so just set MU on action header
              StringHeader h = new StringHeader(av.actionTag, action, sv, mustUnderstand);
              headers.add(h);
            }
        }
    }


}
