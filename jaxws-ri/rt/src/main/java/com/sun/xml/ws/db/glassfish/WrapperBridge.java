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

package com.sun.xml.ws.db.glassfish;

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

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.WrapperComposite;

public class WrapperBridge<T> implements XMLBridge<T> {

    private JAXBRIContextWrapper parent;
    private com.sun.xml.bind.api.Bridge<T> bridge;

    public WrapperBridge(JAXBRIContextWrapper p, com.sun.xml.bind.api.Bridge<T> b) {
        parent = p;
        bridge = b;
    }

    @Override
    public BindingContext context() {
        return parent;
    }

    @Override
    public boolean equals(Object obj) {
        return bridge.equals(obj);
    }

    @Override
    public TypeInfo getTypeInfo() {
        return parent.typeInfo(bridge.getTypeReference());
    }

    @Override
    public int hashCode() {
        return bridge.hashCode();
    }

    static CompositeStructure convert(Object o) {
        WrapperComposite w = (WrapperComposite) o;
        CompositeStructure cs = new CompositeStructure();
        cs.values = w.values;
        cs.bridges = new Bridge[w.bridges.length];
        for (int i = 0; i < cs.bridges.length; i++) {
            cs.bridges[i] = ((BridgeWrapper) w.bridges[i]).getBridge();
        }
        return cs;
    }

    @Override
    public final void marshal(T object, ContentHandler contentHandler, AttachmentMarshaller am) throws JAXBException {
        bridge.marshal((T) convert(object), contentHandler, am);
//		bridge.marshal(object, contentHandler, am);
    }

    @Override
    public void marshal(T object, Node output) throws JAXBException {
        throw new UnsupportedOperationException();
//		bridge.marshal(object, output);
//		bridge.marshal((T) convert(object), output);
    }

    @Override
    public void marshal(T object, OutputStream output, NamespaceContext nsContext, AttachmentMarshaller am) throws JAXBException {
        bridge.marshal((T) convert(object), output, nsContext, am);
    }

    @Override
    public final void marshal(T object, Result result) throws JAXBException {
        throw new UnsupportedOperationException();
//		bridge.marshal(object, result);
    }

    @Override
    public final void marshal(T object, XMLStreamWriter output, AttachmentMarshaller am) throws JAXBException {
        bridge.marshal((T) convert(object), output, am);
    }

    @Override
    public String toString() {
        return BridgeWrapper.class.getName() + " : " + bridge.toString();
    }

    @Override
    public final T unmarshal(InputStream in) throws JAXBException {
        //EndpointArgumentsBuilder.RpcLit.readRequest
        throw new UnsupportedOperationException();
//		return bridge.unmarshal(in);
    }

    @Override
    public final T unmarshal(Node n, AttachmentUnmarshaller au) throws JAXBException {
        //EndpointArgumentsBuilder.RpcLit.readRequest
        throw new UnsupportedOperationException();
//		return bridge.unmarshal(n, au);
    }

    @Override
    public final T unmarshal(Source in, AttachmentUnmarshaller au) throws JAXBException {
        //EndpointArgumentsBuilder.RpcLit.readRequest
        throw new UnsupportedOperationException();
//		return bridge.unmarshal(in, au);
    }

    @Override
    public final T unmarshal(XMLStreamReader in, AttachmentUnmarshaller au) throws JAXBException {
        //EndpointArgumentsBuilder.RpcLit.readRequest
        throw new UnsupportedOperationException();
//		return bridge.unmarshal(in, au);
    }

    @Override
    public boolean supportOutputStream() {
        return true;
    }
}
