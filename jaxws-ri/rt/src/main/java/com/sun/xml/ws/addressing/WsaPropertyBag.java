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

package com.sun.xml.ws.addressing;

import com.oracle.webservices.api.message.BasePropertySet;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.AddressingUtils;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.developer.JAXWSProperties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;


/**
 * Provides access to the Addressing headers.
 *
 * @author Kohsuke Kawaguchi
 * @author Rama Pulavarthi
 * @since 2.1.3
 */
public class WsaPropertyBag extends BasePropertySet {

    public static final String WSA_REPLYTO_FROM_REQUEST = "com.sun.xml.ws.addressing.WsaPropertyBag.ReplyToFromRequest";
    public static final String WSA_FAULTTO_FROM_REQUEST = "com.sun.xml.ws.addressing.WsaPropertyBag.FaultToFromRequest";
    public static final String WSA_MSGID_FROM_REQUEST = "com.sun.xml.ws.addressing.WsaPropertyBag.MessageIdFromRequest";
    public static final String WSA_TO = "com.sun.xml.ws.addressing.WsaPropertyBag.To";

    private final @NotNull AddressingVersion addressingVersion;
    private final @NotNull SOAPVersion soapVersion;
    /**
     * We can't store {@link Message} here as those may get replaced as
     * the packet travels through the pipeline.
     */
    private final @NotNull Packet packet;

    public WsaPropertyBag(AddressingVersion addressingVersion, SOAPVersion soapVersion, Packet packet) {
        this.addressingVersion = addressingVersion;
        this.soapVersion = soapVersion;
        this.packet = packet;
    }

    /**
     * Gets the {@code wsa:To} header.
     *
     * @return
     *      null if the incoming SOAP message didn't have the header.
     */
    @Property(JAXWSProperties.ADDRESSING_TO)
    public String getTo() throws XMLStreamException {
      if (packet.getMessage() == null) {
        return null;
      }
      Header h = packet.getMessage().getHeaders().get(addressingVersion.toTag, false);
      if(h==null) return null;
      return h.getStringContent();
    }

    /**
     * Gets the {@code wsa:To} header.
     *
     * @return
     *      null if the incoming SOAP message didn't have the header.
     */
    @Property(WSA_TO)
    public WSEndpointReference getToAsReference() throws XMLStreamException {
      if (packet.getMessage() == null) {
        return null;
      }
      Header h = packet.getMessage().getHeaders().get(addressingVersion.toTag, false);
      if(h==null) return null;
      return new WSEndpointReference(h.getStringContent(),addressingVersion);
    }

    /**
     * Gets the {@code wsa:From} header.
     *
     * @return
     *      null if the incoming SOAP message didn't have the header.
     */
    @Property(JAXWSProperties.ADDRESSING_FROM)
    public WSEndpointReference getFrom() throws XMLStreamException {
        return getEPR(addressingVersion.fromTag);
    }

    /**
     * Gets the {@code wsa:Action} header content as String.
     *
     * @return
     *      null if the incoming SOAP message didn't have the header.
     */
    @Property(JAXWSProperties.ADDRESSING_ACTION)
    public String getAction() {
        if (packet.getMessage() == null) {
          return null;
        }
        Header h = packet.getMessage().getHeaders().get(addressingVersion.actionTag, false);
        if(h==null) return null;
        return h.getStringContent();
    }

    /**
     * Gets the {@code wsa:MessageID} header content as String.
     *
     * @return
     *      null if the incoming SOAP message didn't have the header.
     */
    // WsaServerTube.REQUEST_MESSAGE_ID is exposed for backward compatibility with 2.1
    @Property({JAXWSProperties.ADDRESSING_MESSAGEID,WsaServerTube.REQUEST_MESSAGE_ID})
    public String getMessageID() {
        if (packet.getMessage() == null) {
          return null;
        }
        return AddressingUtils.getMessageID(packet.getMessage().getHeaders(), addressingVersion,soapVersion);
    }

    private WSEndpointReference getEPR(QName tag) throws XMLStreamException {
        if (packet.getMessage() == null) {
          return null;
        }
        Header h = packet.getMessage().getHeaders().get(tag, false);
        if(h==null) return null;
        return h.readAsEPR(addressingVersion);
    }

    protected PropertyMap getPropertyMap() {
        return model;
    }

    private static final PropertyMap model;
    static {
        model = parse(WsaPropertyBag.class);
    }

    private WSEndpointReference _replyToFromRequest = null;

    @Property(WSA_REPLYTO_FROM_REQUEST)
    public WSEndpointReference getReplyToFromRequest() {
      return _replyToFromRequest;
    }

    public void setReplyToFromRequest(WSEndpointReference ref) {
      _replyToFromRequest = ref;
    }

    private WSEndpointReference _faultToFromRequest = null;

    @Property(WSA_FAULTTO_FROM_REQUEST)
    public WSEndpointReference getFaultToFromRequest() {
      return _faultToFromRequest;
    }

    public void setFaultToFromRequest(WSEndpointReference ref) {
      _faultToFromRequest = ref;
    }

    private String _msgIdFromRequest = null;

    @Property(WSA_MSGID_FROM_REQUEST)
    public String getMessageIdFromRequest() {
      return _msgIdFromRequest;
    }

    public void setMessageIdFromRequest(String id) {
      _msgIdFromRequest = id;
    }
}
