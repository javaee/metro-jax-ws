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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.WrapperComposite;

class JAXBRIContextWrapper implements BindingContext {

    private Map<TypeInfo, TypeReference> typeRefs;
    private Map<TypeReference, TypeInfo> typeInfos;
    private JAXBRIContext context;

    JAXBRIContextWrapper(JAXBRIContext cxt, Map<TypeInfo, TypeReference> refs) {
        context = cxt;
        typeRefs = refs;
        if (refs != null) {
            typeInfos = new java.util.HashMap<TypeReference, TypeInfo>();
            for (TypeInfo ti : refs.keySet()) {
                typeInfos.put(typeRefs.get(ti), ti);
            }
        }
    }

    TypeReference typeReference(TypeInfo ti) {
        return (typeRefs != null) ? typeRefs.get(ti) : null;
    }

    TypeInfo typeInfo(TypeReference tr) {
        return (typeInfos != null) ? typeInfos.get(tr) : null;
    }

    @Override
    public Marshaller createMarshaller() throws JAXBException {
        return context.createMarshaller();
    }

    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        return context.createUnmarshaller();
    }

    @Override
    public void generateSchema(SchemaOutputResolver outputResolver)
            throws IOException {
        context.generateSchema(outputResolver);
    }

    @Override
    public String getBuildId() {
        return context.getBuildId();
    }

    @Override
    public QName getElementName(Class o) throws JAXBException {
        return context.getElementName(o);
    }

    @Override
    public QName getElementName(Object o) throws JAXBException {
        return context.getElementName(o);
    }

    @Override
    public <B, V> com.sun.xml.ws.spi.db.PropertyAccessor<B, V> getElementPropertyAccessor(
            Class<B> wrapperBean, String nsUri, String localName)
            throws JAXBException {
        return new RawAccessorWrapper(context.getElementPropertyAccessor(wrapperBean, nsUri, localName));
    }

    @Override
    public List<String> getKnownNamespaceURIs() {
        return context.getKnownNamespaceURIs();
    }

    public RuntimeTypeInfoSet getRuntimeTypeInfoSet() {
        return context.getRuntimeTypeInfoSet();
    }

    public QName getTypeName(com.sun.xml.bind.api.TypeReference tr) {
        return context.getTypeName(tr);
    }

    @Override
    public int hashCode() {
        return context.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JAXBRIContextWrapper other = (JAXBRIContextWrapper) obj;
        if (this.context != other.context && (this.context == null || !this.context.equals(other.context))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSwaRef() {
        return context.hasSwaRef();
    }

    @Override
    public String toString() {
        return JAXBRIContextWrapper.class.getName() + " : " + context.toString();
    }

    @Override
    public XMLBridge createBridge(TypeInfo ti) {
        TypeReference tr = typeRefs.get(ti);
        com.sun.xml.bind.api.Bridge b = context.createBridge(tr);
        return WrapperComposite.class.equals(ti.type)
                ? new WrapperBridge(this, b)
                : new BridgeWrapper(this, b);
    }

    @Override
    public JAXBContext getJAXBContext() {
        return context;
    }

    @Override
    public QName getTypeName(TypeInfo ti) {
        TypeReference tr = typeRefs.get(ti);
        return context.getTypeName(tr);
    }

    @Override
    public XMLBridge createFragmentBridge() {
        return new MarshallerBridge((com.sun.xml.bind.v2.runtime.JAXBContextImpl) context);
    }

    @Override
    public Object newWrapperInstace(Class<?> wrapperType)
            throws InstantiationException, IllegalAccessException {
        return wrapperType.newInstance();
    }
}
