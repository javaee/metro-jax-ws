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

package com.sun.xml.ws.encoding.jaxb;

import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.bind.api.BridgeContext;
import static com.sun.xml.ws.client.BindingProviderProperties.JAXB_OUTPUTSTREAM;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.SerializationException;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;

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

                NamespaceContext nsc = writer.getNamespaceContext();

                // Let JAXB serialize each param to the output stream
                for (JAXBBridgeInfo param : obj.getBridgeParameters()) {
                    param.serialize(bridgeContext, os, nsc);
                }
            }
            else {
                // Otherwise, use a StAX writer
                for (JAXBBridgeInfo param : obj.getBridgeParameters()) {                
                    param.serialize(bridgeContext, writer);
                }
            }
            
            writer.writeEndElement();            // </ans:operation>
        }
        catch (XMLStreamException e) {
            throw new SerializationException(e);
        }
    }

    public static void serialize(RpcLitPayload obj, BridgeContext bridgeContext, OutputStream writer) {
        QName op = obj.getOperation();
        String opURI = op.getNamespaceURI();
        String startElm = "<ans:"+op.getLocalPart()+" xmlns:ans=\""+opURI+"\">";
        String endElm="</ans:"+op.getLocalPart()+">";
        try {
            writer.write(startElm.getBytes());
            for (JAXBBridgeInfo param : obj.getBridgeParameters()) {
                param.serialize(bridgeContext,writer,null);
            }
            writer.write(endElm.getBytes());
        } catch (IOException e) {
            throw new SerializationException(e);
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
            param.deserialize(reader, bridgeContext);
            
            // reader could be left on CHARS token rather than <partName>
            if (reader.getEventType() == XMLStreamConstants.CHARACTERS &&
                    reader.isWhiteSpace()) {
                XMLStreamReaderUtil.nextContent(reader);
            }
        }
        XMLStreamReaderUtil.nextElementContent(reader);     // </env:body>
    }
}
