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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.DatabindingException;
import com.sun.xml.ws.spi.db.RepeatedElementBridge;
import com.sun.xml.ws.spi.db.RepeatedElementBridge.CollectionHandler;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.XMLBridge;

/**
 * ArrayWrapperBridge
 * 
 * @author shih-chang.chen@oracle.com
 */
public class ArrayWrapperBridge<T> implements XMLBridge<T> {
    private static final Logger logger = Logger.getLogger(ArrayWrapperBridge.class.getName());
    static final String WrapperPrefix  = "aw";
    static final String WrapperPrefixColon = WrapperPrefix + ":";

    private XMLBeansContext parent;
    private ArrayInfo arrayInfo;
    private XMLBeanBridge itemBridge;
    private CollectionHandler arrayHandler ;
    private TypeInfo typeInfo;
    private QName xmlTag = null;
    private Class<T> javaType = null;
    private XmlOptions xmlOptions;

    public ArrayWrapperBridge(XMLBeansContext parent, TypeInfo ti, ArrayInfo info) {
        this.parent = parent;
        this.typeInfo = ti;
        this.javaType = (Class<T>) typeInfo.type;
        this.xmlTag = typeInfo.tagName;
        arrayInfo = info;
        itemBridge = (XMLBeanBridge)parent.createBridge(arrayInfo.itemTypeInfo);
        itemBridge.setXmlTag(info.itemName);
        arrayHandler = RepeatedElementBridge.create(ti);
    }

    public QName getXmlTag() {
        return xmlTag;
    }

    public Class<T> getJavaType() {
        return javaType;
    }

    @Override
    public BindingContext context() {
        return parent;
    }
    
    @Override
    public void marshal(T object, XMLStreamWriter output, AttachmentMarshaller am) throws JAXBException {
        try {
            String prefix = output.getPrefix(typeInfo.tagName.getNamespaceURI());
            if (prefix == null) prefix = WrapperPrefix;
            output.writeStartElement(prefix, typeInfo.tagName.getLocalPart(), typeInfo.tagName.getNamespaceURI());
            output.writeNamespace(prefix, typeInfo.tagName.getNamespaceURI());
            if (object == null) {
                //TODO nil=1?
            } else {
                for(Iterator i = arrayHandler.iterator(object); i.hasNext();) itemBridge.marshal(i.next(), output, am);
            }  
            output.writeEndElement();          
        } catch (XMLStreamException e) {
            throw new DatabindingException(e);
        }
    }

    @Override
    public void marshal(T object, OutputStream output, NamespaceContext nsContext, AttachmentMarshaller am) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void marshal(T object, Node output) throws JAXBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void marshal(T object, ContentHandler contentHandler, AttachmentMarshaller am) throws JAXBException {
        Attributes att = XMLUtil.emptyAttributes();
        try {
            contentHandler.startPrefixMapping(WrapperPrefix, typeInfo.tagName.getNamespaceURI());
            contentHandler.startElement(typeInfo.tagName.getNamespaceURI(), typeInfo.tagName.getLocalPart(), WrapperPrefixColon + typeInfo.tagName.getLocalPart(), att);
        } catch (SAXException e) {
            throw new JAXBException(e);
        }
        if (object == null) {
            //TODO nil=1?
        } else {
            for(Iterator i = arrayHandler.iterator(object); i.hasNext();) itemBridge.marshal(i.next(), contentHandler, am);
        }  
        try {
            contentHandler.endElement(typeInfo.tagName.getNamespaceURI(), typeInfo.tagName.getLocalPart(), null);
            contentHandler.endPrefixMapping(WrapperPrefix);
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
        XMLUtil.verifyTag(reader,xmlTag);
            reader.nextTag();
            QName name = reader.getName();
            ArrayList list = new ArrayList();
            while (reader.getEventType()==XMLStreamReader.START_ELEMENT && name.equals(reader.getName())) {
                list.add(itemBridge.unmarshal(reader, au));
                XMLUtil.toNextTag(reader, name);
            }        
            return (T)arrayHandler.convert(list);
        } catch (XMLStreamException e) {
            throw new DatabindingException(e);
        }
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
