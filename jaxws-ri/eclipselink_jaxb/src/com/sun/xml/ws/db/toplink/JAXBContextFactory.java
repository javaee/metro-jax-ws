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

import static org.eclipse.persistence.jaxb.JAXBContextFactory.DEFAULT_TARGET_NAMESPACE_KEY;
import static org.eclipse.persistence.jaxb.JAXBContextFactory.ECLIPSELINK_OXM_XML_KEY;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.transform.Source;

import org.eclipse.persistence.jaxb.TypeMappingInfo;
import org.eclipse.persistence.jaxb.TypeMappingInfo.ElementScope;
import org.w3c.dom.Element;


import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.BindingContextFactory;
import com.sun.xml.ws.spi.db.BindingInfo;
import com.sun.xml.ws.spi.db.DatabindingException;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.WrapperComposite;

/**
 * JAXBContextFactory
 *
 * @author shih-chang.chen@oracle.com
 */
public class JAXBContextFactory extends BindingContextFactory {
	protected boolean isFor(String str) {
		return (str.equals("toplink.jaxb") ||
			    str.equals("eclipselink.jaxb")||
			    str.equals(this.getClass().getName())||
			    str.equals("org.eclipse.persistence.jaxb"));
	}

	protected BindingContext getContext(Marshaller m) {
		//org.eclipse.persistence.jaxb.JAXBMarshaller jm = (org.eclipse.persistence.jaxb.JAXBMarshaller) m;
		return null;
	}

	protected BindingContext newContext(JAXBContext context) {
		return new JAXBContextWrapper(context, null);
	}

	protected BindingContext newContext(BindingInfo bi) {
		Map<String, Source> extMapping = null;
		Map<String, Object> properties = new HashMap<String, Object>();
		Map<TypeInfo, TypeMappingInfo> map = createTypeMappings(bi.typeInfos());
		HashSet<Type> typeSet = new HashSet<Type>(); 
		ArrayList<TypeMappingInfo> typeList = new ArrayList<TypeMappingInfo>();
		for (TypeMappingInfo tmi : map.values()) {
			typeList.add(tmi);
			typeSet.add(tmi.getType());
		}
		for (Class<?> clss : bi.contentClasses()) {
			if (!typeSet.contains(clss) && !WrapperComposite.class.equals(clss)) {
				typeSet.add(clss);	
				TypeMappingInfo tmi = new TypeMappingInfo();
				tmi.setType(clss);
				typeList.add(tmi);
			}
		}
		TypeMappingInfo[] types = typeList.toArray(new TypeMappingInfo[typeList.size()]);
		properties.put(ECLIPSELINK_OXM_XML_KEY, extMapping);
		properties.put(DEFAULT_TARGET_NAMESPACE_KEY, bi.getDefaultNamespace());
		try {
			org.eclipse.persistence.jaxb.JAXBContext jaxbContext = (org.eclipse.persistence.jaxb.JAXBContext) org.eclipse.persistence.jaxb.JAXBContextFactory
					.createContext(types, properties, bi.getClassLoader());
			return new JAXBContextWrapper(jaxbContext, map);
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new DatabindingException(e.getMessage(), e);
		}
	}

	static Map<TypeInfo, TypeMappingInfo> createTypeMappings(Collection<TypeInfo> col) {
		Map<TypeInfo, TypeMappingInfo> refs = new HashMap<TypeInfo, TypeMappingInfo>();
		if (col == null || col.isEmpty()) {
			return refs;
		}
		for (TypeInfo e : col) {
			if (e.type.equals(WrapperComposite.class)) continue;
			Element xmlElem = findXmlElement(e.properties());
			if (e.isRepeatedElement()) {
				e = repeatedWrapee(e, xmlElem);
				xmlElem = null;
			}

			// GAG
			// Work around possible duplicate global TypeInfos by reusing
			// a single TypeMappingInfo for multiple TypeInfos if not
			// one of the problem classes (see below).
			// First do the obvious check: is the instance already in the map?
			if (refs.get(e) != null)
				continue;
			TypeMappingInfo tmi = null;
			boolean forceLocal = false;
			for (TypeInfo ti : refs.keySet()) {
				// GAG: Workaround for runtime duplicates.
				if (e.tagName.equals(ti.tagName)) {
					if (e.isGlobalElement() && ti.isGlobalElement()) {
						if (e.type.equals(ti.type)) {
							// TODO
							// Eclipselink has issues reusing enums with
							// multiple marshalers.  This still needs to
							// be resolved adequately.
							if (e.type instanceof Class<?>) {
								Class<?> clz = (Class<?>) e.type;
								if (clz.isEnum()) {
									forceLocal = true;
									break;
								} else {
									tmi = refs.get(ti);
									break;
								}
							} else {
								tmi = refs.get(ti);
								break;
							}
						} else {
							// Conflicting types on globals!  May not be
							// a bullet-proof solution possible.
							forceLocal = true;
							break;
						}
					}
				}
			}
			
			if (tmi == null) {
				tmi = new TypeMappingInfo();
				tmi.setXmlTagName(e.tagName);
				tmi.setType((e.getGenericType() != null) ? e.getGenericType()
						: e.type);
				if (e.getGenericType() != null) {
					String gts = e.getGenericType().toString();
					if (gts.startsWith("javax.xml.ws.Holder")) {
						tmi.setType(e.type);
					} else if (gts.startsWith("javax.xml.ws.Response")) {
						tmi.setType(e.type);
					}
					if (Object.class.equals(e.type)) {
						tmi.setType(e.type);
						//System.out.println(e.getGenericType().getClass() + " "
						//		+ e.type);
					}
				}
				// Filter out non-JAXB annotations.
				Annotation[] aa = e.annotations;
				if (aa != null && aa.length != 0) {
					List<Annotation> la = new ArrayList<Annotation>();
					for (Annotation a : aa) {
						for (Class<?> clz : a.getClass().getInterfaces()) {
							if (clz.getName().startsWith(
									"javax.xml.bind.annotation.")) {
								la.add(a);
								break;
							}
						}
					}
					aa = la.toArray(new Annotation[la.size()]);
					// System.out.println("filtered: " + la);
				}
				tmi.setAnnotations(aa);
				if (forceLocal) {
					tmi.setElementScope(ElementScope.Local);
				} else {
					tmi.setElementScope(e.isGlobalElement() ? ElementScope.Global
							: ElementScope.Local);
				}
				tmi.setXmlElement(xmlElem);
			}
			refs.put(e, tmi);
		}
		return refs;
	}

	private static Element findXmlElement(Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}
	  
	private static TypeInfo repeatedWrapee(TypeInfo e, Element xmlAnn) {
        XmlElement xe = getAnnotation(e, XmlElement.class);
	    if (xe != null) e.type = xe.type();
	    if (xmlAnn != null) {
	        String typeAttr = xmlAnn.getAttribute("type");
	        if (typeAttr != null) {
	        	try {
	        		Class<?> cls = Class.forName(typeAttr);
	        		e.type = cls;
	        	} catch (ClassNotFoundException e1) {
	        		e1.printStackTrace();
	        	}
	        }
	    }
	    return e;
    }

	private static <T> T getAnnotation(TypeInfo e, Class<T> cls) {  
	    if (e == null || e.annotations == null) return null;
	    for (Annotation a : e.annotations) if (cls.isInstance(a)) return cls.cast(a);
	    return null;
	}
}
