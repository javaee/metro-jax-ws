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
package com.sun.xml.ws.db.glassfish;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.ws.spi.db.DatabindingException;
import com.sun.xml.ws.spi.db.TypeInfo;

public class BridgeWrapper<T> implements XMLBridge<T> {

	private JAXBRIContextWrapper parent;
	private com.sun.xml.bind.api.Bridge<T> bridge;
	
	public BridgeWrapper(JAXBRIContextWrapper p, com.sun.xml.bind.api.Bridge<T> b) {
		parent = p;
		bridge = b;
	}
	
	public BindingContext context() {
		return parent;
	}
	
	com.sun.xml.bind.api.Bridge getBridge() {
		return bridge;
	}
	
	public boolean equals(Object obj) {
		return bridge.equals(obj);
	}

	public JAXBRIContext getContext() {
		return bridge.getContext();
	}

	public TypeInfo getTypeInfo() {
		return parent.typeInfo(bridge.getTypeReference());
	}

	public int hashCode() {
		return bridge.hashCode();
	}

//	public final void marshal(BridgeContext context, T object, ContentHandler contentHandler) throws JAXBException {
//		bridge.marshal(context, object, contentHandler);
//	}
//
//	public final void marshal(BridgeContext context, T object, Node output) throws JAXBException {
//		bridge.marshal(context, object, output);
//	}
//
//	public final void marshal(BridgeContext context, T object, OutputStream output, NamespaceContext nsContext) throws JAXBException {
//		bridge.marshal(context, object, output, nsContext);
//	}
//
//	public final void marshal(BridgeContext context, T object, Result result) throws JAXBException {
//		bridge.marshal(context, object, result);
//	}
//
//	public final void marshal(BridgeContext context, T object, XMLStreamWriter output) throws JAXBException {
//		bridge.marshal(context, object, output);
//	}

	public void marshal(Marshaller m, T object, ContentHandler contentHandler) throws JAXBException {
		bridge.marshal(m, object, contentHandler);
	}

	public void marshal(Marshaller m, T object, Node output) throws JAXBException {
		bridge.marshal(m, object, output);
	}

	public void marshal(Marshaller m, T object, OutputStream output, NamespaceContext nsContext) throws JAXBException {
		bridge.marshal(m, object, output, nsContext);
	}

	public void marshal(Marshaller m, T object, Result result) throws JAXBException {
		bridge.marshal(m, object, result);
	}

	public void marshal(Marshaller m, T object, XMLStreamWriter output) throws JAXBException {
		bridge.marshal(m, object, output);
//		bridge.marshal(m, (T) convert(object), output);
	}

	public final void marshal(T object, ContentHandler contentHandler, AttachmentMarshaller am) throws JAXBException {
//		bridge.marshal((T) convert(object), contentHandler, am);
		bridge.marshal(object, contentHandler, am);
	}
	
//	Object convert(Object o) {
//		return (o instanceof WrapperComposite)? convertWrapper((WrapperComposite)o) : o;
//	}
//	
//	static CompositeStructure convertWrapper(WrapperComposite w) {
//		CompositeStructure cs = new CompositeStructure();
//		cs.values = w.values;
//		cs.bridges = new Bridge[w.bridges.length];
//		for (int i = 0; i < cs.bridges.length; i++) 
//		    cs.bridges[i] = ((BridgeWrapper)w.bridges[i]).getBridge();
//		return cs;
//	}

	public void marshal(T object, ContentHandler contentHandler) throws JAXBException {
		bridge.marshal(object, contentHandler);
//		bridge.marshal((T) convert(object), contentHandler);
	}

	public void marshal(T object, Node output) throws JAXBException {
		bridge.marshal(object, output);
//		bridge.marshal((T) convert(object), output);
	}

	public void marshal(T object, OutputStream output, NamespaceContext nsContext, AttachmentMarshaller am) throws JAXBException {
//		bridge.marshal((T) convert(object), output, nsContext, am);
		bridge.marshal(object, output, nsContext, am);
	}

	public void marshal(T object, OutputStream output, NamespaceContext nsContext) throws JAXBException {
		bridge.marshal(object, output, nsContext);
//		bridge.marshal((T) convert(object), output, nsContext);
	}

	public final void marshal(T object, Result result) throws JAXBException {
		bridge.marshal(object, result);
	}

	public final void marshal(T object, XMLStreamWriter output,
			AttachmentMarshaller am) throws JAXBException {
		bridge.marshal(object, output, am);
	}

	public final void marshal(T object, XMLStreamWriter output)
			throws JAXBException {
		bridge.marshal(object, output);
	}

	public String toString() {
		return BridgeWrapper.class.getName() + " : " + bridge.toString();
	}

//	public final T unmarshal(BridgeContext context, InputStream in)
//			throws JAXBException {
//		return bridge.unmarshal(context, in);
//	}
//
//	public final T unmarshal(BridgeContext context, Node n)
//			throws JAXBException {
//		return bridge.unmarshal(context, n);
//	}
//
//	public final T unmarshal(BridgeContext context, Source in)
//			throws JAXBException {
//		return bridge.unmarshal(context, in);
//	}
//
//	public final T unmarshal(BridgeContext context, XMLStreamReader in)
//			throws JAXBException {
//		return bridge.unmarshal(context, in);
//	}

	public final T unmarshal(InputStream in) throws JAXBException {
		return bridge.unmarshal(in);
	}

	public final T unmarshal(Node n, AttachmentUnmarshaller au)
			throws JAXBException {
		return bridge.unmarshal(n, au);
	}

	public final T unmarshal(Node n) throws JAXBException {
		return bridge.unmarshal(n);
	}

	public final T unmarshal(Source in, AttachmentUnmarshaller au)
			throws JAXBException {
		return bridge.unmarshal(in, au);
	}

	public final T unmarshal(Source in) throws DatabindingException {
		try {
			return bridge.unmarshal(in);
		} catch (JAXBException e) {
			throw new DatabindingException(e);
		}
	}

	public T unmarshal(Unmarshaller u, InputStream in) throws JAXBException {
		return bridge.unmarshal(u, in);
	}

	public T unmarshal(Unmarshaller context, Node n) throws JAXBException {
		return bridge.unmarshal(context, n);
	}

	public T unmarshal(Unmarshaller u, Source in) throws JAXBException {
		return bridge.unmarshal(u, in);
	}

	public T unmarshal(Unmarshaller u, XMLStreamReader in) throws JAXBException {
		return bridge.unmarshal(u, in);
	}

	public final T unmarshal(XMLStreamReader in, AttachmentUnmarshaller au)
			throws JAXBException {
		return bridge.unmarshal(in, au);
	}

	public final T unmarshal(XMLStreamReader in) throws JAXBException {
		return bridge.unmarshal(in);
	}

    public boolean supportOutputStream() {
    	return true;
    }
}
