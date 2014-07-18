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

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.WebServiceException;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBase64Binary;
import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlByte;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlDecimal;
import org.apache.xmlbeans.XmlDouble;
import org.apache.xmlbeans.XmlFloat;
import org.apache.xmlbeans.XmlInt;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlLong;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.XmlShort;
import org.apache.xmlbeans.XmlString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.xml.ws.api.model.CheckedException;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.db.xmlbeans.FaultInfo.PropInfo;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.BindingInfo;
import com.sun.xml.ws.spi.db.PropertyAccessor;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.WrapperComposite;
import com.sun.xml.ws.spi.db.XMLBridge;

/**
 * XMLBeansContext
 * 
 * @author shih-chang.chen@oracle.com
 */
public final class XMLBeansContext implements BindingContext {
    static final String XsdNs = "http://www.w3.org/2001/XMLSchema";
    static HashMap<Class, SchemaType> builtInSchemaTypes = new HashMap<Class, SchemaType>(); 
    static {
        // http://xmlbeans.apache.org/docs/2.0.0/guide/conXMLBeansSupportBuiltInSchemaTypes.html
        // XmlBeans.getBuiltinTypeSystem().globalTypes()
        builtInSchemaTypes.put(boolean.class, type(XmlBoolean.class));
        builtInSchemaTypes.put(Boolean.class, type(XmlBoolean.class));
        builtInSchemaTypes.put(byte.class,    type(XmlByte.class));
        builtInSchemaTypes.put(Byte.class,    type(XmlByte.class));
        builtInSchemaTypes.put(int.class,     type(XmlInt.class));
        builtInSchemaTypes.put(Integer.class, type(XmlInt.class));
        builtInSchemaTypes.put(double.class,  type(XmlDouble.class));
        builtInSchemaTypes.put(Double.class,  type(XmlDouble.class));
        builtInSchemaTypes.put(float.class,   type(XmlFloat.class));
        builtInSchemaTypes.put(Float.class,   type(XmlFloat.class));
        builtInSchemaTypes.put(long.class,    type(XmlLong.class));
        builtInSchemaTypes.put(Long.class,    type(XmlLong.class));
        builtInSchemaTypes.put(short.class,   type(XmlShort.class));
        builtInSchemaTypes.put(Short.class,   type(XmlShort.class));
        builtInSchemaTypes.put(String.class,  type(XmlString.class));
        builtInSchemaTypes.put(byte[].class,  type(XmlBase64Binary.class));
        builtInSchemaTypes.put(java.util.Calendar.class,   type(XmlDateTime.class));
        builtInSchemaTypes.put(java.math.BigDecimal.class, type(XmlDecimal.class));
        builtInSchemaTypes.put(java.math.BigInteger.class, type(XmlInteger.class));
        builtInSchemaTypes.put(javax.xml.namespace.QName.class, type(XmlQName.class));
    }
    
    static class SchemaInfo {
        String locationDir;
        String locationFileName;
        String targetNamespace;
        Document doc;
    }
    
    BindingInfo bindingInfo;
    ConcurrentHashMap<String, SchemaInfo> schemaInfos;
    HashMap<Class, SchemaType> schemaTypes;
    HashMap<Class, ArrayInfo> arrayInfos;
    HashMap<TypeInfo, FaultInfo> faultInfos ;
    HashMap<Class, JavaBeanInfo> javaBeanInfos;
    HashMap<TypeInfo, JavaBeanBridge> beanBridges = new HashMap<TypeInfo, JavaBeanBridge>();
    DocumentBuilderFactory docBuilderfactory;

