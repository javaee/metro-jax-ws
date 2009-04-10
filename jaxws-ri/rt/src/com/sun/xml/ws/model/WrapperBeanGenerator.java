/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.util.StringUtils;
import static com.sun.xml.ws.org.objectweb.asm.Opcodes.*;
import com.sun.xml.ws.org.objectweb.asm.Type;
import com.sun.xml.ws.org.objectweb.asm.*;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import javax.xml.namespace.QName;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Byte code generator for request,response wrapper and exception beans
 *
 * @author Jitendra Kotamraju
 */
public class WrapperBeanGenerator {

    private static final Logger LOGGER = Logger.getLogger(WrapperBeanGenerator.class.getName());

    private static final String PD                           = ".";
    private static final String JAXWS                        = "jaxws";
    private static final String JAXWS_PACKAGE_PD             = JAXWS+PD;
    private static final String PD_JAXWS_PACKAGE_PD          = PD+JAXWS+PD;

    private static final Class[] jaxbAnns = new Class[] {
        XmlAttachmentRef.class, XmlMimeType.class, XmlJavaTypeAdapter.class,
        XmlList.class, XmlElement.class
    };

    // Creates class's bytes
    private static byte[] createBeanImage(String className,
                               String rootName, String rootNS,
                               String typeName, String typeNS, String[] propOrder,
                               List<Field> fields) throws Exception {

        ClassWriter cw = new ClassWriter(0);
        //org.objectweb.asm.util.TraceClassVisitor cw = new org.objectweb.asm.util.TraceClassVisitor(actual, new java.io.PrintWriter(System.out));

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, replaceDotWithSlash(className), null, "java/lang/Object", null);

        AnnotationVisitor root = cw.visitAnnotation("Ljavax/xml/bind/annotation/XmlRootElement;", true);
        root.visit("name", rootName);
        root.visit("namespace", rootNS);
        root.visitEnd();

        AnnotationVisitor type = cw.visitAnnotation("Ljavax/xml/bind/annotation/XmlType;", true);
        type.visit("name", typeName);
        type.visit("namespace", typeNS);
        if (propOrder.length > 1) {
            AnnotationVisitor propVisitor = type.visitArray("propOrder");
            for(String prop : propOrder) {
                propVisitor.visit("propOrder", prop);
            }
            propVisitor.visitEnd();
        }
        type.visitEnd();

