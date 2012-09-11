/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.db.sdo;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XMLDocument;
import org.eclipse.persistence.sdo.SDOConstants;
import org.eclipse.persistence.sdo.helper.SDODataHelper;
import org.eclipse.persistence.sdo.helper.SDOXMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.XMLBridge;

import javax.xml.bind.JAXBException;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: May 15, 2009
 * Time: 3:44:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class SDOBond<T>  implements XMLBridge<T> {

    private static final String CLASSNAME = SDOBond.class.getName();

    private static final Logger logger = Logger.getLogger( CLASSNAME );

    private TypeInfo ti;
    private QName xmlTag = null;
    private Class<T> javaType = null;
    private Type theType = null;
    private SDOContextWrapper parent;
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    TransformerFactory tf = TransformerFactory.newInstance();
    
    public SDOBond(SDOContextWrapper parent, TypeInfo ti) {
        this.parent = parent;
        this.ti = ti;
        this.javaType = (Class<T>) ti.type;
        this.xmlTag = ti.tagName;
        HelperContext context = parent.getHelperContext();
        this.theType = context.getTypeHelper().getType(javaType);      
    }
    
//    public SDOBond(Class<T> type, QName xml) {
//        logger.entering("SDOBond", "constructor");
//        javaType = type;
//        xmlTag = xml;
//        HelperContext context = parent.getHelperContext();
//        this.theType = context.getTypeHelper().getType(javaType);      
//    }

    public QName getXmlTag() {
        return xmlTag;
    }

    public Class<T> getJavaType() {
        return javaType;
    }

    private T deserialize(Source src, javax.xml.bind.attachment.AttachmentUnmarshaller au) {
        try {
            if (!commonj.sdo.DataObject.class.isAssignableFrom(javaType) && !javaType.isInterface()) {
                return (T) deserializePrimitives(src);
            }            
            HelperContext context = parent.getHelperContext();
            SDOAttachmentUnmarshaller unmarshaller = null;
            if (au != null) {
                unmarshaller = new SDOAttachmentUnmarshaller(au);
            }

            DataFactory dataFactory = context.getDataFactory();
            DataObject loadOptions = dataFactory.create(SDOConstants.ORACLE_SDO_URL, SDOConstants.XMLHELPER_LOAD_OPTIONS);
            //bug 8680450
            loadOptions.set(SDOConstants.TYPE_LOAD_OPTION, theType);
            if (unmarshaller != null) {
                loadOptions.set(SDOConstants.ATTACHMENT_UNMARSHALLER_OPTION, unmarshaller);
            }
            XMLDocument xdoc = context.getXMLHelper().load(src, null, loadOptions);
            DataObject obj = xdoc.getRootObject();
            Object o = SDOUtils.unwrapPrimitives(obj);
            return (T) o;  // ClassCast possible without check
        }
        catch (Exception e) {
            throw new SDODatabindingException(e);
        }
    }

    private Object deserializePrimitives(Source src) throws Exception {
        if (javaType == null) {
            return null;
        }
        String value = null;
        if (src instanceof StAXSource) {
            StAXSource staxSrc = (StAXSource)src;
            XMLStreamReader xr = staxSrc.getXMLStreamReader();
            if(xr.isStartElement()) {
                xr.next();
            }
            StringBuilder sb = new StringBuilder(); 
            while(xr.isCharacters()) {
                sb.append(xr.getText());
                xr.next();
            }
            value = sb.toString().trim();
        } else {
            DOMResult result = new DOMResult();
            Transformer t = tf.newTransformer();
            t.transform(src, result); 
            value = ((Document)result.getNode()).getDocumentElement().getTextContent().trim(); //xmlElement.getTextContent().trim();             
        }
        if (value == null) {
            return null;
        }
        Object o = null;
        try {
            o = ((SDODataHelper) parent.getHelperContext().getDataHelper()).convertFromStringValue(value, theType);
        } catch (Exception e) {
            // content class does not accept null or empty value, such as BigDecimal, Integer etc
            // these type of empty value will cause toplink data helper to fail, workaround to prevent such failures
            if (value.length() == 0) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Deserialized primitive part has 0 length text, result is null");
                }
                return null;
            }            
        }
        
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Deserialized primitive part {0}", o);
        }
        return o;
    }

    private String serializePrimitive(Object obj, Class<?> contentClass) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Primitive class to be serialized ==> {0}", contentClass);
        }    
        HelperContext context = parent.getHelperContext();
        Type type = context.getTypeHelper().getType(contentClass);
        if (type != null) {
            return ((SDODataHelper) context.getDataHelper()).convertToStringValue(obj, type);
        }
        
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Invalid SDO primitive type: {0}", contentClass.getClass().getName());
        }
        throw new SDODatabindingException("Invalid SDO primitive type: "
                + contentClass.getClass().getName());
    }
    
    private void serializeToResult(String value, Result result) {
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element elt = doc.createElementNS(xmlTag.getNamespaceURI(), "ns1:"
                    + xmlTag.getLocalPart());
            doc.appendChild(elt);
            elt.appendChild(doc.createTextNode(value));
            DOMSource ds = new DOMSource(elt);
            Transformer t = tf.newTransformer();
            t.transform(ds, result);
        } catch (Exception e) {
            throw new SDODatabindingException(e.getMessage());
        }
    }

    @Override
    public BindingContext context() {
        return parent;
    }
    
    private void serializeDataObject(DataObject java, Result result,
            javax.xml.bind.attachment.AttachmentMarshaller am) {
        logger.entering(CLASSNAME, "serializeDataObject");
        try {
            HelperContext context = parent.getHelperContext();
            SDOAttachmentMarshaller marshaller = null;
            if (am != null) {
                marshaller = new SDOAttachmentMarshaller(am);
            }

            // check Primitives for T
            SDOXMLHelper sdoXMLHelper = (SDOXMLHelper) context.getXMLHelper();
            
            // Bug 8909750 - Toplink already sets this to "GMT".  ADF
            // resets it before we get here, so don't change it again.
            //sdoXMLHelper.setTimeZone(TimeZone.getTimeZone("GMT"));
            sdoXMLHelper.setTimeZoneQualified(true);

            XMLDocument xmlDoc = sdoXMLHelper.createDocument((DataObject) java, xmlTag.getNamespaceURI(), xmlTag.getLocalPart());
            if (xmlDoc == null) {
                return;
            }
            xmlDoc.setXMLDeclaration(false);
            DataObject saveOptions = null;
            if (marshaller != null) {
                DataFactory dataFactory = parent.getHelperContext().getDataFactory();
                saveOptions = dataFactory.create(SDOConstants.ORACLE_SDO_URL,
                    SDOConstants.XMLHELPER_LOAD_OPTIONS);
                saveOptions.set(SDOConstants.ATTACHMENT_MARSHALLER_OPTION, marshaller);
            }
            sdoXMLHelper.save(xmlDoc, result, saveOptions);

        } catch (Exception e) {
            throw new SDODatabindingException(e);
        }
    }

    @Override
    public void marshal(T object, XMLStreamWriter output,
            AttachmentMarshaller am) throws JAXBException {
        /*  Didn't work due to bug 8539542
        StAXResult result = new StAXResult(writer);
        sdoXMLHelper.save(xmlDoc, result, null);
        */
        try {
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SAX2StaxContentHandler handler = new SAX2StaxContentHandler(output);
        if (object instanceof DataObject) {
            serializeDataObject((DataObject) object, new SAXResult(handler),
                    am);
            return;
        }

       try {
            String value = serializePrimitive(object, javaType);
            String prefix = output.getPrefix(xmlTag.getNamespaceURI());
            //TODO, this is a hack, seems to be wrong. why should namespace returned  is ""?
            if (xmlTag.getNamespaceURI().equals("")) {
                output.writeStartElement("", xmlTag.getLocalPart(), xmlTag.getNamespaceURI());
//            } else if (prefix == null) {
//                output.writeStartElement(xmlTag.getNamespaceURI(), xmlTag.getLocalPart());
            } else {
                output.writeStartElement(prefix, xmlTag.getLocalPart(), xmlTag.getNamespaceURI());
                output.writeNamespace(prefix, xmlTag.getNamespaceURI());
            }
            output.writeCharacters(value);
            output.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SDODatabindingException(e);
        }
    }

    @Override
    public void marshal(T object, OutputStream output,
            NamespaceContext nsContext, AttachmentMarshaller am)
            throws JAXBException {
        if (object instanceof DataObject) {
            serializeDataObject((DataObject) object, new StreamResult(output),
                    am);
            return;
        }
        
        try {
            String value = serializePrimitive(object, javaType);
            String prefix = nsContext.getPrefix(xmlTag.getNamespaceURI());
            StringBuilder sb = new StringBuilder();
            if ("".equals(prefix)) {
                sb.append("<").append(xmlTag.getLocalPart()).append(">");
                sb.append(value);
                sb.append("</").append(xmlTag.getLocalPart()).append(">");
            } else if (prefix != null) {
                sb.append("<").append(prefix).append(":")
                        .append(xmlTag.getLocalPart()).append(">");
                sb.append(value);
                sb.append("</").append(prefix).append(":")
                        .append(xmlTag.getLocalPart()).append(">");
            } else {
                // Unbound namespace!
                sb.append("<ns1:").append(xmlTag.getLocalPart())
                        .append(" xmlns:ns1=\"")
                        .append(xmlTag.getNamespaceURI()).append("\"")
                        .append(">");
                sb.append(value);
                sb.append("</ns1:").append(xmlTag.getLocalPart()).append(">");
            }
        } catch (Exception e) {
            throw new SDODatabindingException(e);
        }
    }

    @Override
    public void marshal(T object, Node output) throws JAXBException {
        Result res = new DOMResult(output);
        if (object instanceof DataObject) {
            serializeDataObject((DataObject) object, res, null);
            return;
        }
        String value = serializePrimitive(object, javaType);
        serializeToResult(value, res);
    }

    @Override
    public void marshal(T object, ContentHandler contentHandler,
            AttachmentMarshaller am) throws JAXBException {
        Result res = new SAXResult(contentHandler);
        if (object instanceof DataObject) {
            serializeDataObject((DataObject) object, res, null);
            return;
        }

        String value = serializePrimitive(object, javaType);
        serializeToResult(value, res);
    }

    @Override
    public void marshal(T object, Result result) throws JAXBException {
        if (object instanceof DataObject) {
            serializeDataObject((DataObject) object, result, null);
            return;
        }

        String value = serializePrimitive(object, javaType);
        serializeToResult(value, result);
    }

    @Override
    public T unmarshal(XMLStreamReader in, AttachmentUnmarshaller au)
            throws JAXBException {
        return deserialize(new StAXSource(in), au);
    }

    @Override
    public T unmarshal(Source in, AttachmentUnmarshaller au)
            throws JAXBException {
        return deserialize(in, au);
    }

    @Override
    public T unmarshal(InputStream in) throws JAXBException {
        return deserialize(new StreamSource(in), null);
    }

    @Override
    public T unmarshal(Node n, AttachmentUnmarshaller au) throws JAXBException {
        return deserialize(new DOMSource(n), au);
    }

    @Override
    public TypeInfo getTypeInfo() {
        return ti;
    }

    @Override
    public boolean supportOutputStream() {
        return true;
    }
    
}
