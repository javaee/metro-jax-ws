package com.sun.xml.ws.db.xmlbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.xml.ws.spi.db.MethodGetter;
import com.sun.xml.ws.spi.db.MethodSetter;
import com.sun.xml.ws.spi.db.PropertyGetter;
import com.sun.xml.ws.spi.db.PropertyGetterBase;
import com.sun.xml.ws.spi.db.PropertySetter;
import com.sun.xml.ws.spi.db.PropertySetterBase;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.XMLBridge;

/**
 * This is to support POJO beans with XMLBeans
 * TODO: use JAXB? or look at com.sun.xml.ws.spi.db.JAXBWrapperAccessor
 * TODO: inheritence? polymorphism?
 * 
 * @author shih-chang.chen@oracle.com
 */
public class JavaBeanInfo {
    static class PropInfo {
        TypeInfo typeInfo;
        PropertyGetter getter;
        PropertySetter setter;
        XMLBridge bridge;
        public PropInfo(TypeInfo typeInfo, PropertyGetter getter, PropertySetter setter) {
            this.typeInfo = typeInfo;
            this.getter = getter;
            this.setter = setter;
        } 
    }
    Class contentClass;
    QName typeName;
    List<PropInfo> properties;
    
    JavaBeanInfo(Class cls) {
        contentClass = cls;
        String tns = "java:" + contentClass.getPackage().getName(); 
        typeName = new QName(tns, cls.getSimpleName());
        HashMap<String, Method> setters = new HashMap<String, Method>();
        HashMap<String, Method> getters = new HashMap<String, Method>();
        
        for (Method method : contentClass.getMethods()) {
            if (method.getDeclaringClass().equals(Object.class)) continue;
            String methodName = method.getName();
            if (PropertySetterBase.setterPattern(method)) {
                String key = methodName.substring(3, methodName.length());
                setters.put(key, method);
            }
            if (PropertyGetterBase.getterPattern(method)) {
                String key = methodName.startsWith("is") ? 
                        methodName.substring(2, method.getName().length()):
                        methodName.substring(3, method.getName().length());
                getters.put(key, method);
            }
        }
        properties = new ArrayList<PropInfo>();
        List<String> sortedPropNames = new ArrayList<String>();
        sortedPropNames.addAll(getters.keySet());
        Collections.sort(sortedPropNames);
        for(String propName : sortedPropNames) {
            Method setMethod = setters.get(propName);
            PropertyGetter getter = new MethodGetter(getters.get(propName));
            PropertySetter setter = null;
            //TODO Read/Marshall-Only property does not have setter; Exception may have some.
            if (setMethod != null) {
                setter = new MethodSetter(setMethod);
                Class<?> propClass = getter.getType();
                if (propClass.equals(setter.getType())) {  
                    Annotation[] ann = {};
                    //TODO array style?
                    if (propClass.isArray()) {
                        ann = new Annotation[1];
                        ann[0] = new JWSAnnotationReader.DummyXmlElementWrapper(tns, propName);
                    }
                    //TODO allways qualified the property name?
                    TypeInfo propTypeInfo = new TypeInfo(new QName(tns, propName), propClass, ann);
                    properties.add(new PropInfo(propTypeInfo, getter, setter));
                }
            }
        }        
    }
}