        for(Field field : fields) {
            FieldVisitor fv = cw.visitField(ACC_PUBLIC, field.fieldName, field.asmType.getDescriptor(), field.getSignature(), null);

            if (!field.noXmlElem) { // Exception fields do not have any @XmlElement
                AnnotationVisitor elem = fv.visitAnnotation("Ljavax/xml/bind/annotation/XmlElement;", true);
                elem.visit("name", field.xmlElem.name);
                elem.visit("namespace", field.xmlElem.ns);
                if (field.xmlElem.nillable) {
                    elem.visit("nillable", true);
                }
                if (field.xmlElem.required) {
                    elem.visit("required", true);
                }
                elem.visitEnd();
            }

            for(Annotation ann : field.jaxbAnnotations) {
                if (ann instanceof XmlMimeType) {
                    AnnotationVisitor mime = fv.visitAnnotation("Ljavax/xml/bind/annotation/XmlMimeType;", true);
                    mime.visit("value", ((XmlMimeType)ann).value());
                    mime.visitEnd();
                } else if (ann instanceof XmlJavaTypeAdapter) {
                    AnnotationVisitor ada = fv.visitAnnotation("Ljavax/xml/bind/annotation/adapters/XmlJavaTypeAdapter;", true);
                    ada.visit("value", getASMType(((XmlJavaTypeAdapter)ann).value()));
                    // XmlJavaTypeAdapter.type() is for package only. No need to copy.
                    // ada.visit("type", ((XmlJavaTypeAdapter)ann).type());
                    ada.visitEnd();
                } else if (ann instanceof XmlAttachmentRef) {
                    AnnotationVisitor att = fv.visitAnnotation("Ljavax/xml/bind/annotation/XmlAttachmentRef;", true);
                    att.visitEnd();
                } else if (ann instanceof XmlList) {
                    AnnotationVisitor list = fv.visitAnnotation("Ljavax/xml/bind/annotation/XmlList;", true);
                    list.visitEnd();
                } else {
                    throw new WebServiceException("Unknown JAXB annotation " + ann);
                }
            }
            
            fv.visitEnd();
        }

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        if (LOGGER.isLoggable(Level.INFO)) {
            // Class's @XmlRootElement
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            sb.append("@XmlRootElement(name=");
            sb.append(rootName);
            sb.append(", namespace=");
            sb.append(rootNS);
            sb.append(")");

            // Class's @XmlType
            sb.append("\n");
            sb.append("@XmlType(name=");
            sb.append(typeName);
            sb.append(", namespace=");
            sb.append(typeNS);
            if (propOrder.length > 1) {
                sb.append(", propOrder={");
                for(int i=0; i < propOrder.length; i++) {
                    if (i != 0) {
                        sb.append(", ");
                    }
                    sb.append(propOrder[i]);
                }
                sb.append("}");
            }
            sb.append(")");

            // class declaration
            sb.append("\n");
            sb.append("public class ");
            sb.append(className);
            sb.append(" {");

            // fields declaration
            for(Field field : fields) {
                sb.append("\n");

                // Field's @XmlRootElement decl
                if (!field.noXmlElem) {
                    sb.append("\n    ");
                    sb.append("@XmlRootElement(name=");
                    sb.append(field.xmlElem.name);
                    sb.append(", namespace=");
                    sb.append(field.xmlElem.ns);
                    if (field.xmlElem.nillable) {
                        sb.append(", nillable=true");
                    }
                    if (field.xmlElem.required) {
                        sb.append(", required=true");
                    }
                    sb.append(")");
                }

                // Field's other JAXB annotations
                for(Annotation ann : field.jaxbAnnotations) {
                    sb.append("\n    ");

                    if (ann instanceof XmlMimeType) {
                        sb.append("@XmlMimeType(value=");
                        sb.append(((XmlMimeType)ann).value());
                        sb.append(")");
                    } else if (ann instanceof XmlJavaTypeAdapter) {
                        sb.append("@XmlJavaTypeAdapter(value=");
                        sb.append(((XmlJavaTypeAdapter)ann).value());
                        sb.append(", type=");
                        sb.append(((XmlJavaTypeAdapter)ann).type());
                        sb.append(")");
                    } else if (ann instanceof XmlAttachmentRef) {
                        sb.append("@XmlAttachmentRef");
                    } else if (ann instanceof XmlList) {
                        sb.append("@XmlList");
                    } else {
                        throw new WebServiceException("Unknown JAXB annotation " + ann);
                    }
                }

                // Field declaration
                sb.append("\n    ");
                sb.append("public ");
                if (field.getSignature() == null) {
                    sb.append(field.asmType.getDescriptor());
                } else {
                    sb.append(field.getSignature());
                }
                sb.append(" ");
                sb.append(field.fieldName);
            }

            sb.append("\n\n}");
            LOGGER.fine(sb.toString());
        }

