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

package com.sun.xml.ws.db.toplink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.DatabindingException;
import com.sun.xml.ws.spi.db.ServiceArtifactSchemaGenerator;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.ws.spi.db.JAXBWrapperAccessor;
import com.sun.xml.ws.spi.db.PropertyAccessor;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.WrapperComposite;

import org.eclipse.persistence.Version;
import org.eclipse.persistence.jaxb.JAXBMarshaller;
import org.eclipse.persistence.jaxb.JAXBUnmarshaller;
import org.eclipse.persistence.jaxb.TypeMappingInfo;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class JAXBContextWrapper implements BindingContext {
	org.eclipse.persistence.jaxb.JAXBContext jaxbContext;	  
	ObjectPool<JAXBMarshaller>   mpool;	  
    ObjectPool<JAXBUnmarshaller> upool;	
    Map<TypeInfo, TypeMappingInfo> infoMap;
    Map<TypeMappingInfo, QName> typeNames;
    Map<Class<?>, JAXBWrapperAccessor> wrapperAccessors;
    SEIModel seiModel;

    private boolean hasSwaRef = false;

	JAXBContextWrapper(javax.xml.bind.JAXBContext cxt, Map<TypeInfo, TypeMappingInfo> map, SEIModel model) {
		jaxbContext = (org.eclipse.persistence.jaxb.JAXBContext) cxt;
		infoMap = map;
	    mpool = new ObjectPool<JAXBMarshaller>() {
			protected JAXBMarshaller newInstance() {
				try {
                    return (JAXBMarshaller) jaxbContext.createMarshaller();
                } catch (JAXBException e) {
                    e.printStackTrace();
                    throw new DatabindingException(e);
                }
			}
		};
	    upool = new ObjectPool<JAXBUnmarshaller>() {
			protected JAXBUnmarshaller newInstance() {
                try {
				    return (JAXBUnmarshaller) jaxbContext.createUnmarshaller();
                } catch (JAXBException e) {
                    e.printStackTrace();
                    throw new DatabindingException(e);
                }
			}
		};
		wrapperAccessors = new HashMap<Class<?>, JAXBWrapperAccessor>();
		hasSwaRef = jaxbContext.hasSwaRef();
        seiModel = model;
	}
	
	public String getBuildId() {
		return Version.getBuildRevision();
	}

	public XMLBridge createBridge(TypeInfo ref) {
		return (XMLBridge) (WrapperComposite.class.equals(ref.type) ? new com.sun.xml.ws.spi.db.WrapperBridge(this, ref) : new JAXBBond(this, ref));
	}

	public XMLBridge createFragmentBridge() {
		return new JAXBBond(this, null);
	}

	public Marshaller createMarshaller() throws JAXBException {
		return jaxbContext.createMarshaller();
	}

	public Unmarshaller createUnmarshaller() throws JAXBException {
		return jaxbContext.createUnmarshaller();
	}

	public void generateSchema(SchemaOutputResolver outputResolver)
			throws IOException {
		jaxbContext.generateSchema(outputResolver);
        if (seiModel != null) {
            ServiceArtifactSchemaGenerator xsdgen = new ServiceArtifactSchemaGenerator(seiModel);
            xsdgen.generate(outputResolver);
        }
	}

	public QName getElementName(Object o) throws JAXBException {
		// TODO Auto-generated method stub
		return null;
	}

    public QName getElementName(Class cls) throws JAXBException {
        XmlRootElement xre = (XmlRootElement) cls.getAnnotation(XmlRootElement.class);
        XmlType xt = (XmlType) cls.getAnnotation(XmlType.class);
        if (xt != null && xt.name() != null && !"".equals(xt.name())) return null;
        if (xre != null) {
            String lp = xre.name();
            String ns = xre.namespace();
            if (ns.equals("##default")) {
                XmlSchema xs = cls.getPackage().getAnnotation(XmlSchema.class);
                ns = (xs != null) ? xs.namespace() : "";
          }
          return new QName(ns, lp);
      }
      return null;
    }

	public <B, V> PropertyAccessor<B, V> getElementPropertyAccessor(Class<B> wrapperBean, String ns, String name) throws JAXBException {
		JAXBWrapperAccessor wa = wrapperAccessors.get(wrapperBean);
		if (wa == null) {
			wa = new JAXBWrapperAccessor(wrapperBean);
			wrapperAccessors.put(wrapperBean, wa);
		}
		return wa.getPropertyAccessor(ns, name);
	}

	public JAXBContext getJAXBContext() {
		return jaxbContext;
	}

	public List<String> getKnownNamespaceURIs() {
		// TODO
		return new ArrayList<String>();
	}

	public QName getTypeName(TypeInfo tr) {
		if (typeNames == null) typeNames = jaxbContext.getTypeMappingInfoToSchemaType();
	    TypeMappingInfo tmi = infoMap.get(tr);
	    return typeNames.get(tmi);
	}

	public boolean hasSwaRef() {
		return hasSwaRef;
	}
	
    public Object newWrapperInstace(Class<?> wrapperType)
            throws InstantiationException, IllegalAccessException {
        return wrapperType.newInstance();
    }
}
