/*
 * $Id: XMLEncoder.java,v 1.1 2005-07-23 00:21:27 jitu Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.encoding.xml;
import java.nio.ByteBuffer;
import javax.xml.stream.XMLStreamWriter;

import com.sun.pept.encoding.Encoder;
import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.server.ServerRtException;


/**
 * @author WS Development Team
 */
public abstract class XMLEncoder implements Encoder {

    /*
     * @see com.sun.pept.encoding.Encoder#encodeAndSend(com.sun.pept.ept.MessageInfo)
     */
    public abstract void encodeAndSend(MessageInfo messageInfo);

    /*
     * @see com.sun.pept.encoding.Encoder#encode(com.sun.pept.ept.MessageInfo)
     */
    public abstract ByteBuffer encode(MessageInfo messageInfo);

    public InternalMessage toInternalMessage(MessageInfo messageInfo) {
        return null;
    }

    protected void writeJAXBBeanInfo(JAXBBeanInfo beanInfo, XMLStreamWriter writer) {
        JAXBTypeSerializer.getInstance().serialize(
                beanInfo.getBean(), writer, beanInfo.getJAXBContext());
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
