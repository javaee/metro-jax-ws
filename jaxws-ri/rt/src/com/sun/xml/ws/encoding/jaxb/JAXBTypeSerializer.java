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
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.SerializationException;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Vivek Pandey
 */
public final class JAXBTypeSerializer {
    private JAXBTypeSerializer() {}    // no instanciation please

    public static void serialize(Object obj, XMLStreamWriter writer, JAXBContext context) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
            marshaller.marshal(obj, writer);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }    
    
    /* for FI, it will be a whole document, not fragment
     * called by setPayload and writeTo methods in XMLMessage class
     */
    public static void serializeDocument(Object obj, XMLStreamWriter writer, JAXBContext context) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(obj, writer);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }    

    public static void serialize(Object obj, OutputStream os, JAXBContext context) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
            marshaller.marshal(obj, os);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
        
    /*
     * Marshalls arbitrary type object with the given tag name
     */
    public static DOMSource serialize(Object bean, JAXBContext context) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
            DOMResult domResult = new DOMResult();
            marshaller.marshal(bean, domResult);
            return new DOMSource(domResult.getNode());
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }
    
    /*
     * @see JAXBTypeSerializerIf#deserialize(XMLStreamReader,JAXBContext)
     */
    public static Object deserialize(XMLStreamReader reader, JAXBContext context) {
        Object obj = null;
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                obj = unmarshaller.unmarshal(reader);
            
            // reader could be left on CHARS token rather than </body>
            if (reader.getEventType() == XMLStreamConstants.CHARACTERS 
                    && reader.isWhiteSpace()) {
                XMLStreamReaderUtil.nextContent(reader);
            }
            return obj;
            
        } catch (DeserializationException e) {
            throw e;
        } catch (Exception e) {
            throw new DeserializationException(e);
        }
    }
        
    /*
     * convert JAXB bean as a Source 
     *
    public static Object toSource(Object obj, JAXBContext context) {
        try {
            // Use ctxt to marshall the object
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
            marshaller.marshal(obj, bos);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            return new StreamSource(bis);
        } catch (JAXBException e) {
            throw new DeserializationException(new LocalizableExceptionAdapter(
                    e));
        }
    }
    */
    
    /*
     * Convert Source object as a JAXB bean
     */
    public static Object deserialize(Source source, JAXBContext context) {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return unmarshaller.unmarshal(source);
        } catch (JAXBException e) {
            throw new DeserializationException(e);
        }
    }

    /*
     * JAXB object is serialized. Note that the BridgeContext is cached per
     * thread, and JAXBBridgeInfo should contain correct BridgeContext for the
     * current thread.
     */
    public static void serialize(JAXBBridgeInfo bridgeInfo, BridgeContext bridgeContext,
        XMLStreamWriter writer) {
        try {
            Bridge bridge = bridgeInfo.getBridge();
            Object value = bridgeInfo.getValue();
            bridge.marshal(bridgeContext, value, writer);
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }
    
    /*
     * JAXB object is serialized. Note that the BridgeContext is cached per
     * thread, and JAXBBridgeInfo should contain correct BridgeContext for the
     * current thread.
     */
    public static void serialize(JAXBBridgeInfo bridgeInfo, BridgeContext bridgeContext,
        OutputStream os, NamespaceContext nsContext) {
        try {
            Bridge bridge = bridgeInfo.getBridge();
            Object value = bridgeInfo.getValue();
            bridge.marshal(bridgeContext, value, os, nsContext);
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }
    
    /*
     * JAXB object is serialized to DOMSource
     */
    public static void serialize(JAXBBridgeInfo bridgeInfo,
        BridgeContext bridgeContext, Node node) {
        try {
            Bridge bridge = bridgeInfo.getBridge();
            Object value = bridgeInfo.getValue();
            bridge.marshal(bridgeContext, value, node);
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }
    
    /*
     * JAXB object is deserialized and is set in JAXBBridgeInfo. Note that
     * the BridgeContext is cached per thread, and JAXBBridgeInfo should contain
     * correct BridgeContext for the current thread.
     */
    public static void deserialize(XMLStreamReader reader, JAXBBridgeInfo bridgeInfo,
        BridgeContext bridgeContext) 
    {
        try {
            Bridge bridge = bridgeInfo.getBridge();
            Object value = bridge.unmarshal(bridgeContext, reader);
            bridgeInfo.setValue(value);
            
            // reader could be left on CHARS token rather than </body>
            if (reader.getEventType() == XMLStreamConstants.CHARACTERS &&
                    reader.isWhiteSpace()) {
                XMLStreamReaderUtil.nextContent(reader);
            }
        } catch (JAXBException e) {
            throw new DeserializationException(e);
        }
    }

    public static void deserialize(Source source, JAXBBridgeInfo bridgeInfo,
        BridgeContext bridgeContext)
    {
        try {
            Bridge bridge = bridgeInfo.getBridge();
            Object value = bridge.unmarshal(bridgeContext, source);
            bridgeInfo.setValue(value);
        } catch (JAXBException e) {
            throw new DeserializationException(e);
        }
    }

    public static void deserialize(InputStream stream, JAXBBridgeInfo bridgeInfo,
        BridgeContext bridgeContext)
    {
        try {
            Bridge bridge = bridgeInfo.getBridge();
            Object value = bridge.unmarshal(bridgeContext, stream);
            bridgeInfo.setValue(value);
        } catch (JAXBException e) {
            throw new DeserializationException(e);
        }
    }
    /*
     * JAXB bean in one context is converted to JAXB bean in another context
     *
    public static Object toNewJAXBBean(Object obj, JAXBContext ctxt, JAXBContext newCtxt) {
        try {
            // Use ctxt to marshall the object
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Marshaller marshaller = ctxt.createMarshaller();
            marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
            marshaller.marshal(obj, bos);
            // Use newCtxt to unmarshall the object
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            Unmarshaller unmarshaller = newCtxt.createUnmarshaller();
            Object newObj = unmarshaller.unmarshal(bis);
            bos.close();
            bis.close();
            return newObj;
        } catch (JAXBException e) {
            throw new DeserializationException(new LocalizableExceptionAdapter(
                    e));
        } catch (IOException e) {
            throw new DeserializationException(new LocalizableExceptionAdapter(
                    e));
        }
    }
    */
    
}
