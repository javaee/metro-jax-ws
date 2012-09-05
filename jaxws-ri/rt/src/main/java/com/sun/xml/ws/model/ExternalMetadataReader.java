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

package com.sun.xml.ws.model;

import com.oracle.xmlns.webservices.jaxws_databinding.JavaMethod;
import com.oracle.xmlns.webservices.jaxws_databinding.JavaParam;
import com.oracle.xmlns.webservices.jaxws_databinding.JavaWsdlMappingType;
import com.oracle.xmlns.webservices.jaxws_databinding.ObjectFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBResult;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oracle.xmlns.webservices.jaxws_databinding.ExistingAnnotationsType.MERGE;

/**
 * Metadata Reader able to read from either class annotations or external metadata files or combine both,
 * depending on configuration provided in xml file itself.
 *
 * @author shih-chang.chen@oracle.com, miroslav.kos@oracle.com
 */
public class ExternalMetadataReader extends ReflectAnnotationReader {

    private static final String NAMESPACE_WEBLOGIC_WSEE_DATABINDING = "http://xmlns.oracle.com/weblogic/weblogic-wsee-databinding";
    private static final String NAMESPACE_JAXWS_RI_EXTERNAL_METADATA = "http://xmlns.oracle.com/webservices/jaxws-databinding";

    /**
     * map of readers for defined java types
     */
    private Map<String, JavaWsdlMappingType> readers = new HashMap<String, JavaWsdlMappingType>();

    public ExternalMetadataReader(Collection<File> files, Collection<String> resourcePaths, ClassLoader classLoader, boolean xsdValidation) {

        if (files != null) {
            for (File file : files) {
                try {
                    String namespace = Util.documentRootNamespace(newSource(file));
                    JavaWsdlMappingType externalMapping = parseMetadata(xsdValidation, newSource(file), namespace);
                    readers.put(externalMapping.getJavaTypeName(), externalMapping);
                } catch (Exception e) {
                    throw new RuntimeModelerException("runtime.modeler.external.metadata.unable.to.read", file.getAbsolutePath());
                }
            }
        }

        if (resourcePaths != null) {
            for (String resourcePath : resourcePaths) {
                try {
                    String namespace = Util.documentRootNamespace(newSource(resourcePath, classLoader));
                    JavaWsdlMappingType externalMapping = parseMetadata(xsdValidation, newSource(resourcePath, classLoader), namespace);
                    readers.put(externalMapping.getJavaTypeName(), externalMapping);
                } catch (Exception e) {
                    throw new RuntimeModelerException("runtime.modeler.external.metadata.unable.to.read", resourcePath);
                }
            }
        }
    }

    private StreamSource newSource(String resourcePath, ClassLoader classLoader) {
        InputStream is = classLoader.getResourceAsStream(resourcePath);
        return new StreamSource(is);
    }

    private JavaWsdlMappingType parseMetadata(boolean xsdValidation, StreamSource source, String namespace) throws JAXBException, IOException, TransformerException {
        if (NAMESPACE_WEBLOGIC_WSEE_DATABINDING.equals(namespace)) {
            return Util.transform(source);
        } if (NAMESPACE_JAXWS_RI_EXTERNAL_METADATA.equals(namespace)) {
            return Util.read(source, xsdValidation);
        } else {
            throw new RuntimeModelerException("runtime.modeler.external.metadata.unsupported.schema", namespace, Arrays.asList(NAMESPACE_WEBLOGIC_WSEE_DATABINDING, NAMESPACE_JAXWS_RI_EXTERNAL_METADATA).toString());
        }
    }