    static SchemaType type(Class<?> cls) {
        try {
            Field field = cls.getField("type");
            return (SchemaType) field.get(cls);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return null;
    }
    
    static QName globalElementName(Class<?> cls) {
        SchemaType schemaType = XMLBeansContext.type(cls);
        return (schemaType != null && schemaType.isDocumentType()) ? schemaType.getDocumentElementName() : null;
    }

    public XMLBeansContext(BindingInfo bi) {
        bindingInfo = bi;
        docBuilderfactory = DocumentBuilderFactory.newInstance();
//      docBuilderfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, isXMLSecurityDisabled(secureXmlProcessing));
        docBuilderfactory.setNamespaceAware(true);
        schemaInfos = new ConcurrentHashMap<String, SchemaInfo>();
        schemaTypes = new HashMap<Class,  SchemaType>();
        arrayInfos = new HashMap<Class, ArrayInfo>();
        faultInfos = new HashMap<TypeInfo, FaultInfo>();
        javaBeanInfos = new HashMap<Class, JavaBeanInfo>(); 
        ArrayList<TypeInfo> typeInfos = new ArrayList<TypeInfo>();
        HashMap<Class, FaultInfo> exceptionMap = new HashMap<Class, FaultInfo>();
        for(JavaMethod jm : bi.getSEIModel().getJavaMethods()) for(CheckedException ce: jm.getCheckedExceptions()) {
            FaultInfo exinfo = exceptionMap.get(ce.getExceptionClass());
            if (exinfo == null)  {
                exinfo = new FaultInfo(ce, bi.getSEIModel().getTargetNamespace());
                exceptionMap.put(ce.getExceptionClass(), exinfo);
                if (exinfo.propInfo != null) for (PropInfo pi : exinfo.propInfo) addTypeInfo(typeInfos, pi.typeInfo);
                else addTypeInfo(typeInfos, exinfo.detailInfo.typeInfo);
            }
            faultInfos.put(ce.getDetailType(), exinfo);
        }
        //TODO add exception propTypes
        typeInfos.addAll(bi.typeInfos());
        for(TypeInfo ti : typeInfos) addInfo(ti);
    }

    
    void addInfo(TypeInfo ti) {
        Class<?> cls =  (Class<?>)ti.type;
        if(WrapperComposite.class.equals(cls)) return;
        if(Exception.class.isAssignableFrom(cls)) return;
        if (arrayInfos.containsKey(cls)) return;
        TypeInfo arrayWrapper = ti.getWrapperType();
        if (arrayWrapper != null && !arrayInfos.containsKey(arrayWrapper.type)) {
            arrayInfos.put((Class<?>)arrayWrapper.type, new ArrayInfo(arrayWrapper, bindingInfo.getSEIModel().getTargetNamespace()));
        }
        if (schemaType(cls) != null) return; 
        if (javaBeanInfos.containsKey(cls)) return;
        if (cls.isArray()) {
            addInfo(ti.getItemType());
            return;
        }
        JavaBeanInfo jbi = new JavaBeanInfo(cls);
        javaBeanInfos.put(cls,  jbi);
        for(JavaBeanInfo.PropInfo prop : jbi.properties) addInfo(prop.typeInfo);
    }
    
    void addTypeInfo(List<TypeInfo> typeInfos, TypeInfo ti) {
        TypeInfo item = ti.getItemType();
        if (item != null) typeInfos.add(item);
        typeInfos.add(ti);
    }
    
    SchemaType schemaType(Class<?> cls) {
        SchemaType xsdType = schemaTypes.get(cls);
        if (xsdType != null) return xsdType;
        else {
            if(XmlObject.class.isAssignableFrom(cls)) {
                try {
                    SchemaType schemaType = type(cls);
                    String xsdLocDir = null;
                    String xsdLoc = null;
                    String srcName = schemaType.getSourceName();
//                  Object impl = org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance(schemaType, null);
                    String implName = schemaType.getFullJavaImplName();
                    Class clazz = (implName!=null)? Class.forName(implName) : cls;
                    if (clazz.getProtectionDomain().getCodeSource() != null && srcName!=null) {
                        xsdLocDir = "jar:" + clazz.getProtectionDomain().getCodeSource().getLocation().toExternalForm() + "!/schemaorg_apache_xmlbeans/src/";
                        xsdLoc = xsdLocDir + srcName;
                        if (!schemaInfos.containsKey(xsdLoc)) {
                            SchemaInfo schemaInfo = new SchemaInfo();
                            schemaInfo.targetNamespace = schemaType.getName() != null ? schemaType.getName().getNamespaceURI() : 
                                schemaType.getDocumentElementName().getNamespaceURI();
//                            schemaInfo.location = new URL(xsdLoc);
                            schemaInfo.locationDir = xsdLocDir;
                            schemaInfo.locationFileName = srcName;
                            schemaInfos.put(xsdLoc, schemaInfo);
                        }
                    }
                    schemaTypes.put(cls, schemaType);
                    return schemaType;
                } catch (Exception e) {
                    throw new WebServiceException(e);
                }
            }
            return builtInSchemaTypes.get(cls);
        }
    }
    
    @Override
    public Object newWrapperInstace(Class<?> wrapperType) {
//        return getHelperContext().getDataFactory().create(wrapperType);
        return null;
    }
    
    @Override
    public Marshaller createMarshaller() throws JAXBException {
        //XMLBeans is not an JAXB implementation
        throw new UnsupportedOperationException();
    }

    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        //XMLBeans is not an JAXB implementation
        throw new UnsupportedOperationException();
    }

