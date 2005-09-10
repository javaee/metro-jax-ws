/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.ws.encoding.xml;
import javax.xml.stream.XMLStreamReader;

import com.sun.pept.encoding.Decoder;
import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.jaxb.*;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;

import static javax.xml.stream.XMLStreamReader.*;
import javax.xml.soap.SOAPMessage;
import java.util.logging.Logger;



/**
 * @author WS Development Team
 */
public class XMLDecoder implements Decoder {
    
    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".xml.decoder");
    
    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Decoder#decode(com.sun.pept.ept.MessageInfo)
     */
    public void decode(MessageInfo arg0) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Decoder#receieveAndDecode(com.sun.pept.ept.MessageInfo)
     */
    public void receiveAndDecode(MessageInfo arg0) {
        throw new UnsupportedOperationException();
    }

    /**
     * parses and binds body from xmlMessage.
     * @param xmlMessage
     * @param messageInfo
     * @return InternalMessage representation of xmlMessage
     */
    public InternalMessage toInternalMessage(XMLMessage xmlMessage,
                    MessageInfo messageInfo) {
            return null;
    }
    
    /**
     * Parses and binds xmlMessage.
     * @param xmlMessage
     * @param internalMessage
     * @param messageInfo
     *
     */
    public InternalMessage toInternalMessage(XMLMessage xmlMessage,
            InternalMessage internalMessage, MessageInfo messageInfo) {
        return null;
    }

    public SOAPMessage toSOAPMessage(MessageInfo messageInfo) {
        return null;
    }

    public void toMessageInfo(InternalMessage internalMessage, MessageInfo messageInfo) { }

    public void decodeDispatchMethod(XMLStreamReader reader, InternalMessage request, MessageInfo messageInfo) {
    }


    /*
    *
    */
   protected void convertBodyBlock(InternalMessage request, MessageInfo messageInfo) {
       BodyBlock bodyBlock = request.getBody();
       if (bodyBlock != null) {
           Object value = bodyBlock.getValue();
           if (value instanceof JAXBBeanInfo) {
               System.out.println("******* NOT HANDLED JAXBBeanInfo ***********");
           } else if (value instanceof XMLMessage) {
               XMLMessage xmlMessage = (XMLMessage)value;
               //XMLStreamReader reader = SourceReaderFactory.createSourceReader(source, true);
               //XMLStreamReaderUtil.nextElementContent(reader);
               //decodeBodyContent(reader, request, messageInfo);
           } else {
               System.out.println("****** Unknown type in BodyBlock ***** "+value.getClass());
           }
       }
   }

}
