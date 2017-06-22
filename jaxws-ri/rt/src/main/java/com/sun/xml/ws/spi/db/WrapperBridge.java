/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.spi.db;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.bind.JAXBException;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

//import com.sun.xml.ws.spi.db.BindingContext;
//import com.sun.xml.ws.spi.db.RepeatedElementBridge;
//import com.sun.xml.ws.spi.db.XMLBridge;
//import com.sun.xml.ws.spi.db.DatabindingException;
//import com.sun.xml.ws.spi.db.TypeInfo;
//import com.sun.xml.ws.spi.db.WrapperComposite;

/**
 * WrapperBridge handles RPC-Literal body and Document-Literal wrappers without static 
 * wrapper classes.
 * 
 * @author shih-chang.chen@oracle.com
 */
public class WrapperBridge<T> implements XMLBridge<T> {
    
    BindingContext parent;
    TypeInfo typeInfo;
    static final String WrapperPrefix  = "w";
    static final String WrapperPrefixColon = WrapperPrefix + ":";
    
    public WrapperBridge(BindingContext p, TypeInfo ti) {
        this.parent = p;
        this.typeInfo = ti;
    }
    
    @Override
    public BindingContext context() {
        return parent;
    }

    @Override
    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    @Override
    public final void marshal(T object, ContentHandler contentHandler, AttachmentMarshaller am) throws JAXBException {
        WrapperComposite w = (WrapperComposite) object;
        Attributes att = new Attributes() {
            @Override public int getLength() { return 0; }
            @Override public String getURI(int index) { return null; }
            @Override public String getLocalName(int index)  { return null; }
            @Override public String getQName(int index) { return null; }
            @Override public String getType(int index) { return null; }
            @Override public String getValue(int index)  { return null; }
            @Override public int getIndex(String uri, String localName)  { return 0; }
            @Override public int getIndex(String qName) {  return 0; }
            @Override public String getType(String uri, String localName)  { return null; }
            @Override public String getType(String qName)  { return null; }
            @Override public String getValue(String uri, String localName)  { return null; }
            @Override public String getValue(String qName)  { return null; }
        };
        try {
            contentHandler.startPrefixMapping(WrapperPrefix, typeInfo.tagName.getNamespaceURI());
            contentHandler.startElement(typeInfo.tagName.getNamespaceURI(), typeInfo.tagName.getLocalPart(), WrapperPrefixColon + typeInfo.tagName.getLocalPart(), att);
        } catch (SAXException e) {
            throw new JAXBException(e);
        }
        if (w.bridges != null) for (int i = 0; i < w.bridges.length; i++) {
            if (w.bridges[i] instanceof RepeatedElementBridge) {
                RepeatedElementBridge rbridge = (RepeatedElementBridge) w.bridges[i];
                for (Iterator itr = rbridge.collectionHandler().iterator(w.values[i]); itr.hasNext();) {
                    rbridge.marshal(itr.next(), contentHandler, am);
                }                
            } else {
                w.bridges[i].marshal(w.values[i], contentHandler, am);
            }
        }
        try {
            contentHandler.endElement(typeInfo.tagName.getNamespaceURI(), typeInfo.tagName.getLocalPart(), null);
            contentHandler.endPrefixMapping(WrapperPrefix);
        } catch (SAXException e) {
            throw new JAXBException(e);
        }
//      bridge.marshal(object, contentHandler, am);
    }

    @Override
    public void marshal(T object, Node output) throws JAXBException {
        throw new UnsupportedOperationException();
//      bridge.marshal(object, output);
//      bridge.marshal((T) convert(object), output);
    }

    @Override
    public void marshal(T object, OutputStream output, NamespaceContext nsContext, AttachmentMarshaller am) throws JAXBException {
//      bridge.marshal((T) convert(object), output, nsContext, am);
    }
    
    @Override
    public final void marshal(T object, Result result) throws JAXBException {
        throw new UnsupportedOperationException();
//      bridge.marshal(object, result);
    }

    @Override
    public final void marshal(T object, XMLStreamWriter output, AttachmentMarshaller am) throws JAXBException {
        WrapperComposite w = (WrapperComposite) object;
        try {
//          output.writeStartElement(typeInfo.tagName.getNamespaceURI(), typeInfo.tagName.getLocalPart());
//          System.out.println(typeInfo.tagName.getNamespaceURI());
            
            //The prefix is to workaround an eclipselink bug
            String prefix = output.getPrefix(typeInfo.tagName.getNamespaceURI());
            if (prefix == null) prefix = WrapperPrefix;
            output.writeStartElement(prefix, typeInfo.tagName.getLocalPart(), typeInfo.tagName.getNamespaceURI());
            output.writeNamespace(prefix, typeInfo.tagName.getNamespaceURI());

//          output.writeStartElement("", typeInfo.tagName.getLocalPart(), typeInfo.tagName.getNamespaceURI());
//          output.writeDefaultNamespace(typeInfo.tagName.getNamespaceURI());
//          System.out.println("======== " + output.getPrefix(typeInfo.tagName.getNamespaceURI()));
//          System.out.println("======== " + output.getNamespaceContext().getPrefix(typeInfo.tagName.getNamespaceURI()));
//          System.out.println("======== " + output.getNamespaceContext().getNamespaceURI(""));
        } catch (XMLStreamException e) {
            e.printStackTrace();
            throw new DatabindingException(e);
        }
        if (w.bridges != null) for (int i = 0; i < w.bridges.length; i++) {
            if (w.bridges[i] instanceof RepeatedElementBridge) {
                RepeatedElementBridge rbridge = (RepeatedElementBridge) w.bridges[i];
                for (Iterator itr = rbridge.collectionHandler().iterator(w.values[i]); itr.hasNext();) {
                    rbridge.marshal(itr.next(), output, am);
                }                
            } else {
                w.bridges[i].marshal(w.values[i], output, am);
            }
        }
        try {
            output.writeEndElement();
        } catch (XMLStreamException e) {
            throw new DatabindingException(e);
        }
    }
    
    @Override
    public final T unmarshal(InputStream in) throws JAXBException {
        //EndpointArgumentsBuilder.RpcLit.readRequest
        throw new UnsupportedOperationException();      
//      return bridge.unmarshal(in);
    }

    @Override
    public final T unmarshal(Node n, AttachmentUnmarshaller au) throws JAXBException {
        //EndpointArgumentsBuilder.RpcLit.readRequest
        throw new UnsupportedOperationException();      
//      return bridge.unmarshal(n, au);
    }

    @Override
    public final T unmarshal(Source in, AttachmentUnmarshaller au) throws JAXBException {
        //EndpointArgumentsBuilder.RpcLit.readRequest
        throw new UnsupportedOperationException();      
//      return bridge.unmarshal(in, au);
    }

    @Override
    public final T unmarshal(XMLStreamReader in, AttachmentUnmarshaller au) throws JAXBException {
        //EndpointArgumentsBuilder.RpcLit.readRequest
        throw new UnsupportedOperationException();      
//      return bridge.unmarshal(in, au);
    }

    @Override
    public boolean supportOutputStream() {
        return false;
    }
}
