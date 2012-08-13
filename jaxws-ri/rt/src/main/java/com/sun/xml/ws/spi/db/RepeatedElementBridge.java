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
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.spi.db;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * RepeatedElementBridge
 * 
 * @author shih-chang.chen@oracle.com
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RepeatedElementBridge<T> implements XMLBridge<T> {
    
    XMLBridge<T> delegate;    
    CollectionHandler collectionHandler;

    public RepeatedElementBridge(TypeInfo typeInfo, XMLBridge xb) { 
        delegate = xb;
        collectionHandler = create(typeInfo);
    }
    
    public CollectionHandler collectionHandler() {
        return collectionHandler;
    }

    public BindingContext context() {
        return delegate.context();
    }

    public void marshal(T object, XMLStreamWriter output, AttachmentMarshaller am) throws JAXBException {
        delegate.marshal(object, output, am);
    }

    public void marshal(T object, OutputStream output, NamespaceContext nsContext, AttachmentMarshaller am) throws JAXBException {
        delegate.marshal(object, output, nsContext, am);
    }

    public void marshal(T object, Node output) throws JAXBException {
        delegate.marshal(object, output);
    }

    public void marshal(T object, ContentHandler contentHandler, AttachmentMarshaller am) throws JAXBException {
        delegate.marshal(object, contentHandler, am);
    }

    public void marshal(T object, Result result) throws JAXBException {
        delegate.marshal(object, result);
    }

    public T unmarshal(XMLStreamReader in, AttachmentUnmarshaller au) throws JAXBException {
        return delegate.unmarshal(in, au);
    }

    public T unmarshal(Source in, AttachmentUnmarshaller au) throws JAXBException {
        return delegate.unmarshal(in, au);
    }

    public T unmarshal(InputStream in) throws JAXBException {
        return delegate.unmarshal(in);
    }

    public T unmarshal(Node n, AttachmentUnmarshaller au) throws JAXBException {
        return delegate.unmarshal(n, au);
    }

    public TypeInfo getTypeInfo() {
        return delegate.getTypeInfo();
    }

    public boolean supportOutputStream() {
        return delegate.supportOutputStream();
    }
    
    static public interface CollectionHandler {
        int getSize(Object c);
        Iterator iterator(Object c);
        Object convert(List list);
    }  
    
    static class BaseCollectionHandler implements CollectionHandler {
        Class type;
        BaseCollectionHandler(Class c) {type = c;}
        public int getSize(Object c) { return ((Collection) c).size(); }
        public Object convert(List list) {
            try {
                Object o = type.newInstance();
                ((Collection)o).addAll(list);
                return o;
            } catch (Exception e) {
                e.printStackTrace();
            } 
            return list;
        }
        public Iterator iterator(Object c) {return ((Collection)c).iterator();}   
    }
    
    static final CollectionHandler ListHandler = new BaseCollectionHandler(List.class) {
        public Object convert(List list) {return list;}
    };
  
    static final CollectionHandler HashSetHandler = new BaseCollectionHandler(HashSet.class) {
        public Object convert(List list) { return new HashSet(list);}
    };
    
    static public CollectionHandler create(TypeInfo ti) {
        Class javaClass = (Class) ti.type;
        if (javaClass.isArray()) {
            return new ArrayHandler((Class) ti.getItemType().type);
        } else if (List.class.equals(javaClass) || Collection.class.equals(javaClass)) {
            return ListHandler;
        } else if (Set.class.equals(javaClass) || HashSet.class.equals(javaClass)) {
            return HashSetHandler;
        } else {
            return new BaseCollectionHandler(javaClass);
        }
    }
    
    static class ArrayHandler implements CollectionHandler {
        Class componentClass;            
        public ArrayHandler(Class component) {
            componentClass = component;
        }            
        public int getSize(Object c) {
            return java.lang.reflect.Array.getLength(c);
        }            
        public Object convert(List list) {
            Object array = java.lang.reflect.Array.newInstance(componentClass, list.size());
            for (int i = 0; i < list.size(); i++) {
                java.lang.reflect.Array.set(array, i, list.get(i));
            }
            return array;
        }            
        public Iterator iterator(final Object c) {
            return new Iterator() {
                int index = 0;
                public boolean hasNext() {
                    if (c == null || java.lang.reflect.Array.getLength(c) ==0) return false;
                    return (index != java.lang.reflect.Array.getLength(c));
                }   
                public Object next() {
                    return java.lang.reflect.Array.get(c, index++);
                }
                public void remove() {}        
            };
        }       
    }    
}
