/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.db.xmlbeans;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.WebServiceException;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlBase64Binary;
import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlByte;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlDecimal;
import org.apache.xmlbeans.XmlDouble;
import org.apache.xmlbeans.XmlFloat;
import org.apache.xmlbeans.XmlInt;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlLong;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.XmlShort;
import org.apache.xmlbeans.XmlString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.ws.util.xml.XmlUtil;

/**
 * XMLBeanBridge
 * 
 * @author shih-chang.chen@oracle.com
 */
public class XMLBeanBridge<T> implements XMLBridge<T> {
    private static final Logger logger = Logger.getLogger(XMLBeanBridge.class.getName());
    private TypeInfo typeInfo;
    private QName xmlTag = null;
    private Class<T> javaType = null;
    private XMLBeansContext parent;
    private SchemaType schemaType;
    TransformerFactory tf = XmlUtil.newTransformerFactory();
    DocumentBuilderFactory dbf = XmlUtil.newDocumentBuilderFactory();
    private XmlOptions xmlOptions;
    Method parseStax = null;

    public XMLBeanBridge(XMLBeansContext parent, TypeInfo ti) {
        this.parent = parent;
        this.typeInfo = ti;
        this.javaType = (Class<T>) ti.type;
        this.xmlTag = ti.tagName;
        schemaType = parent.schemaType(javaType);
        if (schemaType == null ) throw new WebServiceException(ti.type + " is not supported in XMLBeans");  
        Class xJavaClass = schemaType.getJavaClass();

        xmlOptions = new XmlOptions();
        if (!schemaType.isDocumentType()) xmlOptions.setSaveSyntheticDocumentElement(xmlTag);
        boolean isEnclosingClass = false;
        Class enclosingClass = xJavaClass.getEnclosingClass();
        if (enclosingClass != null) {
            xJavaClass = enclosingClass;
            isEnclosingClass = true;
        }
        for (Class<?> c : xJavaClass.getDeclaredClasses()) {
            if ("Factory".equals(c.getSimpleName())) {
                try {
                    if (schemaType != null && !schemaType.isDocumentType() && !isEnclosingClass) {
                        xmlOptions.setLoadReplaceDocumentElement(null);
                    }
                    parseStax = c.getMethod("parse", XMLStreamReader.class, XmlOptions.class);        
                    break;        
                } catch (Exception e) {
                }
            }
        }
    }

    public QName getXmlTag() {
        return xmlTag;
    }
    
    void setXmlTag(QName name) {
        xmlTag = name;
        if (!schemaType.isDocumentType()) xmlOptions.setSaveSyntheticDocumentElement(xmlTag);
    }

    public Class<T> getJavaType() {
        return javaType;
    }
    
    private void serializeToResult(String value, Result result) {
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element elt = doc.createElementNS(xmlTag.getNamespaceURI(), "ns1:" + xmlTag.getLocalPart());
            doc.appendChild(elt);
            elt.appendChild(doc.createTextNode(value));
            DOMSource ds = new DOMSource(elt);
            Transformer t = tf.newTransformer();
            t.transform(ds, result);
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    @Override
    public BindingContext context() {
        return parent;
    }

    XmlObject xmlObject(Object o) {
        return (XmlObject)((o instanceof XmlObject)? o : schemaType.newValue(o));
    }
    
    @Override
    public void marshal(T object, XMLStreamWriter output, AttachmentMarshaller am) throws JAXBException {
        SAX2StaxContentHandler handler = new SAX2StaxContentHandler(output, false);
        XmlObject xo = xmlObject(object);
        try {
            xo.save(handler, handler, xmlOptions); 
        } catch (SAXException e) {
            throw new JAXBException(e);
        }
    }

    @Override
    public void marshal(T object, OutputStream output, NamespaceContext nsContext, AttachmentMarshaller am) throws JAXBException {
        XmlObject xo = xmlObject(object);
        try {
            xo.save(output, xmlOptions);
        } catch (IOException e) {
            throw new JAXBException(e);
        }
    }

    @Override
    public void marshal(T object, Node output) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void marshal(T object, ContentHandler contentHandler, AttachmentMarshaller am) throws JAXBException {
        XmlObject xo = xmlObject(object);
        try {
            xo.save(contentHandler, null, xmlOptions);
        } catch (SAXException e) {
            throw new JAXBException(e);
        }
    }

    @Override
    public void marshal(T object, Result result) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public T unmarshal(XMLStreamReader reader, AttachmentUnmarshaller au) throws JAXBException {
        try {
            Object o =  parseStax.invoke(null, reader, xmlOptions);
            return (T)convert(o);
        } catch (Exception e) {
            throw new WebServiceException(e);
        } 
    }
    
    private Object convert(Object o) {
        if (o instanceof XmlAnySimpleType) {
            if (o instanceof XmlBoolean) return ((XmlBoolean)o).getBooleanValue();
            if (o instanceof XmlByte)    return ((XmlByte)o).getByteValue();
            if (o instanceof XmlInt)     return ((XmlInt)o).getIntValue();
            if (o instanceof XmlDouble)  return ((XmlDouble)o).getDoubleValue();
            if (o instanceof XmlFloat)   return ((XmlFloat)o).getFloatValue();
            if (o instanceof XmlLong)    return ((XmlLong)o).getLongValue();
            if (o instanceof XmlShort)   return ((XmlShort)o).getShortValue();
            if (o instanceof XmlString)  return ((XmlString)o).getStringValue();
            if (o instanceof XmlInteger) return ((XmlInteger)o).getBigIntegerValue();
            if (o instanceof XmlDecimal) return ((XmlDecimal)o).getBigDecimalValue();            
            if (o instanceof XmlQName)   return ((XmlQName)o).getQNameValue();
            if (o instanceof XmlDateTime)return ((XmlDateTime)o).getCalendarValue();
            if (o instanceof XmlBase64Binary)  return ((XmlBase64Binary)o).getByteArrayValue();
        } 
        return o;
    }

    @Override
    public T unmarshal(Source in, AttachmentUnmarshaller au) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public T unmarshal(InputStream in) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public T unmarshal(Node n, AttachmentUnmarshaller au) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    @Override
    public boolean supportOutputStream() {
        return false;
    }
}
