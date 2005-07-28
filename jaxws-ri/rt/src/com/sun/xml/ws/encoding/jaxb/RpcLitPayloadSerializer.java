/*
 * $Id: RpcLitPayloadSerializer.java,v 1.6 2005-07-28 21:56:54 spericas Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.encoding.jaxb;

import com.sun.xml.bind.api.BridgeContext;
import java.util.List;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import com.sun.pept.ept.MessageInfo;

import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.SerializationException;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

import static com.sun.xml.ws.client.BindingProviderProperties.JAXB_OUTPUTSTREAM;

public class RpcLitPayloadSerializer {
    
    /*
     * Uses BridgeContext to serialize the rpc/lit payload. First it writes
     * the operation, and then it serializes each parameter
     */
    public static void serialize(RpcLitPayload obj, BridgeContext bridgeContext,
        MessageInfo messageInfo, XMLStreamWriter writer) 
    {
        try {
            QName op = obj.getOperation();
            String opURI = op.getNamespaceURI();
            
            writer.writeStartElement("ans", op.getLocalPart(), opURI);
            writer.setPrefix("ans", opURI);
            writer.writeNamespace("ans", opURI);
            

            // Pass output stream directly to JAXB when available
            OutputStream os = (OutputStream) messageInfo.getMetaData(JAXB_OUTPUTSTREAM);
            if (os != null) {
                /*
                 * Make sure that current element is closed before passing the
                 * output stream to JAXB. Using Zephyr, it suffices to write
                 * an empty string (TODO: other StAX impls?).
                 */
                writer.writeCharacters("");

                // Flush output of StAX serializer
                writer.flush();

                // Let JAXB serialize each param to the output stream
                for (JAXBBridgeInfo param : obj.getBridgeParameters()) {                
                    JAXBTypeSerializer.getInstance().serialize(param, bridgeContext, os);
                }
            }
            else {
                // Otherwise, use a StAX writer
                for (JAXBBridgeInfo param : obj.getBridgeParameters()) {                
                    JAXBTypeSerializer.getInstance().serialize(param, bridgeContext,
                        writer);
                }
            }
            
            writer.writeEndElement();            // </ans:operation>
        }
        catch (XMLStreamException e) {
            throw new SerializationException(new LocalizableExceptionAdapter(e));
        }
    }
    
    /*
     * Uses BridgeContext to deserialize the rpc/lit payload. First it reads
     * the operation element, and then it deserializes each parameter. If the
     * expected parameter is not found, it throws an exception
     */
    public static void deserialize(XMLStreamReader reader, RpcLitPayload payload,
        BridgeContext bridgeContext) 
    {
        XMLStreamReaderUtil.nextElementContent(reader);     // </operation> or <partName>
        for (JAXBBridgeInfo param: payload.getBridgeParameters()) {
            // throw exception if the part accessor name is not what we expect
            QName partName = reader.getName();
            if (!partName.equals(param.getName())) {
                throw new DeserializationException("xsd.unexpectedElementName",
                        new Object[]{param.getName(), partName});
            }
            JAXBTypeSerializer.getInstance().deserialize(reader, param,
                bridgeContext);
            
            // reader could be left on CHARS token rather than <partName>
            if (reader.getEventType() == XMLStreamConstants.CHARACTERS &&
                    reader.isWhiteSpace()) {
                XMLStreamReaderUtil.nextContent(reader);
            }
        }
        XMLStreamReaderUtil.nextElementContent(reader);     // </env:body>
    }
}
