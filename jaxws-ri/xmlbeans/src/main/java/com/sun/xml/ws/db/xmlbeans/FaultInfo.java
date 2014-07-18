package com.sun.xml.ws.db.xmlbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;

import com.sun.xml.ws.api.model.CheckedException;
import com.sun.xml.ws.spi.db.PropertyGetterBase;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.XMLBridge;

/** 
 * Remove this, reuse JavaBeanInfo
 * 
 * @author shih-chang.chen@oracle.com
 */
class FaultInfo {
    static class PropInfo {
        TypeInfo typeInfo;
        Method getter;
        XMLBridge bridge;
        public PropInfo(TypeInfo typeInfo, Method getter) {
            this.typeInfo = typeInfo;
            this.getter = getter;
        }
    }
    
    CheckedException implModel;
    List<PropInfo> propInfo;
    PropInfo detailInfo;
    public FaultInfo(CheckedException impl, String tns) {
        this.implModel = impl;
        propInfo = model(implModel.getExceptionClass(), tns);
        if (propInfo.size() == 1) {
            detailInfo = propInfo.get(0);
            propInfo = null;
        }
//        if (WrapperComposite.class.equals(impl.getDetailType().type)) {
//        } else {
//            detailInfo = new PropInfo(impl.getDetailType(), impl.getFaultInfoGetter());
//        }
    }
    
    static List<PropInfo> model(Class cls, String portTypeNS) {
        List<PropInfo> props = new ArrayList<PropInfo>();
        PropInfo detailInfo = null;
        PropInfo stringMessage = null;
        String tns = "java:"+cls.getPackage().getName();
        for (Method method : cls.getMethods()) {
            if (isFaultBeanPropertyGetter(method)) {
                String methodName = method.getName();
                QName propName = new QName(tns, methodName.substring(3));                    
                Class<?> propClass = method.getReturnType();    
                Annotation[] ann = {};
                if (propClass.isArray()) {
                    ann = new Annotation[1];
                    ann[0] = new JWSAnnotationReader.DummyXmlElementWrapper(tns, propName.getLocalPart());
                }
                Type propType = method.getGenericReturnType();
                TypeInfo ti = new TypeInfo(propName, propClass, ann);
                PropInfo pi = new PropInfo(ti, method);
                if ("getFaultInfo".equals(methodName)) detailInfo = pi;
                else if ("getMessage".equals(methodName)) stringMessage = pi;
                else props.add(pi);
            }
        }
        if (detailInfo != null ) {
            props.clear();
            props.add(detailInfo);
        } else {
            if (props.size() == 0) {
                props.add(stringMessage);
            }
        } 
        //This single property handling is how this is different from JavaBean ....
        if (props.size() == 1) {
            detailInfo = props.get(0);
            props.clear();
            Class propClass = (Class)detailInfo.typeInfo.type;
            SchemaType schemaType = XMLBeansContext.type(propClass);            
            QName propName = (schemaType != null && schemaType.isDocumentType()) ?  schemaType.getDocumentElementName() : 
                new QName(tns, cls.getSimpleName()); 
//jax-ws  uses  new QName(tns, cls.getSimpleName()); 
//jax-rpc uses  new QName(cls.getPackage().getName(), singlePropName(propClass));            
            Annotation[] ann = detailInfo.typeInfo.annotations;
            if (propClass.isArray()) {
                ann = new Annotation[1];
                ann[0] = new JWSAnnotationReader.DummyXmlElementWrapper(portTypeNS, propName.getLocalPart());
                propName = new QName(portTypeNS, propName.getLocalPart());            
            }            
            TypeInfo ti = new TypeInfo(propName, propClass, ann); 
            props.add(new PropInfo(ti, detailInfo.getter));
        }
        return props;
    }
    
    //single property Some JAX-RPC use this way  
    static private String singlePropName(Class cls) {
        if (XmlObject.class.equals(cls)) return "anyType";
//      if (XmlObject.class.equals(cls)) return null;//XmlObject is mapped to xsd:any
        if (cls.isArray()) {
            Class componentType = cls.getComponentType();
            String item = ArrayInfo.itemName(componentType);
            return "ArrayOf"+item+((XmlObject.class.isAssignableFrom(componentType)? "" : "_literal"));
        } else if(XmlObject.class.isAssignableFrom(cls)) {
            SchemaType schemaType = XMLBeansContext.type(cls);
            if (schemaType != null) {
                return schemaType.getName().getLocalPart();
            }
        } else {
            SchemaType schemaType = XMLBeansContext.builtInSchemaTypes.get(cls);
            if (schemaType != null) return schemaType.getName().getLocalPart();
        }
        return cls.getSimpleName();
    }

    static final public String GET_FAULT_INFO_METHOD_NAME = "getFaultInfo"; 
    
    static final public String[] EXCLUDED_GETTERS = {
        "getCause", 
        "getLocalizedMessage", 
        "getStackTrace", 
        "getClass",
        "getSuppressed"};
    
    static boolean isFaultBeanPropertyGetter(Method method) {
        String methodName = method.getName();
        for (String exclude : EXCLUDED_GETTERS) if (exclude.equals(methodName)) return false;
        return PropertyGetterBase.getterPattern(method);
    }
}