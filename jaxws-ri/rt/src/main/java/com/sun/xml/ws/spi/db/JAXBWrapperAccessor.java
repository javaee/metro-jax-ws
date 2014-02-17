/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.spi.db;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 * JAXBWrapperAccessor
 * 
 * @author shih-chang.chen@oracle.com
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JAXBWrapperAccessor extends WrapperAccessor {

    protected Class<?> contentClass;
    protected HashMap<Object, Class> elementDeclaredTypes;

    public JAXBWrapperAccessor(Class<?> wrapperBean) {
        contentClass = (Class<?>) wrapperBean;

        HashMap<Object, PropertySetter> setByQName = new HashMap<Object, PropertySetter>();
        HashMap<Object, PropertySetter> setByLocalpart = new HashMap<Object, PropertySetter>();
        HashMap<String, Method> publicSetters = new HashMap<String, Method>();

        HashMap<Object, PropertyGetter> getByQName = new HashMap<Object, PropertyGetter>();
        HashMap<Object, PropertyGetter> getByLocalpart = new HashMap<Object, PropertyGetter>();
        HashMap<String, Method> publicGetters = new HashMap<String, Method>();

        HashMap<Object, Class> elementDeclaredTypesByQName = new HashMap<Object, Class>();
        HashMap<Object, Class> elementDeclaredTypesByLocalpart = new HashMap<Object, Class>();

        for (Method method : contentClass.getMethods()) {
            if (PropertySetterBase.setterPattern(method)) {
                String key = method.getName()
                        .substring(3, method.getName().length()).toLowerCase();
                publicSetters.put(key, method);
            }
            if (PropertyGetterBase.getterPattern(method)) {
                String methodName = method.getName();
                String key = methodName.startsWith("is") ? methodName
                        .substring(2, method.getName().length()).toLowerCase()
                        : methodName.substring(3, method.getName().length())
                                .toLowerCase();
                publicGetters.put(key, method);
            }
        }
        HashSet<String> elementLocalNames = new HashSet<String>();
        for (Field field : getAllFields(contentClass)) {
            XmlElementWrapper xmlElemWrapper = field.getAnnotation(XmlElementWrapper.class);
            XmlElement xmlElem = field.getAnnotation(XmlElement.class);
            XmlElementRef xmlElemRef = field.getAnnotation(XmlElementRef.class);
            String fieldName = field.getName().toLowerCase();
            String namespace = "";
            String localName = field.getName();
            if (xmlElemWrapper != null) {
                namespace = xmlElemWrapper.namespace();
                if (xmlElemWrapper.name() != null && !xmlElemWrapper.name().equals("")
                        && !xmlElemWrapper.name().equals("##default")) {
                    localName = xmlElemWrapper.name();
                }            	
            }else if (xmlElem != null) {
                namespace = xmlElem.namespace();
                if (xmlElem.name() != null && !xmlElem.name().equals("")
                        && !xmlElem.name().equals("##default")) {
                    localName = xmlElem.name();
                }
            } else if (xmlElemRef != null) {
                namespace = xmlElemRef.namespace();
                if (xmlElemRef.name() != null && !xmlElemRef.name().equals("")
                        && !xmlElemRef.name().equals("##default")) {
                    localName = xmlElemRef.name();
                }
            }
            if (elementLocalNames.contains(localName)) {
                this.elementLocalNameCollision = true;
            } else {
                elementLocalNames.add(localName);
            }

            QName qname = new QName(namespace, localName);
            if (field.getType().equals(JAXBElement.class)) {
                if (field.getGenericType() instanceof ParameterizedType) {
                    Type arg = ((ParameterizedType) field.getGenericType())
                            .getActualTypeArguments()[0];
                    if (arg instanceof Class) {
                        elementDeclaredTypesByQName.put(qname, (Class) arg);
                        elementDeclaredTypesByLocalpart.put(localName,
                                (Class) arg);
                    } else if (arg instanceof GenericArrayType) {
                        Type componentType = ((GenericArrayType) arg)
                                .getGenericComponentType();
                        if (componentType instanceof Class) {
                            Class arrayClass = Array.newInstance(
                                    (Class) componentType, 0).getClass();
                            elementDeclaredTypesByQName.put(qname, arrayClass);
                            elementDeclaredTypesByLocalpart.put(localName,
                                    arrayClass);
                        }
                    }
                }

            }
            Method setMethod = accessor(publicSetters, fieldName, localName);
            Method getMethod = accessor(publicGetters, fieldName, localName);
            if ( isProperty(field, getMethod, setMethod) ) {
                PropertySetter setter = createPropertySetter(field, setMethod);
                PropertyGetter getter = createPropertyGetter(field, getMethod);
                setByQName.put(qname, setter);
                setByLocalpart.put(localName, setter);
                getByQName.put(qname, getter);
                getByLocalpart.put(localName, getter);
            }
        }
        if (this.elementLocalNameCollision) {
            this.propertySetters = setByQName;
            this.propertyGetters = getByQName;
            elementDeclaredTypes = elementDeclaredTypesByQName;
        } else {
            this.propertySetters = setByLocalpart;
            this.propertyGetters = getByLocalpart;
            elementDeclaredTypes = elementDeclaredTypesByLocalpart;
        }
    }

    static private Method accessor(HashMap<String, Method> map, String fieldName, String localName) {
        Method a = map.get(fieldName);
        if (a == null) a = map.get(localName);
        if (a == null && fieldName.startsWith("_")) a = map.get(fieldName.substring(1));
        return a;
    }

    static private boolean isProperty(Field field, Method getter, Method setter) {
        if (java.lang.reflect.Modifier.isPublic(field.getModifiers())) return true;
        if (getter == null) return false;
        if (setter == null) {
            return java.util.Collection.class.isAssignableFrom(field.getType()) ||
                   java.util.Map.class.isAssignableFrom(field.getType()) ; 
        } else {
            return true;    
        }
    }

    static private List<Field> getAllFields(Class<?> clz) {
        List<Field> list = new ArrayList<Field>();
        while (!Object.class.equals(clz)) {
            list.addAll(Arrays.asList(getDeclaredFields(clz)));
            clz = clz.getSuperclass();
        }
        return list;
    }
    
    static private Field[] getDeclaredFields(final Class<?> clz) {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Field[]>() {
                        @Override
                        public Field[] run() throws IllegalAccessException {
                            return clz.getDeclaredFields();
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw new WebServiceException(e);
        }
    }

    static private PropertyGetter createPropertyGetter(Field field, Method getMethod) {
        if (!field.isAccessible()) {
            if (getMethod != null) {
                MethodGetter methodGetter = new MethodGetter(getMethod);
                if (methodGetter.getType().toString().equals(field.getType().toString())) {
                    return methodGetter;
                }
            }
        }
        return new PrivFieldGetter(field);
    }

    static private PropertySetter createPropertySetter(Field field,
            Method setter) {
        if (!field.isAccessible()) {
            if (setter != null) {
                MethodSetter injection = new MethodSetter(setter);
                if (injection.getType().toString().equals(field.getType().toString())) {
                    return injection;
                }
            }
        }
        return new PrivFieldSetter(field);
    }

    private Class getElementDeclaredType(QName name) {
        Object key = (this.elementLocalNameCollision) ? name : name
                .getLocalPart();
        return elementDeclaredTypes.get(key);
    }

    @Override
    public PropertyAccessor getPropertyAccessor(String ns, String name) {
        final QName n = new QName(ns, name);
        final PropertySetter setter = getPropertySetter(n);
        final PropertyGetter getter = getPropertyGetter(n);
        final boolean isJAXBElement = setter.getType()
                .equals(JAXBElement.class);
        final boolean isListType = java.util.List.class.isAssignableFrom(setter
                .getType());
        final Class elementDeclaredType = isJAXBElement ? getElementDeclaredType(n)
                : null;
        return new PropertyAccessor() {
            @Override
            public Object get(Object bean) throws DatabindingException {
                Object val;
                if (isJAXBElement) {
                    JAXBElement<Object> jaxbElement = (JAXBElement<Object>) JAXBWrapperAccessor.get(getter, bean);
                    val = (jaxbElement == null) ? null : jaxbElement.getValue();
                } else {
                    val = JAXBWrapperAccessor.get(getter, bean);
                }
                if (val == null && isListType) {
                    val = new java.util.ArrayList();
                    set(bean, val);
                }
                return val;
            }

            @Override
            public void set(Object bean, Object value) throws DatabindingException {
                if (isJAXBElement) {
                    JAXBElement<Object> jaxbElement = new JAXBElement<Object>(
                            n, elementDeclaredType, contentClass, value);
                    JAXBWrapperAccessor.set(setter, bean, jaxbElement);
                } else {
                    JAXBWrapperAccessor.set(setter, bean, value);
                }
            }
        };
    }    

    static  private Object get(PropertyGetter getter, Object wrapperInstance) {
        return (getter instanceof PrivFieldGetter)?
            ((PrivFieldGetter)getter).getPriv(wrapperInstance):
            getter.get(wrapperInstance);    
    }

    static private void set(PropertySetter setter, Object wrapperInstance, Object value) {
        if (setter instanceof PrivFieldSetter) 
            ((PrivFieldSetter)setter).setPriv(wrapperInstance, value);
        else
            setter.set(wrapperInstance, value); 
    }
    

    static private class PrivFieldSetter extends FieldSetter {     
        private PrivFieldSetter(Field f) {
            super(f);
        }
        private void setPriv(final Object instance, final Object val) {
            final Object resource = (type.isPrimitive() && val == null)? uninitializedValue(type): val;
            if (field.isAccessible()) {
                try {
                    field.set(instance, resource);
                } catch (Exception e) {
                    throw new WebServiceException(e);
                }
            } else {
                try {
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                        public Object run() throws IllegalAccessException {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            field.set(instance, resource);
                            return null;
                        }
                    });
                } catch (PrivilegedActionException e) {
                    throw new WebServiceException(e);
                }
            }
        }
    }

    static private class PrivFieldGetter extends FieldGetter {      
        private PrivFieldGetter(Field f) {
            super(f);
        }        
        static private class PrivilegedGetter implements PrivilegedExceptionAction {
            private Object value;
            private Field  field;
            private Object instance;
            public PrivilegedGetter(Field field, Object instance) {
                super();
                this.field = field;
                this.instance = instance;
            }
            public Object run() throws IllegalAccessException {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                value = field.get(instance);            
                return null;
            }
        }            
        private Object getPriv(final Object instance) {
            if (field.isAccessible()) {
                try {
                    return field.get(instance);
                } catch (Exception e) {
                    throw new WebServiceException(e);
                }
            } else {
                PrivilegedGetter privilegedGetter = new PrivilegedGetter(field, instance);
                try {
                    AccessController.doPrivileged(privilegedGetter);
                } catch (PrivilegedActionException e) {
                    throw new WebServiceException(e);
                }
                return privilegedGetter.value;
            }
        }
    }
}