        return cw.toByteArray();
    }

    private static String replaceDotWithSlash(String name) {
        return name.replace('.', '/');
    }

    static Class createRequestWrapperBean(String className, Method method, QName reqElemName, ClassLoader cl) {

        LOGGER.fine("Request Wrapper Class : "+className);

        List<Field> fields = collectRequestWrapperMembers(method);

        String[] propOrder = getPropOrder(fields);

        byte[] image;
        try {
            image = createBeanImage(className, reqElemName.getLocalPart(), reqElemName.getNamespaceURI(),
                reqElemName.getLocalPart(), reqElemName.getNamespaceURI(), propOrder,
                fields);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }

        return Injector.inject(cl, className, image);
    }

    static Class createResponseWrapperBean(String className, Method method, QName resElemName, ClassLoader cl) {

        LOGGER.fine("Response Wrapper Class : "+className);

        List<Field> fields = collectResponseWrapperMembers(method);

        String[] propOrder = getPropOrder(fields);

        byte[] image;
        try {
            image = createBeanImage(className, resElemName.getLocalPart(), resElemName.getNamespaceURI(),
                resElemName.getLocalPart(), resElemName.getNamespaceURI(), propOrder,
                fields);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }

        return Injector.inject(cl, className, image);
    }

    private static String[] getPropOrder(List<Field> fields) {
        String[] propOrder = new String[fields.size()];
        for(int i=0; i < fields.size(); i++) {
            propOrder[i] = fields.get(i).fieldName;
        }
        return propOrder;
    }

    private static List<Field> collectRequestWrapperMembers(Method method) {

        List<Field> fields = new ArrayList<Field>();
        Annotation[][] paramAnns = method.getParameterAnnotations();
        java.lang.reflect.Type[] paramTypes = method.getGenericParameterTypes();
        Type[] asmTypes = Type.getArgumentTypes(method);
        for(int i=0; i < paramTypes.length; i++) {
            WebParam webParam = findAnnotation(paramAnns[i], WebParam.class);
            if (webParam != null && webParam.header()) {
                continue;
            }
            List<Annotation> jaxb = collectJAXBAnnotations(paramAnns[i]);

            java.lang.reflect.Type paramType =  getHolderValueType(paramTypes[i]);
            Type asmType = isHolder(paramTypes[i]) ? getASMType(paramType) : asmTypes[i];

            String paramNamespace = "";
            String paramName =  "arg"+i;
            WebParam.Mode mode = WebParam.Mode.IN;
            if (webParam != null) {
                mode = webParam.mode();
                if (webParam.name().length() > 0)
                    paramName = webParam.name();
                if (webParam.targetNamespace().length() > 0)
                    paramNamespace = webParam.targetNamespace();
            }

            String fieldName = JAXBRIContext.mangleNameToVariableName(paramName);
            //We wont have to do this if JAXBRIContext.mangleNameToVariableName() takes
            //care of mangling java reserved keywords
            fieldName = getJavaReservedVarialbeName(fieldName);

            Field memInfo = new Field(fieldName, paramType, asmType, paramName, paramNamespace, jaxb);

            if (mode.equals(WebParam.Mode.IN) || mode.equals(WebParam.Mode.INOUT)) {
                fields.add(memInfo);
            }

            adjustXmlElement(jaxb, memInfo);
        }
        return fields;
    }

    private static List<Field> collectResponseWrapperMembers(Method method) {

        List<Field> fields = new ArrayList<Field>();

        // Collect all OUT, INOUT parameters as fields
        Annotation[][] paramAnns = method.getParameterAnnotations();
        java.lang.reflect.Type[] paramTypes = method.getGenericParameterTypes();
        Type[] asmTypes = Type.getArgumentTypes(method);
        for(int i=0; i < paramTypes.length; i++) {
            WebParam webParam = findAnnotation(paramAnns[i], WebParam.class);
            if (webParam != null) {
                if (webParam.header() || webParam.mode() == WebParam.Mode.IN) {
                    continue;
                }
            }
            if (!isHolder(paramTypes[i])) {
                continue;
            }

            List<Annotation> jaxb = collectJAXBAnnotations(paramAnns[i]);

            java.lang.reflect.Type paramType = getHolderValueType(paramTypes[i]);
            Type asmType = getASMType(paramType);

            String paramNamespace = "";
            String paramName =  "arg"+i;

            if (webParam != null) {
                if (webParam.name().length() > 0)
                    paramName = webParam.name();
                if (webParam.targetNamespace().length() > 0)
                    paramNamespace = webParam.targetNamespace();
            }

            String fieldName = JAXBRIContext.mangleNameToVariableName(paramName);
            //We wont have to do this if JAXBRIContext.mangleNameToVariableName() takes
            //care of mangling java reserved keywords
            fieldName = getJavaReservedVarialbeName(fieldName);
            Field f = new Field(fieldName, paramType, asmType, paramName, paramNamespace, jaxb);
            fields.add(f);
            adjustXmlElement(jaxb, f);
        }

        WebResult webResult = method.getAnnotation(WebResult.class);
        java.lang.reflect.Type returnType = method.getGenericReturnType();
        Type asmType = Type.getReturnType(method);
        if (!((webResult != null && webResult.header()) || returnType == Void.TYPE)) {
            String fieldElementName = "return";
            String fieldName = "_return";
            String fieldNamespace = "";

            if (webResult != null) {
                if (webResult.name().length() > 0) {
                    fieldElementName = webResult.name();
                    fieldName = JAXBRIContext.mangleNameToVariableName(webResult.name());
                    //We wont have to do this if JAXBRIContext.mangleNameToVariableName() takes
                    //care of mangling java identifiers
                    fieldName = getJavaReservedVarialbeName(fieldName);
                }
                if (webResult.targetNamespace().length() > 1) {
                    fieldNamespace = webResult.targetNamespace();
                }
            }

            List<Annotation> jaxb = collectJAXBAnnotations(method.getAnnotations());

            Field f = new Field(fieldName, returnType, asmType, fieldElementName, fieldNamespace, jaxb);
            fields.add(f);
            adjustXmlElement(jaxb, f);
        }
        return fields;
    }

    private static boolean isHolder(java.lang.reflect.Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType)type;
            if (p.getRawType().equals(Holder.class)) {
                return true;
            }
        }
        return false;
    }

    private static Type getASMType(java.lang.reflect.Type t) {
        assert t!=null;

        if (t instanceof Class) {
            return Type.getType((Class)t);
        }

        if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)t;
            if (pt.getRawType() instanceof Class) {
                return Type.getType((Class)pt.getRawType());
            }
        }
        if (t instanceof GenericArrayType) {
            // TODO
        }

        if (t instanceof WildcardType) {
            // TODO
        }
        if (t instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable)t;
            if (tv.getBounds()[0] instanceof Class) {
                return Type.getType((Class)tv.getBounds()[0]);
            }
        }

        // covered all the cases
        assert false;
        throw new IllegalArgumentException("Not creating ASM Type for type = "+t);
    }

    private static java.lang.reflect.Type getHolderValueType(java.lang.reflect.Type paramType) {
        if (paramType instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType)paramType;
            if (p.getRawType().equals(Holder.class)) {
                return p.getActualTypeArguments()[0];
            }
        }
        return paramType;
    }

    private static <T extends Annotation> T findAnnotation(Annotation[] anns, Class<T> annotationClass) {
        for(Annotation a : anns) {
            if (a.annotationType() == annotationClass) {
                return (T)a;
            }
        }
        return null;
    }

    static Class createExceptionBean(String className, Class exception, String typeNS, String elemName, String elemNS, ClassLoader cl) {

        List<Field> fields = collectExceptionProperties(exception);
        String[] propOrder = getPropOrder(fields);
        
        byte[] image;
        try {
            image = createBeanImage(className, elemName, elemNS,
                exception.getSimpleName(), typeNS, propOrder,
                fields);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }

        return Injector.inject(cl, className, image);
    }

    private static List<Field> collectExceptionProperties(Class exception) {
        List<Field> fields = new ArrayList<Field>();

        Method[] methods = exception.getMethods();
        for (Method method : methods) {
            int mod = method.getModifiers();
            if (!Modifier.isPublic(mod)
                || (Modifier.isFinal(mod) && Modifier.isStatic(mod))
                || Modifier.isTransient(mod)) { // no final static, transient, non-public
                continue;
            }
            String name = method.getName();
            if (!(name.startsWith("get") || name.startsWith("is")) || skipProperties.contains(name) ||
                name.equals("get") || name.equals("is")) {
                // Don't bother with invalid propertyNames.
                continue;
            }

            java.lang.reflect.Type[] paramTypes = method.getGenericParameterTypes();
            java.lang.reflect.Type returnType = method.getGenericReturnType();
            Type asmType = Type.getReturnType(method);
            if (paramTypes.length == 0) {
                if (name.startsWith("get")) {
                    String fieldName = StringUtils.decapitalize(name.substring(3));
                    Field field = new Field(fieldName, returnType, asmType, true, Collections.<Annotation>emptyList());
                    fields.add(field);
                } else {
                    String fieldName = StringUtils.decapitalize(name.substring(2));
                    Field field = new Field(fieldName, returnType, asmType, true, Collections.<Annotation>emptyList());
                    fields.add(field);
                }
            }
        }
        Collections.sort(fields);
        return fields;
    }


    private static List<Annotation> collectJAXBAnnotations(Annotation[] anns) {
        List<Annotation> jaxbAnnotation = new ArrayList<Annotation>();
        for(Class c : jaxbAnns) {
            Annotation a = findAnnotation(anns, c);
            if (a != null) {
                jaxbAnnotation.add(a);
            }
        }
        return jaxbAnnotation;
    }

    private static void adjustXmlElement(List<Annotation> list, Field f) {
        XmlElement elemAnn = null;
        for(Annotation a : list) {
            if (a.annotationType() == XmlElement.class) {
                elemAnn = (XmlElement)a;
                list.remove(a);
                break;
            }
        }

        f.xmlElem.name = (elemAnn != null && !elemAnn.name().equals("##default"))
                ? elemAnn.name() : f.xmlElem.name;

        f.xmlElem.ns = (elemAnn != null && !elemAnn.namespace().equals("##default"))
                ? elemAnn.namespace() : f.xmlElem.ns;

        f.xmlElem.nillable = f.xmlElem.nillable
                || (elemAnn != null && elemAnn.nillable());

        f.xmlElem.required = elemAnn != null && elemAnn.required();

    }

    private static class Field implements Comparable<Field> {
        private final java.lang.reflect.Type reflectType;
        private final Type asmType;
        private final String fieldName;
        private final List<Annotation> jaxbAnnotations;
        private final boolean noXmlElem;
        private final FieldXmlElement xmlElem;

        Field(String paramName, java.lang.reflect.Type paramType, Type asmType, String elementName,
              String elementNS, List<Annotation> jaxbAnnotations) {
            this(paramName, paramType, asmType, false, elementName, elementNS, jaxbAnnotations);
        }

        Field(String paramName, java.lang.reflect.Type paramType, Type asmType, boolean noXmlElem,
              List<Annotation> jaxbAnnotations) {
            this(paramName, paramType, asmType, noXmlElem, null,null, jaxbAnnotations);
        }

        Field(String paramName, java.lang.reflect.Type paramType, Type asmType, boolean noXmlElem,
              String elementName, String elementNS, List<Annotation> jaxbAnnotations) {
            this.reflectType = paramType;
            this.asmType = asmType;
            this.fieldName = paramName;
            this.noXmlElem = noXmlElem;
            boolean nillable = reflectType instanceof GenericArrayType;
            if (!nillable && reflectType instanceof Class) {
                nillable = ((Class)reflectType).isArray();
            }
            this.xmlElem = new FieldXmlElement(elementName, elementNS, nillable);
            this.jaxbAnnotations = jaxbAnnotations;
        }

        String getSignature() {
            if (reflectType instanceof Class) {
                return null;
            }
            if (reflectType instanceof TypeVariable) {
                return null;
            }
            return FieldSignature.vms(reflectType);
        }

        boolean isXmlElem() {
            if (noXmlElem)
                return false;
            return (!fieldName.equals(xmlElem.name) || !xmlElem.nillable || !xmlElem.required);
        }

        public int compareTo(Field o) {
            return fieldName.compareTo(o.fieldName);
        }

        static class FieldXmlElement {
            String name;
            String ns;
            boolean nillable;
            boolean required;

            FieldXmlElement(String name, String ns, boolean nillable) {
                this.name = name;
                this.ns = ns;
                this.nillable = nillable;
            }
        }

    }

    // TODO MOVE Names to runtime (instead of doing the following)

    /**
     * See if its a java keyword name, if so then mangle the name
     */
    private static @NotNull String getJavaReservedVarialbeName(@NotNull String name) {
        String reservedName = reservedWords.get(name);
        return reservedName == null ? name : reservedName;
    }

    private static final Map<String, String> reservedWords;

    static {
        reservedWords = new HashMap<String, String>();
        reservedWords.put("abstract", "_abstract");
        reservedWords.put("assert", "_assert");
        reservedWords.put("boolean", "_boolean");
        reservedWords.put("break", "_break");
        reservedWords.put("byte", "_byte");
        reservedWords.put("case", "_case");
        reservedWords.put("catch", "_catch");
        reservedWords.put("char", "_char");
        reservedWords.put("class", "_class");
        reservedWords.put("const", "_const");
        reservedWords.put("continue", "_continue");
        reservedWords.put("default", "_default");
        reservedWords.put("do", "_do");
        reservedWords.put("double", "_double");
        reservedWords.put("else", "_else");
        reservedWords.put("extends", "_extends");
        reservedWords.put("false", "_false");
        reservedWords.put("final", "_final");
        reservedWords.put("finally", "_finally");
        reservedWords.put("float", "_float");
        reservedWords.put("for", "_for");
        reservedWords.put("goto", "_goto");
        reservedWords.put("if", "_if");
        reservedWords.put("implements", "_implements");
        reservedWords.put("import", "_import");
        reservedWords.put("instanceof", "_instanceof");
        reservedWords.put("int", "_int");
        reservedWords.put("interface", "_interface");
        reservedWords.put("long", "_long");
        reservedWords.put("native", "_native");
        reservedWords.put("new", "_new");
        reservedWords.put("null", "_null");
        reservedWords.put("package", "_package");
        reservedWords.put("private", "_private");
        reservedWords.put("protected", "_protected");
        reservedWords.put("public", "_public");
        reservedWords.put("return", "_return");
        reservedWords.put("short", "_short");
        reservedWords.put("static", "_static");
        reservedWords.put("strictfp", "_strictfp");
        reservedWords.put("super", "_super");
        reservedWords.put("switch", "_switch");
        reservedWords.put("synchronized", "_synchronized");
        reservedWords.put("this", "_this");
        reservedWords.put("throw", "_throw");
        reservedWords.put("throws", "_throws");
        reservedWords.put("transient", "_transient");
        reservedWords.put("true", "_true");
        reservedWords.put("try", "_try");
        reservedWords.put("void", "_void");
        reservedWords.put("volatile", "_volatile");
        reservedWords.put("while", "_while");
        reservedWords.put("enum", "_enum");
    }

    private static final Set<String> skipProperties = new HashSet<String>();
    static{
        skipProperties.add("getCause");
        skipProperties.add("getLocalizedMessage");
        skipProperties.add("getClass");
        skipProperties.add("getStackTrace");
    }

}
