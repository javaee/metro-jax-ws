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
package com.sun.xml.ws.encoding.jaxb;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.ws.encoding.soap.SerializationException;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;

import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.transform.Source;
import java.io.OutputStream;
import java.io.InputStream;

import org.w3c.dom.Node;

/**
 * XML infoset represented as a JAXB object and {@link Bridge}.
 *
 * @author WS Development Team
 */
public final class JAXBBridgeInfo {
    private final Bridge bridge;
    private Object value;

    public JAXBBridgeInfo(Bridge bridge) {
        this.bridge = bridge;
    }

    public JAXBBridgeInfo(Bridge bridge, Object value) {
        this(bridge);
        this.value = value;
    }

    public QName getName() {
        return bridge.getTypeReference().tagName;
    }

    public TypeReference getType(){
        return bridge.getTypeReference();
    }

    public Bridge getBridge() {
        return bridge;
    }

    public Object getValue() {
        return value;
    }

    public static JAXBBridgeInfo copy(JAXBBridgeInfo payload) {
        return new JAXBBridgeInfo(payload.getBridge(), payload.getValue());
    }

    /**
     * JAXB object is serialized. Note that the BridgeContext is cached per
     * thread, and JAXBBridgeInfo should contain correct BridgeContext for the
     * current thread.
     */
    public void serialize(BridgeContext bridgeContext, OutputStream os, NamespaceContext nsContext) {
        try {
            bridge.marshal(bridgeContext, value, os, nsContext);
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    /**
     * Serialize to StAX.
     */
    public void serialize(BridgeContext bridgeContext, XMLStreamWriter writer) {
        try {
            bridge.marshal(bridgeContext, value, writer);
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    /**
     * Serialize to DOM.
     */
    public void serialize(BridgeContext bridgeContext, Node node) {
        try {
            bridge.marshal(bridgeContext, value, node);
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    public void deserialize(Source source, BridgeContext bridgeContext) {
        try {
            value = bridge.unmarshal(bridgeContext, source);
        } catch (JAXBException e) {
            throw new DeserializationException(e);
        }
    }

    public void deserialize(InputStream stream, BridgeContext bridgeContext) {
        try {
            value = bridge.unmarshal(bridgeContext, stream);
        } catch (JAXBException e) {
            throw new DeserializationException(e);
        }
    }

    /*
    * JAXB object is deserialized and is set in JAXBBridgeInfo. Note that
    * the BridgeContext is cached per thread, and JAXBBridgeInfo should contain
    * correct BridgeContext for the current thread.
    */
    public void deserialize(XMLStreamReader reader, BridgeContext bridgeContext)  {
        try {
            value = bridge.unmarshal(bridgeContext, reader);

            // reader could be left on CHARS token rather than </body>
            if (reader.getEventType() == XMLStreamConstants.CHARACTERS &&
                    reader.isWhiteSpace()) {
                XMLStreamReaderUtil.nextContent(reader);
            }
        } catch (JAXBException e) {
            throw new DeserializationException(e);
        }
    }
}