    @Override
    public JAXBContext getJAXBContext() {
        //XMLBeans is not an JAXB implementation
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasSwaRef() {
        return false;
    }

    @Override
    public QName getElementName(Object o) throws JAXBException {
        return null;
    }

    @Override
    public QName getElementName(Class c) throws JAXBException {
        if (XmlObject.class.equals(c)) return new QName(XsdNs, "any");
        SchemaType schemaType = schemaTypes.get(c);        
        return (schemaType != null)? schemaType.getDocumentElementName() : null;
    }

    @Override
    public XMLBridge createBridge(TypeInfo ref) {
        FaultInfo faultInfo = faultInfos.get(ref);
        return (faultInfo != null) ?  new FaultBridge(this, faultInfo) : bridge(ref);
    }
    
    XMLBridge bridge(TypeInfo ref) {
        if (WrapperComposite.class.equals(ref.type)) {
            return new com.sun.xml.ws.spi.db.WrapperBridge(this, ref);
        } else {
            if (beanBridges.containsKey(ref)) return beanBridges.get(ref);
            Class cls = (Class)ref.type;
            if (cls.isArray()) {
                ArrayInfo awi = arrayInfos.get(ref.type);
                if (awi != null) return new ArrayWrapperBridge(this, ref, awi);
            }  else if (javaBeanInfos.containsKey(cls)) {
                JavaBeanBridge jbb = new JavaBeanBridge(this, ref, javaBeanInfos.get(cls));
                return jbb;
            }
            return new XMLBeanBridge(this, ref);
        }
    }

    @Override
    public XMLBridge createFragmentBridge() {
//        return new SDOBond(this, null);
        return null;
    }

    @Override
    public <B, V> PropertyAccessor<B, V> getElementPropertyAccessor(Class<B> wrapperBean, String nsUri, String localName) throws JAXBException {
        //TODO this is only used in doc-lit wrapper when wrapperBean exists
        return null;
    }

    @Override
    public List<String> getKnownNamespaceURIs() {
        // TODO
        return new ArrayList<String>();
    }
    
    Document read(String urlStr) throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
        DocumentBuilder docBu = docBuilderfactory.newDocumentBuilder();
        return docBu.parse(new URL(urlStr).openStream());        
    }
    
