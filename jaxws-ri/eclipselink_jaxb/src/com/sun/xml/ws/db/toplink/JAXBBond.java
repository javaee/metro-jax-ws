/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.xml.ws.db.toplink;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;

import org.eclipse.persistence.jaxb.JAXBMarshaller;
import org.eclipse.persistence.jaxb.JAXBTypeElement;
import org.eclipse.persistence.jaxb.JAXBUnmarshaller;
import org.eclipse.persistence.jaxb.TypeMappingInfo;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;


import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.ws.spi.db.TypeInfo;

public class JAXBBond<T> implements XMLBridge<T> {
	JAXBContextWrapper parent;
	TypeInfo typeInfo;
	TypeMappingInfo mappingInfo;
	boolean isParameterizedType = false;

	public JAXBBond(JAXBContextWrapper p, TypeInfo ti) {
		this.parent = p;
		this.typeInfo = ti;
		if (parent.infoMap != null)
			mappingInfo = parent.infoMap.get(ti);
		if (mappingInfo != null)
			isParameterizedType = (mappingInfo.getType() instanceof ParameterizedType);
	}

	public BindingContext context() {
		return parent;
	}

	public TypeInfo getTypeInfo() {
		return typeInfo;
	}

	public void marshal(T object, XMLStreamWriter output,
			AttachmentMarshaller am) throws JAXBException {
		JAXBMarshaller marshaller = null;
		try {
			marshaller = parent.mpool.allocate();
			marshaller.setAttachmentMarshaller(am);
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT,
					true);
			if (mappingInfo != null) {
				if (isParameterizedType) {
					JAXBTypeElement jte = new JAXBTypeElement(
							mappingInfo.getXmlTagName(), object,
							(ParameterizedType) mappingInfo.getType());
					marshaller.marshal(jte, output, mappingInfo);
				} else {
					JAXBElement<T> elt = new JAXBElement<T>(
							mappingInfo.getXmlTagName(),
							(Class<T>) typeInfo.type, object);
					marshaller.marshal(elt, output, mappingInfo);
				}
			} else
				marshaller.marshal(object, output);
		} finally {
			if (marshaller != null) {
				marshaller.setAttachmentMarshaller(null);
				parent.mpool.replace(marshaller);
			}
		}
	}

	// TODO NamespaceContext nsContext
	public void marshal(T object, OutputStream output,
			NamespaceContext nsContext, AttachmentMarshaller am)
			throws JAXBException {
		JAXBMarshaller marshaller = null;

		try {
			marshaller = parent.mpool.allocate();
			marshaller.setAttachmentMarshaller(am);
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT,
					true);
			if (mappingInfo != null) {
				if (isParameterizedType) {
					JAXBTypeElement jte = new JAXBTypeElement(
							mappingInfo.getXmlTagName(), object,
							(ParameterizedType) mappingInfo.getType());
					marshaller.marshal(jte, new StreamResult(output),
							mappingInfo);
				} else {
					JAXBElement<T> elt = new JAXBElement<T>(
							mappingInfo.getXmlTagName(),
							(Class<T>) mappingInfo.getType(), object);
					// marshaller.marshal(elt, output);
					// GAG missing
					marshaller.marshal(elt, new StreamResult(output),
							mappingInfo);
				}
			} else
				marshaller.marshal(object, output);
		} finally {
			if (marshaller != null) {
				marshaller.setAttachmentMarshaller(null);
				parent.mpool.replace(marshaller);
			}
		}
	}

	public void marshal(T object, Node output) throws JAXBException {
		JAXBMarshaller marshaller = null;
		try {
			marshaller = parent.mpool.allocate();
			// marshaller.setAttachmentMarshaller(am);
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT,
					true);
			if (mappingInfo != null) {
				if (isParameterizedType) {
					JAXBTypeElement jte = new JAXBTypeElement(
							mappingInfo.getXmlTagName(), object,
							(ParameterizedType) mappingInfo.getType());
					marshaller.marshal(jte, new DOMResult(output), mappingInfo);
				} else {
					JAXBElement<T> elt = new JAXBElement<T>(
							mappingInfo.getXmlTagName(),
							(Class<T>) mappingInfo.getType(), object);
					// marshaller.marshal(elt, output);
					marshaller.marshal(elt, new DOMResult(output), mappingInfo);
				}
			} else
				marshaller.marshal(object, output);
		} finally {
			if (marshaller != null) {
				marshaller.setAttachmentMarshaller(null);
				parent.mpool.replace(marshaller);
			}
		}
	}

	public void marshal(T object, ContentHandler contentHandler,
			AttachmentMarshaller am) throws JAXBException {
		JAXBMarshaller marshaller = null;
		try {
			marshaller = parent.mpool.allocate();
			marshaller.setAttachmentMarshaller(am);
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT,
					true);
			if (mappingInfo != null) {
				if (isParameterizedType) {
					JAXBTypeElement jte = new JAXBTypeElement(
							mappingInfo.getXmlTagName(), object,
							(ParameterizedType) mappingInfo.getType());
					marshaller.marshal(jte, new SAXResult(contentHandler),
							mappingInfo);
				} else {
					JAXBElement<T> elt = new JAXBElement<T>(
							mappingInfo.getXmlTagName(),
							(Class<T>) mappingInfo.getType(), object);
					// marshaller.marshal(elt, contentHandler);

					// GAG missing
					marshaller.marshal(elt, new SAXResult(contentHandler),
							mappingInfo);

					// marshaller.marshal(elt, contentHandler, mappingInfo);
				}
			} else
				marshaller.marshal(object, contentHandler);
		} finally {
			if (marshaller != null) {
				marshaller.setAttachmentMarshaller(null);
				parent.mpool.replace(marshaller);
			}
		}
	}

	public void marshal(T object, Result result) throws JAXBException {
		JAXBMarshaller marshaller = null;
		try {
			marshaller = parent.mpool.allocate();
			marshaller.setAttachmentMarshaller(null);
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT,
					true);
			if (mappingInfo != null) {
				if (isParameterizedType) {
					JAXBTypeElement jte = new JAXBTypeElement(
							mappingInfo.getXmlTagName(), object,
							(ParameterizedType) mappingInfo.getType());
					marshaller.marshal(jte, result, mappingInfo);
				} else {
					JAXBElement<T> elt = new JAXBElement<T>(
							mappingInfo.getXmlTagName(),
							(Class<T>) mappingInfo.getType(), object);
					// marshaller.marshal(elt, result);
					marshaller.marshal(elt, result, mappingInfo);
				}
			} else
				marshaller.marshal(object, result);
		} finally {
			if (marshaller != null) {
				marshaller.setAttachmentMarshaller(null);
				parent.mpool.replace(marshaller);
			}
		}
	}

	// This is used in RPC
	public T unmarshal(XMLStreamReader in, AttachmentUnmarshaller au)
			throws JAXBException {
		JAXBUnmarshaller unmarshaller = null;
		try {
			QName tagName = null;
			if (in.getEventType() == XMLStreamConstants.START_ELEMENT)
				tagName = in.getName();

			unmarshaller = parent.upool.allocate();
			unmarshaller.setAttachmentUnmarshaller(au);
			Object o = ((mappingInfo != null) ? unmarshaller.unmarshal(in,
					mappingInfo) : unmarshaller.unmarshal(in));
			if (o instanceof JAXBElement)
				o = ((JAXBElement) o).getValue();
			// TODO recycle to pool
			
			// Workaround for Eclipselink JAXB not consuming END_ELEMENT.
            try {
				if (in.getEventType() == XMLStreamConstants.END_ELEMENT && in.getName().equals(tagName)) 
					in.next();
			} catch (XMLStreamException e) {
				throw new WebServiceException(e);
			}
			return (T) o;
		} finally {
			if (unmarshaller != null) {
				unmarshaller.setAttachmentUnmarshaller(null);
				parent.upool.replace(unmarshaller);
			}
		}
	}

	public T unmarshal(Source in, AttachmentUnmarshaller au)
			throws JAXBException {
		JAXBUnmarshaller unmarshaller = null;
		try {
			unmarshaller = parent.upool.allocate();
			unmarshaller.setAttachmentUnmarshaller(au);
			Object o;
			if (mappingInfo != null)
				o = unmarshaller.unmarshal(in, mappingInfo);
			else
				o = unmarshaller.unmarshal(in);
			if (o instanceof JAXBElement)
				o = ((JAXBElement) o).getValue();
			// TODO recycle to pool
			return (T) o;
		} finally {
			if (unmarshaller != null) {
				unmarshaller.setAttachmentUnmarshaller(null);
				parent.upool.replace(unmarshaller);
			}
		}
	}

	public T unmarshal(InputStream in) throws JAXBException {
		JAXBUnmarshaller unmarshaller = null;
		try {
			unmarshaller = parent.upool.allocate();
			// GAG missing
			Object o = ((mappingInfo != null) ? unmarshaller.unmarshal(
					new StreamSource(in), mappingInfo) : unmarshaller
					.unmarshal(in));
			// Object o = unmarshaller.unmarshal(in);
			if (o instanceof JAXBElement)
				o = ((JAXBElement) o).getValue();
			// TODO recycle to pool
			return (T) o;
		} finally {
			if (unmarshaller != null) {
				unmarshaller.setAttachmentUnmarshaller(null);
				parent.upool.replace(unmarshaller);
			}
		}
	}

	public T unmarshal(Node in, AttachmentUnmarshaller au) throws JAXBException {
		JAXBUnmarshaller unmarshaller = null;
		try {
			unmarshaller = parent.upool.allocate();
			unmarshaller.setAttachmentUnmarshaller(au);
			Object o = ((mappingInfo != null) ? unmarshaller.unmarshal(
					new DOMSource(in), mappingInfo) : unmarshaller
					.unmarshal(in));
			// Object o = unmarshaller.unmarshal(in);
			if (o instanceof JAXBElement)
				o = ((JAXBElement) o).getValue();
			// TODO recycle to pool
			return (T) o;
		} finally {
			if (unmarshaller != null) {
				unmarshaller.setAttachmentUnmarshaller(null);
				parent.upool.replace(unmarshaller);
			}
		}
	}

	public boolean supportOutputStream() {
		return true;
	}
}
