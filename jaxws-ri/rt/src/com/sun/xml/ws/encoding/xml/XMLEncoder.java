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
package com.sun.xml.ws.encoding.xml;
import java.nio.ByteBuffer;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.ws.pept.encoding.Encoder;
import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.server.ServerRtException;



/**
 * @author WS Development Team
 */
public class XMLEncoder implements Encoder {

    /*
     * @see com.sun.pept.encoding.Encoder#encodeAndSend(com.sun.pept.ept.MessageInfo)
     */
    public void encodeAndSend(MessageInfo messageInfo) {
        throw new UnsupportedOperationException();
    }

    /*
     * @see com.sun.pept.encoding.Encoder#encode(com.sun.pept.ept.MessageInfo)
     */
    public ByteBuffer encode(MessageInfo messageInfo) {
        throw new UnsupportedOperationException();
    }

    public InternalMessage toInternalMessage(MessageInfo messageInfo) {
        return null;
    }

    public XMLMessage toXMLMessage(InternalMessage internalMessage, MessageInfo messageInfo) {
        return null;
    }

    /*
     * Replace the body in SOAPMessage with the BodyBlock of InternalMessage
     */
    public XMLMessage toXMLMessage(InternalMessage internalMessage,
            XMLMessage xmlMessage) {
        try {
            BodyBlock bodyBlock = internalMessage.getBody();
            Object value = bodyBlock.getValue();
            if (value == null) {
                return xmlMessage;
            }
            if (value instanceof XMLMessage) {
                return (XMLMessage)value;
            } else {
                throw new UnsupportedOperationException("Unknown object in BodyBlock:"+value.getClass());
            }
        } catch(Exception e) {
            throw new ServerRtException("xmlencoder.err", new Object[]{e});
        }
    }

}