    void inlineInclude(Document xsdDoc, String locationDir, HashSet<String> inlined) {
        Element schema = xsdDoc.getDocumentElement();
        NodeList includes = schema.getElementsByTagNameNS(XsdNs, "include");
        for(int i = 0; i < includes.getLength(); i++) {
            Element incl = (Element)includes.item(i);
            String schemaLocation = incl.getAttribute("schemaLocation");
            if (inlined.contains(schemaLocation)) {
                schema.removeChild(incl);   
                continue;
            } else {
                String inclFileLoc = locationDir + schemaLocation;
                try {
                    Document inclXsdDoc = read(inclFileLoc);
                    inlined.add(inclFileLoc);
                    inlineInclude(inclXsdDoc, locationDir, inlined);
                    NodeList childNodes = inclXsdDoc.getDocumentElement().getChildNodes();
                    for(int c = 0; c < childNodes.getLength(); c++) {
                        Node child = childNodes.item(c);
                        Node adopt = xsdDoc.importNode(child, true);
                        schema.insertBefore(adopt, incl);
                    }
                    schema.removeChild(incl);   
                } catch (Exception e) {
                    throw new WebServiceException(e);
                }
            }
        }
    }
    void collectImport(Document xsdDoc, String locationDir) {
        Element schema = xsdDoc.getDocumentElement();
        NodeList imports = schema.getElementsByTagNameNS(XsdNs, "import");
        for(int i = 0; i < imports.getLength(); i++) {
            Element impo = (Element)imports.item(i);
            String schemaLocation = impo.getAttribute("schemaLocation");
            String impoFileLoc = locationDir + schemaLocation;  
            if (!schemaInfos.containsKey(impoFileLoc)) {
                try {
                    Document impoXsdDoc = read(impoFileLoc);
                    SchemaInfo schemaInfo = new SchemaInfo();
                    schemaInfo.targetNamespace = impoXsdDoc.getDocumentElement().getAttribute("targetNamespace");
                    schemaInfo.locationDir = locationDir;
                    schemaInfo.locationFileName = schemaLocation;
                    schemaInfo.doc = impoXsdDoc;
                    schemaInfos.put(impoFileLoc, schemaInfo);
                    collectImport(impoXsdDoc, locationDir);
                } catch (Exception e) {
                    throw new WebServiceException(e);
                }
            }
        }
    }

    //inline include schema 
    public void generateSchema(SchemaOutputResolver outputResolver) throws IOException {
        for (SchemaInfo si : schemaInfos.values()) {
            String loc = si.locationDir + si.locationFileName;
            try {
                if (si.doc == null) si.doc = read(loc);
                collectImport(si.doc, si.locationDir);
            } catch (Exception e) {
                throw new WebServiceException(e);
            }
        }
        HashSet<String> done = new HashSet<String>();
        for (SchemaInfo si : schemaInfos.values()) {
            String loc = si.locationDir + si.locationFileName;
            if (done.contains(loc)) continue;
            Result res = outputResolver.createOutput(si.targetNamespace, loc);
            if (si.doc != null) done.add(loc);
            HashSet<String> inlined = new HashSet<String>();
            inlined.add(loc);
            inlineInclude(si.doc, si.locationDir, inlined);
            done.addAll(inlined);
            ((DOMResult)res).setNode(si.doc);
        }
        XmlBeansServiceArtifactSchemaGenerator wxsd = new XmlBeansServiceArtifactSchemaGenerator(bindingInfo.getSEIModel(), this);
        wxsd.generate(outputResolver);
    }

    
//    public void xgenerateSchema(SchemaOutputResolver outputResolver) throws IOException {
//        try {
//            TransformerFactory tf = XmlUtil.newTransformerFactory();
//            Transformer tx = tf.newTransformer();
//            for (SchemaInfo si : schemaInfos.values()) {
//                URL loc = new URL(si.locationDir + si.locationFileName);
//                Result res = outputResolver.createOutput(si.targetNamespace, loc.toExternalForm());
//                StreamSource ss = new StreamSource(loc.openStream());
//                ss.setSystemId(loc.toExternalForm());
//                tx.transform(ss, res);
//            }
//        } catch (Exception e) {
//            throw new IOException(e.getMessage());
//        }
//    }

    /**
     * http://xmlbeans.apache.org/docs/2.0.0/guide/conXMLBeansSupportBuiltInSchemaTypes.html
     * 
     */
    @Override
    public QName getTypeName(TypeInfo ti) {
        if (ti.type instanceof Class) {
            Class cls = (Class)ti.type ;
            if (XmlObject.class.equals(cls)) return null;//XmlObject is mapped to xsd:any
            SchemaType schemaType = schemaType(cls);        
            if (schemaType != null) return schemaType.getName();
//            schemaType = builtInSchemaTypes.get(cls);
//            if (schemaType != null) return schemaType.getName();
            ArrayInfo arrayInfo = (cls.isArray()) ? arrayInfos.get(cls) : null;
            if (arrayInfo != null) return arrayInfo.typeName;
            JavaBeanInfo jbi = javaBeanInfos.get(cls);
            if (jbi != null) return jbi.typeName;
        }
        return null;
    }

    //TODO how to get this?
    public String getBuildId() {
        return "Apache XmlBeans version 2.6.0-r1364789";
    }
}
