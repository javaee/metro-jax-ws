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
package com.sun.xml.ws.db.toplink;

import static org.eclipse.persistence.jaxb.JAXBContextFactory.DEFAULT_TARGET_NAMESPACE_KEY;
import static org.eclipse.persistence.jaxb.JAXBContextFactory.ECLIPSELINK_OXM_XML_KEY;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.WebServiceException;

import org.eclipse.persistence.jaxb.TypeMappingInfo;
import org.eclipse.persistence.jaxb.TypeMappingInfo.ElementScope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.model.ParameterImpl;
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

    static final public String OXM_XML_OVERRIDE = "eclipselink-oxm-xml";
    static final public String OXM_XML_ELEMENT = "eclipselink-oxm-xml.xml-element";

    @Override
    protected boolean isFor(String str) {
        return (str.equals("toplink.jaxb")
                || str.equals("eclipselink.jaxb")
                || str.equals(this.getClass().getName())
                || str.equals("org.eclipse.persistence.jaxb"));
    }

    @Override
    protected BindingContext getContext(Marshaller m) {
        //org.eclipse.persistence.jaxb.JAXBMarshaller jm = (org.eclipse.persistence.jaxb.JAXBMarshaller) m;
        return null;
    }

    @Override
    protected BindingContext newContext(JAXBContext context) {
        return new JAXBContextWrapper(context, null);
    }

    @Override
    protected BindingContext newContext(BindingInfo bi) {
        Map<String, Source> extMapping = (Map<String, Source>) bi.properties().get(OXM_XML_OVERRIDE);
        Map<String, Object> properties = new HashMap<String, Object>();
        Map<TypeInfo, TypeMappingInfo> map = createTypeMappings(bi.typeInfos());
        //chen workaround for document-literal wrapper - new feature on eclipselink API requested
        for (TypeInfo tinfo : map.keySet()) {
            WrapperParameter wp = (WrapperParameter) tinfo.properties().get(WrapperParameter.class.getName());
            if (wp != null) {
                Class<?> wrpCls = (Class) tinfo.type;
                Element javaAttributes = null;
                for (ParameterImpl p : wp.getWrapperChildren()) {
                    Element xmlelem = findXmlElement(p.getTypeInfo().properties());
                    if (xmlelem != null) {
                        if (javaAttributes == null) {
                            javaAttributes = javaAttributes(wrpCls, extMapping);
                        }
                        xmlelem = (Element) javaAttributes.getOwnerDocument().importNode(xmlelem, true);
                        String fieldName = getFieldName(p, wrpCls);
                        xmlelem.setAttribute("java-attribute", fieldName);
                        javaAttributes.appendChild(xmlelem);
                    }
                }
                wrpCls.getPackage().getName();
            }
        }

//		Source src = extMapping.get("com.sun.xml.ws.test.toplink.jaxws");
//		if (src != null){
//	        TransformerFactory tf = TransformerFactory.newInstance();
//	        try {
//	            Transformer t = tf.newTransformer();
//	            java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(); 
//	            StreamResult sax = new StreamResult(bo);
//	            t.transform(src, sax);
//	            System.out.println(new String(bo.toByteArray()));
//	        } catch (TransformerConfigurationException e) {
//	            e.printStackTrace();
//	            throw new WebServiceException(e.getMessage(), e);
//	        } catch (TransformerException e) {
//	            e.printStackTrace();
//	            throw new WebServiceException(e.getMessage(), e);
//	        }
//		}

        HashSet<Type> typeSet = new HashSet<Type>();
        HashSet<TypeMappingInfo> typeList = new HashSet<TypeMappingInfo>();
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
            throw new DatabindingException(e.getMessage(), e);
        }
    }

    private String getFieldName(ParameterImpl p, Class wrpCls) {
        for (Field f : wrpCls.getFields()) {
            XmlElement xe = f.getAnnotation(XmlElement.class);
            if (xe != null && p.getName().getLocalPart().equals(xe.name())) {
                return f.getName();
            } else {
                if (p.getName().getLocalPart().equals(f.getName())) {
                    return f.getName();
                }
            }
        }
        return null;
    }
    static DocumentBuilderFactory docBuilderFactory;
    static String OXMTNS = "http://www.eclipse.org/eclipselink/xsds/persistence/oxm";

    // TODO - fix with new secure factory util class
    static {
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
    }

    private Element javaAttributes(Class<?> wrpCls, Map<String, Source> extMapping) {
        XmlRootElement xmlRootElement = wrpCls.getAnnotation(XmlRootElement.class);
        XmlType xmlType = wrpCls.getAnnotation(XmlType.class);
        Element xmlbindings = xmlbindings(wrpCls, extMapping);
        extMapping.put(wrpCls.getPackage().getName(), new DOMSource(xmlbindings));
        Element javatypes = child(xmlbindings, "java-types");
        Element javatype = null;
        NodeList javatypeList = xmlbindings.getElementsByTagNameNS(OXMTNS, "java-type");
        for (int i = 0; javatype == null && i < javatypeList.getLength(); i++) {
            Element e = (Element) javatypeList.item(i);
            if (wrpCls.getName().equals(e.getAttribute("name"))) {
                javatype = e;
            }
        }
        if (javatype == null) {
            javatype = javatypes.getOwnerDocument().createElementNS(OXMTNS, "java-type");
            javatype.setAttribute("name", wrpCls.getName());
            javatypes.appendChild(javatype);
            if (xmlRootElement != null) {
                Element r = javatype.getOwnerDocument().createElementNS(OXMTNS, "xml-root-element");
                r.setAttribute("name", xmlRootElement.name());
                r.setAttribute("namespace", xmlRootElement.namespace());
                javatype.appendChild(r);
            }
            if (xmlType != null) {
                Element r = javatype.getOwnerDocument().createElementNS(OXMTNS, "xml-type");
                r.setAttribute("name", xmlType.name());
                r.setAttribute("namespace", xmlType.namespace());
                StringBuilder propOrdr = new StringBuilder();
                if (xmlType.propOrder() != null) {
                    for (int pi = 0; pi < xmlType.propOrder().length; pi++) {
                        propOrdr.append(xmlType.propOrder()[pi]);
                        propOrdr.append(" ");
                    }
                }
                r.setAttribute("prop-order", propOrdr.toString().trim());
                javatype.appendChild(r);
            }
        }
        return child(javatype, "java-attributes");
    }

    Element child(Element parent, String name) {
        NodeList list = parent.getElementsByTagNameNS(OXMTNS, name);
        if (list == null || list.getLength() == 0) {
            Element c = parent.getOwnerDocument().createElementNS(OXMTNS, name);
            parent.appendChild(c);
            return c;
        } else {
            return (Element) list.item(0);
        }
    }

    Element xmlbindings(Class<?> wrpCls, Map<String, Source> extMapping) {
        Source src = extMapping.get(wrpCls.getPackage().getName());
        Element xmlbindings = null;
        if (src != null) {
            if (src instanceof DOMSource) {
                xmlbindings = (Element) ((DOMSource) src).getNode();
            } else {
                TransformerFactory tf = TransformerFactory.newInstance();
                try {
                    Transformer t = tf.newTransformer();
                    DOMResult dr = new DOMResult();
                    t.transform(src, dr);
                    Node n = dr.getNode();
                    if (n instanceof Document) {
                        xmlbindings = ((Document) n).getDocumentElement();
                    } else if (n instanceof Element) {
                        xmlbindings = (Element) n;
                    }
                } catch (TransformerConfigurationException e) {
                    throw new WebServiceException(e.getMessage(), e);
                } catch (TransformerException e) {
                    throw new WebServiceException(e.getMessage(), e);
                }
            }
        } else {
            try {
                DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
                Document doc = builder.newDocument();
                xmlbindings = doc.createElementNS(OXMTNS, "xml-bindings");
                doc.appendChild(xmlbindings);
            } catch (ParserConfigurationException e) {
                throw new WebServiceException(e.getMessage(), e);
            }
        }
        return xmlbindings;
    }

    static Map<TypeInfo, TypeMappingInfo> createTypeMappings(Collection<TypeInfo> col) {
        Map<TypeInfo, TypeMappingInfo> refs = new HashMap<TypeInfo, TypeMappingInfo>();
        if (col == null || col.isEmpty()) {
            return refs;
        }
        for (TypeInfo e : col) {
            if (e.type.equals(WrapperComposite.class)) {
                continue;
            }
            Element xmlElem = findXmlElement(e.properties());
            if (e.isRepeatedElement()) {
                e = repeatedWrapee(e, xmlElem);
                xmlElem = null;
            }

            // Work around possible duplicate global TypeInfos by reusing
            // a single TypeMappingInfo for multiple TypeInfos if not
            // one of the problem classes (see below).
            // First do the obvious check: is the instance already in the map?
            if (refs.get(e) != null) {
                continue;
            }
            TypeMappingInfo tmi = null;
            boolean forceLocal = false;
            for (TypeInfo ti : refs.keySet()) {
                // Workaround for runtime duplicates.
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
                tmi.setType((e.getGenericType() != null) ? e.getGenericType() : e.type);
                tmi.setNillable(e.isNillable());
                if (e.getGenericType() != null) {
                    String gts = e.getGenericType().toString();
                    if (gts.startsWith("javax.xml.ws.Holder")) {
                        tmi.setType(e.type);
                    } else if (gts.startsWith("javax.xml.ws.Response")) {
                        tmi.setType(e.type);
                    } else if (gts.startsWith("java.util.concurrent.Future")) {
                        tmi.setType(e.type);
                    }
                    if (Object.class.equals(e.type)) {
                        tmi.setType(e.type);
                        //System.out.println(e.getGenericType().getClass() + " "
                        //		+ e.type);
                    }
                    if (tmi.getType() instanceof GenericArrayType) {
                        tmi.setType(typeToClass(tmi.getType(), null));
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
        return (Element) properties.get(OXM_XML_ELEMENT);
    }

    private static TypeInfo repeatedWrapee(TypeInfo e, Element xmlAnn) {
        XmlElement xe = getAnnotation(e, XmlElement.class);
        if (xe != null) {
            e.type = xe.type();
        }
        if (xmlAnn != null) {
            String typeAttr = xmlAnn.getAttribute("type");
            if (typeAttr != null) {
                try {
                    Class<?> cls = Class.forName(typeAttr);
                    e.type = cls;
                } catch (ClassNotFoundException e1) {
                    throw new DatabindingException(e1.getMessage(), e1);
                }
            }
        }
        return e;
    }

    private static <T> T getAnnotation(TypeInfo e, Class<T> cls) {
        if (e == null || e.annotations == null) {
            return null;
        }
        for (Annotation a : e.annotations) {
            if (cls.isInstance(a)) {
                return cls.cast(a);
            }
        }
        return null;
    }

    //Bug 13899624 workaround
    @SuppressWarnings("FinalStaticMethod")
    public static final Class<?> typeToClass(Type type, ClassLoader cl) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof TypeVariable) {
            return typeToClass(((TypeVariable) type).getBounds()[0], cl);
        } else if (type instanceof ParameterizedType) {
            return typeToClass(((ParameterizedType) type).getRawType(), cl);
        } else if (type instanceof GenericArrayType) {
            Type t = ((GenericArrayType) type).getGenericComponentType();
            Class<?> clz = typeToClass(t, cl);
            String className;
            if (clz.isPrimitive()) {
                char tc = 0;
                if (clz.equals(boolean.class)) {
                    tc = 'Z';
                } else if (clz.equals(byte.class)) {
                    tc = 'B';
                } else if (clz.equals(char.class)) {
                    tc = 'C';
                } else if (clz.equals(double.class)) {
                    tc = 'D';
                } else if (clz.equals(float.class)) {
                    tc = 'F';
                } else if (clz.equals(int.class)) {
                    tc = 'I';
                } else if (clz.equals(long.class)) {
                    tc = 'J';
                } else if (clz.equals(short.class)) {
                    tc = 'S';
                } else {
                    // String msg =
                    // ToplinkJaxbPluginLogger.unknownPrimitiveLoggable(tc)
                    // .getMessageText();
                    // log.error(msg);
                    throw new WebServiceException("unknown type " + type);
                }
                className = "[" + tc;
            } else if (clz.isArray()) {
                className = "[" + clz.getName();
            } else {
                className = "[L" + clz.getName() + ';';
            }
            try {
                return classForName(className, cl);
            } catch (ClassNotFoundException e) {
                // String uc =
                // ToplinkJaxbPluginLogger.unknownClassNameLoggable(className)
                // .getMessageText();
                // log.error(uc);
                // throw new WebServiceException(uc);
                throw new WebServiceException("unknown type " + type);

            }
        } else {
            // String ut = ToplinkJaxbPluginLogger.unexpectedTypeLoggable(type)
            // .getMessageText();
            // log.error(ut);
            // throw new WebServiceException(ut);
            throw new WebServiceException("unknown type " + type);
        }
    }

    static public Class<?> classForName(String name, ClassLoader cl) throws ClassNotFoundException {
        if (cl != null) {
            try {
                return cl.loadClass(name);
            } catch (ClassNotFoundException e) {
                return classForName(name);
            }
        }
        return classForName(name);
    }

    static public Class<?> classForName(String name) throws ClassNotFoundException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            try {
                return cl.loadClass(name);
            } catch (ClassNotFoundException e) {
                return Class.forName(name);
            }
        }
        return Class.forName(name);
    }
}