    private StreamSource newSource(File file) {
        try {
            return new StreamSource(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeModelerException("runtime.modeler.external.metadata.unable.to.read", file.getAbsolutePath());
        }
    }

    public <A extends Annotation> A getAnnotation(Class<A> annType, Class<?> cls) {
        JavaWsdlMappingType r = reader(cls);
        return r == null ? super.getAnnotation(annType, cls) : Util.annotation(r, annType);
    }

    private JavaWsdlMappingType reader(Class<?> cls) {
        return readers.get(cls.getName());
    }

    Annotation[] getAnnotations(List<Object> objects) {
        ArrayList<Annotation> list = new ArrayList<Annotation>();
        for (Object a : objects) {
            if (Annotation.class.isInstance(a)) {
                list.add(Annotation.class.cast(a));
            }
        }
        return list.toArray(new Annotation[list.size()]);
    }

    public Annotation[] getAnnotations(final Class<?> c) {

        Merger<Annotation[]> merger = new Merger<Annotation[]>(reader(c)) {
            Annotation[] reflection() {
                return ExternalMetadataReader.super.getAnnotations(c);
            }

            Annotation[] external() {
                return getAnnotations(reader.getClassAnnotation());
            }
        };
        return merger.merge();
    }

    public Annotation[] getAnnotations(final Method m) {
        Merger<Annotation[]> merger = new Merger<Annotation[]>(reader(m.getDeclaringClass())) {
            Annotation[] reflection() {
                return ExternalMetadataReader.super.getAnnotations(m);
            }

            Annotation[] external() {
                JavaMethod jm = getJavaMethod(m, reader);
                return (jm == null) ? new Annotation[0] : getAnnotations(jm.getMethodAnnotation());
            }
        };
        return merger.merge();
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(final Class<A> annType, final Method m) {
        Merger<Annotation> merger = new Merger<Annotation>(reader(m.getDeclaringClass())) {
            Annotation reflection() {
                return ExternalMetadataReader.super.getAnnotation(annType, m);
            }

            Annotation external() {
                JavaMethod jm = getJavaMethod(m, reader);
                return Util.annotation(jm, annType);
            }
        };
        return (A) merger.merge();
    }

    public Annotation[][] getParameterAnnotations(final Method m) {
        Merger<Annotation[][]> merger = new Merger<Annotation[][]>(reader(m.getDeclaringClass())) {
            Annotation[][] reflection() {
                return ExternalMetadataReader.super.getParameterAnnotations(m);
            }

            Annotation[][] external() {
                JavaMethod jm = getJavaMethod(m, reader);
                Annotation[][] a = m.getParameterAnnotations();
                for (int i = 0; i < m.getParameterTypes().length; i++) {
                    if (jm == null) continue;
                    JavaParam jp = jm.getJavaParams().getJavaParam().get(i);
                    a[i] = getAnnotations(jp.getParamAnnotation());
                }
                return a;
            }
        };
        return merger.merge();
    }

    public void getProperties(final Map<String, Object> prop, final Class<?> cls) {

        JavaWsdlMappingType r = reader(cls);

        // no external reader or it requires annotations merging ...
        if (r == null || MERGE.equals(r.getExistingAnnotations())) {
            super.getProperties(prop, cls);
        }

        // TODO-Miran: handle eclipselink-oxm-xml here ...
    }

    public void getProperties(final Map<String, Object> prop, final Method m) {

        JavaWsdlMappingType r = reader(m.getDeclaringClass());

        // no external reader or it requires annotations merging ...
        if (r == null || MERGE.equals(r.getExistingAnnotations())) {
            super.getProperties(prop, m);
        }

        if (r != null) {
            JavaMethod jm = getJavaMethod(m, r);
            Element[] e = Util.annotation(jm);
            prop.put("eclipselink-oxm-xml.xml-element", findXmlElement(e));
        }

    }

    public void getProperties(final Map<String, Object> prop, final Method m, int pos) {

        JavaWsdlMappingType r = reader(m.getDeclaringClass());

        // no external reader or it requires annotations merging ...
        if (r == null || MERGE.equals(r.getExistingAnnotations())) {
            super.getProperties(prop, m, pos);
        }

        if (r != null) {
            JavaMethod jm = getJavaMethod(m, r);
            if (jm == null) return;
            JavaParam jp = jm.getJavaParams().getJavaParam().get(pos);
            Element[] e = Util.annotation(jp);
            prop.put("eclipselink-oxm-xml.xml-element", findXmlElement(e));
        }
    }

    JavaMethod getJavaMethod(Method method, JavaWsdlMappingType r) {

        JavaWsdlMappingType.JavaMethods javaMethods = r.getJavaMethods();
        if (javaMethods == null) {
            return null;
        }

        List<JavaMethod> sameName = new ArrayList<JavaMethod>();
        for (JavaMethod jm : javaMethods.getJavaMethod()) {
            if (method.getName().equals(jm.getName())) {
                sameName.add(jm);
            }
        }

        if (sameName.isEmpty()) {
            return null;
        } else {
            if (sameName.size() == 1) {
                return sameName.get(0);
            } else {
                Class<?>[] argCls = method.getParameterTypes();
                for (JavaMethod jm : sameName) {
                    JavaMethod.JavaParams params = jm.getJavaParams();
                    if (params != null && params.getJavaParam() != null && params.getJavaParam().size() == argCls.length) {
                        int count = 0;
                        for (int i = 0; i < argCls.length; i++) {
                            JavaParam jp = params.getJavaParam().get(i);
                            if (argCls[i].getName().equals(jp.getJavaType())) {
                                count++;
                            }
                        }
                        if (count == argCls.length) {
                            return jm;
                        }
                    }
                }
            }
        }
        return null;
    }

    Element findXmlElement(Element[] xa) {
        if (xa == null) return null;
        for (Element e : xa) {
            if (e.getLocalName().equals("java-type")) return e;
            if (e.getLocalName().equals("xml-element")) return e;
        }
        return null;
    }

    /**
     * Helper class to merge two different arrays of annotation objects. It merges annotations based on attribute
     * <code>existing-annotations</code> in external customization file.
     * <p/>
     * We suppose that in the result array there wouldn't be two annotations of same type:
     * annotation.annotationType().getName(); if there are found such annotations the one from reflection is
     * considered overriden and is thrown away.
     * <p/>
     * The helper can work either with one and two dimensional array, but it can be used for two single Annotation
     * objects;
     */
    static abstract class Merger<T> {

        JavaWsdlMappingType reader;

        Merger(JavaWsdlMappingType r) {
            this.reader = r;
        }

        abstract T reflection();

        abstract T external();

        @SuppressWarnings("unchecked")
        T merge() {
            T reflection = reflection();
            if (reader == null) {
                return reflection;
            }

            T external = external();
            if (!MERGE.equals(reader.getExistingAnnotations())) {
                return external;
            }

            if (reflection instanceof Annotation) {
                return (T) doMerge((Annotation) reflection, (Annotation) external);
            } else if (reflection instanceof Annotation[][]) {
                return (T) doMerge((Annotation[][]) reflection, (Annotation[][]) external);
            } else {
                return (T) doMerge((Annotation[]) reflection, (Annotation[]) external);
            }
        }

        private Annotation doMerge(Annotation reflection, Annotation external) {
            return external != null ? external : reflection;
        }

        private Annotation[][] doMerge(Annotation[][] reflection, Annotation[][] external) {
            for (int i = 0; i < reflection.length; i++) {
                reflection[i] = doMerge(reflection[i], external.length > i ? external[i] : null);
            }
            return reflection;
        }

        private Annotation[] doMerge(Annotation[] annotations, Annotation[] externalAnnotations) {
            HashMap<String, Annotation> mergeMap = new HashMap<String, Annotation>();
            if (annotations != null) {
                for (Annotation reflectionAnnotation : annotations) {
                    mergeMap.put(reflectionAnnotation.annotationType().getName(), reflectionAnnotation);
                }
            }

            // overriding happens here, based on annotationType().getName() ...
            if (externalAnnotations != null) {
                for (Annotation externalAnnotation : externalAnnotations) {
                    mergeMap.put(externalAnnotation.annotationType().getName(), externalAnnotation);
                }
            }
            Collection<Annotation> values = mergeMap.values();
            int size = values.size();
            return size == 0 ? null : values.toArray(new Annotation[size]);
        }

    }

    static class Util {

        static final String SchemaFileName = "jaxws-databinding.xsd";
        static Schema schema;
        static JAXBContext jaxbContext;

        static {
            SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            try {
                URL xsdUrl = Util.class.getClassLoader().getResource(SchemaFileName);
                if (xsdUrl != null) {
                    schema = sf.newSchema(xsdUrl);
                }
            } catch (SAXException e1) {
                //      e1.printStackTrace();
            }

            @SuppressWarnings("unchecked")
            Class[] cls = {ObjectFactory.class};
            try {
                jaxbContext = JAXBContext.newInstance(cls);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("unchecked")
        public static JavaWsdlMappingType read(Source src, boolean xsdValidation) throws IOException, JAXBException {
            try {
                Unmarshaller um = jaxbContext.createUnmarshaller();
                if (xsdValidation) {
                    if (schema == null) {
                        //TODO 0 warning for schema == null
                    }
                    um.setSchema(schema);
                }
                Object o = um.unmarshal(src);
                return getJavaWsdlMapping(o);
            } catch (JAXBException e) {
                // throw new
                // WebServiceException(WsDatabindingMessages.mappingFileCannotRead
                // (src.getSystemId()), e);
                URL url = new URL(src.getSystemId());
                Source s = new StreamSource(url.openStream());
                Unmarshaller um = jaxbContext.createUnmarshaller();
                if (xsdValidation) {
                    if (schema == null) {
                        //TODO 0 warning for schema == null
                    }
                    um.setSchema(schema);
                }
                Object o = um.unmarshal(s);
                return getJavaWsdlMapping(o);
            }
        }

        public static JavaWsdlMappingType transform(Source src) throws TransformerException, JAXBException {
            Source xsl = new StreamSource(Util.class.getResourceAsStream("/jaxws-databinding-translate-namespaces.xml"));
            JAXBResult result = new JAXBResult(jaxbContext);
            Transformer transformer = TransformerFactory.newInstance().newTemplates(xsl).newTransformer();

            transformer.transform(src, result);
            return getJavaWsdlMapping(result.getResult());
        }


        static JavaWsdlMappingType getJavaWsdlMapping(Object o) {
            Object val = (o instanceof JAXBElement) ? ((JAXBElement) o).getValue() : o;
            if (val instanceof JavaWsdlMappingType) return (JavaWsdlMappingType) val;
    //    else if (val instanceof JavaWsdlMappings)
    //      for (JavaWsdlMappingType m: ((JavaWsdlMappings) val).getJavaWsdlMapping())
    //        if (seiName.equals(m.javaTypeName)) return m;
            return null;
        }

        static <T> T findInstanceOf(Class<T> type, List<Object> objects) {
            for (Object o : objects) {
                if (type.isInstance(o)) {
                    return type.cast(o);
                }
            }
            return null;
        }

        static public <T> T annotation(JavaWsdlMappingType jwse, Class<T> anntype) {
            if (jwse == null || jwse.getClassAnnotation() == null) {
                return null;
            }
            return findInstanceOf(anntype, jwse.getClassAnnotation());
        }

        static public <T> T annotation(JavaMethod jm, Class<T> anntype) {
            if (jm == null || jm.getMethodAnnotation() == null) {
                return null;
            }
            return findInstanceOf(anntype, jm.getMethodAnnotation());
        }

        static public <T> T annotation(JavaParam jp, Class<T> anntype) {
            if (jp == null || jp.getParamAnnotation() == null) {
                return null;
            }
            return findInstanceOf(anntype, jp.getParamAnnotation());
        }

        static public Element[] annotation(JavaMethod jm) {
            if (jm == null || jm.getMethodAnnotation() == null) {
                return null;
            }
            return findElements(jm.getMethodAnnotation());
        }

        static public Element[] annotation(JavaParam jp) {
            if (jp == null || jp.getParamAnnotation() == null) {
                return null;
            }
            return findElements(jp.getParamAnnotation());
        }

        private static Element[] findElements(List<Object> objects) {
            List<Element> elems = new ArrayList<Element>();
            for (Object o : objects) {
                if (o instanceof Element) {
                    elems.add((Element) o);
                }
            }
            return elems.toArray(new Element[elems.size()]);
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
            return classForName(name, Thread.currentThread().getContextClassLoader());
        }

        static String documentRootNamespace(Source src) throws XMLStreamException {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader streamReader = factory.createXMLStreamReader(src);
            XMLStreamReaderUtil.nextElementContent(streamReader);
            String namespaceURI = streamReader.getName().getNamespaceURI();
            XMLStreamReaderUtil.close(streamReader);
            return namespaceURI;
        }
    }


}
