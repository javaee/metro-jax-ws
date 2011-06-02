package com.sun.xml.ws.db.sdo;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.TypeHelper;

import com.sun.xml.ws.spi.db.PropertyGetter;
import com.sun.xml.ws.spi.db.PropertySetter;

import javax.xml.namespace.QName;

import com.sun.xml.ws.spi.db.PropertyAccessor;
import com.sun.xml.ws.spi.db.WrapperAccessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: May 19, 2009
 * Time: 10:40:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class SDOWrapperAccessor extends WrapperAccessor {

    private SDOContextWrapper contextWrapper = null;
    protected Class<?> contentClass;

    public SDOWrapperAccessor(SDOContextWrapper contextWrapper, Class<?> wrapperBean) {
        this.contextWrapper = contextWrapper;
        contentClass = wrapperBean;
        initBuilders();
    }

    protected void initBuilders() {
        HashMap<Object, PropertySetter> setByQName = new HashMap<Object, PropertySetter>();
        HashMap<Object, PropertyGetter> getByQName = new HashMap<Object, PropertyGetter>();
        HashMap<Object, PropertySetter> setByLocalpart = new HashMap<Object, PropertySetter>();
        HashMap<Object, PropertyGetter> getByLocalpart = new HashMap<Object, PropertyGetter>();

        HashSet<String> elementLocalNames = new HashSet<String>();

        TypeHelper helper = contextWrapper.getHelperContext().getTypeHelper();
        Type type = helper.getType(contentClass);

        @SuppressWarnings("unchecked")
        List<Property> properties = (List<Property>) type.getDeclaredProperties();
        for (Property p : properties) {
            QName qname = SDOUtils.getPropertyElementName(contextWrapper.getHelperContext(), p);
            SDOPropertyBuilder pBuilder = new SDOPropertyBuilder(qname, p.getType().getInstanceClass());
            setByQName.put(qname, pBuilder);
            getByQName.put(qname, pBuilder);
            setByLocalpart.put(qname.getLocalPart(), pBuilder);
            getByLocalpart.put(qname.getLocalPart(), pBuilder);
            if (elementLocalNames.contains(qname.getLocalPart())) {
                elementLocalNameCollision = true;
            } else {
                elementLocalNames.add(qname.getLocalPart());
            }
        }

        if (elementLocalNameCollision) {
            propertySetters = setByQName;
            propertyGetters = getByQName;

        } else {
            propertySetters = setByLocalpart;
            propertyGetters = getByLocalpart;
        }
    }

    /**
     * Knows the mapping of all the properties of a contaning type
     */
    static class SDOPropertyBuilder implements PropertyAccessor,
            PropertyGetter, PropertySetter {

        private QName qname;
        private Class type;

        public SDOPropertyBuilder(QName qname, Class type) {
            this.qname = qname;
            this.type = type;
        }

        public Class getType() {
            return type;
        }

        public <A> A getAnnotation(Class<A> annotationType) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        // get the property value from the wrapper intance
        public Object get(Object instance) {
            if (instance instanceof DataObject) {
                DataObject wrapperBean = (DataObject) instance;
                Property p = wrapperBean.getInstanceProperty(qname.getLocalPart());
                if (p == null) {
                    throw new SDODatabindingException("Property not found: " + p);
                }
                Object o = wrapperBean.get(p);
                return SDOUtils.unwrapPrimitives(o);
            } else {
                throw new SDODatabindingException("Invalid SDO object: " + instance);
            }
        }

        // set the property of the instance to the value
        public void set(Object instance, Object value) {
            if (instance instanceof DataObject) {
                DataObject wrapperBean = (DataObject) instance;
                Property p = wrapperBean.getInstanceProperty(qname.getLocalPart());
                if (p == null) {
                    throw new SDODatabindingException("Property not found: " + p);
                }
                wrapperBean.set(p, value);
            } else {
                throw new SDODatabindingException("Invalid SDO object: " + instance);

            }
        }
    }
}
