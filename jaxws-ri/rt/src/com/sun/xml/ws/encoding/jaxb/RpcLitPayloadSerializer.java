/*
 * $Id: RpcLitPayloadSerializer.java,v 1.1 2005-05-23 22:28:41 bbissett Exp $
 */
/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved. SUN
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding.jaxb;

import com.sun.xml.bind.api.BridgeContext;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;

import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.SerializationException;
import com.sun.xml.ws.streaming.XMLReader;
import com.sun.xml.ws.streaming.XMLWriter;
import com.sun.xml.ws.util.exception.JAXRPCExceptionBase;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

public class RpcLitPayloadSerializer {
    
    public static RpcLitPayload deserialize(RpcLitPayload payload, XMLReader reader,
            JAXBContext context) {
        try {
            List<RpcLitParameter> params = payload.getParameters();
            QName operation = reader.getName(); // Operation name
            // get down to part accessor
            if(params.size() > 0)                
                reader.nextElementContent();
            for (RpcLitParameter parameter: params) {
                //throw exception if the part accessor name is not what we expect
                QName partName = reader.getName();
                if(!partName.equals(parameter.getName()))
                    throw new DeserializationException("xsd.unexpectedElementName", new Object[]{parameter.getName(), partName});
                
                Class type = parameter.getType();                
                Object value = JAXBTypeSerializer.getInstance().deserialize(type,
                        reader, context);
                parameter.setValue(value);
            }
            reader.nextElementContent();
            
            //to take care of empty operation, e.g. <ans:Operation></ans:operation>
            if(reader.getState() == XMLReader.END && reader.getName().equals(operation))
            	reader.nextElementContent();
            
            // reader could be left on CHARS token rather than </body>
            if (reader.getState() == XMLReader.CHARS &&
                    reader.getValue().trim().length() == 0) {
                reader.nextContent();
            }
            return payload;
        } catch (DeserializationException e) {
            throw e;
        } catch (JAXRPCExceptionBase e) {
            throw new DeserializationException(e);
        } catch (Exception e) {
            throw new DeserializationException(new LocalizableExceptionAdapter(
                    e));
        }
    }
    
    /*
     * Uses BridgeContext to serialize the rpc/lit payload. First it writes
     * the operation, and then it serializes each parameter
     */
    public static void serialize(RpcLitPayload obj, BridgeContext bridgeContext,
        XMLWriter writer) {
        QName op = obj.getOperation();
        writer.startElement(op.getLocalPart(), op.getNamespaceURI(), "ans");
        for (JAXBBridgeInfo param : obj.getBridgeParameters()) {
            JAXBTypeSerializer.getInstance().serialize(param, bridgeContext,
                writer);
        }
        writer.endElement();            // </ans:operation>
    }
    
    /*
     * Uses BridgeContext to deserialize the rpc/lit payload. First it reads
     * the operation element, and then it deserializes each parameter. If the
     * expected parameter is not found, it throws an exception
     */
    public static void deserialize(XMLReader reader, RpcLitPayload payload,
        BridgeContext bridgeContext) {

        reader.nextElementContent();            // </operation> or <partName>
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
            if (reader.getState() == XMLReader.CHARS &&
                    reader.getValue().trim().length() == 0) {
                reader.nextContent();
            }
        }
        reader.nextElementContent();            // </env:body>
    }
}
