package com.sun.xml.ws.db.xmlbeans;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;

import com.sun.xml.ws.spi.db.TypeInfo;

class ArrayInfo {
    Class<?> javaType;
    TypeInfo itemTypeInfo;
    QName typeName;
    QName itemName;
    
    ArrayInfo(TypeInfo ti, String tns) {
        javaType = (Class<?>)ti.type;
        itemTypeInfo = ti.getItemType();
        Class<?> componentType = (Class<?>)itemTypeInfo.type;
        String item = itemName(componentType);
        itemName = new QName(tns, item);
        typeName = new QName(tns, "ArrayOf"+item+(XmlObject.class.isAssignableFrom(componentType)? "" : "_literal"));
    } 
    
    static String itemName(Class<?> cls) {
        String clsName = cls.getName();
        if (cls.isPrimitive()) return clsName;
        else if (cls.equals(java.lang.Boolean.class))    return "JavaLangboolean";
        else if (cls.equals(java.lang.Byte.class))       return "JavaLangbyte";
        else if (cls.equals(java.lang.Integer.class))    return "JavaLangint";
        else if (cls.equals(java.lang.Double.class))     return "JavaLangdouble";
        else if (cls.equals(java.lang.Float.class))      return "JavaLangfloat";
        else if (cls.equals(java.lang.Long.class))       return "JavaLanglong";
        else if (cls.equals(java.lang.Short.class))      return "JavaLangshort";
        else if (cls.equals(java.lang.String.class))     return "JavaLangstring";
        else if (cls.equals(byte[].class))               return "base64Binary";
        else if (cls.equals(java.util.Calendar.class))   return "JavaUtilCalendardateTime";
        else if (cls.equals(java.math.BigDecimal.class)) return "JavaMathdecimal";
        else if (cls.equals(java.math.BigInteger.class)) return "JavaMathinteger";
        else if (cls.equals(javax.xml.namespace.QName.class)) return "QName";
        else {
            return clsName.substring(clsName.lastIndexOf(".")+1);
        }             
    }
}